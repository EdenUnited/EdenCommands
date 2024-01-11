package at.haha007.edencommands.argument.player;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import at.haha007.edencommands.argument.Argument;
import at.haha007.edencommands.argument.ParsedArgument;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class OfflinePlayerArgument extends Argument<OfflinePlayer> {
    private static final Random random = new Random();

    @NotNull
    private final Function<String, Component> playerNotFoundErrorProvider;

    private OfflinePlayerArgument(@Nullable at.haha007.edencommands.TabCompleter tabCompleter,
                                  @Nullable TriState filterByName,
                                  @Nullable Function<String, Component> playerNotFoundErrorProvider) {
        super(tabCompleter == null ? new TabCompleter() : tabCompleter, filterByName == null || filterByName.toBooleanOrElse(true));
        if (playerNotFoundErrorProvider == null)
            playerNotFoundErrorProvider = s -> Component.text("Player not found: <%s>!".formatted(s));
        this.playerNotFoundErrorProvider = playerNotFoundErrorProvider;
    }

    public static OfflinePlayerArgumentBuilder builder() {
        return new OfflinePlayerArgumentBuilder();
    }

    public @NotNull ParsedArgument<OfflinePlayer> parse(CommandContext context) throws CommandException {
        String key = context.input()[context.pointer()];
        if (key.isEmpty())
            throw new CommandException(playerNotFoundErrorProvider.apply(key), context);
        CommandSender sender = context.sender();
        final Location location;
        if (sender instanceof BlockCommandSender bcs) location = bcs.getBlock().getLocation().toCenterLocation();
        else if (sender instanceof Player player) location = player.getLocation();
        else location = null;
        switch (key.toLowerCase()) {
            //nearest
            case "@p" -> {
                if (location == null) throw new CommandException(playerNotFoundErrorProvider.apply(key), context);
                return new ParsedArgument<>(location.getWorld().getPlayers().stream()
                        .min(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(location)))
                        .orElseThrow(() -> new CommandException(playerNotFoundErrorProvider.apply(key), context)), 1);
            }
            //random
            case "@r" -> {
                Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
                if (players.length == 0) throw new CommandException(playerNotFoundErrorProvider.apply(key), context);
                return new ParsedArgument<>(players[random.nextInt(players.length)], 1);
            }
            //sender
            case "@s" -> {
                if (!(sender instanceof Player player))
                    throw new CommandException(playerNotFoundErrorProvider.apply(key), context);
                return new ParsedArgument<>(player, 1);
            }
            default -> {
                OfflinePlayer player;
                try {
                    UUID uuid = UUID.fromString(key);
                    player = Bukkit.getOfflinePlayer(uuid);
                } catch (IllegalArgumentException e) {
                    player = Bukkit.getOfflinePlayer(key);
                }
                //if the player has never been on the server throw
                if (player.getLastSeen() == 0)
                    throw new CommandException(playerNotFoundErrorProvider.apply(key), context);
                return new ParsedArgument<>(player, 1);
            }
        }
    }

    private static class TabCompleter implements at.haha007.edencommands.TabCompleter {
        public List<AsyncTabCompleteEvent.Completion> apply(CommandContext context) {
            List<String> completions = Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).toList();
            completions = new ArrayList<>(completions);
            completions.addAll(List.of("@p", "@r", "@s"));
            return completions.stream().map(AsyncTabCompleteEvent.Completion::completion).toList();
        }
    }

    public static class OfflinePlayerArgumentBuilder {
        private at.haha007.edencommands.TabCompleter tabCompleter;
        private TriState filterByName;
        private Function<String, Component> playerNotFoundErrorProvider;

        OfflinePlayerArgumentBuilder() {
        }

        public OfflinePlayerArgumentBuilder tabCompleter(at.haha007.edencommands.TabCompleter tabCompleter) {
            this.tabCompleter = tabCompleter;
            return this;
        }

        public OfflinePlayerArgumentBuilder filterByName(TriState filterByName) {
            this.filterByName = filterByName;
            return this;
        }

        public OfflinePlayerArgumentBuilder playerNotFoundErrorProvider(Function<String, Component> playerNotFoundErrorProvider) {
            this.playerNotFoundErrorProvider = playerNotFoundErrorProvider;
            return this;
        }

        public OfflinePlayerArgument build() {
            return new OfflinePlayerArgument(tabCompleter, filterByName, playerNotFoundErrorProvider);
        }

        public String toString() {
            return "OfflinePlayerArgument.OfflinePlayerArgumentBuilder(tabCompleter=" + this.tabCompleter + ", filterByName=" + this.filterByName + ", playerNotFoundErrorProvider=" + this.playerNotFoundErrorProvider + ")";
        }
    }
}
