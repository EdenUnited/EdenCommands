package at.haha007.edencommands.tree.node;

import at.haha007.edencommands.tree.CommandContext;
import org.bukkit.permissions.Permission;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class CommandNode {

    protected Consumer<CommandContext> executor;
    protected final List<CommandNode> children = new ArrayList<>();
    private Function<CommandContext, List<String>> tabCompleter;
    private Permission permission;

    public CommandNode executes(Consumer<CommandContext> executor) {
        this.executor = executor;
        return this;
    }

    public CommandNode withPermission(String permission) {
        this.permission = new Permission(permission);
        return this;
    }

    public CommandNode tabCompletes(Function<CommandContext, List<String>> tabCompleter) {
        this.tabCompleter = tabCompleter;
        return this;
    }

    public CommandNode then(CommandNode node) {
        children.add(node);
        return this;
    }

    public List<String> tabComplete(Stack<String> command, CommandContext context) {
        //should never be empty, just to be sure
        if (command.isEmpty()) return List.of();
        String element = command.pop();

        //if it doesn't match any completions for this element return an empty list
        if (!matches(element)) return List.of();

        if (permission != null && !context.getSender().hasPermission(permission))
            return List.of();

        //if this is the last element return the tab completions for this element
        if (command.isEmpty()) return tabCompleter == null ? List.of() : tabCompleter.apply(context);

        //recursion
        return children.stream()
                .map(c -> c.tabComplete((Stack<String>) command.clone(), context))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public void execute(Stack<String> command, CommandContext context) {
        //should never be empty, just to be sure
        if (command.isEmpty() || context.isWasExecuted()) return;
        String element = command.pop();

        //if it doesn't match don't do anything
        if (!matches(element)) return;

        if (permission != null && !context.getSender().hasPermission(permission)) {
            context.setPermissionFailed(true);
            return;
        }

        //recursion
        children.forEach(c -> c.execute((Stack<String>) command.clone(), context));

        //if no child was executed, execute this node
        if (context.isWasExecuted()) return;

        if (executor == null) return;

        context.setWasExecuted(true);
        List<String> remaining = new ArrayList<>(command);
        Collections.reverse(remaining);
        context.setRemainingCommand(remaining);
        executor.accept(context);
    }

    abstract protected boolean matches(String s);

}
