package at.haha007.edencommands.eden;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

@Accessors(fluent = true)
@EqualsAndHashCode
abstract class CommandNode<T extends CommandNode<T>> {
    @Getter
    private final Map<String, CommandNode<?>> children = new LinkedHashMap<>();

    @Getter
    @NotNull
    private CommandExecutor executor = c -> {
    };

    @Getter
    private Predicate<CommandSender> requirement;

    @Getter
    @Setter
    private Component usageText;

    @NotNull
    public T then(@NotNull CommandNode<?> child) {
        children.put(child.name(), child);
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

    public boolean canUse(@NotNull CommandSender sender) {
        return requirement.test(sender);
    }

    //api end, internal start

    @NotNull
    protected abstract T getThis();

    boolean execute(InternalContext context) {
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
        for (CommandNode<?> child : children.values()) {
            if (child.execute(context.next()))
                return true;
        }
        return sendUsageText(context.sender());
    }

    List<String> tabComplete(InternalContext context) {
        if (context.hasNext())
            return children.values().stream()
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
