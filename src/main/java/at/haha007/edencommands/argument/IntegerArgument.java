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

public class IntegerArgument extends Argument<Integer> {
    @NotNull
    private final List<Filter<Integer>> limitations;
    @NotNull
    private final Function<String, Component> notIntegerMessage;

    @Builder
    private IntegerArgument(@NotNull @Singular java.util.List<Filter<Integer>> limitations,
                            Function<String, Component> notIntegerMessage,
                            @NotNull @Singular List<Integer> completions,
                            TriState filterByName) {

        super(new IntegerArgument.TabCompleter(completions, limitations), filterByName == null || filterByName.toBooleanOrElse(true));

        this.limitations = limitations;
        //create notIntegerMessage if it is not set
        if (notIntegerMessage == null) {
            notIntegerMessage = s -> Component.text("Argument <", NamedTextColor.RED)
                    .append(Component.text(s, NamedTextColor.GOLD))
                    .append(Component.text("> must be of type int!", NamedTextColor.RED));
        }
        this.notIntegerMessage = notIntegerMessage;
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
        Optional<Component> any = limitations.stream()
                .map(l -> l.checkLimit(sender, i)).filter(Objects::nonNull).findAny();
        if (any.isPresent()) {
            throw new CommandException(any.get(), context);
        }

        //return as parsed argument
        return new ParsedArgument<>(i, 1);
    }

    @AllArgsConstructor
    private static class TabCompleter implements Function<CommandContext, List<AsyncTabCompleteEvent.Completion>> {
        //format to 5 decimal places, 0.1+0.2 can be annoying
        private static final DecimalFormat format = new DecimalFormat("#.#####");
        private final List<Integer> completions;
        private final List<Filter<Integer>> filters;

        public List<AsyncTabCompleteEvent.Completion> apply(CommandContext context) {
            CommandSender sender = context.sender();
            return completions.stream()
                    .filter(f -> filters.stream().anyMatch(e -> e.checkLimit(sender, f) != null))
                    .map(format::format)
                    .map(AsyncTabCompleteEvent.Completion::completion)
                    .toList();
        }
    }

    /**
     * Filters values by a minimum, Smaller values can't be parsed
     */
    @AllArgsConstructor
    @NotNull
    public static class MinimumFilter implements Filter<Integer> {
        private final Component error;
        private final int min;

        public Component checkLimit(CommandSender sender, Integer d) {
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
    public static class MaximumFilter implements Filter<Integer> {
        private final Component error;
        private final int max;

        public Component checkLimit(CommandSender sender, Integer d) {
            if (max > d)
                return error;
            return null;
        }
    }
}
