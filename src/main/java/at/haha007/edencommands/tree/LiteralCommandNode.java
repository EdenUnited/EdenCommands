package at.haha007.edencommands.tree;

import at.haha007.edencommands.CommandExecutor;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@Accessors(fluent = true)
public final class LiteralCommandNode extends CommandNode<LiteralCommandNode> {
    @NotNull
    private final String literal;
    @Nullable
    private final Component tooltip;

    public static LiteralCommandBuilder builder(String literal) {
        return new LiteralCommandBuilder(literal);
    }

    private LiteralCommandNode(@NotNull String literal,
                               @Nullable Component tooltip,
                               List<CommandNode<?>> children,
                               CommandExecutor executor,
                               Predicate<CommandSender> requirement,
                               Component usageText) {
        super(List.copyOf(children), executor, requirement, usageText);
        this.literal = literal.toLowerCase();
        this.tooltip = tooltip;
    }

    public List<AsyncTabCompleteEvent.Completion> tabComplete(InternalContext context) {
        if (!testRequirement(context.sender()))
            return List.of();
        if (context.hasNext() && literal.equalsIgnoreCase(context.current())) {
            return super.tabComplete(context);
        }
        if (startsWith(literal, context.current())) {
            return List.of(AsyncTabCompleteEvent.Completion.completion(literal, tooltip));
        }
        return List.of();
    }

    public boolean execute(InternalContext context) {
        if (!context.current().equalsIgnoreCase(literal))
            return false;
        return super.execute(context);
    }

    private boolean startsWith(String literal, String start) {
        return literal.toLowerCase().startsWith(start.toLowerCase());
    }

    public static class LiteralCommandBuilder implements CommandBuilder<LiteralCommandBuilder> {
        private final List<CommandBuilder<?>> children = new ArrayList<>();
        private final List<Predicate<CommandSender>> requirements = new ArrayList<>();
        private CommandExecutor executor;
        private Component usageText;
        private final String literal;
        private Component tooltip;

        private LiteralCommandBuilder(@NotNull String literal) {
            if (literal.contains(" ")) throw new IllegalArgumentException("literal");
            this.literal = literal;
        }

        @NotNull
        public LiteralCommandBuilder clone() {
            LiteralCommandBuilder clone = new LiteralCommandBuilder(literal);
            clone.requirements.addAll(requirements);
            clone.children.addAll(children);
            clone.executor = executor;
            clone.usageText = usageText;
            clone.tooltip = tooltip;
            return clone;
        }

        @NotNull
        public LiteralCommandBuilder then(@NotNull CommandBuilder<?> child) {
            children.add(child);
            return this;
        }

        @NotNull
        public LiteralCommandBuilder executor(@NotNull CommandExecutor executor) {
            this.executor = executor;
            return this;
        }

        @NotNull
        public LiteralCommandBuilder requires(@NotNull Predicate<CommandSender> requirement) {
            requirements.add(requirement);
            return this;
        }

        @NotNull
        public LiteralCommandBuilder usageText(@NotNull Component usage) {
            usageText = usage;
            return this;
        }

        @NotNull
        public LiteralCommandBuilder tooltip(@NotNull Component tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        @NotNull
        public LiteralCommandNode build() {
            Predicate<CommandSender> requirement = c -> true;
            for (Predicate<CommandSender> r : requirements) {
                requirement = requirement.and(r);
            }
            return new LiteralCommandNode(literal, tooltip, children.stream().map(CommandBuilder::build).collect(Collectors.toList()), executor, requirement, usageText);
        }
    }

}
