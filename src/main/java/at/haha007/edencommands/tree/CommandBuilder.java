package at.haha007.edencommands.tree;

import at.haha007.edencommands.CommandExecutor;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public interface CommandBuilder<T extends CommandBuilder<T>> {
    @NotNull
    T then(@NotNull CommandBuilder<?> child);

    @NotNull
    T executor(@NotNull CommandExecutor executor);

    @NotNull
    T requires(@NotNull Predicate<CommandSender> requirement);

    @NotNull
    T usageText(@NotNull Component usage);

    @NotNull
    T clone();

    @NotNull
    CommandNode<?> build();
}
