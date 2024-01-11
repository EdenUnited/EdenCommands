package at.haha007.edencommands.annotations;

import at.haha007.edencommands.TabCompleter;
import at.haha007.edencommands.*;
import at.haha007.edencommands.annotations.annotations.*;
import at.haha007.edencommands.argument.Argument;
import at.haha007.edencommands.argument.ParsedArgument;
import at.haha007.edencommands.tree.CommandBuilder;
import at.haha007.edencommands.tree.LiteralCommandNode;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent.Completion;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class AnnotatedCommandLoader {
    private final CommandTree root = CommandTree.root();
    private final Map<String, TabCompleter> tabCompleterMap = new HashMap<>();
    private final Map<String, ArgumentParser<?>> argumentMap = new HashMap<>();
    private final Map<String, Requirement> requirementMap = new HashMap<>();
    private final JavaPlugin plugin;
    private final Map<String, String> literalMapper = new HashMap<>();
    private static final String FIELD_ACCESS_FAILURE = "Failed to set field accessible!";

    public AnnotatedCommandLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void mapLiterals(Map<String, String> map) {
        literalMapper.putAll(map);
    }

    public void mapLiteral(String literal, String mappedLiteral) {
        literalMapper.put(literal, mappedLiteral);
    }

    public List<? extends CommandBuilder<?>> getCommands() {
        Map<String, Argument<?>> arguments = new HashMap<>();
        TabCompleter emptyTabCompleter = context -> List.of();
        this.argumentMap.forEach((key, value) -> {
            TabCompleter tabCompleter = tabCompleterMap.getOrDefault(key, emptyTabCompleter);

            //noinspection rawtypes,unchecked
            Argument<?> argument = new Argument<>(tabCompleter, true) {
                @Override
                @NotNull
                public ParsedArgument parse(CommandContext context) throws CommandException {
                    try {
                        return value.parse(context);
                    } catch (CommandException e) {
                        throw e;
                    } catch (Exception e) {
                        plugin.getLogger().throwing(getClass().getSimpleName(), "parse", e);
                        throw new CommandException(Component.text("Failed to parse argument: " + key), context);
                    }
                }
            };
            arguments.put(key, argument);
        });

        return root.getChildren().stream()
                .map(tree -> tree.toCommand(arguments, literalMapper, requirementMap))
                .toList();
    }

    public void register(CommandRegistry registry) {
        List<? extends CommandBuilder<?>> commands = getCommands();
        for (CommandBuilder<?> cmd : commands) {
            if (!(cmd instanceof LiteralCommandNode.LiteralCommandBuilder literalCommandBuilder)) {
                registry.getPlugin().getLogger().warning("Not a LiteralCommandBuilder: " + cmd);
                continue;
            }
            registry.register(literalCommandBuilder.build());
        }
    }

    public void addAnnotated(Object obj) {
        Arrays.stream(obj.getClass().getDeclaredMethods()).forEach(method -> addAnnotatedSilent(method, obj));
        Arrays.stream(obj.getClass().getDeclaredFields()).forEach(field -> addAnnotatedSilent(field, obj));
    }

    private void addAnnotated(Method method, Object obj) {
        if (method.getDeclaringClass() != obj.getClass()) {
            throw new IllegalArgumentException("Field is not declared in the given object!");
        }
        registerCommand(method, obj);
        registerRequirement(method, obj);
        registerTabCompleter(method, obj);
        registerArgument(method, obj);
    }

    private void addAnnotated(Field field, Object obj) {
        if (field.getDeclaringClass() != obj.getClass()) {
            throw new IllegalArgumentException("Field is not declared in the given object!");
        }
        registerCommand(field, obj);
        registerRequirement(field, obj);
        registerTabCompleter(field, obj);
        registerArgument(field, obj);
    }

    private void registerArgument(Method method, Object obj) {
        if (!method.isAnnotationPresent(CommandArgument.class)) {
            return;
        }
        CommandArgument annotation = method.getDeclaredAnnotation(CommandArgument.class);
        String key = annotation.value();
        if (key == null || key.isBlank()) {
            return;
        }
        key = key.trim();
        Class<?> outputType = method.getReturnType();
        if (outputType != ParsedArgument.class) {
            throw new IllegalArgumentException("Method does not return a ParsedArgument!");
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1 || parameterTypes[0] != CommandContext.class) {
            throw new IllegalArgumentException("Method does not take a CommandContext as parameter: " + method.getName());
        }

        if (!method.trySetAccessible())
            throw new IllegalArgumentException("Method is not accessible!");
        //noinspection unchecked
        ArgumentParser<?> argument = c -> (ParsedArgument<Object>) method.invoke(obj, c);
        argumentMap.put(key, argument);
    }

    private void registerArgument(Field field, Object obj) {
        if (!field.isAnnotationPresent(CommandArgument.class)) {
            return;
        }
        CommandArgument annotation = field.getDeclaredAnnotation(CommandArgument.class);
        String key = annotation.value();
        if (key == null || key.isBlank()) {
            return;
        }
        key = key.trim();
        if (argumentMap.containsKey(key)) {
            throw new IllegalArgumentException("Argument with key " + key + " already exists!");
        }
        try {
            if (!field.trySetAccessible())
                throw new IllegalArgumentException("Field is not accessible!");
            Object o = field.get(obj);
            if (o == null) {
                throw new IllegalArgumentException("ArgumentParser field is null!");
            }
            if (!field.getType().isInstance(o)) {
                throw new IllegalArgumentException("ArgumentParser field is not of type ArgumentParser!");
            }
            ArgumentParser<?> parser = (ArgumentParser<?>) o;
            argumentMap.put(key, parser);
        } catch (IllegalAccessException e) {
            plugin.getLogger().throwing(getClass().getSimpleName(), "registerArgument", e);
        }
    }

    private void registerTabCompleter(Method method, Object obj) {
        if (!method.isAnnotationPresent(at.haha007.edencommands.annotations.annotations.TabCompleter.class)) {
            return;
        }
        at.haha007.edencommands.annotations.annotations.TabCompleter annotation =
                method.getDeclaredAnnotation(at.haha007.edencommands.annotations.annotations.TabCompleter.class);
        String key = annotation.value();
        if (key == null || key.isBlank()) {
            return;
        }
        key = key.trim();
        Class<?> outputType = method.getReturnType();
        if (outputType != List.class) {
            throw new IllegalArgumentException("Method does not return a List<AsyncTabCompleteEvent.Completion>!");
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1 || parameterTypes[0] != CommandContext.class) {
            throw new IllegalArgumentException("Method does not take a CommandContext as parameter!");
        }
        if (!method.trySetAccessible())
            throw new IllegalArgumentException("Method is not accessible!");
        try {
            TabCompleter tabCompleter = context -> {
                try {
                    //noinspection unchecked
                    return (List<Completion>) method.invoke(obj, context);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    plugin.getLogger().throwing(getClass().getSimpleName(), "registerTabCompleter", e);
                    return List.of();
                }
            };
            tabCompleterMap.put(key, tabCompleter);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Method does not return a List<AsyncTabCompleteEvent.Completion!");
        }
    }

    private void registerTabCompleter(Field field, Object obj) {
        if (!field.isAnnotationPresent(at.haha007.edencommands.annotations.annotations.TabCompleter.class)) {
            return;
        }
        at.haha007.edencommands.annotations.annotations.TabCompleter annotation =
                field.getDeclaredAnnotation(at.haha007.edencommands.annotations.annotations.TabCompleter.class);
        String key = annotation.value();
        if (key == null || key.isBlank()) {
            return;
        }
        key = key.trim();
        if (!field.trySetAccessible())
            throw new IllegalArgumentException(FIELD_ACCESS_FAILURE);
        try {
            Object o = field.get(obj);
            if (o == null) {
                throw new IllegalArgumentException("TabCompleter field is null!");
            }
            if (!field.getType().isInstance(o)) {
                throw new IllegalArgumentException("TabCompleter field is not of type TabCompleter!");
            }
            tabCompleterMap.put(key, (TabCompleter) o);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Field is not accessible!");
        }
    }

    private void registerRequirement(Method method, Object obj) {
        if (!method.isAnnotationPresent(CommandRequirement.class)) {
            return;
        }
        CommandRequirement annotation = method.getDeclaredAnnotation(CommandRequirement.class);
        String key = annotation.value();
        if (key == null || key.isBlank()) {
            return;
        }
        key = key.trim();
        Class<?> outputType = method.getReturnType();
        if (!(outputType == boolean.class || outputType == Boolean.class)) {
            throw new IllegalArgumentException("Method does not return a boolean!");
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1 || parameterTypes[0] != CommandContext.class) {
            throw new IllegalArgumentException("Method does not take a CommandContext as parameter!");
        }

        if (!method.trySetAccessible())
            throw new IllegalArgumentException("Failed to set method accessible!");
        Requirement requirement;
        requirement = c -> {
            try {
                return (boolean) method.invoke(obj, c);
            } catch (IllegalAccessException | InvocationTargetException e) {
                plugin.getLogger().throwing(getClass().getSimpleName(), "registerRequirement", e);
                return false;
            }
        };

        requirementMap.put(key, requirement);
    }

    private void registerRequirement(Field field, Object obj) {
        if (!field.isAnnotationPresent(CommandRequirement.class)) {
            return;
        }
        CommandRequirement annotation = field.getDeclaredAnnotation(CommandRequirement.class);
        String key = annotation.value();
        if (key == null || key.isBlank()) {
            return;
        }
        key = key.trim();
        if (field.getType() != Requirement.class) {
            throw new IllegalArgumentException("Field is not a Requirement");
        }

        if (!field.trySetAccessible())
            throw new IllegalArgumentException(FIELD_ACCESS_FAILURE);
        Requirement requirement;
        try {
            requirement = (Requirement) field.get(obj);
        } catch (IllegalAccessException e) {
            plugin.getLogger().throwing(getClass().getSimpleName(), "registerRequirement", e);
            return;
        }

        requirementMap.put(key, requirement);
    }


    private void registerCommand(Method method, Object obj) {
        String command = getCommandAnnotationValue(method);
        if (command == null) {
            return;
        }
        List<String> list = Arrays.stream(command.trim().split("\\s+")).toList();
        list = new LinkedList<>(list);
        list.add(0, "root");
        boolean sync = getAnnotationSync(method);
        if (!method.trySetAccessible())
            throw new IllegalArgumentException("Failed to set method accessible!");
        CommandExecutor executor = new MethodCommandExecutor(obj, method);
        if (sync) executor = new SyncCommandExecutor(executor, plugin);

        String requirement = Optional.ofNullable(method.getDeclaredAnnotation(CommandRequirement.class))
                .map(CommandRequirement::value).map(s -> s.isEmpty() ? null : s.trim()).orElse(null);

        boolean asDefaultCommand = method.isAnnotationPresent(DefaultExecutor.class);
        this.root.add(list, requirement, executor, asDefaultCommand);
    }

    private void registerCommand(Field field, Object obj) {
        String command = getCommandAnnotationValue(field);
        if (command == null) {
            return;
        }
        List<String> list = Arrays.stream(command.trim().split("\\s+")).toList();

        boolean sync = getAnnotationSync(field);
        if (!field.trySetAccessible())
            throw new IllegalArgumentException(FIELD_ACCESS_FAILURE);

        Object o;
        try {
            o = field.get(obj);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Failed to get field value!", e);
        }
        if (o == null) throw new IllegalArgumentException("Field is null!");
        if (!(o instanceof CommandExecutor executor)) {
            throw new IllegalArgumentException("Field is not a CommandExecutor!");
        }


        if (sync) executor = new SyncCommandExecutor(executor, plugin);

        String requirement = Optional.ofNullable(field.getDeclaredAnnotation(CommandRequirement.class))
                .map(CommandRequirement::value).map(s -> s.isEmpty() ? null : s.trim()).orElse(null);

        boolean asDefaultCommand = field.isAnnotationPresent(DefaultExecutor.class);

        this.root.add(list, requirement, executor, asDefaultCommand);
    }

    //DONE
    //@Command
    //@DefaultExecutor
    //@SyncCommand
    //@CommandList

    //TODO
    //@CommandArgument
    //@CommandRequirement
    //@TabCompleter

    private void addAnnotatedSilent(Method method, Object obj) {
        try {
            addAnnotated(method, obj);
        } catch (IllegalArgumentException ignored) {
            plugin.getLogger().severe("Failed to add method " + method.getName() + " to command tree!");
        }
    }

    private void addAnnotatedSilent(Field field, Object obj) {
        try {
            addAnnotated(field, obj);
        } catch (IllegalArgumentException ignored) {
            plugin.getLogger().severe("Failed to add field " + field.getName() + " to command tree!");
        }
    }

    private boolean getAnnotationSync(Method method) {
        return method.isAnnotationPresent(SyncCommand.class)
                || method.getDeclaringClass().isAnnotationPresent(SyncCommand.class);
    }

    private boolean getAnnotationSync(Field method) {
        return method.isAnnotationPresent(SyncCommand.class)
                || method.getDeclaringClass().isAnnotationPresent(SyncCommand.class);
    }

    private String getCommandAnnotationValue(Method method) {
        String commandString = Optional.ofNullable(method.getDeclaringClass().getDeclaredAnnotation(CommandList.class))
                .map(CommandList::value).stream()
                .flatMap(Arrays::stream)
                .map(Command::value)
                .collect(Collectors.joining(" "));
        if (commandString.isBlank())
            commandString = Optional.ofNullable(method.getDeclaringClass().getDeclaredAnnotation(Command.class))
                    .map(Command::value).orElse("");
        String sub = Optional.ofNullable(method.getDeclaredAnnotation(CommandList.class))
                .map(CommandList::value).stream()
                .flatMap(Arrays::stream)
                .map(Command::value)
                .collect(Collectors.joining(" "));
        if (sub.isBlank()) sub = Optional.ofNullable(method.getDeclaredAnnotation(Command.class))
                .map(Command::value).orElse("");
        if (sub.isBlank()) return null;
        return commandString + " " + sub;
    }

    private String getCommandAnnotationValue(Field field) {
        String commandString = Optional.ofNullable(field.getDeclaringClass().getDeclaredAnnotation(CommandList.class))
                .map(CommandList::value).stream()
                .flatMap(Arrays::stream)
                .map(Command::value)
                .collect(Collectors.joining(" "));
        if (commandString.isBlank())
            commandString = Optional.ofNullable(field.getDeclaringClass().getDeclaredAnnotation(Command.class))
                    .map(Command::value).orElse("");
        String sub = Optional.ofNullable(field.getDeclaredAnnotation(CommandList.class))
                .map(CommandList::value).stream()
                .flatMap(Arrays::stream)
                .map(Command::value)
                .collect(Collectors.joining(" "));
        if (sub.isBlank()) sub = Optional.ofNullable(field.getDeclaredAnnotation(Command.class))
                .map(Command::value).orElse("");
        if (sub.isBlank()) return null;
        return commandString + " " + sub;
    }

    @Override
    public String toString() {
        return "AnnotatedCommandLoader{" +
                "root=" + root +
                ", tabCompleters=" + tabCompleterMap +
                ", argumentMap=" + argumentMap +
                ", requirementMap=" + requirementMap +
                ", plugin=" + plugin +
                ", literalMapper=" + literalMapper +
                '}';
    }
}
