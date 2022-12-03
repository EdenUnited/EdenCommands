package at.haha007.edencommands.tree;

import at.haha007.edencommands.CommandExecutor;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public interface CommandBuilder<T extends CommandBuilder<T>> {
    /**
     * "/command subcommand"
     * "/command    other"
     *     ^          ^
     *  1parent   2children
     * @param child A Child command under the current one
     * @return this
     */
    @NotNull
    T then(@NotNull CommandBuilder<?> child);

    /**
     * @param executor the @{@link CommandExecutor} that should be run when the command is run
     * @return this
     */
    @NotNull
    T executor(@NotNull CommandExecutor executor);

    /**
     * @param requirement A condition that has to match to execute the command. ie: permissions, gamemode
     * @return this
     */
    @NotNull
    T requires(@NotNull Predicate<CommandSender> requirement);

    /**
     * @param usage the Usage text that shows when the command failed
     * @return this
     */
    @NotNull
    T usageText(@NotNull Component usage);

    /**
     * @return a clone of the builder
     */
    @NotNull
    T clone();

    /**
     * @return a new @{@link CommandNode}
     */
    @NotNull
    CommandNode<?> build();
}
