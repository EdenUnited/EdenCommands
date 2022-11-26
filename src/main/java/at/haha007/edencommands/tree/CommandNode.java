package at.haha007.edencommands.tree;

import at.haha007.edencommands.CommandException;
import at.haha007.edencommands.CommandExecutor;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Accessors(fluent = true)
@EqualsAndHashCode
abstract class CommandNode<T extends CommandNode<T>> {
    private static final CommandExecutor defaultExecutor = c -> {
    };

    private final List<CommandNode<?>> children;

    @NotNull
    private final CommandExecutor executor;

    private final Predicate<CommandSender> requirement;

    private final Component usageText;

    protected CommandNode(List<CommandNode<?>> children, CommandExecutor executor, Predicate<CommandSender> requirement, Component usageText) {
        this.children = children;
        this.executor = executor == null ? defaultExecutor : executor;
        this.requirement = requirement;
        this.usageText = usageText;
    }

    public boolean testRequirement(@NotNull CommandSender sender) {
        if (requirement == null)
            return true;
        return requirement.test(sender);
    }

    public boolean execute(InternalContext context) {
        if (!testRequirement(context.sender()))
            return false;
        if (!context.hasNext()) {
            try {
                executor.execute(context.context());
                return true;
            } catch (CommandException e) {
                e.sendErrorMessage(context.sender());
                return true;
            } catch (Throwable e) {
                e.printStackTrace();
                return sendUsageText(context.sender());
            }
        }
        for (CommandNode<?> child : children) {
            if (child.execute(context.next()))
                return true;
        }
        return sendUsageText(context.sender());
    }

    public List<AsyncTabCompleteEvent.Completion> tabComplete(InternalContext context) {
        if (context.hasNext())
            return children.stream()
                    .map(c -> c.tabComplete(context.next()))
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .toList();
        return null;
    }

    private boolean sendUsageText(CommandSender sender) {
        if (usageText == null) return false;
        sender.sendMessage(usageText);
        return true;
    }
}