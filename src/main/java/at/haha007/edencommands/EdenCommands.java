package at.haha007.edencommands;

import at.haha007.edencommands.annotations.Command;
import at.haha007.edencommands.annotations.EnumCommandType;
import at.haha007.edencommands.tree.CommandContext;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.stream.Collectors;

import static at.haha007.edencommands.tree.node.ArgumentCommandNode.argument;
import static at.haha007.edencommands.tree.node.LiteralCommandNode.literal;
import static at.haha007.edencommands.tree.node.argument.IntegerArgumentParser.intParser;
import static at.haha007.edencommands.tree.node.argument.StringArgumentParser.stringParser;

public final class EdenCommands extends JavaPlugin implements Listener {
    @Getter
    private static EdenCommands edenCommands;

    public void onEnable() {
        edenCommands = this;
        CommandRegistry.register(literal("short").executes(c -> c.getSender().sendMessage("name: " + c.getSender().getName())));
        CommandRegistry.register(literal("test").then(
                argument("player", stringParser())
                        .tabCompletes(c -> Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList()))));
        CommandRegistry.register(
                literal("tab")
                        .executes(c -> c.getSender().sendMessage("Command executed"))
                        .then(argument("test", intParser(-10, 10)).executes(
                                c -> c.getSender().sendMessage(c.getParameter("test", Integer.class).toString())))
                        .then(literal("bau").executes(c -> c.getSender().sendMessage("bau")))
                        .then(literal("bbu").executes(c -> c.getSender().sendMessage("bbu")))
                        .then(literal("bcu").executes(c -> c.getSender().sendMessage("bcu")))
        );
        CommandRegistry.register(this);
    }

    @Command("sub")
    private final Object sub = new Object() {
        @Command("b")
        void b(CommandContext context) {
            context.getSender().sendMessage("b");
        }

        @Command(value = "c,-10,10", type = EnumCommandType.INTEGER)
        void c(CommandContext context) {
            context.getSender().sendMessage(context.getParameter("c", Integer.class).toString());
        }
    };

    @Command("myCommand")
    private void myCommand(CommandContext context) {
        context.getSender().sendMessage("name: " + String.join(" ", context.getRemainingCommand()));
    }

    @Command("meow")
    private void meow(CommandContext context) {
        context.getSender().sendMessage("name: " + context.getSender().getName());
    }
}
