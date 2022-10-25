package at.haha007.edencommands.argument;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public enum FloatArgument {
    ;

    public static Argument<Float> floatArgument(Component error) {
        return new Argument<>() {
            @NotNull
            public ParsedArgument<Float> parse(CommandContext context) throws CommandException {
                try {
                    return new ParsedArgument<>(Float.parseFloat(context.input()[context.pointer()]), 1);
                } catch (NumberFormatException e) {
                    throw new CommandException(error, context);
                }
            }
        };
    }

    public static Argument<Float> floatArgument(int min, Component error) {
        return new Argument<>() {
            @NotNull
            public ParsedArgument<Float> parse(CommandContext context) throws CommandException {
                try {
                    float f = Float.parseFloat(context.input()[context.pointer()]);
                    if (f < min)
                        throw new CommandException(error, context);
                    return new ParsedArgument<>(f, 1);

                } catch (NumberFormatException e) {
                    throw new CommandException(error, context);
                }
            }
        };
    }

    public static Argument<Float> floatArgument(int min, int max, Component error) {
        return new Argument<>() {
            @NotNull
            public ParsedArgument<Float> parse(CommandContext context) throws CommandException {
                try {
                    float f = Float.parseFloat(context.input()[context.pointer()]);
                    if (f < min || f > max)
                        throw new CommandException(error, context);
                    return new ParsedArgument<>(f, 1);
                } catch (NumberFormatException e) {
                    throw new CommandException(error, context);
                }
            }
        };
    }

}
