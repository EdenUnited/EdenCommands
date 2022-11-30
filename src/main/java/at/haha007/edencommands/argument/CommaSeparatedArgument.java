package at.haha007.edencommands.argument;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommaSeparatedArgument<T> extends Argument<List<T>> {
    private final Argument<T> argument;

    public CommaSeparatedArgument(Argument<T> argument) {
        super(new CommaSeparatedTabCompleter<>(argument), false);
        this.argument = argument;
    }

    @Override
    public @NotNull ParsedArgument<List<T>> parse(CommandContext context) throws CommandException {
        String[] args = context.input()[context.pointer()].split(",");
        List<T> list = new ArrayList<>();
        for (String arg : args) {
            context.input()[context.pointer()] = arg;
            ParsedArgument<T> parsedT = argument.parse(context);
            if (parsedT.pointerIncrements() != 1)
                throw new IllegalStateException("The CommaSeparatedArgument is only applicable to Arguments with 1 pointer increment!");
            list.add(parsedT.result());
        }
        return new ParsedArgument<>(list, 1);
    }

    private static class CommaSeparatedTabCompleter<T> implements Function<CommandContext, List<AsyncTabCompleteEvent.Completion>> {
        private final Argument<T> argument;

        public CommaSeparatedTabCompleter(Argument<T> argument) {
            this.argument = argument;
        }

        public List<AsyncTabCompleteEvent.Completion> apply(CommandContext context) {
            String[] args = context.input()[context.pointer()].split(",");
            String argsStr = String.join(",", Arrays.copyOfRange(args, 0, args.length - 1));
            String start = argsStr.length() > 0 ? argsStr + "," : argsStr;
            context.input()[context.pointer()] = args[args.length - 1];
            List<AsyncTabCompleteEvent.Completion> completions = argument.tabComplete(context);
            return completions.stream()
                    .map(c -> AsyncTabCompleteEvent.Completion.completion(start + c.suggestion(), c.tooltip()))
                    .collect(Collectors.toList());
        }
    }
}
