package at.haha007.edencommands.annotations;

import at.haha007.edencommands.CommandExecutor;
import at.haha007.edencommands.CommandRegistry;
import at.haha007.edencommands.SyncCommandExecutor;
import at.haha007.edencommands.tree.CommandBuilder;
import at.haha007.edencommands.tree.LiteralCommandNode;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class AnnotatedCommandLoader {
    private final ArgumentParserProvider argumentParserProvider = new ArgumentParserProvider();
    private final CommandTree root = CommandTree.root();
    private final JavaPlugin plugin;

    public AnnotatedCommandLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void addArgumentParser(String typeName, ArgumentParser<?> parser) {
        argumentParserProvider.register(typeName, parser);
    }

    public void addDefaultArgumentParsers() {
        for (DefaultArgumentParsers value : DefaultArgumentParsers.values()) {
            addArgumentParser(value.getKey(), value);
        }
    }

    public void addAnnotated(Object obj) {
        for (Method method : obj.getClass().getDeclaredMethods()) {
            Command cmd = method.getDeclaredAnnotation(Command.class);
            String command = getAnnotationValue(method);
            if (cmd == null || command == null)
                continue;
            method.setAccessible(true);
            CommandExecutor executor = new MethodCommandExecutor(obj, method);
            if (cmd.sync())
                executor = new SyncCommandExecutor(executor, plugin);
            this.root.add(command, executor);
        }
    }

    public void addAnnotated(Method method) {
        Command cmd = method.getDeclaredAnnotation(Command.class);
        String command = getAnnotationValue(method);
        if (cmd == null || command == null) {
            throw new IllegalArgumentException("Method is not annotated with @Command!");
        }
        method.setAccessible(true);
        CommandExecutor executor = new MethodCommandExecutor(null, method);
        if (cmd.sync())
            executor = new SyncCommandExecutor(executor, plugin);
        this.root.add(command, executor);
    }

    public void register(CommandRegistry registry) {
        for (CommandBuilder<?> cmd : root.getChildCommands(argumentParserProvider)) {
            if (!(cmd instanceof LiteralCommandNode.LiteralCommandBuilder literalCommandBuilder)) {
                throw new RuntimeException("CommandBuilder is not a LiteralCommandBuilder!");
            }
            registry.register(literalCommandBuilder.build());
        }
    }

    private String getAnnotationValue(Method method) {
        String root = Optional.ofNullable(method.getDeclaringClass().getDeclaredAnnotation(CommandList.class))
                .map(CommandList::value).stream()
                .flatMap(Arrays::stream)
                .map(Command::value)
                .collect(Collectors.joining(" "));
        String sub = Optional.ofNullable(method.getDeclaredAnnotation(Command.class))
                .map(Command::value)
                .orElse(null);
        if (sub == null) {
            return null;
        }
        return root + " " + sub;
    }
}
