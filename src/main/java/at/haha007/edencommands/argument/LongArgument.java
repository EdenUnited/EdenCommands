package at.haha007.edencommands.argument;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class LongArgument extends Argument<Long> {
    @NotNull
    private final List<Filter<Long>> filters;
    @NotNull
    private final Function<String, Component> notLongMessage;

    @Builder
    private LongArgument(@NotNull @Singular List<Filter<Long>> filters,
                         Function<String, Component> notLongMessage,
                         @NotNull @Singular List<Completion<Long>> completions,
                         TriState filterByName) {

        super(new LongArgument.TabCompleter(completions, filters), filterByName == null || filterByName.toBooleanOrElse(true));

        this.filters = filters;
        //create notLongMessage if it is not set
        if (notLongMessage == null) {
            notLongMessage = s -> Component.text("Argument <", NamedTextColor.RED)
                    .append(Component.text(s, NamedTextColor.GOLD))
                    .append(Component.text("> must be of type long!", NamedTextColor.RED));
        }
        this.notLongMessage = notLongMessage;
    }

    @Override
    public @NotNull ParsedArgument<Long> parse(CommandContext context) throws CommandException {
        long l;
        String s = context.input()[context.pointer()];
        //parse the long
        try {
            l = Long.parseLong(s);
        } catch (NumberFormatException e) {
            throw new CommandException(notLongMessage.apply(s), context);
        }

        //check limitations
        CommandSender sender = context.sender();
        Optional<Component> any = filters.stream()
                .map(f -> f.check(sender, l)).filter(Objects::nonNull).findAny();
        if (any.isPresent()) {
            throw new CommandException(any.get(), context);
        }

        //return as parsed argument
        return new ParsedArgument<>(l, 1);
    }

    @AllArgsConstructor
    private static class TabCompleter implements Function<CommandContext, List<AsyncTabCompleteEvent.Completion>> {
        private final List<Completion<Long>> completions;
        private final List<Filter<Long>> filters;

        public List<AsyncTabCompleteEvent.Completion> apply(CommandContext context) {
            CommandSender sender = context.sender();
            return completions.stream()
                    .filter(i -> filters.stream().anyMatch(e -> e.check(sender, i.completion()) != null))
                    .map(c -> AsyncTabCompleteEvent.Completion.completion(String.valueOf(c.completion()), c.tooltip()))
                    .toList();
        }
    }

    /**
     * Filters values by a minimum, Smaller values can't be parsed
     */
    @AllArgsConstructor
    @NotNull
    public static class MinimumFilter implements Filter<Long> {
        private final Component error;
        private final long min;

        public Component check(CommandSender sender, Long d) {
            if (d < min)
                return error;
            return null;
        }
    }

    /**
     * Filters values by a maximum, Larger values can't be parsed
     */
    @AllArgsConstructor
    @NotNull
    public static class MaximumFilter implements Filter<Long> {
        private final Component error;
        private final long max;

        public Component check(CommandSender sender, Long d) {
            if (d > max)
                return error;
            return null;
        }
    }
}
