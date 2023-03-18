package at.haha007.edencommands.argument;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class BooleanArgument extends Argument<Boolean> {
    @NotNull
    private final List<BooleanMode> modes;
    private final Function<String, Component> notBooleanError;

    public BooleanArgument(Function<String, Component> notBooleanError,
                           @NotNull List<BooleanMode> modes,
                           TriState filterByName) {
        super(context -> (modes.isEmpty() ? List.of(BooleanMode.TRUE_FALSE) : modes).stream()
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

    public static BooleanArgumentBuilder builder() {
        return new BooleanArgumentBuilder();
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

    public static class BooleanArgumentBuilder {
        private Function<String, Component> notBooleanError;
        private ArrayList<BooleanMode> modes;
        private TriState filterByName;

        BooleanArgumentBuilder() {
        }

        public BooleanArgumentBuilder notBooleanError(Function<String, Component> notBooleanError) {
            this.notBooleanError = notBooleanError;
            return this;
        }

        public BooleanArgumentBuilder mode(BooleanMode mode) {
            if (this.modes == null) this.modes = new ArrayList<>();
            this.modes.add(mode);
            return this;
        }

        public BooleanArgumentBuilder modes(Collection<? extends BooleanMode> modes) {
            if (this.modes == null) this.modes = new ArrayList<>();
            this.modes.addAll(modes);
            return this;
        }

        public BooleanArgumentBuilder clearModes() {
            if (this.modes != null)
                this.modes.clear();
            return this;
        }

        public BooleanArgumentBuilder filterByName(TriState filterByName) {
            this.filterByName = filterByName;
            return this;
        }

        public BooleanArgument build() {
            List<BooleanMode> modes = switch (this.modes == null ? 0 : this.modes.size()) {
                case 0 -> java.util.Collections.emptyList();
                case 1 -> java.util.Collections.singletonList(this.modes.get(0));
                default -> List.copyOf(this.modes);
            };

            return new BooleanArgument(notBooleanError, modes, filterByName);
        }

        public String toString() {
            return "BooleanArgument.BooleanArgumentBuilder(notBooleanError=" + this.notBooleanError + ", modes=" + this.modes + ", filterByName=" + this.filterByName + ")";
        }
    }
}
