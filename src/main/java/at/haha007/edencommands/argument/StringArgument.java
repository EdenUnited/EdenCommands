package at.haha007.edencommands.argument;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import lombok.Builder;
import lombok.Singular;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class StringArgument extends Argument<String> {
    private final StringParser parser;

    @Builder
    public StringArgument(StringParser parser,
                          @NotNull @Singular List<AsyncTabCompleteEvent.Completion> completions,
                          TriState filterByName) {
        super(c -> completions, filterByName == null || filterByName.toBooleanOrElse(true));
        this.parser = parser == null ? word() : parser;
    }

    @Override
    public @NotNull ParsedArgument<String> parse(CommandContext context) throws CommandException {
        String[] input = context.input();
        input = Arrays.copyOfRange(input, context.pointer(), input.length);
        return parser.parse(input);
    }


    /**
     * @return A StringParser that matches a single word
     */
    public static StringParser word() {
        return new WordParser();
    }

    /**
     * @return A StringParser that matches the remaining command
     */
    public static StringParser greedy() {
        return new GreedyParser();
    }

    /**
     * @return A StringParser that matches a single word
     */
    public static StringParser quoted(Component quotationErrorMessage) {
        return new QuotedParser(quotationErrorMessage);
    }

    public interface StringParser {
        ParsedArgument<String> parse(String[] input) throws CommandException;
    }

    private static final class WordParser implements StringParser {
        public ParsedArgument<String> parse(String[] input) {
            return new ParsedArgument<>(input[0], 1);
        }
    }

    private static final class GreedyParser implements StringParser {
        public ParsedArgument<String> parse(String[] input) {
            return new ParsedArgument<>(String.join(" ", input), input.length);
        }
    }


    private record QuotedParser(Component error) implements StringParser {
        public ParsedArgument<String> parse(String[] input) throws CommandException {
            StringBuilder sb = new StringBuilder();
            Iterator<String> itr = Arrays.stream(input).iterator();

            String s = itr.next();
            boolean isEnd = s.endsWith("\"") && !s.endsWith("\\\"");
            if (!s.startsWith("\""))
                throw new CommandException(error, null);

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

            if (sb.length() == 0)
                throw new CommandException(error, null);

            if (sb.charAt(sb.length() - 1) != '\"')
                throw new CommandException(error, null);

            sb.deleteCharAt(sb.length() - 1);
            return new ParsedArgument<>(sb.toString(), amount);
        }
    }
}