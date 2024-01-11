package at.haha007.edencommands;

import org.bukkit.command.CommandSender;

import java.util.Arrays;
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
public record CommandContext(CommandSender sender,
                             String[] input,
                             Map<String, Parameter<?>> parsedParameters,
                             int pointer) {

    /**
     * @param key the key of the @{@link at.haha007.edencommands.tree.ArgumentCommandNode} that parsed the value
     * @param <T> the Type to cast the argument to
     * @return the parsed parameter cast to a provided Type
     * @throws ClassCastException       if the argument was of a different class
     * @throws IllegalArgumentException if the argument is not in the map
     */
    @SuppressWarnings("unchecked")
    public <T> T parameter(String key) {
        Parameter<?> parameter = parsedParameters.get(key);
        if (parameter == null) {
            throw new IllegalArgumentException("There is no argument with this key: '" + key + "'");
        }
        return (T) parameter.arg();
    }


    public String parameterString(String key) {
        Parameter<?> parameter = parsedParameters.get(key);
        if (parameter == null) {
            throw new IllegalArgumentException("There is no argument with this key: '" + key + "'");
        }
        String[] arr = Arrays.copyOfRange(input, parameter.from(), parameter.to() + 1);
        return String.join(" ", arr);
    }


    //both ends inclusive
    public record Parameter<T>(T arg, int from, int to) {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommandContext that = (CommandContext) o;

        if (pointer != that.pointer) return false;
        if (!sender.equals(that.sender)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(input, that.input)) return false;
        return parsedParameters.equals(that.parsedParameters);
    }

    @Override
    public int hashCode() {
        int result = sender.hashCode();
        result = 31 * result + Arrays.hashCode(input);
        result = 31 * result + parsedParameters.hashCode();
        result = 31 * result + pointer;
        return result;
    }

    @Override
    public String toString() {
        return "CommandContext{" +
                "sender=" + sender +
                ", input=" + Arrays.toString(input) +
                ", parsedParameters=" + parsedParameters +
                ", pointer=" + pointer +
                '}';
    }
}
