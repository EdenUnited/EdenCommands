package at.haha007.edencommands.annotations;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.tree.CommandBuilder;
import at.haha007.edencommands.tree.CommandNode;
import at.haha007.edencommands.tree.InternalContext;
import at.haha007.edencommands.tree.LiteralCommandNode;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

class CommandTreeTest {

    @Test
    void add() {
        CommandTree tree = CommandTree.root();
        tree.add("command a", this::execute);
        tree.add("command b", this::execute);
        tree.add("command b c", this::execute);
        tree.add("command b d", this::execute);
        tree.add("command b e", this::execute);
        System.out.println(tree);
        Assertions.assertTrue(true);
    }

    private void execute(CommandContext context) {
        System.out.println("command executed");
    }

    @Test
    void asCommand() {
        CommandSender mockSender = Mockito.mock(CommandSender.class);

        CommandTree tree = CommandTree.root();
        tree.add("command a", c -> System.out.println("command a"));
        tree.add("command b", c -> System.out.println("command b"));
        tree.add("command b c", c -> System.out.println("command b c"));
        tree.add("command b d", c -> System.out.println("command b d"));
        tree.add("command b e", c -> System.out.println("command b e"));
        ArgumentParserProvider provider = new ArgumentParserProvider();
        Arrays.stream(DefaultArgumentParsers.values()).forEach(p -> provider.register(p.getKey(), p));
        List<? extends CommandNode<?>> commands = tree.getChildCommands(provider).stream().map(CommandBuilder::build).toList();
        for (CommandNode<?> command : commands) {
            if (!(command instanceof LiteralCommandNode literalCommandNode)) {
                throw new RuntimeException("CommandBuilder is not a LiteralCommandBuilder!");
            }
            System.out.println(literalCommandNode);
            command.execute(new InternalContext(
                    mockSender,
                    new String[]{"command", "b", "c"},
                    0,
                    new LinkedHashMap<>()
            ));
            command.execute(new InternalContext(
                    mockSender,
                    new String[]{"command", "a"},
                    0,
                    new LinkedHashMap<>()
            ));
            command.tabComplete(new InternalContext(
                    mockSender,
                    new String[]{"command", ""},
                    0,
                    new LinkedHashMap<>()
            ));
        }
        Assertions.assertTrue(true);
    }

    @Test
    void arguments() {
        CommandSender mockSender = Mockito.mock(CommandSender.class);

        CommandTree tree = CommandTree.root();
        tree.add("command a{type:boolean}", c -> System.out.println("a = " + c.parameter("a")));
        ArgumentParserProvider provider = new ArgumentParserProvider();
        Arrays.stream(DefaultArgumentParsers.values()).forEach(p -> provider.register(p.getKey(), p));

        List<? extends CommandNode<?>> commands = tree.getChildCommands(provider).stream().map(CommandBuilder::build).toList();
        for (CommandNode<?> command : commands) {
            if (!(command instanceof LiteralCommandNode literalCommandNode)) {
                throw new RuntimeException("CommandBuilder is not a LiteralCommandBuilder!");
            }
            command.execute(new InternalContext(
                    mockSender,
                    new String[]{"command", "false"},
                    0,
                    new LinkedHashMap<>()
            ));
            command.execute(new InternalContext(
                    mockSender,
                    new String[]{"command"},
                    0,
                    new LinkedHashMap<>()
            ));
            command.execute(new InternalContext(
                    mockSender,
                    new String[]{"command", "true", "true"},
                    0,
                    new LinkedHashMap<>()
            ));
        }
        Assertions.assertTrue(true);
    }
}