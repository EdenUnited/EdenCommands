package at.haha007.edencommands.eden.argument;

import at.haha007.edencommands.eden.CommandContext;
import at.haha007.edencommands.eden.CommandException;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public enum IntegerArgument {
    ;

    public static Argument<Integer> integer(Component error) {
        return new Argument<>() {
            @NotNull
            public ParsedArgument<Integer> parse(CommandContext context) throws CommandException {
                try {
                    return new ParsedArgument<>(Integer.parseInt(context.input()[context.pointer()]), 1);
                } catch (NumberFormatException e) {
                    throw new CommandException(error, context);
                }
            }
        };
    }

    public static Argument<Integer> integer(int min, Component error) {
        return new Argument<>() {
            @NotNull
            public ParsedArgument<Integer> parse(CommandContext context) throws CommandException {
                try {
                    int i = Integer.parseInt(context.input()[context.pointer()]);
                    if (i < min)
                        throw new CommandException(error, context);
                    return new ParsedArgument<>(i, 1);

                } catch (NumberFormatException e) {
                    throw new CommandException(error, context);
                }
            }
        };
    }

    public static Argument<Integer> integer(int min, int max, Component error) {
        return new Argument<>() {
            @NotNull
            public ParsedArgument<Integer> parse(CommandContext context) throws CommandException {
                try {
                    int i = Integer.parseInt(context.input()[context.pointer()]);
                    if (i < min || i > max)
                        throw new CommandException(error, context);
                    return new ParsedArgument<>(i, 1);
                } catch (NumberFormatException e) {
                    throw new CommandException(error, context);
                }
            }
        };
    }

}
