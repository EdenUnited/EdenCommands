package at.haha007.edencommands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

public class CommandException extends Exception {
    @Nullable
    private final Component errorMessage;
    @Nullable
    private final CommandContext context;

    public CommandException(@Nullable Component errorMessage, @Nullable CommandContext context) {
        this.errorMessage = errorMessage;
        this.context = context;
    }

    public void sendErrorMessage(CommandSender sender) {
        if (errorMessage != null)
            sender.sendMessage(errorMessage);
    }

    public @Nullable CommandContext context() {
        return this.context;
    }
}
