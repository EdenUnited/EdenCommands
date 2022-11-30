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

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class FloatArgument extends Argument<Float> {
    @NotNull
    private final List<Filter<Float>> filters;
    @NotNull
    private final Function<String, Component> notFloatMessage;

    @Builder
    private FloatArgument(@NotNull @Singular List<Filter<Float>> filters,
                           Function<String, Component> notFloatMessage,
                           @NotNull @Singular List<Completion<Float>> completions,
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

    @AllArgsConstructor
    private static class TabCompleter implements Function<CommandContext, List<AsyncTabCompleteEvent.Completion>> {
        //format to 5 decimal places, 0.1+0.2 can be annoying
        private static final DecimalFormat format = new DecimalFormat("#.#####");
        private final List<Completion<Float>> completions;
        private final List<Filter<Float>> filters;

        public List<AsyncTabCompleteEvent.Completion> apply(CommandContext context) {
            CommandSender sender = context.sender();
            return completions.stream()
                    .filter(f -> filters.stream().anyMatch(e -> e.check(sender, f.completion()) != null))
                    .map(c -> AsyncTabCompleteEvent.Completion.completion(format.format(c.completion()), c.tooltip()))
                    .toList();
        }

    }

    /**
     * Filters values by a minimum, Smaller values can't be parsed
     */
    @AllArgsConstructor
    @NotNull
    public static class MinimumFilter implements Filter<Float> {
        private final Component error;
        private final float min;

        public Component check(CommandSender sender, Float d) {
            if (min < d)
                return error;
            return null;
        }
    }

    /**
     * Filters values by a maximum, Larger values can't be parsed
     */
    @AllArgsConstructor
    @NotNull
    public static class MaximumFilter implements Filter<Float> {
        private final Component error;
        private final float max;

        public Component check(CommandSender sender, Float d) {
            if (max > d)
                return error;
            return null;
        }
    }

    /**
     * Filters values by a maximum, Larger values can't be parsed
     */
    @AllArgsConstructor
    @NotNull
    public static class NaNFilter implements Filter<Float> {
        private final Component error;

        public Component check(CommandSender sender, Float d) {
            if (d.isNaN())
                return error;
            return null;
        }
    }
}
