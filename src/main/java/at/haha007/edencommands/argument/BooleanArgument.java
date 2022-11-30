package at.haha007.edencommands.argument;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import lombok.Builder;
import lombok.Singular;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class BooleanArgument extends Argument<Boolean> {
    @NotNull
    private final List<BooleanMode> modes;
    private final Function<String, Component> notBooleanError;

    @Builder
    public BooleanArgument(Function<String, Component> notBooleanError,
                           @NotNull @Singular List<BooleanMode> modes,
                           TriState filterByName) {
        super(context -> modes.stream()
                        .map(m -> new String[]{m.yes, m.no})
                        .flatMap(Arrays::stream)
                        .map(AsyncTabCompleteEvent.Completion::completion)
                        .toList(),
                filterByName == null || filterByName.toBooleanOrElse(true));
        this.modes = modes.isEmpty() ? List.of(BooleanMode.TRUE_FALSE) : modes;
        if (notBooleanError == null) {
            notBooleanError = s -> Component.text("Argument <", NamedTextColor.RED)
                    .append(Component.text(s, NamedTextColor.GOLD))
                    .append(Component.text("> must be of type double!", NamedTextColor.RED));
        }
        this.notBooleanError = notBooleanError;
    }

    @Override
    public @NotNull ParsedArgument<Boolean> parse(CommandContext context) throws CommandException {
        String s = context.input()[context.pointer()];
        for (BooleanMode mode : modes) {
            if (mode.yes.equalsIgnoreCase(s)) return new ParsedArgument<>(true, 1);
            if (mode.no.equalsIgnoreCase(s)) return new ParsedArgument<>(false, 1);
        }
        throw new CommandException(notBooleanError.apply(s), context);
    }

    public record BooleanMode(@Pattern("\\S+") String yes, @Pattern("\\S+") String no) {
        public static final BooleanMode TRUE_FALSE = new BooleanMode("true", "false");
        public static final BooleanMode YES_NO = new BooleanMode("yes", "no");
        public static final BooleanMode Y_N = new BooleanMode("y", "n");
    }
}
