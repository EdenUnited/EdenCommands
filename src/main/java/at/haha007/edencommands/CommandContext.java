package at.haha007.edencommands;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.command.CommandSender;

import java.util.Map;

@AllArgsConstructor
@EqualsAndHashCode
@Accessors(fluent = true)
public class CommandContext {
    @Getter
    private final CommandSender sender;
    @Getter
    private final String[] input;
    @Getter
    private final Map<String, ?> parsedParameters;
    @Getter
    private final int pointer;

    @SuppressWarnings("unchecked")
    public <T> T parameter(String key) {
        Object argument = parsedParameters.get(key);
        if (argument == null) {
            throw new IllegalArgumentException("There is no argument with this key: '" + key + "'");
        }
        return (T) argument;
    }
}
