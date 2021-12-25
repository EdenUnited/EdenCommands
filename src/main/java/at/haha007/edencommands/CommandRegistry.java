package at.haha007.edencommands;

import at.haha007.edencommands.annotations.Command;
import at.haha007.edencommands.tree.CommandContext;
import at.haha007.edencommands.tree.node.CommandNode;
import at.haha007.edencommands.tree.node.LiteralCommandNode;
import at.haha007.edencommands.tree.node.NodeCommand;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class CommandRegistry {
    @Getter
    private static final Set<NodeCommand> registeredCommands = new HashSet<>();

    public static void register(CommandNode node) {
        if (!(node instanceof LiteralCommandNode literal))
            throw new UnsupportedOperationException("CommandNode needs to be a LiteralCommandNode");
        NodeCommand command = new NodeCommand(literal);
        registeredCommands.add(command);
    }

    public static void register(Object object) {
        Class<?> clazz = object.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            try {
                CommandNode node = createNode(field, object);
                if (node == null) continue;
                if (!(node instanceof LiteralCommandNode)) {
                    Bukkit.getLogger().severe("Cannot create command: Root node must be a LiteralCommandNode");
                    Bukkit.getLogger().severe(object.getClass().getSimpleName() + " - " + field.getName());
                    continue;
                }
                CommandRegistry.register(node);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        for (Method method : clazz.getDeclaredMethods()) {
            try {
                CommandNode node = createNode(method, object);
                if (node == null) continue;
                if (!(node instanceof LiteralCommandNode)) {
                    Bukkit.getLogger().severe("Cannot create command: Root node must be a LiteralCommandNode");
                    Bukkit.getLogger().severe(object.getClass().getSimpleName() + " - " + method.getName());
                    continue;
                }
                CommandRegistry.register(node);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static CommandNode createNode(Field field, Object object) throws IllegalAccessException {
        if (!field.isAnnotationPresent(Command.class)) return null;
        Command cmd = field.getAnnotation(Command.class);
        CommandNode node = cmd.type().parse(cmd.value());

        field.setAccessible(true);
        object = field.get(object);
        for (Field f : object.getClass().getDeclaredFields()) {
            CommandNode subNode = createNode(f, object);
            if (subNode == null) continue;
            node.then(subNode);
        }
        for (Method m : object.getClass().getDeclaredMethods()) {
            CommandNode subNode = createNode(m, object);
            if (subNode == null) continue;
            node.then(subNode);
        }

        String permission = cmd.permission();
        if (!permission.isEmpty())
            node.withPermission(permission);
        Optional<Consumer<CommandContext>> defaultMethod = getDefaultMethod(object);
        defaultMethod.ifPresent(node::executes);
        String tabs = cmd.tabCompletes();
        if (!tabs.isEmpty()) {
            List<String> l = List.of(tabs.split(","));
            node.tabCompletes(c -> l);
        }
        return node;
    }

    private static Optional<Consumer<CommandContext>> getDefaultMethod(Object object) {
        try {
            Method method = object.getClass().getDeclaredMethod("onCommand", CommandContext.class);
            return Optional.of(c -> {
                method.setAccessible(true);
                try {
                    method.invoke(object, c);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    private static CommandNode createNode(Method method, Object object) throws IllegalAccessException {
        if (!method.isAnnotationPresent(Command.class)) return null;
        Command cmd = method.getAnnotation(Command.class);
        CommandNode node = cmd.type().parse(cmd.value());
        String tabs = cmd.tabCompletes();
        if (!tabs.isEmpty()) {
            List<String> l = List.of(tabs.split(","));
            node.tabCompletes(c -> l);
        }
        String permission = cmd.permission();
        if (!permission.isEmpty())
            node.withPermission(permission);
        node.executes(c -> {
            method.setAccessible(true);
            try {
                method.invoke(object, c);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        return node;
    }
}
