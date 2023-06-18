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

public class IntegerArgument extends Argument<Integer> {
    @NotNull
    private final List<Filter<Integer>> filters;
    @NotNull
    private final Function<String, Component> notIntegerMessage;

    private IntegerArgument(@NotNull List<Filter<Integer>> filters,
                            @Nullable Function<String, Component> notIntegerMessage,
                            @NotNull List<Completion<Integer>> completions,
                            TriState filterByName) {

        super(new IntegerArgument.TabCompleter(completions, filters), filterByName == null || filterByName.toBooleanOrElse(true));

        this.filters = filters;
        //create notIntegerMessage if it is not set
        if (notIntegerMessage == null) {
            notIntegerMessage = s -> Component.text("Argument <", NamedTextColor.RED)
                    .append(Component.text(s, NamedTextColor.GOLD))
                    .append(Component.text("> must be of type int!", NamedTextColor.RED));
        }
        this.notIntegerMessage = notIntegerMessage;
    }

    public static IntegerArgumentBuilder builder() {
        return new IntegerArgumentBuilder();
    }

    @Override
    public @NotNull ParsedArgument<Integer> parse(CommandContext context) throws CommandException {
        int i;
        String s = context.input()[context.pointer()];
        //parse the int
        try {
            i = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new CommandException(notIntegerMessage.apply(s), context);
        }

        //check limitations
        CommandSender sender = context.sender();
        Optional<Component> any = filters.stream()
                .map(l -> l.check(sender, i)).filter(Objects::nonNull).findAny();
        if (any.isPresent()) {
            throw new CommandException(any.get(), context);
        }

        //return as parsed argument
        return new ParsedArgument<>(i, 1);
    }

    private record TabCompleter(List<Completion<Integer>> completions, List<Filter<Integer>> filters)
            implements at.haha007.edencommands.TabCompleter {

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
    public static class MinimumFilter implements Filter<Integer> {
        private final Component error;
        private final int min;

        public MinimumFilter(Component error, int min) {
            this.error = error;
            this.min = min;
        }

        public Component check(CommandSender sender, Integer i) {
            if (i < min)
                return error;
            return null;
        }
    }

    /**
     * Filters values by a minimum, Smaller values can't be parsed
     */
    @NotNull
    public static class RangeFilter implements Filter<Integer> {
        private final Component error;
        private final float min;
        private final float max;

        public RangeFilter(Component error, long min, long max) {
            this.error = error;
            this.min = min;
            this.max = max;
        }

        public Component check(CommandSender sender, Integer i) {
            if (i < min)
                return error;
            if (i > max)
                return error;
            return null;
        }
    }

    /**
     * Filters values by a maximum, Larger values can't be parsed
     */
    @NotNull
    public static class MaximumFilter implements Filter<Integer> {
        private final Component error;
        private final int max;

        public MaximumFilter(Component error, int max) {
            this.error = error;
            this.max = max;
        }

        public Component check(CommandSender sender, Integer i) {
            if (i > max)
                return error;
            return null;
        }
    }

    public static class IntegerArgumentBuilder {
        private ArrayList<Filter<Integer>> filters;
        private Function<String, Component> notIntegerMessage;
        private ArrayList<Completion<Integer>> completions;
        private TriState filterByName;

        IntegerArgumentBuilder() {
        }

        public IntegerArgumentBuilder filter(Filter<Integer> filter) {
            if (this.filters == null) this.filters = new ArrayList<>();
            this.filters.add(filter);
            return this;
        }

        public IntegerArgumentBuilder filters(Collection<? extends Filter<Integer>> filters) {
            if (this.filters == null) this.filters = new ArrayList<>();
            this.filters.addAll(filters);
            return this;
        }

        public IntegerArgumentBuilder clearFilters() {
            if (this.filters != null)
                this.filters.clear();
            return this;
        }

        public IntegerArgumentBuilder notIntegerMessage(Function<String, Component> notIntegerMessage) {
            this.notIntegerMessage = notIntegerMessage;
            return this;
        }

        public IntegerArgumentBuilder completion(Completion<Integer> completion) {
            if (this.completions == null) this.completions = new ArrayList<>();
            this.completions.add(completion);
            return this;
        }

        public IntegerArgumentBuilder completions(Collection<? extends Completion<Integer>> completions) {
            if (this.completions == null) this.completions = new ArrayList<>();
            this.completions.addAll(completions);
            return this;
        }

        public IntegerArgumentBuilder clearCompletions() {
            if (this.completions != null)
                this.completions.clear();
            return this;
        }

        public IntegerArgumentBuilder filterByName(TriState filterByName) {
            this.filterByName = filterByName;
            return this;
        }

        public IntegerArgument build() {
            List<Filter<Integer>> filters = switch (this.filters == null ? 0 : this.filters.size()) {
                case 0 -> Collections.emptyList();
                case 1 -> Collections.singletonList(this.filters.get(0));
                default -> List.copyOf(this.filters);
            };
            List<Completion<Integer>> completions = switch (this.completions == null ? 0 : this.completions.size()) {
                case 0 -> Collections.emptyList();
                case 1 -> Collections.singletonList(this.completions.get(0));
                default -> List.copyOf(this.completions);
            };

            return new IntegerArgument(filters, notIntegerMessage, completions, filterByName);
        }

        public String toString() {
            return "IntegerArgument.IntegerArgumentBuilder(filters=" + this.filters + ", notIntegerMessage=" + this.notIntegerMessage + ", completions=" + this.completions + ", filterByName=" + this.filterByName + ")";
        }
    }
}
