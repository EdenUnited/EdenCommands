package at.haha007.edencommands.tree;

import at.haha007.edencommands.CommandExecutor;
import at.haha007.edencommands.Requirement;
import org.jetbrains.annotations.NotNull;

public interface CommandBuilder<T extends CommandBuilder<T>> {
    /**
     * "/command subcommand"
     * "/command    other"
     * ^          ^
     * 1parent   2children
     *
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
    T executor(CommandExecutor executor);

    /**
     * @param requirement A condition that has to match to execute the command. ie: permissions, gamemode
     * @return this
     */
    @NotNull
    T requires(@NotNull Requirement requirement);

    /**
     * @param commandExecutor the @{@link CommandExecutor} that should be run when the command is run ignoring the subcommands if none are executed
     * @return this
     */
    @NotNull
    T defaultExecutor(CommandExecutor commandExecutor);

    /**
     * @return a clone of the builder
     */
    @NotNull
    T clone();

    /**
     * @return a new @{@link CommandNode}
     */
    @NotNull
    CommandNode build();
}
