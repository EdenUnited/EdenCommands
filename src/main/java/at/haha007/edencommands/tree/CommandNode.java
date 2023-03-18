package at.haha007.edencommands.tree;

import at.haha007.edencommands.CommandException;
import at.haha007.edencommands.CommandExecutor;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

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

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof final CommandNode<?> other)) return false;
        if (!other.canEqual(this)) return false;
        final Object this$children = this.children;
        final Object other$children = other.children;
        if (!Objects.equals(this$children, other$children)) return false;
        final Object this$executor = this.executor;
        final Object other$executor = other.executor;
        if (!Objects.equals(this$executor, other$executor)) return false;
        final Object this$requirement = this.requirement;
        final Object other$requirement = other.requirement;
        if (!Objects.equals(this$requirement, other$requirement))
            return false;
        final Object this$usageText = this.usageText;
        final Object other$usageText = other.usageText;
        return Objects.equals(this$usageText, other$usageText);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof CommandNode;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $children = this.children;
        result = result * PRIME + ($children == null ? 43 : $children.hashCode());
        final Object $executor = this.executor;
        result = result * PRIME + ($executor == null ? 43 : $executor.hashCode());
        final Object $requirement = this.requirement;
        result = result * PRIME + ($requirement == null ? 43 : $requirement.hashCode());
        final Object $usageText = this.usageText;
        result = result * PRIME + ($usageText == null ? 43 : $usageText.hashCode());
        return result;
    }
}
