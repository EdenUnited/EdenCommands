package at.haha007.edencommands.tree;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import at.haha007.edencommands.CommandExecutor;
import at.haha007.edencommands.argument.Argument;
import at.haha007.edencommands.argument.ParsedArgument;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class ArgumentCommandNode<T> extends CommandNode<ArgumentCommandNode<T>> {
    @NotNull
    private final String key;
    private final Argument<T> argument;

    /**
     * @param key      the key the access the parsed argument from the @{@link at.haha007.edencommands.CommandContext}
     * @param argument the @{@link Argument} to parse and tab-complete part of the command
     * @return a new @{@link ArgumentCommandBuilder}
     */
    public static <T> ArgumentCommandBuilder<T> builder(String key, Argument<T> argument) {
        return new ArgumentCommandBuilder<>(argument, key);
    }

    private ArgumentCommandNode(@NotNull String key,
                                Argument<T> argument,
                                CommandExecutor defaultExecutor,
                                Predicate<CommandContext> requirement,
                                CommandExecutor executor,
                                List<CommandNode<?>> children) {
        super(children, executor, requirement, defaultExecutor);
        this.key = key;
        this.argument = argument;
    }

    public List<AsyncTabCompleteEvent.Completion> tabComplete(ContextBuilder context) {
        if (!testRequirement(context.build()))
            return List.of();
        try {
            ParsedArgument<T> parse = argument.parse(context.build());
            context = context.next(parse.pointerIncrements() - 1);
            if (context.hasNext()) {
                return super.tabComplete(context.next(parse.pointerIncrements() - 1));
            }
            return argument.tabComplete(context.build());
        } catch (CommandException e1) {
            return argument.tabComplete(context.build());
        }
    }

    public boolean execute(ContextBuilder context) {
        if (!testRequirement(context.build()))
            return false;
        ParsedArgument<T> parse;
        try {
            parse = argument.parse(context.build());
        } catch (CommandException exception) {
            return false;
        }
        context.putArgument(key, parse);
        context = context.next(parse.pointerIncrements() - 1);
        return super.execute(context);
    }

    public Argument<T> argument() {
        return this.argument;
    }

    @Override
    public String toString() {
        return "ArgumentCommandNode{" +
                "key='" + key + '\'' +
                ", argument=" + argument +
                "} " + super.toString();
    }

    public static final class ArgumentCommandBuilder<T> implements CommandBuilder<ArgumentCommandBuilder<T>> {
        private final List<CommandBuilder<?>> children = new ArrayList<>();
        private final List<Predicate<CommandContext>> requirements = new ArrayList<>();
        private CommandExecutor executor;
        private CommandExecutor defaultExecutor;
        @NotNull
        private final Argument<T> argument;
        @NotNull
        private final String key;

        private ArgumentCommandBuilder(@NotNull Argument<T> argument, @NotNull String key) {
            this.argument = argument;
            this.key = key;
        }

        /**
         * @return a clone of this instance
         */
        @SuppressWarnings("MethodDoesntCallSuperMethod")
        @NotNull
        public ArgumentCommandBuilder<T> clone() {
            ArgumentCommandBuilder<T> clone = new ArgumentCommandBuilder<>(argument, key);
            clone.children.addAll(children);
            clone.requirements.addAll(requirements);
            clone.executor = executor;
            clone.defaultExecutor = defaultExecutor;
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
        public ArgumentCommandBuilder<T> then(@NotNull CommandBuilder<?> child) {
            children.add(child);
            return this;
        }

        /**
         * @param executor the @{@link CommandExecutor} that should be run when the command is run
         * @return this
         */
        @NotNull
        public ArgumentCommandBuilder<T> executor(@NotNull CommandExecutor executor) {
            this.executor = executor;
            return this;
        }

        /**
         * @param requirement A condition that has to match to execute the command. ie: permissions, gamemode
         * @return this
         */
        @NotNull
        public ArgumentCommandBuilder<T> requires(@NotNull Predicate<CommandContext> requirement) {
            requirements.add(requirement);
            return this;
        }

        /**
         * @param defaultExecutor the @{@link CommandExecutor} that should be run when the command is run without any arguments
         * @return this
         */
        @NotNull
        public ArgumentCommandBuilder<T> defaultExecutor(@NotNull CommandExecutor defaultExecutor) {
            this.defaultExecutor = defaultExecutor;
            return this;
        }

        /**
         * @return a new @{@link ArgumentCommandNode}
         */
        @NotNull
        public ArgumentCommandNode<T> build() {
            Predicate<CommandContext> requirement = c -> true;
            for (Predicate<CommandContext> r : requirements) {
                requirement = requirement.and(r);
            }
            return new ArgumentCommandNode<>(key,
                    argument,
                    defaultExecutor,
                    requirement,
                    executor,
                    children.stream().map(CommandBuilder::build).collect(Collectors.toList()));
        }
    }
}
