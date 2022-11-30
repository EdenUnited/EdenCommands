package at.haha007.edencommands.argument;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class EnumArgument<T extends Enum<T>> extends Argument<T> {

    private final Class<T> clazz;
    private final Component errorMessage;

    public EnumArgument(@NotNull Class<T> clazz, Component errorMessage) {
        this(clazz, errorMessage, null);
    }

    public EnumArgument(@NotNull Class<T> clazz, Component errorMessage, Function<T, Component> tooltipProvider) {
        super(new TabCompleter<>(clazz, tooltipProvider == null ? t -> null : tooltipProvider), true);
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
        @NotNull
        private final Function<T, Component> tooltipProvider;

        public List<AsyncTabCompleteEvent.Completion> apply(CommandContext context) {
            List<AsyncTabCompleteEvent.Completion> list = new ArrayList<>();
            for (T t : clazz.getEnumConstants()) {
                String name = t.name();
                String suggestion = name.toLowerCase();
                Component tooltip = tooltipProvider.apply(t);
                list.add(AsyncTabCompleteEvent.Completion.completion(suggestion, tooltip));
            }
            return list;
        }
    }
}
