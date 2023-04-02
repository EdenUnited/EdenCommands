package at.haha007.edencommands.argument;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class StringArgument extends Argument<String> {
    private final StringParser parser;

    public StringArgument(StringParser parser,
                          @NotNull List<AsyncTabCompleteEvent.Completion> completions,
                          TriState filterByName) {
        super(c -> completions, filterByName == null || filterByName.toBooleanOrElse(true));
        this.parser = parser == null ? word() : parser;
    }

    public static StringArgumentBuilder builder() {
        return new StringArgumentBuilder();
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

    public static class StringArgumentBuilder {
        private StringParser parser;
        private ArrayList<AsyncTabCompleteEvent.Completion> completions;
        private TriState filterByName;

        StringArgumentBuilder() {
        }

        public StringArgumentBuilder parser(StringParser parser) {
            this.parser = parser;
            return this;
        }

        public StringArgumentBuilder completion(AsyncTabCompleteEvent.Completion completion) {
            if (this.completions == null) this.completions = new ArrayList<>();
            this.completions.add(completion);
            return this;
        }

        public StringArgumentBuilder completions(Collection<? extends AsyncTabCompleteEvent.Completion> completions) {
            if (this.completions == null) this.completions = new ArrayList<>();
            this.completions.addAll(completions);
            return this;
        }

        public StringArgumentBuilder clearCompletions() {
            if (this.completions != null)
                this.completions.clear();
            return this;
        }

        public StringArgumentBuilder filterByName(TriState filterByName) {
            this.filterByName = filterByName;
            return this;
        }

        public StringArgument build() {
            List<AsyncTabCompleteEvent.Completion> completions = switch (this.completions == null ? 0 : this.completions.size()) {
                case 0 -> Collections.emptyList();
                case 1 -> Collections.singletonList(this.completions.get(0));
                default -> List.copyOf(this.completions);
            };

            return new StringArgument(parser, completions, filterByName);
        }

        public String toString() {
            return "StringArgument.StringArgumentBuilder(parser=" + this.parser + ", completions=" + this.completions + ", filterByName=" + this.filterByName + ")";
        }
    }
}