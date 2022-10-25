package at.haha007.edencommands.argument;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;

public enum StringArgument {
    ;

    public static Argument<String> word() {
        return new Argument<>() {
            @NotNull
            public ParsedArgument<String> parse(CommandContext context) {
                return new ParsedArgument<>(context.input()[context.pointer()], 1);
            }
        };
    }

    public static Argument<String> quotedString(Component error) {
        return new Argument<>() {
            @NotNull
            public ParsedArgument<String> parse(CommandContext context) throws CommandException {
                int start = context.pointer();
                String[] input = context.input();
                input = Arrays.copyOfRange(input, start, input.length);

                StringBuilder sb = new StringBuilder();
                Iterator<String> itr = Arrays.stream(input).iterator();

                String s = itr.next();
                boolean isEnd = s.endsWith("\"") && !s.endsWith("\\\"");
                if (!s.startsWith("\""))
                    throw new CommandException(error, context);

                s = s.replaceAll("\\\\\"", "\"");
                sb.append(s);
                sb.deleteCharAt(0);
                int amount = 1;
                while (itr.hasNext() && !isEnd) {
                    s = itr.next();
                    isEnd = s.endsWith("\"") && !s.endsWith("\\\"");
                    s = s.replaceAll("\\\\\"", "\"");
                    sb.append(" ").append(s);
                    amount++;
                }

                if(sb.length() == 0)
                    throw new CommandException(error, context);

                if(sb.charAt(sb.length() - 1) != '\"')
                    throw new CommandException(error, context);

                sb.deleteCharAt(sb.length() - 1);
                return new ParsedArgument<>(sb.toString(), amount);
            }
        };
    }

    public static Argument<String> greedy() {
        return new Argument<>() {
            @NotNull
            public ParsedArgument<String> parse(CommandContext context) {
                int start = context.pointer();
                String[] input = context.input();
                input = Arrays.copyOfRange(input, start, input.length);
                StringBuilder sb = new StringBuilder();
                Iterator<String> itr = Arrays.stream(input).iterator();
                sb.append(itr.next());
                while (itr.hasNext()) {
                    sb.append(" ").append(itr.next());
                }

                return new ParsedArgument<>(sb.toString(), input.length);
            }
        };
    }
}
