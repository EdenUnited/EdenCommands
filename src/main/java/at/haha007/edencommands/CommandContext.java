package at.haha007.edencommands;

import org.bukkit.command.CommandSender;

import java.util.Map;

/**
 * @param sender           The sender that executed the command
 * @param input            the command input, split by whitespace.
 *                         "/command foo bar" -> ["command", "foo", "bar"]
 * @param parsedParameters The command arguments that got parsed by some @{@link at.haha007.edencommands.argument.Argument} implementation
 *                         Access via CommandContext.parameter(key)
 * @param pointer          The index the node is at
 *                         Heavily used by @{@link at.haha007.edencommands.argument.Argument} implementations
 *                         "/command foo bar"
 *                         0     1   2
 */
public record CommandContext(CommandSender sender, String[] input, Map<String, ?> parsedParameters, int pointer) {

    /**
     * @param key the key of the @{@link at.haha007.edencommands.tree.ArgumentCommandNode} that parsed the value
     * @param <T> the Type to cast the argument to
     * @return the parsed parameter cast to a provided Type
     * @throws ClassCastException       if the argument was of a different class
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
