package at.haha007.edencommands.annotations;

import at.haha007.edencommands.CommandExecutor;
import at.haha007.edencommands.Requirement;
import at.haha007.edencommands.argument.Argument;
import at.haha007.edencommands.tree.ArgumentCommandNode;
import at.haha007.edencommands.tree.CommandBuilder;
import at.haha007.edencommands.tree.LiteralCommandNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class CommandTree {


    //'key':'type'
    //eg.: 'test':'literal'
    //has to be able to parse with escaped characters

    //map: type -> argument


    private final List<CommandTree> children = new ArrayList<>();
    private final String key;
    private CommandExecutor executor;
    private CommandExecutor defaultExecutor;
    private String requirement;

    private CommandTree(String key) {
        this.key = key;
    }

    public static CommandTree root() {
        return new CommandTree("root");
    }

    public boolean add(@NotNull List<String> cmd, @Nullable String requirement, @NotNull CommandExecutor executor, boolean isDefault) {
        String key = cmd.get(0);
        List<String> rest = cmd.subList(1, cmd.size());
        if (!key.equals(this.key) ) return false;
        if (rest.isEmpty()) {
            if (isDefault)
                this.defaultExecutor = executor;
            else
                this.executor = executor;
            this.requirement = requirement;
            return true;
        }
        for (CommandTree child : children) {
            if (child.add(rest, requirement, executor, isDefault)) return true;
        }
        key = rest.get(0);
        CommandTree child = new CommandTree(key);
        children.add(child);
        return true;
    }

    @NotNull
    public CommandBuilder<?> toCommand(Map<String, Argument<?>> argumentMap,
                                       Map<String, String> literalMapper,
                                       Map<String, Requirement> requirements) {
        CommandBuilder<?> cmd;
        if (argumentMap.containsKey(key)) {
            Argument<?> argument = argumentMap.get(key);
            cmd = ArgumentCommandNode.builder(key, argument);
        } else {
            String mapped = literalMapper.getOrDefault(key, key);
            cmd = LiteralCommandNode.builder(mapped);
        }
        if (executor != null) cmd.executor(executor);
        if (defaultExecutor != null) cmd.defaultExecutor(defaultExecutor);
        Requirement requirement = calculateRequirement(requirements);
        if (requirement != null) cmd.requires(requirement);

        return cmd;
    }

    public List<CommandTree> getChildren() {
        return children;
    }

    private Requirement calculateRequirement(Map<String, Requirement> requirements) {
        Requirement childRequirement = children.stream()
                .map(c -> c.calculateRequirement(requirements)).filter(Objects::nonNull)
                .reduce(Requirement::or)
                .orElse(null);

        if (childRequirement == null) {
            if (requirement == null) return null;
            Requirement requirement = requirements.get(this.requirement);
            if (requirement == null) throw new IllegalArgumentException("Unknown requirement: " + this.requirement);
            return requirement;
        }

        if (requirement == null) return childRequirement;

        Requirement requirement = requirements.get(this.requirement);
        if (requirement == null) throw new IllegalArgumentException("Unknown requirement: " + this.requirement);
        return childRequirement.and(requirement);
    }

    @Override
    public String toString() {
        return "CommandTree{" +
                "children=" + children +
                ", key='" + key + '\'' +
                ", executor=" + executor +
                ", defaultExecutor=" + defaultExecutor +
                ", requirement='" + requirement + '\'' +
                '}';
    }
}
