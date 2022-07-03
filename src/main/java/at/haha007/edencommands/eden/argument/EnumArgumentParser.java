package at.haha007.edencommands.eden.argument;

import at.haha007.edencommands.eden.CommandContext;
import at.haha007.edencommands.eden.CommandException;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class EnumArgumentParser<T extends Enum<T>> extends Argument<T> {

    private final Class<T> clazz;
    private final Component errorMessage;

    public EnumArgumentParser(@NotNull Class<T> clazz, Component errorMessage) {
        if (!clazz.isEnum()) throw new IllegalArgumentException();
        if (clazz.getEnumConstants().length == 0) throw new IllegalArgumentException();
        this.errorMessage = errorMessage;
        this.clazz = clazz;
        tabCompleter(context -> Arrays.stream(clazz.getEnumConstants()).map(T::name).map(String::toLowerCase).toList());
    }

    public @NotNull ParsedArgument<T> parse(CommandContext context) throws CommandException {
        try {
            return new ParsedArgument<>(Enum.valueOf(clazz, context.input()[context.pointer()].toUpperCase()), 1);
        } catch (IllegalArgumentException e) {
            throw new CommandException(errorMessage, context);
        }
    }
}
