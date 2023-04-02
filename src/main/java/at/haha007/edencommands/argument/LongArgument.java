package at.haha007.edencommands.argument;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class LongArgument extends Argument<Long> {
    @NotNull
    private final List<Filter<Long>> filters;
    @NotNull
    private final Function<String, Component> notLongMessage;

    private LongArgument(@NotNull List<Filter<Long>> filters,
                         @Nullable Function<String, Component> notLongMessage,
                         @NotNull List<Completion<Long>> completions,
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

    public static LongArgumentBuilder builder() {
        return new LongArgumentBuilder();
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

    private record TabCompleter(List<Completion<Long>> completions, List<Filter<Long>> filters)
            implements Function<CommandContext, List<AsyncTabCompleteEvent.Completion>> {

        public List<AsyncTabCompleteEvent.Completion> apply(CommandContext context) {
            CommandSender sender = context.sender();
            return completions.stream()
                    .filter(d -> filters.stream().noneMatch(e -> e.check(sender, d.completion()) != null))
                    .map(c -> AsyncTabCompleteEvent.Completion.completion(String.valueOf(c.completion()), c.tooltip()))
                    .toList();
        }
    }

    /**
     * Filters values by a minimum, Smaller values can't be parsed
     */
    @NotNull
    public static class MinimumFilter implements Filter<Long> {
        private final Component error;
        private final long min;

        public MinimumFilter(Component error, long min) {
            this.error = error;
            this.min = min;
        }

        public Component check(CommandSender sender, Long d) {
            if (d < min)
                return error;
            return null;
        }
    }

    /**
     * Filters values by a minimum, Smaller values can't be parsed
     */
    @NotNull
    public static class RangeFilter implements Filter<Long> {
        private final Component error;
        private final float min;
        private final float max;

        public RangeFilter(Component error, long min, long max) {
            this.error = error;
            this.min = min;
            this.max = max;
        }

        public Component check(CommandSender sender, Long l) {
            if (l < min)
                return error;
            if (l > max)
                return error;
            return null;
        }
    }

    /**
     * Filters values by a maximum, Larger values can't be parsed
     */
    @NotNull
    public static class MaximumFilter implements Filter<Long> {
        private final Component error;
        private final long max;

        public MaximumFilter(Component error, long max) {
            this.error = error;
            this.max = max;
        }

        public Component check(CommandSender sender, Long d) {
            if (d > max)
                return error;
            return null;
        }
    }

    public static class LongArgumentBuilder {
        private ArrayList<Filter<Long>> filters;
        private Function<String, Component> notLongMessage;
        private ArrayList<Completion<Long>> completions;
        private TriState filterByName;

        LongArgumentBuilder() {
        }

        public LongArgumentBuilder filter(Filter<Long> filter) {
            if (this.filters == null) this.filters = new ArrayList<>();
            this.filters.add(filter);
            return this;
        }

        public LongArgumentBuilder filters(Collection<? extends Filter<Long>> filters) {
            if (this.filters == null) this.filters = new ArrayList<>();
            this.filters.addAll(filters);
            return this;
        }

        public LongArgumentBuilder clearFilters() {
            if (this.filters != null)
                this.filters.clear();
            return this;
        }

        public LongArgumentBuilder notLongMessage(Function<String, Component> notLongMessage) {
            this.notLongMessage = notLongMessage;
            return this;
        }

        public LongArgumentBuilder completion(Completion<Long> completion) {
            if (this.completions == null) this.completions = new ArrayList<>();
            this.completions.add(completion);
            return this;
        }

        public LongArgumentBuilder completions(Collection<? extends Completion<Long>> completions) {
            if (this.completions == null) this.completions = new ArrayList<>();
            this.completions.addAll(completions);
            return this;
        }

        public LongArgumentBuilder clearCompletions() {
            if (this.completions != null)
                this.completions.clear();
            return this;
        }

        public LongArgumentBuilder filterByName(TriState filterByName) {
            this.filterByName = filterByName;
            return this;
        }

        public LongArgument build() {
            List<Filter<Long>> filters = switch (this.filters == null ? 0 : this.filters.size()) {
                case 0 -> Collections.emptyList();
                case 1 -> Collections.singletonList(this.filters.get(0));
                default -> List.copyOf(this.filters);
            };
            List<Completion<Long>> completions = switch (this.completions == null ? 0 : this.completions.size()) {
                case 0 -> Collections.emptyList();
                case 1 -> Collections.singletonList(this.completions.get(0));
                default -> List.copyOf(this.completions);
            };

            return new LongArgument(filters, notLongMessage, completions, filterByName);
        }

        public String toString() {
            return "LongArgument.LongArgumentBuilder(filters=" + this.filters + ", notLongMessage=" + this.notLongMessage + ", completions=" + this.completions + ", filterByName=" + this.filterByName + ")";
        }
    }
}
