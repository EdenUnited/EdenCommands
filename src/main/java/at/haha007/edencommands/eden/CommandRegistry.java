package at.haha007.edencommands.eden;

import at.haha007.edencommands.eden.argument.Argument;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class CommandRegistry implements Listener {

    private final Map<String, NodeCommand> registeredCommands = new HashMap<>();
    private final Plugin plugin;

    public CommandRegistry(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void destroy() {
        registeredCommands.values().forEach(c -> Bukkit.getCommandMap().getKnownCommands().values().remove(c));
        registeredCommands.clear();
        HandlerList.unregisterAll(this);
    }

    public void register(LiteralCommandNode node) {
        String literal = node.literal().toLowerCase();
        if (registeredCommands.containsKey(literal)) {
            registeredCommands.get(literal).rootNode.merge(node);
            return;
        }
        NodeCommand command = new NodeCommand(node);
        registeredCommands.put(literal.toLowerCase(), command);
        Bukkit.getCommandMap().register(plugin.getName(), command);
    }

    @EventHandler
    private void onTabComplete(AsyncTabCompleteEvent event) {
        String buffer = event.getBuffer();
        if (!buffer.startsWith("/"))
            return;
        if (buffer.toLowerCase().startsWith("/" + plugin.getName().toLowerCase() + ":")) {
            buffer = "/" + buffer.substring(plugin.getName().length() + 2);
        }

        String[] args = buffer.substring(1).split("\\s+");
        if (buffer.endsWith(" ")) {
            String[] a = new String[args.length + 1];
            System.arraycopy(args, 0, a, 0, args.length);
            a[a.length - 1] = "";
            args = a;
        }

        NodeCommand command = registeredCommands.get(args[0].toLowerCase());
        if (command == null) return;
        List<String> completions = command.rootNode().tabComplete(new InternalContext(event.getSender(), args, 0, new LinkedHashMap<>()));
        if (completions == null)
            completions = List.of();
        completions = completions.stream().distinct().sorted().toList();
        event.setCompletions(completions);
    }

    public static LiteralCommandNode literal(String key) {
        return new LiteralCommandNode(key);
    }

    public static <T> ArgumentCommandNode<T> argument(String key, Argument<T> argument) {
        return new ArgumentCommandNode<>(key, argument);
    }

    public static Predicate<CommandSender> permission(String permission) {
        return new PermissionRequirement(permission);
    }

    private record PermissionRequirement(String permission) implements Predicate<CommandSender> {
        public boolean test(CommandSender sender) {
            return sender.hasPermission(permission);
        }
    }

    private class NodeCommand extends Command {
        private final LiteralCommandNode rootNode;

        private NodeCommand(LiteralCommandNode node) {
            super(node.literal().toLowerCase());
            rootNode = node;
        }

        public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
            String[] input = new String[args.length + 1];
            input[0] = rootNode.literal();
            System.arraycopy(args, 0, input, 1, args.length);
            Bukkit.getScheduler().runTaskAsynchronously(plugin,
                    () -> rootNode.execute(new InternalContext(sender, input, 0, new LinkedHashMap<>())));
            return true;
        }

        public boolean testPermissionSilent(@NotNull CommandSender target) {
            if (!(rootNode.requirement() instanceof PermissionRequirement))
                return true;
            return rootNode.testRequirement(target);
        }

        public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args)
                throws IllegalArgumentException {
            return List.of();
        }

        public LiteralCommandNode rootNode() {
            return rootNode;
        }
    }
}
