package at.haha007.edencommands;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Accessors(fluent = true)
@EqualsAndHashCode
abstract class CommandNode<T extends CommandNode<T>> {
    private static final CommandExecutor defaultExecutor = c -> {
    };

    @Getter
    private final List<CommandNode<?>> children = new ArrayList<>();

    @Getter
    @NotNull
    private CommandExecutor executor = defaultExecutor;

    @Getter
    private Predicate<CommandSender> requirement;

    @Getter
    @Setter
    private Component usageText;

    @NotNull
    public T then(@NotNull CommandNode<?> child) {
        children.add(child);
        return getThis();
    }

    @NotNull
    public T executor(@NotNull CommandExecutor executor) {
        this.executor = executor;
        return getThis();
    }

    public T requires(@NotNull Predicate<CommandSender> requirement) {
        this.requirement = requirement;
        return getThis();
    }

    @NotNull
    abstract public String name();

    //api end, internal start

    protected boolean testRequirement(@NotNull CommandSender sender) {
        if (requirement == null)
            return true;
        return requirement.test(sender);
    }

    @NotNull
    protected abstract T getThis();

    boolean execute(InternalContext context) {
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

    List<AsyncTabCompleteEvent.Completion> tabComplete(InternalContext context) {
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

    void merge(CommandNode<T> node) {
        if (executor == defaultExecutor)
            executor = node.executor;
        if (requirement == null)
            requirement = node.requirement;
        if (usageText == null)
            usageText = node.usageText;
        children.addAll(node.children);
    }
}
