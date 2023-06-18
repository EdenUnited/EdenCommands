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

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;

public class DoubleArgument extends Argument<Double> {
    @NotNull
    private final List<Filter<Double>> filters;
    @NotNull
    private final Function<String, Component> notDoubleMessage;

    private DoubleArgument(@NotNull List<Filter<Double>> filters,
                           @Nullable Function<String, Component> notDoubleMessage,
                           @NotNull List<Completion<Double>> completions,
                           TriState filterByName) {

        super(new TabCompleter(completions, filters), filterByName == null || filterByName.toBooleanOrElse(true));

        this.filters = filters;
        //create notDoubleMessage if it is not set
        if (notDoubleMessage == null) {
            notDoubleMessage = s -> Component.text("Argument <", NamedTextColor.RED)
                    .append(Component.text(s, NamedTextColor.GOLD))
                    .append(Component.text("> must be of type double!", NamedTextColor.RED));
        }
        this.notDoubleMessage = notDoubleMessage;
    }

    public static DoubleArgumentBuilder builder() {
        return new DoubleArgumentBuilder();
    }

    @Override
    public @NotNull ParsedArgument<Double> parse(CommandContext context) throws CommandException {
        double d;
        String s = context.input()[context.pointer()];
        //parse the double
        try {
            d = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new CommandException(notDoubleMessage.apply(s), context);
        }

        //check limitations
        CommandSender sender = context.sender();
        Optional<Component> any = filters.stream()
                .map(l -> l.check(sender, d)).filter(Objects::nonNull).findAny();
        if (any.isPresent()) {
            throw new CommandException(any.get(), context);
        }

        //return as parsed argument
        return new ParsedArgument<>(d, 1);
    }

    private record TabCompleter(List<Completion<Double>> completions, List<Filter<Double>> filters)
            implements at.haha007.edencommands.TabCompleter {
        //format to 8 decimal places, 0.1+0.2 can be annoying
        private static final DecimalFormat format = new DecimalFormat("#.########");

        public List<AsyncTabCompleteEvent.Completion> apply(CommandContext context) {
            CommandSender sender = context.sender();
            return completions.stream()
                    .filter(d -> filters.stream().noneMatch(e -> e.check(sender, d.completion()) != null))
                    .map(c -> AsyncTabCompleteEvent.Completion.completion(format.format(c.completion()), c.tooltip()))
                    .toList();
        }
    }

    /**
     * Filters values by a minimum, Smaller values can't be parsed
     */
    @NotNull
    public static class MinimumFilter implements Filter<Double> {
        private final Component error;
        private final double min;

        public MinimumFilter(Component error, double min) {
            this.error = error;
            this.min = min;
        }

        public Component check(CommandSender sender, Double d) {
            if (d < min)
                return error;
            return null;
        }
    }

    /**
     * Filters values by a minimum, Smaller values can't be parsed
     */
    @NotNull
    public static class RangeFilter implements Filter<Double> {
        private final Component error;
        private final double min;
        private final double max;

        public RangeFilter(Component error, double min, double max) {
            this.error = error;
            this.min = min;
            this.max = max;
        }

        public Component check(CommandSender sender, Double d) {
            if (d.isNaN())
                return error;
            if (d < min)
                return error;
            if (d > max)
                return error;
            return null;
        }
    }

    /**
     * Filters values by a maximum, Larger values can't be parsed
     */
    @NotNull
    public static class MaximumFilter implements Filter<Double> {
        private final Component error;
        private final double max;

        public MaximumFilter(Component error, double max) {
            this.error = error;
            this.max = max;
        }

        public Component check(CommandSender sender, Double d) {
            if (d > max)
                return error;
            return null;
        }
    }

    /**
     * Filters values by a maximum, Larger values can't be parsed
     */
    @NotNull
    public static class NaNFilter implements Filter<Double> {
        private final Component error;

        public NaNFilter(Component error) {
            this.error = error;
        }

        public Component check(CommandSender sender, Double d) {
            if (d.isNaN())
                return error;
            return null;
        }
    }

    public static class DoubleArgumentBuilder {
        private ArrayList<Filter<Double>> filters;
        private Function<String, Component> notDoubleMessage;
        private ArrayList<Completion<Double>> completions;
        private TriState filterByName;

        DoubleArgumentBuilder() {
        }

        public DoubleArgumentBuilder filter(Filter<Double> filter) {
            if (this.filters == null) this.filters = new ArrayList<>();
            this.filters.add(filter);
            return this;
        }

        public DoubleArgumentBuilder filters(Collection<? extends Filter<Double>> filters) {
            if (this.filters == null) this.filters = new ArrayList<>();
            this.filters.addAll(filters);
            return this;
        }

        public DoubleArgumentBuilder clearFilters() {
            if (this.filters != null)
                this.filters.clear();
            return this;
        }

        public DoubleArgumentBuilder notDoubleMessage(Function<String, Component> notDoubleMessage) {
            this.notDoubleMessage = notDoubleMessage;
            return this;
        }

        public DoubleArgumentBuilder completion(Completion<Double> completion) {
            if (this.completions == null) this.completions = new ArrayList<>();
            this.completions.add(completion);
            return this;
        }

        public DoubleArgumentBuilder completions(Collection<? extends Completion<Double>> completions) {
            if (this.completions == null) this.completions = new ArrayList<>();
            this.completions.addAll(completions);
            return this;
        }

        public DoubleArgumentBuilder clearCompletions() {
            if (this.completions != null)
                this.completions.clear();
            return this;
        }

        public DoubleArgumentBuilder filterByName(TriState filterByName) {
            this.filterByName = filterByName;
            return this;
        }

        public DoubleArgument build() {
            List<Filter<Double>> filters = switch (this.filters == null ? 0 : this.filters.size()) {
                case 0 -> Collections.emptyList();
                case 1 -> Collections.singletonList(this.filters.get(0));
                default -> List.copyOf(this.filters);
            };
            List<Completion<Double>> completions = switch (this.completions == null ? 0 : this.completions.size()) {
                case 0 -> Collections.emptyList();
                case 1 -> Collections.singletonList(this.completions.get(0));
                default -> List.copyOf(this.completions);
            };

            return new DoubleArgument(filters, notDoubleMessage, completions, filterByName);
        }

        public String toString() {
            return "DoubleArgument.DoubleArgumentBuilder(filters=" + this.filters + ", notDoubleMessage=" + this.notDoubleMessage + ", completions=" + this.completions + ", filterByName=" + this.filterByName + ")";
        }
    }
}
