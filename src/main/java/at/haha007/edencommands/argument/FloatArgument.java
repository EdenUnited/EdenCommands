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

public class FloatArgument extends Argument<Float> {
    @NotNull
    private final List<Filter<Float>> filters;
    @NotNull
    private final Function<String, Component> notFloatMessage;

    private FloatArgument(@NotNull List<Filter<Float>> filters,
                          @Nullable Function<String, Component> notFloatMessage,
                          @NotNull List<Completion<Float>> completions,
                          TriState filterByName) {

        super(new TabCompleter(completions, filters), filterByName == null || filterByName.toBooleanOrElse(true));

        this.filters = filters;
        //create notFloatMessage if it is not set
        if (notFloatMessage == null) {
            notFloatMessage = s -> Component.text("Argument <", NamedTextColor.RED)
                    .append(Component.text(s, NamedTextColor.GOLD))
                    .append(Component.text("> must be of type float!", NamedTextColor.RED));
        }
        this.notFloatMessage = notFloatMessage;
    }

    public static FloatArgumentBuilder builder() {
        return new FloatArgumentBuilder();
    }

    @Override
    public @NotNull ParsedArgument<Float> parse(CommandContext context) throws CommandException {
        float f;
        String s = context.input()[context.pointer()];
        //parse the float
        try {
            f = Float.parseFloat(s);
        } catch (NumberFormatException e) {
            throw new CommandException(notFloatMessage.apply(s), context);
        }

        //check limitations
        CommandSender sender = context.sender();
        Optional<Component> any = filters.stream()
                .map(l -> l.check(sender, f)).filter(Objects::nonNull).findAny();
        if (any.isPresent()) {
            throw new CommandException(any.get(), context);
        }

        //return as parsed argument
        return new ParsedArgument<>(f, 1);
    }

    private record TabCompleter(List<Completion<Float>> completions, List<Filter<Float>> filters)
            implements at.haha007.edencommands.TabCompleter {
        //format to 5 decimal places, 0.1+0.2 can be annoying
        private static final DecimalFormat format = new DecimalFormat("#.#####");

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
    public static class MinimumFilter implements Filter<Float> {
        private final Component error;
        private final float min;

        public MinimumFilter(Component error, float min) {
            this.error = error;
            this.min = min;
        }

        public Component check(CommandSender sender, Float f) {
            if (f < min)
                return error;
            return null;
        }
    }

    /**
     * Filters values by a minimum, Smaller values can't be parsed
     */
    @NotNull
    public static class RangeFilter implements Filter<Float> {
        private final Component error;
        private final float min;
        private final float max;

        public RangeFilter(Component error, float min, float max) {
            this.error = error;
            this.min = min;
            this.max = max;
        }

        public Component check(CommandSender sender, Float f) {
            if(f.isNaN())
                return error;
            if (f < min)
                return error;
            if (f > max)
                return error;
            return null;
        }
    }

    /**
     * Filters values by a maximum, Larger values can't be parsed
     */
    @NotNull
    public static class MaximumFilter implements Filter<Float> {
        private final Component error;
        private final float max;

        public MaximumFilter(Component error, float max) {
            this.error = error;
            this.max = max;
        }

        public Component check(CommandSender sender, Float f) {
            if (f > max)
                return error;
            return null;
        }
    }

    /**
     * Filters values by a maximum, Larger values can't be parsed
     */
    @NotNull
    public static class NaNFilter implements Filter<Float> {
        private final Component error;

        public NaNFilter(Component error) {
            this.error = error;
        }

        public Component check(CommandSender sender, Float d) {
            if (d.isNaN())
                return error;
            return null;
        }
    }

    public static class FloatArgumentBuilder {
        private ArrayList<Filter<Float>> filters;
        private Function<String, Component> notFloatMessage;
        private ArrayList<Completion<Float>> completions;
        private TriState filterByName;

        FloatArgumentBuilder() {
        }

        public FloatArgumentBuilder filter(Filter<Float> filter) {
            if (this.filters == null) this.filters = new ArrayList<>();
            this.filters.add(filter);
            return this;
        }

        public FloatArgumentBuilder filters(Collection<? extends Filter<Float>> filters) {
            if (this.filters == null) this.filters = new ArrayList<>();
            this.filters.addAll(filters);
            return this;
        }

        public FloatArgumentBuilder clearFilters() {
            if (this.filters != null)
                this.filters.clear();
            return this;
        }

        public FloatArgumentBuilder notFloatMessage(Function<String, Component> notFloatMessage) {
            this.notFloatMessage = notFloatMessage;
            return this;
        }

        public FloatArgumentBuilder completion(Completion<Float> completion) {
            if (this.completions == null) this.completions = new ArrayList<>();
            this.completions.add(completion);
            return this;
        }

        public FloatArgumentBuilder completions(Collection<? extends Completion<Float>> completions) {
            if (this.completions == null) this.completions = new ArrayList<>();
            this.completions.addAll(completions);
            return this;
        }

        public FloatArgumentBuilder clearCompletions() {
            if (this.completions != null)
                this.completions.clear();
            return this;
        }

        public FloatArgumentBuilder filterByName(TriState filterByName) {
            this.filterByName = filterByName;
            return this;
        }

        public FloatArgument build() {
            List<Filter<Float>> filters = switch (this.filters == null ? 0 : this.filters.size()) {
                case 0 -> Collections.emptyList();
                case 1 -> Collections.singletonList(this.filters.get(0));
                default -> List.copyOf(this.filters);
            };
            List<Completion<Float>> completions = switch (this.completions == null ? 0 : this.completions.size()) {
                case 0 -> Collections.emptyList();
                case 1 -> Collections.singletonList(this.completions.get(0));
                default -> List.copyOf(this.completions);
            };

            return new FloatArgument(filters, notFloatMessage, completions, filterByName);
        }

        public String toString() {
            return "FloatArgument.FloatArgumentBuilder(filters=" + this.filters + ", notFloatMessage=" + this.notFloatMessage + ", completions=" + this.completions + ", filterByName=" + this.filterByName + ")";
        }
    }
}
