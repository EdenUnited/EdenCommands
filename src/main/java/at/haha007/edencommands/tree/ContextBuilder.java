package at.haha007.edencommands.tree;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandContext.Parameter;
import at.haha007.edencommands.argument.ParsedArgument;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class ContextBuilder {
    private final CommandSender sender;
    private final String[] input;
    private final int stackPointer;
    private final Map<String, Parameter<?>> parsedArguments;

    private ContextBuilder(CommandSender sender, String[] input, int stackPointer, Map<String, CommandContext.Parameter<?>> parsedArguments) {
        this.sender = sender;
        this.input = input;
        this.stackPointer = stackPointer;
        this.parsedArguments = parsedArguments;
    }

    public ContextBuilder(CommandSender sender, String[] input) {
        this(sender, input, 0, new LinkedHashMap<>());
    }

    public String current() {
        return input[stackPointer];
    }

    public ContextBuilder next() {
        return new ContextBuilder(sender, input, stackPointer + 1, parsedArguments);
    }

    public ContextBuilder next(int pointerIncrements) {
        return new ContextBuilder(sender, input, stackPointer + pointerIncrements, parsedArguments);
    }

    public boolean hasNext() {
        return input.length > stackPointer + 1;
    }

    public void putArgument(String key, ParsedArgument<?> argument) {
        CommandContext.Parameter<Object> arg = new Parameter<>(argument.result(), stackPointer, stackPointer + argument.pointerIncrements() - 1);
        parsedArguments.put(key, arg);
    }

    public CommandContext build() {
        return new CommandContext(sender, input.clone(), parsedArguments, stackPointer);
    }

    public CommandSender sender() {
        return this.sender;
    }

    @Override
    public String toString() {
        return "ContextBuilder{" +
                "sender=" + sender +
                ", input=" + Arrays.toString(input) +
                ", stackPointer=" + stackPointer +
                ", parsedArguments=" + parsedArguments +
                '}';
    }
}
