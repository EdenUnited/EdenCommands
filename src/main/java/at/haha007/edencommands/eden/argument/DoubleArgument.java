package at.haha007.edencommands.eden.argument;

import at.haha007.edencommands.eden.CommandContext;
import at.haha007.edencommands.eden.CommandException;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public enum DoubleArgument {
    ;

    public static Argument<Double> doubleArgument(Component error) {
        return new Argument<>() {
            @NotNull
            public ParsedArgument<Double> parse(CommandContext context) throws CommandException {
                try {
                    return new ParsedArgument<>(Double.parseDouble(context.input()[context.pointer()]), 1);
                } catch (NumberFormatException e) {
                    throw new CommandException(error, context);
                }
            }
        };
    }

    public static Argument<Double> doubleArgument(int min, Component error) {
        return new Argument<>() {
            @NotNull
            public ParsedArgument<Double> parse(CommandContext context) throws CommandException {
                try {
                    double d = Double.parseDouble(context.input()[context.pointer()]);
                    if (d < min)
                        throw new CommandException(error, context);
                    return new ParsedArgument<>(d, 1);

                } catch (NumberFormatException e) {
                    throw new CommandException(error, context);
                }
            }
        };
    }

    public static Argument<Double> doubleArgument(int min, int max, Component error) {
        return new Argument<>() {
            @NotNull
            public ParsedArgument<Double> parse(CommandContext context) throws CommandException {
                try {
                    double d = Double.parseDouble(context.input()[context.pointer()]);
                    if (d < min || d > max)
                        throw new CommandException(error, context);
                    return new ParsedArgument<>(d, 1);
                } catch (NumberFormatException e) {
                    throw new CommandException(error, context);
                }
            }
        };
    }

}
