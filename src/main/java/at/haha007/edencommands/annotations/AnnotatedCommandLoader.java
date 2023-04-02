package at.haha007.edencommands.annotations;

import at.haha007.edencommands.CommandExecutor;
import at.haha007.edencommands.CommandRegistry;
import at.haha007.edencommands.SyncCommandExecutor;
import at.haha007.edencommands.tree.CommandBuilder;
import at.haha007.edencommands.tree.LiteralCommandNode;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
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

    public List<CommandBuilder<?>> getCommands() {
        return root.getChildCommands(argumentParserProvider);
    }

    public void addAnnotated(Object obj) {
        Arrays.stream(obj.getClass().getDeclaredMethods()).forEach(method -> addAnnotatedSilent(method, obj));
    }

    public void addAnnotated(Method method, Object obj) {
        if (method.getDeclaringClass() != obj.getClass()) {
            throw new IllegalArgumentException("Method is not declared in the given object!");
        }
        String command = getAnnotationValue(method);
        if (command == null) {
            throw new IllegalArgumentException("Method is not annotated with @Command!");
        }
        boolean sync = getAnnotationSync(method);
        method.setAccessible(true);
        CommandExecutor executor = new MethodCommandExecutor(obj, method);
        if (sync)
            executor = new SyncCommandExecutor(executor, plugin);
        this.root.add(command, executor);
    }

    private void addAnnotatedSilent(Method method, Object obj) {
        try {
            addAnnotated(method, obj);
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void register(CommandRegistry registry) {
        for (CommandBuilder<?> cmd : root.getChildCommands(argumentParserProvider)) {
            if (!(cmd instanceof LiteralCommandNode.LiteralCommandBuilder literalCommandBuilder)) {
                throw new RuntimeException("CommandBuilder is not a LiteralCommandBuilder!");
            }
            registry.register(literalCommandBuilder.build());
        }
    }

    private boolean getAnnotationSync(Method method) {

        return method.isAnnotationPresent(SyncCommand.class)
                || method.getDeclaringClass().isAnnotationPresent(SyncCommand.class);
    }

    private String getAnnotationValue(Method method) {
        String root = Optional.ofNullable(method.getDeclaringClass().getDeclaredAnnotation(CommandList.class))
                .map(CommandList::value).stream()
                .flatMap(Arrays::stream)
                .map(Command::value)
                .collect(Collectors.joining(" "));
        if (root.isBlank()) root = Optional.ofNullable(method.getDeclaringClass().getDeclaredAnnotation(Command.class))
                .map(Command::value).orElse("");
        String sub = Optional.ofNullable(method.getDeclaredAnnotation(CommandList.class))
                .map(CommandList::value).stream()
                .flatMap(Arrays::stream)
                .map(Command::value)
                .collect(Collectors.joining(" "));
        if (sub.isBlank()) sub = Optional.ofNullable(method.getDeclaredAnnotation(Command.class))
                .map(Command::value).orElse("");
        if (sub.isBlank()) return null;
        return root + " " + sub;
    }
}
