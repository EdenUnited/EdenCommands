package at.haha007.edencommands.tree.node;

import at.haha007.edencommands.tree.CommandContext;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Stack;

public class NodeCommand extends Command {
    private final LiteralCommandNode root;

    public NodeCommand(LiteralCommandNode root) {
        super(root.getLiteral());
        this.root = root;
        Bukkit.getCommandMap().register("edencommands", this);
        setPermission("edencommands." + root.getPermission());
        Bukkit.getCommandMap().getKnownCommands().remove("edencommands:" + root.getLiteral());
    }

    public @Nullable String getPermission() {
        return "edencommands." + root.getPermission();
    }

    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        CommandContext context = new CommandContext();
        context.setSender(sender);
        Stack<String> stack = new Stack<>();
        for (int i = args.length - 1; i >= 0; i--) {
            stack.push(args[i]);
        }
        stack.push(root.getLiteral());
        root.execute(stack, context);
        if (!context.isWasExecuted() && context.isPermissionFailed()) {
            sender.sendMessage(Bukkit.getPermissionMessage());
            return true;
        }
        return context.isWasExecuted();
    }

    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String lab, @NotNull String[] args) throws IllegalArgumentException {
        CommandContext context = new CommandContext();
        context.setSender(sender);
        Stack<String> stack = new Stack<>();
        for (int i = args.length - 1; i >= 0; i--) {
            stack.push(args[i]);
        }
        stack.push(root.getLiteral());
        return root.tabComplete(stack, context);
    }
}
