package at.haha007.edencommands.argument;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@Accessors(fluent = true)
public abstract class Argument<T> {
    protected Function<CommandContext, List<String>> tabCompleter = c -> null;

    /**
     * Filters tab completions by the string that gets tab-completed
     */
    protected boolean filterByName = true;

    /**
     * Parses the argument
     *
     * @param context The context that gets parsed
     * @return The parsed argument
     * @throws CommandException if the argument couldn't get parsed
     */
    @NotNull
    public abstract ParsedArgument<T> parse(CommandContext context) throws CommandException;

    /**
     * Returns the tab completions for a given {@link CommandContext}
     *
     * @param context The context for the completion
     * @return A {@link List<String>} of completions
     */
    public final List<String> tabComplete(CommandContext context) {
        String start = context.input()[context.pointer()];
        List<String> list = tabCompleter.apply(context);
        if (list == null) return List.of();
        Stream<String> stream = list.stream();
        if (filterByName)
            stream = stream.filter(s -> startsWith(s, start));
        return stream.toList();
    }

    protected boolean startsWith(String literal, String start) {
        return literal.toLowerCase().startsWith(start.toLowerCase());
    }
}
