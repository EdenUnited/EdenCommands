package at.haha007.edencommands.tree;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.argument.ParsedArgument;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.command.CommandSender;

import java.util.Map;

@Accessors(fluent = true)
@AllArgsConstructor()
public class InternalContext {
    @Getter(AccessLevel.PACKAGE)
    private final CommandSender sender;
    private final String[] input;
    private final int stackPointer;
    private final Map<String, Object> parsedArguments;

    String current() {
        return input[stackPointer];
    }

    InternalContext next() {
        return new InternalContext(sender, input, stackPointer + 1, parsedArguments);
    }

    InternalContext next(int pointerIncrements) {
        return new InternalContext(sender, input, stackPointer + pointerIncrements, parsedArguments);
    }

    boolean hasNext() {
        return input.length > stackPointer + 1;
    }

    void putArgument(String key, ParsedArgument<?> argument) {
        parsedArguments.put(key, argument.result());
    }

    CommandContext context() {
        return new CommandContext(sender, input, parsedArguments, stackPointer);
    }

}
