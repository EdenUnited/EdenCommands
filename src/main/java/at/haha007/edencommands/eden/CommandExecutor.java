package at.haha007.edencommands.eden;

public interface CommandExecutor {
    /**
     * @param context Context to get arguments and sender
     */
    void execute(CommandContext context) throws CommandException;
}
