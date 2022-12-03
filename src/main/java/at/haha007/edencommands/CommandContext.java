package at.haha007.edencommands;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.function.Predicate;

@AllArgsConstructor
@EqualsAndHashCode
@Accessors(fluent = true)
public class CommandContext {
    /**
     * The sender that executed the command
     */
    @Getter
    private final CommandSender sender;
    /**
     * the command input, split by whitespace.
     * "/command foo bar" -> ["command", "foo", "bar"]
     */
    @Getter
    private final String[] input;
    /**
     * The command arguments that got parsed by some @{@link at.haha007.edencommands.argument.Argument} implementation
     * Access via CommandContext.parameter(key)
     */
    @Getter
    private final Map<String, ?> parsedParameters;
    /**
     * The index the node is at
     * Heavily used by @{@link at.haha007.edencommands.argument.Argument} implementations
     * "/command foo bar"
     *      0     1   2
     */
    @Getter
    private final int pointer;

    /**
     * @param key the key of the @{@link at.haha007.edencommands.tree.ArgumentCommandNode} that parsed the value
     * @return the parsed parameter cast to a provided Type
     * @param <T> the Type to cast the argument to
     * @throws ClassCastException if the argument was of a different class
     * @throws IllegalArgumentException if the argument is not in the map
     */
    @SuppressWarnings("unchecked")
    public <T> T parameter(String key) {
        Object argument = parsedParameters.get(key);
        if (argument == null) {
            throw new IllegalArgumentException("There is no argument with this key: '" + key + "'");
        }
        return (T) argument;
    }
}
