package at.haha007.edencommands.argument;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class EnumArgument<T extends Enum<T>> extends Argument<T> {
    @NotNull
    private final Class<T> clazz;
    @NotNull
    private final Function<String, Component> errorMessage;
    @NotNull
    private final List<Filter<T>> filters;

    public static <U extends Enum<U>> EnumArgumentBuilder<U> builder(Class<U> clazz) {
        return new EnumArgumentBuilder<>(clazz);
    }

    private EnumArgument(@NotNull Class<T> clazz, Function<String, Component> errorMessage, Function<T, Component> tooltipProvider, @NotNull List<Filter<T>> filters) {
        super(new TabCompleter<>(clazz, tooltipProvider == null ? t -> null : tooltipProvider, filters), true);
        if (!clazz.isEnum()) throw new IllegalArgumentException();
        if (clazz.getEnumConstants().length == 0) throw new IllegalArgumentException();
        this.errorMessage = errorMessage == null ? s -> Component.text("Unknown value <%s>".formatted(s)) : errorMessage;
        this.clazz = clazz;
        this.filters = filters;
    }

    public @NotNull ParsedArgument<T> parse(CommandContext context) throws CommandException {
        try {
            CommandSender sender = context.sender();
            T value = Enum.valueOf(clazz, context.input()[context.pointer()].toUpperCase());
            Optional<Component> any = filters.stream().map(l -> l.check(sender, value)).filter(Objects::nonNull).findAny();
            if (any.isPresent()) {
                throw new CommandException(any.get(), context);
            }
            return new ParsedArgument<>(value, 1);
        } catch (IllegalArgumentException e) {
            throw new CommandException(errorMessage.apply(context.input()[context.pointer()]), context);
        }
    }


    private record TabCompleter<T extends Enum<T>>(@NotNull Class<T> clazz,
                                                   @NotNull Function<T, Component> tooltipProvider,
                                                   @NotNull List<Filter<T>> filters)
            implements Function<CommandContext, List<AsyncTabCompleteEvent.Completion>> {

        public List<AsyncTabCompleteEvent.Completion> apply(CommandContext context) {
            List<AsyncTabCompleteEvent.Completion> list = new ArrayList<>();
            CommandSender sender = context.sender();
            for (T t : clazz.getEnumConstants()) {
                Optional<Component> any = filters.stream().map(l -> l.check(sender, t)).filter(Objects::nonNull).findAny();
                if (any.isPresent())
                    continue;
                String name = t.name();
                String suggestion = name.toLowerCase();
                Component tooltip = tooltipProvider.apply(t);
                list.add(AsyncTabCompleteEvent.Completion.completion(suggestion, tooltip));
            }
            return list;
        }
    }

    public static class EnumArgumentBuilder<T extends Enum<T>> {
        @NotNull
        private final Class<T> clazz;
        private Function<String, Component> errorMessage;
        private Function<T, Component> tooltipProvider;
        @NotNull
        private final List<Filter<T>> filters = new ArrayList<>();

        private EnumArgumentBuilder(@NotNull Class<T> clazz) {
            this.clazz = clazz;
        }

        public EnumArgument<T> build() {
            return new EnumArgument<>(clazz, errorMessage, tooltipProvider, filters);
        }

        public EnumArgumentBuilder<T> filter(Filter<T> filter) {
            filters.add(filter);
            return this;
        }

        public EnumArgumentBuilder<T> filters(Collection<Filter<T>> filters) {
            this.filters.addAll(filters);
            return this;
        }

        public EnumArgumentBuilder<T> errorMessage(Function<String, Component> errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public EnumArgumentBuilder<T> tooltipProvider(Function<T, Component> tooltipProvider) {
            this.tooltipProvider = tooltipProvider;
            return this;
        }

    }
}
