package at.haha007.edencommands.eden;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.command.CommandSender;

import java.util.Map;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
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
    public <T> T parameter(String key, Class<T> clazz) {
        Object argument = parsedParameters.get(key);
        if (argument == null) {
            throw new IllegalArgumentException("There is no argument with this key: '" + key + "'");
        }
        if (!clazz.isAssignableFrom(argument.getClass())) {
            throw new IllegalArgumentException("The argument '" + key + "' is of type " + argument.getClass().getSimpleName() + ", not " + clazz.getSimpleName());
        }

        return (T) argument;
    }
}
