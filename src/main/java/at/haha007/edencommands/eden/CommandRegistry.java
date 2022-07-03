package at.haha007.edencommands.eden;

import at.haha007.edencommands.eden.argument.Argument;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommandRegistry implements Listener {
    private final Map<String, NodeCommand> registeredCommands = new HashMap<>();

    public CommandRegistry(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void register(LiteralCommandNode node) {
        String literal = node.literal().toLowerCase();
        NodeCommand command = new NodeCommand(node);
        registeredCommands.put(literal.toLowerCase(), command);
        Bukkit.getCommandMap().register(literal, command);
        Bukkit.getCommandMap().getKnownCommands().remove(literal + ":" + literal);
    }

    @EventHandler
    private void onTabComplete(AsyncTabCompleteEvent event) {
        String buffer = event.getBuffer();
        if (!buffer.startsWith("/"))
            return;

        String[] args = buffer.substring(1).split("\\W+");
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

    public LiteralCommandNode literal(String key) {
        return new LiteralCommandNode(key);
    }

    public <T> ArgumentCommandNode<T> argument(String key, Argument<T> argument) {
        return new ArgumentCommandNode<>(key, argument);
    }


    private static class NodeCommand extends Command {
        private final LiteralCommandNode rootNode;

        private NodeCommand(LiteralCommandNode node) {
            super(node.literal().toLowerCase());
            rootNode = node;
        }

        public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
            String[] input = new String[args.length + 1];
            input[0] = label;
            System.arraycopy(args, 0, input, 1, args.length);
            return rootNode.execute(new InternalContext(sender, input, 0, new LinkedHashMap<>()));
        }

        public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
            return List.of();
        }

        public LiteralCommandNode rootNode() {
            return rootNode;
        }
    }
}
