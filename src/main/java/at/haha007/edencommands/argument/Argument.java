package at.haha007.edencommands.argument;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import at.haha007.edencommands.TabCompleter;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

public abstract class Argument<T> {
    @NotNull
    private final TabCompleter tabCompleter;

    /**
     * Filters tab completions by the string that gets tab-completed
     */
    private final boolean filterByName;

    protected Argument(@NotNull TabCompleter tabCompleter, boolean filterByName) {
        this.tabCompleter = tabCompleter;
        this.filterByName = filterByName;
    }

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
        List<AsyncTabCompleteEvent.Completion> list = tabCompleter.apply(context);
        if (list == null) return List.of();
        Stream<AsyncTabCompleteEvent.Completion> stream = list.stream();
        if (filterByName)
            stream = stream.filter(s -> matches(s.suggestion(), context.input()[context.pointer()]));
        return stream.toList();
    }

    private boolean matches(String literal, String start) {
        return literal.contains(start);
    }
}
