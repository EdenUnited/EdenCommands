package at.haha007.edencommands.argument;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommaSeparatedArgument<T> extends Argument<List<T>> {
    private final Argument<T> argument;
    private final boolean distinct;

    public CommaSeparatedArgument(Argument<T> argument, boolean distinct) {
        super(new CommaSeparatedTabCompleter<>(argument, distinct)::tabComplete, false);
        this.argument = argument;
        this.distinct = distinct;
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

        return new ParsedArgument<>(distinct ? list.stream().distinct().toList() : list, 1);
    }

    private record CommaSeparatedTabCompleter<T>(Argument<T> argument, boolean distinct) {
        public List<AsyncTabCompleteEvent.Completion> tabComplete(CommandContext context) {
            String key = context.input()[context.pointer()];
            String[] args = key.split(",", -1);
            Set<String> blocked = Arrays.stream(args).map(String::toLowerCase).collect(Collectors.toSet());
            String argsStr = String.join(",", Arrays.copyOfRange(args, 0, args.length - 1));
            String start = !argsStr.isEmpty() ? argsStr + "," : argsStr;
            context.input()[context.pointer()] = args[args.length - 1];
            List<AsyncTabCompleteEvent.Completion> completions = argument.tabComplete(context);
            Stream<AsyncTabCompleteEvent.Completion> stream = completions.stream();
            if (distinct) {
                stream = stream.filter(s -> !blocked.contains(s.suggestion().toLowerCase()));
            }
            return stream.map(c -> AsyncTabCompleteEvent.Completion.completion(start + c.suggestion(), c.tooltip()))
                    .toList();
        }
    }
}
