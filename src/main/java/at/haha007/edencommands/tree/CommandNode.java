package at.haha007.edencommands.tree;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import at.haha007.edencommands.CommandExecutor;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public abstract class CommandNode<T extends CommandNode<T>> {

    //child commands
    @NotNull
    private final List<CommandNode<?>> children;

    @Nullable
    private final CommandExecutor executor;

    //default executor ignores subcommands and just executes the command if there was no subcommand executed
    @Nullable
    private final CommandExecutor defaultExecutor;

    //can be a permission or any other requirement such as gamemode
    @Nullable
    private final Predicate<CommandContext> requirement;

    protected CommandNode(@NotNull List<CommandNode<?>> children,
                          @Nullable CommandExecutor executor,
                          @Nullable Predicate<CommandContext> requirement,
                          @Nullable CommandExecutor defaultExecutor) {
        this.children = children;
        this.executor = executor;
        this.requirement = requirement;
        this.defaultExecutor = defaultExecutor;
    }

    /**
     * @param context the command context to test
     * @return true if the requirement is met or no requirement is set
     */
    public boolean testRequirement(@NotNull CommandContext context) {
        if (requirement == null)
            return true;
        return requirement.test(context);
    }

    /**
     * @param context the internal context to parse
     * @return true if the command was executed successfully
     */
    public boolean execute(ContextBuilder context) {
        if (!testRequirement(context.build()))
            return false;
        if (!context.hasNext()) {
            try {
                if (executor != null) {
                    executor.execute(context.build());
                    return true;
                }
            } catch (CommandException e) {
                if (e.getMessage() != null)
                    e.sendErrorMessage(context.sender());
                else
                    return false;
                return true;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        for (CommandNode<?> child : children) {
            if (!context.hasNext())
                break;
            if (child.execute(context.next()))
                return true;
        }
        if (defaultExecutor == null)
            return false;
        try {
            defaultExecutor.execute(context.build());
            return true;
        } catch (CommandException e) {
            if (e.getMessage() != null)
                e.sendErrorMessage(context.sender());
            else
                return false;
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<AsyncTabCompleteEvent.Completion> tabComplete(ContextBuilder context) {
        if (context.hasNext()) {
            return children.stream()
                    .map(c -> c.tabComplete(context.next()))
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .toList();
        }
        return null;
    }

    @Override
    public String toString() {
        return "CommandNode{" +
                "children=" + children +
                ", executor=" + executor +
                ", defaultExecutor=" + defaultExecutor +
                ", requirement=" + requirement +
                '}';
    }
}
