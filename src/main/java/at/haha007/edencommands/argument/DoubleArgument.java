package at.haha007.edencommands.argument;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
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

public class DoubleArgument extends Argument<Double> {
    @NotNull
    private final List<Filter<Double>> limitations;
    @NotNull
    private final Function<String, Component> notDoubleMessage;

    @Builder
    private DoubleArgument(@NotNull @Singular List<Filter<Double>> limitations,
                           Function<String, Component> notDoubleMessage,
                           @NotNull @Singular List<Double> completions,
                           TriState filterByName) {
        this.limitations = limitations;

        //create notDoubleMessage if it is not set
        if (notDoubleMessage == null) {
            notDoubleMessage = s -> Component.text("Argument <", NamedTextColor.RED)
                    .append(Component.text(s, NamedTextColor.GOLD))
                    .append(Component.text("> must be of type double!", NamedTextColor.RED));
        }
        this.notDoubleMessage = notDoubleMessage;

        //format to 8 decimal places, 0.1+0.2 can be annoying
        DecimalFormat format = new DecimalFormat("#.########");
        List<String> tabCompletions = completions.stream().map(format::format).toList();

        super.tabCompleter = c -> tabCompletions;
        if (filterByName == null) filterByName = TriState.NOT_SET;
        super.filterByName = filterByName.toBooleanOrElse(true);
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
        Optional<Component> any = limitations.stream()
                .map(l -> l.checkLimit(sender, d)).filter(Objects::nonNull).findAny();
        if (any.isPresent()) {
            throw new CommandException(any.get(), context);
        }

        //return as parsed argument
        return new ParsedArgument<>(d, 1);
    }

    /**
     * Filters values by a minimum, Smaller values can't be parsed
     */
    @AllArgsConstructor
    @NotNull
    public static class MinimumFilter implements Filter<Double> {
        private final Component error;
        private final double min;

        public Component checkLimit(CommandSender sender, Double d) {
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
    public static class MaximumFilter implements Filter<Double> {
        private final Component error;
        private final double max;

        public Component checkLimit(CommandSender sender, Double d) {
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
    public static class NaNFilter implements Filter<Double> {
        private final Component error;

        public Component checkLimit(CommandSender sender, Double d) {
            if (d.isNaN())
                return error;
            return null;
        }
    }
}
