package at.haha007.edencommands.argument;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

@Accessors(fluent = true)
public abstract class Argument<T> {
    @Setter
    @NotNull
    private Function<CommandContext, List<String>> tabCompleter = c -> null;

    @NotNull
    public abstract ParsedArgument<T> parse(CommandContext context) throws CommandException;

    public List<String> tabComplete(CommandContext context) {
        String start = context.input()[context.pointer()];
        List<String> list = tabCompleter.apply(context);
        if (list == null) return List.of();
        return list.stream().filter(s -> startsWith(s, start)).toList();
    }

    protected boolean startsWith(String literal, String start) {
        return literal.toLowerCase().startsWith(start.toLowerCase());
    }
}
