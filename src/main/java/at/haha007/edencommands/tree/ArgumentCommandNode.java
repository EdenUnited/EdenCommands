package at.haha007.edencommands.tree;

import at.haha007.edencommands.CommandException;
import at.haha007.edencommands.CommandExecutor;
import at.haha007.edencommands.argument.Argument;
import at.haha007.edencommands.argument.ParsedArgument;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
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
                                Component usageText,
                                Predicate<CommandSender> requirement,
                                CommandExecutor executor,
                                List<CommandNode<?>> children) {
        super(children, executor, requirement, usageText);
        this.key = key;
        this.argument = argument;
    }

    public List<AsyncTabCompleteEvent.Completion> tabComplete(InternalContext context) {
        if (!testRequirement(context.sender()))
            return List.of();
        try {
            ParsedArgument<T> parse = argument.parse(context.context());
            context = context.next(parse.pointerIncrements() - 1);
            if (context.hasNext()) {
                return super.tabComplete(context.next(parse.pointerIncrements() - 1));
            }
            return argument.tabComplete(context.context());
        } catch (CommandException e1) {
            return argument.tabComplete(context.context());
        }
    }

    public boolean execute(InternalContext context) throws CommandException {
        if (!testRequirement(context.sender()))
            return false;
        ParsedArgument<T> parse = argument.parse(context.context());
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
        private final List<Predicate<CommandSender>> requirements = new ArrayList<>();
        private CommandExecutor executor;
        private Component usageText;
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
        @NotNull
        public ArgumentCommandBuilder<T> clone() {
            ArgumentCommandBuilder<T> clone = new ArgumentCommandBuilder<>(argument, key);
            clone.children.addAll(children);
            clone.requirements.addAll(requirements);
            clone.executor = executor;
            clone.usageText = usageText;
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
        public ArgumentCommandBuilder<T> requires(@NotNull Predicate<CommandSender> requirement) {
            requirements.add(requirement);
            return this;
        }

        /**
         * @param usage the Usage text that shows when the command failed
         * @return this
         */
        @NotNull
        public ArgumentCommandBuilder<T> usageText(@NotNull Component usage) {
            usageText = usage;
            return this;
        }

        /**
         * @return a new @{@link ArgumentCommandNode}
         */
        @NotNull
        public CommandNode<?> build() {
            Predicate<CommandSender> requirement = c -> true;
            for (Predicate<CommandSender> r : requirements) {
                requirement = requirement.and(r);
            }
            return new ArgumentCommandNode<>(key, argument, usageText, requirement, executor, children.stream().map(CommandBuilder::build).collect(Collectors.toList()));
        }
    }
}
