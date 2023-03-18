package at.haha007.edencommands.tree;

import at.haha007.edencommands.CommandExecutor;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class LiteralCommandNode extends CommandNode<LiteralCommandNode> {
    @NotNull
    private final String literal;
    @Nullable
    private final Component tooltip;

    /**
     * @param literal the literal
     * @return a new @{@link LiteralCommandBuilder}
     */
    public static LiteralCommandBuilder builder(@NotNull String literal) {
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

    public @NotNull String literal() {
        return this.literal;
    }

    public @Nullable Component tooltip() {
        return this.tooltip;
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

        /**
         * @return a clone of this instance
         */
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

        /**
         * "/command subcommand"
         *     ^          ^
         *   parent     child
         * @param child A Child command under the current one
         * @return this
         */
        @NotNull
        public LiteralCommandBuilder then(@NotNull CommandBuilder<?> child) {
            children.add(child);
            return this;
        }

        /**
         * @param executor the @{@link CommandExecutor} that should be run when the command is run
         * @return this
         */
        @NotNull
        public LiteralCommandBuilder executor(@NotNull CommandExecutor executor) {
            this.executor = executor;
            return this;
        }

        /**
         * @param requirement A condition that has to match to execute the command. ie: permissions, gamemode
         * @return this
         */
        @NotNull
        public LiteralCommandBuilder requires(@NotNull Predicate<CommandSender> requirement) {
            requirements.add(requirement);
            return this;
        }

        /**
         * @param usage the Usage text that shows when the command failed
         * @return this
         */
        @NotNull
        public LiteralCommandBuilder usageText(@NotNull Component usage) {
            usageText = usage;
            return this;
        }

        /**
         * @param tooltip the tooltip that hovers over the argument when you hover over it
         * @return this
         */
        @NotNull
        public LiteralCommandBuilder tooltip(@NotNull Component tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        /**
         * @return a new @{@link LiteralCommandNode}
         */
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
