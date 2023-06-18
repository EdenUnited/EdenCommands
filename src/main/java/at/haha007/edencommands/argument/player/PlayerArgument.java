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
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PlayerArgument extends Argument<Player> {
    private static final Random random = new Random();

    private final boolean exact;
    @NotNull
    private final Function<String, Component> playerNotFoundErrorProvider;

    private PlayerArgument(@Nullable at.haha007.edencommands.TabCompleter tabCompleter,
                           @Nullable TriState filterByName,
                           @Nullable TriState exact,
                           @Nullable Function<String, Component> playerNotFoundErrorProvider) {
        super(tabCompleter == null ? new TabCompleter() : tabCompleter, filterByName == null || filterByName.toBooleanOrElse(true));
        this.exact = exact == null || exact.toBooleanOrElse(true);
        if (playerNotFoundErrorProvider == null)
            playerNotFoundErrorProvider = s -> Component.text("Player not found: <%s>!".formatted(s));
        this.playerNotFoundErrorProvider = playerNotFoundErrorProvider;
    }

    public static PlayerArgumentBuilder builder() {
        return new PlayerArgumentBuilder();
    }

    public @NotNull ParsedArgument<Player> parse(CommandContext context) throws CommandException {
        String key = context.input()[context.pointer()];
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
        }


        Player player = exact ? Bukkit.getPlayerExact(key) : Bukkit.getPlayer(key);
        if (player == null)
            throw new CommandException(playerNotFoundErrorProvider.apply(key), context);
        return new ParsedArgument<>(player, 1);
    }

    private static class TabCompleter implements at.haha007.edencommands.TabCompleter {
        public List<AsyncTabCompleteEvent.Completion> apply(CommandContext context) {
            List<String> completions = Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).toList();
            completions = new ArrayList<>(completions);
            completions.addAll(List.of("@p", "@r", "@s"));
            return completions.stream().map(AsyncTabCompleteEvent.Completion::completion).collect(Collectors.toList());
        }
    }

    public static class PlayerArgumentBuilder {
        private at.haha007.edencommands.TabCompleter tabCompleter;
        private TriState filterByName;
        private TriState exact;
        private Function<String, Component> playerNotFoundErrorProvider;

        PlayerArgumentBuilder() {
        }

        public PlayerArgumentBuilder tabCompleter(at.haha007.edencommands.TabCompleter tabCompleter) {
            this.tabCompleter = tabCompleter;
            return this;
        }

        public PlayerArgumentBuilder filterByName(TriState filterByName) {
            this.filterByName = filterByName;
            return this;
        }

        public PlayerArgumentBuilder exact(TriState exact) {
            this.exact = exact;
            return this;
        }

        public PlayerArgumentBuilder playerNotFoundErrorProvider(Function<String, Component> playerNotFoundErrorProvider) {
            this.playerNotFoundErrorProvider = playerNotFoundErrorProvider;
            return this;
        }

        public PlayerArgument build() {
            return new PlayerArgument(tabCompleter, filterByName, exact, playerNotFoundErrorProvider);
        }

        public String toString() {
            return "PlayerArgument.PlayerArgumentBuilder(tabCompleter=" + this.tabCompleter + ", filterByName=" + this.filterByName + ", exact=" + this.exact + ", playerNotFoundErrorProvider=" + this.playerNotFoundErrorProvider + ")";
        }
    }
}
