package at.haha007.edencommands.argument;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public interface Filter<T> {
    /**
     * applies limitations to the parsing
     *
     * @param sender the sender of the command
     * @param type   the T that should be filtered
     * @return the error message that should be shown or null if it passed
     */
    Component check(CommandSender sender, T type);
}

