package at.haha007.edencommands.argument;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class EnumArgument<T extends Enum<T>> extends Argument<T> {

    private final Class<T> clazz;
    private final Component errorMessage;

    public EnumArgument(@NotNull Class<T> clazz, Component errorMessage) {
        super(new TabCompleter<>(clazz), true);
        if (!clazz.isEnum()) throw new IllegalArgumentException();
        if (clazz.getEnumConstants().length == 0) throw new IllegalArgumentException();
        this.errorMessage = errorMessage;
        this.clazz = clazz;
    }

    public @NotNull ParsedArgument<T> parse(CommandContext context) throws CommandException {
        try {
            return new ParsedArgument<>(Enum.valueOf(clazz, context.input()[context.pointer()].toUpperCase()), 1);
        } catch (IllegalArgumentException e) {
            throw new CommandException(errorMessage, context);
        }
    }


    @AllArgsConstructor
    private static class TabCompleter<T extends Enum<T>> implements Function<CommandContext, List<AsyncTabCompleteEvent.Completion>> {
        private final Class<T> clazz;

        @Override
        public List<AsyncTabCompleteEvent.Completion> apply(CommandContext context) {
            return Arrays.stream(clazz.getEnumConstants())
                    .map(T::name).map(String::toLowerCase)
                    .map(AsyncTabCompleteEvent.Completion::completion)
                    .toList();
        }
    }
}
