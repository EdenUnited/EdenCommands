package at.haha007.edencommands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

public class CommandException extends Exception {
    @Nullable
    private final transient Component errorMessage;
    @Nullable
    private final transient CommandContext context;

    public CommandException(@Nullable Component errorMessage, @Nullable CommandContext context) {
        this.errorMessage = errorMessage;
        this.context = context;
    }

    public void sendErrorMessage(CommandSender sender) {
        if (errorMessage != null)
            sender.sendMessage(errorMessage);
    }

    @Nullable
    public CommandContext context() {
        return this.context;
    }
}
