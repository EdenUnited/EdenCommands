package at.haha007.edencommands.tree;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import at.haha007.edencommands.CommandExecutor;
import at.haha007.edencommands.Requirement;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public abstract class CommandNode {

    //child commands
    @NotNull
    private final List<CommandNode> children;

    @Nullable
    private final CommandExecutor executor;

    //default executor ignores subcommands and just executes the command if there was no subcommand executed
    @Nullable
    private final CommandExecutor defaultExecutor;

    //can be a permission or any other requirement such as gamemode
    @Nullable
    private final Requirement requirement;

    protected CommandNode(@NotNull List<CommandNode> children,
                          @Nullable CommandExecutor executor,
                          @Nullable Requirement requirement,
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

        if (context.hasNext()) {
            if(executeChildren(context)) return true;
        } else {
            try {
                if (executor != null) {
                    executor.execute(context.build());
                    return true;
                }
            } catch (CommandException e) {
                return handleException(context, e);
            } catch (Exception e) {
                context.printTrace(e);
            }
        }

        if (defaultExecutor == null)
            return false;
        try {
            defaultExecutor.execute(context.build());
            return true;
        } catch (CommandException e) {
            return handleException(context, e);
        } catch (Exception e) {
            context.printTrace(e);
        }
        return false;
    }

    private boolean handleException(ContextBuilder context, CommandException e) {
        if (e.getMessage() != null) {
            e.sendErrorMessage(context.sender());
            return true;
        } else {
            return false;
        }
    }

    private boolean executeChildren(ContextBuilder context) {
        for (CommandNode child : children) {
            if (child.execute(context.next()))
                return true;
        }
        return false;
    }

    @NotNull
    public List<AsyncTabCompleteEvent.Completion> tabComplete(ContextBuilder context) {
        if (context.hasNext()) {
            return children.stream()
                    .map(c -> c.tabComplete(context.next()))
                    .filter(Predicate.not(List::isEmpty))
                    .flatMap(List::stream)
                    .toList();
        }
        return List.of();
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
