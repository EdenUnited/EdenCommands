package at.haha007.edencommands.argument;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum PlayerArgument {
    ;

    private static final Random rand = new Random();

    private static List<String> tab(CommandContext context) {
        List<String> list = new ArrayList<>(Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).toList());
        if (context.sender() instanceof Player) {
            list.addAll(List.of("@p", "@r", "@s"));
        } else if (context.sender() instanceof BlockCommandSender) {
            list.add("@r");
            list.add("@p");
        } else {
            list.add("@r");
        }
        Collections.sort(list);
        return list;
    }

    private static Optional<Player> getNearestPlayer(Location location) {
        Vector v = location.toVector();
        Collection<Player> players = location.getWorld().getPlayers();
        return players.stream().min(Comparator.comparingDouble(p -> p.getLocation().toVector().distanceSquared(v)));
    }

    private static Optional<Player> getPlayer(String target, CommandSender sender, boolean exact) {
        if (target.equalsIgnoreCase("@p")) {
            if (sender instanceof BlockCommandSender blockCommandSender) {
                return getNearestPlayer(blockCommandSender.getBlock().getLocation().toCenterLocation());
            }
            if (sender instanceof Player player) {
                return getNearestPlayer(player.getLocation());
            }
        }
        if (target.equalsIgnoreCase("@r")) {
            Player[] online = Bukkit.getOnlinePlayers().toArray(new Player[0]);
            if (online.length == 0)
                return Optional.empty();
            return Optional.of(online[rand.nextInt(online.length)]);
        }
        if (target.equalsIgnoreCase("@s")) {
            if (sender instanceof Player player)
                return Optional.of(player);
        }

        //handle
        return Optional.ofNullable(exact ? Bukkit.getPlayerExact(target) : Bukkit.getPlayer(target));
    }

    public static Argument<Player> player(Component error) {
        Argument<Player> argument = new Argument<>() {
            @Override
            public @NotNull ParsedArgument<Player> parse(CommandContext context) throws CommandException {
                String target = context.input()[context.pointer()];
                CommandSender sender = context.sender();
                Player player = getPlayer(target, sender, false).orElseThrow(() -> new CommandException(error, context));
                return new ParsedArgument<>(player, 1);
            }
        };
        argument.tabCompleter(PlayerArgument::tab);
        return argument;
    }

    public static Argument<Player> exactPlayer(Component error) {
        Argument<Player> argument = new Argument<>() {
            @Override
            public @NotNull ParsedArgument<Player> parse(CommandContext context) throws CommandException {
                String target = context.input()[context.pointer()];
                CommandSender sender = context.sender();
                Player player = getPlayer(target, sender, true).orElseThrow(() -> new CommandException(error, context));
                return new ParsedArgument<>(player, 1);
            }
        };
        argument.tabCompleter(PlayerArgument::tab);
        return argument;
    }

    public static Argument<OfflinePlayer> offlinePlayer(Component error) {
        Argument<OfflinePlayer> argument = new Argument<>() {
            @Override
            public @NotNull ParsedArgument<OfflinePlayer> parse(CommandContext context) throws CommandException {
                String name = context.input()[context.pointer()];
                if (name.isBlank())
                    throw new CommandException(error, context);
                Optional<Player> optionalPlayer = getPlayer(name, context.sender(), false);
                if (optionalPlayer.isPresent())
                    return new ParsedArgument<>(optionalPlayer.get(), 1);
                OfflinePlayer player = Bukkit.getOfflinePlayer(name);
                if (!player.hasPlayedBefore())
                    throw new CommandException(error, context);
                return new ParsedArgument<>(player, 1);
            }
        };
        argument.tabCompleter(PlayerArgument::tab);
        return argument;
    }

    public static Argument<List<Player>> playerList(Function<String, Component> error) {
        return new Argument<>() {
            @Override
            public @NotNull ParsedArgument<List<Player>> parse(CommandContext context) throws CommandException {
                String[] a = context.input()[context.pointer()].split(",");
                if (a.length == 1 && a[0].equals("@a"))
                    return new ParsedArgument<>(new ArrayList<>(Bukkit.getOnlinePlayers()), 1);
                List<Player> list = new ArrayList<>();
                for (String key : a) {
                    list.add(getPlayer(key, context.sender(), true).orElseThrow(() -> new CommandException(error.apply(key), context)));
                }
                list = list.stream().distinct().toList();
                return new ParsedArgument<>(list, 1);
            }

            public List<String> tabComplete(CommandContext context) {
                String in = context.input()[context.pointer()];
                if (in.contains("@a")) return List.of("@a");
                String[] targets = split(in);
                String last = targets[targets.length - 1];
                Set<String> targetSet = new HashSet<>();
                for (String target : targets) {
                    targetSet.add(target.toLowerCase());
                }
                List<String> list = Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(s -> startsWith(s, last))
                        .filter(s -> !targetSet.contains(s.toLowerCase()))
                        .toList();
                list = new ArrayList<>(list);
                if (getPlayer(last, context.sender(), true).isPresent())
                    list.add(last + ",");
                list.sort(null);
                targets = Arrays.stream(targets).distinct().toList().toArray(new String[0]);
                String start = targets.length <= 1 ? "" : String.join(",", Arrays.copyOfRange(targets, 0, targets.length - 1)) + ",";
                return list.stream().map(s -> start + s).toList();
            }

            private String[] split(String s) {
                List<String> a = new ArrayList<>();
                Pattern pattern = Pattern.compile(",");
                Matcher matcher = pattern.matcher(s);
                while (matcher.find()) {
                    a.add(s.substring(0, matcher.start()));
                    s = s.substring(matcher.end());
                    matcher = pattern.matcher(s);
                }
                a.add(s);
                return a.toArray(new String[0]);
            }
        };
    }
}
