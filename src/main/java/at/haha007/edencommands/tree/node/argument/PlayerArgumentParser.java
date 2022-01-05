package at.haha007.edencommands.tree.node.argument;

import at.haha007.edencommands.tree.CommandContext;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class PlayerArgumentParser implements ArgumentParser<OfflinePlayer> {
    private static final PlayerArgumentParser std = new PlayerArgumentParser();
    private static final PlayerArgumentParser exact = new PlayerArgumentParser() {
        public OfflinePlayer parse(String name) {
            return Bukkit.getPlayerExact(name);
        }
    };
    private static final PlayerArgumentParser offline = new PlayerArgumentParser() {
        public OfflinePlayer parse(String name) {
            UUID uuid = Bukkit.getPlayerUniqueId(name);
            if (uuid == null) return null;
            return Bukkit.getOfflinePlayer(uuid);
        }
    };

    public OfflinePlayer parse(String name) {
        return Bukkit.getPlayer(name);
    }

    public static PlayerArgumentParser playerParser() {
        return std;
    }

    public static PlayerArgumentParser exactPlayerParser() {
        return exact;
    }

    public static PlayerArgumentParser offlinePlayerParser() {
        return offline;
    }

    public static Function<CommandContext, List<String>> playerTabCompleter() {
        return c -> Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).toList();
    }
}
