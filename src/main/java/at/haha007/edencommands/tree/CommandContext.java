package at.haha007.edencommands.tree;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandContext {
    @Setter
    @Getter
    private boolean wasExecuted;
    @Setter
    @Getter
    private CommandSender sender;
    @Setter
    @Getter
    private List<String> remainingCommand;
    @Setter
    @Getter
    private boolean permissionFailed = false;

    private final Map<String, Object> parameters = new HashMap<>();

    public <T> T getParameter(String key, Class<T> type) {
        return (T) parameters.get(key);
    }

    public <T> void addParameter(String key, T obj) {
        parameters.put(key, obj);
    }
}
