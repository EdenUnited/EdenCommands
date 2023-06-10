package at.haha007.edencommands.tree;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandExecutor;
import at.haha007.edencommands.Requirement;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class LiteralCommandNode extends CommandNode {
    @NotNull
    private final String literal;
    @Nullable
    private final Component tooltip;
    private final boolean ignoreCase;

    /**
     * @param literal the literal
     * @return a new @{@link LiteralCommandBuilder}
     */
    public static LiteralCommandBuilder builder(@NotNull String literal) {
        return new LiteralCommandBuilder(literal);
    }

    private LiteralCommandNode(@NotNull String literal,
                               @Nullable Component tooltip,
                               List<CommandNode> children,
                               CommandExecutor executor,
                               Requirement requirement,
                               CommandExecutor defaultExecutor,
                               boolean ignoreCase) {
        super(List.copyOf(children), executor, requirement, defaultExecutor);
        this.literal = literal.toLowerCase();
        this.tooltip = tooltip;
        this.ignoreCase = ignoreCase;
    }

    public List<AsyncTabCompleteEvent.Completion> tabComplete(ContextBuilder context) {
        if (!testRequirement(context.build()))
            return List.of();
        if (context.hasNext() && ignoreCase && literal.equalsIgnoreCase(context.current())) {
            return super.tabComplete(context);
        }
        if (context.hasNext() && !ignoreCase && literal.equals(context.current())) {
            return super.tabComplete(context);
        }
        if (!context.hasNext() && startsWith(literal, context.current())) {
            return List.of(AsyncTabCompleteEvent.Completion.completion(literal, tooltip));
        }
        return List.of();
    }

    public boolean execute(ContextBuilder context) {
        if (ignoreCase && !context.current().equalsIgnoreCase(literal))
            return false;
        if (!ignoreCase && !context.current().equals(literal))
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

    @Override
    public String toString() {
        return "LiteralCommandNode{" +
                "literal='" + literal + '\'' +
                ", tooltip=" + tooltip +
                "} " + super.toString();
    }

    public static class LiteralCommandBuilder implements CommandBuilder<LiteralCommandBuilder> {
        private final List<CommandBuilder<?>> children = new ArrayList<>();
        private final List<Requirement> requirements = new ArrayList<>();
        private CommandExecutor executor;
        private CommandExecutor defaultExecutor;
        private final String literal;
        private Component tooltip;
        private boolean ignoreCase;

        private LiteralCommandBuilder(@NotNull String literal) {
            if (literal.contains(" ")) throw new IllegalArgumentException("literal");
            this.literal = literal;
        }

        /**
         * @return a clone of this instance
         */
        @SuppressWarnings("MethodDoesntCallSuperMethod")
        @NotNull
        public LiteralCommandBuilder clone() {
            LiteralCommandBuilder clone = new LiteralCommandBuilder(literal);
            clone.requirements.addAll(requirements);
            clone.children.addAll(children);
            clone.executor = executor;
            clone.defaultExecutor = defaultExecutor;
            clone.tooltip = tooltip;
            return clone;
        }

        /**
         * "/command subcommand"
         * ^          ^
         * parent     child
         *
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
        public LiteralCommandBuilder executor(CommandExecutor executor) {
            this.executor = executor;
            return this;
        }

        /**
         * @param requirement A condition that has to match to execute the command. ie: permissions, gamemode
         * @return this
         */
        @NotNull
        public LiteralCommandBuilder requires(@NotNull Requirement requirement) {
            requirements.add(requirement);
            return this;
        }

        /**
         * @param commandExecutor the @{@link CommandExecutor} that should be run when the command is run
         * @return this
         */
        @NotNull
        public LiteralCommandBuilder defaultExecutor(CommandExecutor commandExecutor) {
            defaultExecutor = commandExecutor;
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

        @NotNull
        public LiteralCommandBuilder ignoreCase(boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
            return this;
        }

        /**
         * @return a new @{@link LiteralCommandNode}
         */
        @NotNull
        public LiteralCommandNode build() {
            Predicate<CommandContext> requirement = c -> true;
            for (Predicate<CommandContext> r : requirements) {
                requirement = requirement.and(r);
            }
            Requirement req = requirement::test;
            return new LiteralCommandNode(literal,
                    tooltip,
                    children.stream().map(CommandBuilder::build).collect(Collectors.toList()),
                    executor,
                    req,
                    defaultExecutor,
                    ignoreCase);
        }
    }

}
