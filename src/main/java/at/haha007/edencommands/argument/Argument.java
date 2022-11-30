package at.haha007.edencommands.argument;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@AllArgsConstructor
public abstract class Argument<T> {
    @NotNull
    private final Function<CommandContext, List<AsyncTabCompleteEvent.Completion>> tabCompleter;

    /**
     * Filters tab completions by the string that gets tab-completed
     */
    private final boolean filterByName;

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
    public final List<AsyncTabCompleteEvent.Completion> tabComplete(CommandContext context) {
        String start = context.input()[context.pointer()];
        List<AsyncTabCompleteEvent.Completion> list = tabCompleter.apply(context);
        if (list == null) return List.of();
        Stream<AsyncTabCompleteEvent.Completion> stream = list.stream();
        if (filterByName)
            stream = stream.filter(s -> matches(s.suggestion(), start));
        return stream.toList();
    }

    private boolean matches(String literal, String start) {
        return literal.contains(start);
//        return literal.toLowerCase().startsWith(start.toLowerCase());
    }
}
