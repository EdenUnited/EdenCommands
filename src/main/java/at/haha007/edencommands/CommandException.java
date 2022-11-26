package at.haha007.edencommands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
@Accessors(fluent = true)
public class CommandException extends Exception {
    @Nullable
    private Component errorMessage;
    @Nullable
    @Getter
    private CommandContext context;

    public void sendErrorMessage(CommandSender sender) {
        if (errorMessage != null)
            sender.sendMessage(errorMessage);
    }
}
