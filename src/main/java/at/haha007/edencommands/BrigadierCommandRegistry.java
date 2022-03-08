package at.haha007.edencommands;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

//This class requires ProtocolLib, make sure to add the dependency in your plugin.yml
public class BrigadierCommandRegistry {
    private final CommandDispatcher<Player> dispatcher;
    private final ProtocolManager pm = ProtocolLibrary.getProtocolManager();
    private final PacketListener chatListener;
    private final PacketListener tabListener;

    public BrigadierCommandRegistry(Plugin plugin) {
        dispatcher = new CommandDispatcher<>();

        tabListener = new PacketAdapter(plugin, PacketType.Play.Client.TAB_COMPLETE) {
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                int transactionId = packet.getIntegers().read(0);
                String text = packet.getStrings().read(0);
                if (text.charAt(0) != '/')
                    return;
                if (text.startsWith("/edencommands:"))
                    text = text.replaceFirst("/edencommands:", "/");
                text = text.substring(1);
                ParseResults<Player> parseResults = dispatcher.parse(text, event.getPlayer());
                CompletableFuture<Suggestions> futureSuggestions = dispatcher.getCompletionSuggestions(parseResults);
                futureSuggestions
                        .thenApply(suggestions -> tabComplete(event.getPlayer(), suggestions, transactionId))
                        .thenAccept(completed -> {
                            if (completed)
                                event.setCancelled(true);
                        });
            }
        };

        chatListener = new PacketAdapter(plugin, PacketType.Play.Client.CHAT) {
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                String text = packet.getStrings().read(0);
                if (text.charAt(0) != '/')
                    return;
                if (text.startsWith("/edencommands:"))
                    text = text.replaceFirst("/edencommands:", "/");
                text = text.substring(1);
                try {
                    dispatcher.execute(text, event.getPlayer());
                } catch (CommandSyntaxException e) {
                    event.getPlayer().sendMessage(Component.text(e.getMessage(), NamedTextColor.RED));
                }
            }
        };

        pm.addPacketListener(tabListener);
        pm.addPacketListener(chatListener);
    }

    public CommandDispatcher<Player> dispatcher() {
        return dispatcher;
    }

    public void register(LiteralArgumentBuilder<Player> commandNode) {
        dispatcher.register(commandNode);

        Command command = new Command(commandNode.getLiteral()) {
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
                return false;
            }
        };
        command.setPermission("edencommands." + commandNode.getLiteral());
        Bukkit.getCommandMap().register("edencommands", command);
    }

    private boolean tabComplete(Player player, Suggestions suggestions, int transactionId) {
        List<Suggestion> list = suggestions.getList();
        if (list.isEmpty())
            return false;
        suggestions = new Suggestions(new StringRange(suggestions.getRange().getStart() + 1, suggestions.getRange().getEnd() + 1), suggestions.getList());
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.TAB_COMPLETE);
        packet.getIntegers().write(0, transactionId);
        packet.getModifier().write(1, suggestions);

        try {
            pm.sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return true;
    }

    public LiteralArgumentBuilder<Player> literal(String key) {
        return LiteralArgumentBuilder.literal(key);
    }

    public <T> RequiredArgumentBuilder<Player, T> argument(String key, ArgumentType<T> argumentType) {
        return RequiredArgumentBuilder.argument(key, argumentType);
    }

    public void unregister() {
        pm.removePacketListener(chatListener);
        pm.removePacketListener(tabListener);
    }
}
