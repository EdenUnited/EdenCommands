package at.haha007.edencommands.annotations;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.tree.CommandBuilder;
import at.haha007.edencommands.tree.CommandNode;
import at.haha007.edencommands.tree.ContextBuilder;
import at.haha007.edencommands.tree.LiteralCommandNode;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        System.out.println("----------------");
        for (CommandBuilder<?> childCommand : tree.getChildCommands(new ArgumentParserProvider(), Map.of("command", "b"))) {
            System.out.println(childCommand.build());
        }
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
        List<? extends CommandNode> commands = tree.getChildCommands(provider).stream().map(CommandBuilder::build).toList();
        for (CommandNode command : commands) {
            if (!(command instanceof LiteralCommandNode literalCommandNode)) {
                throw new RuntimeException("CommandBuilder is not a LiteralCommandBuilder!");
            }
            System.out.println(literalCommandNode);
            command.execute(new ContextBuilder(
                    mockSender,
                    new String[]{"command", "b", "c"}
            ));
            command.execute(new ContextBuilder(
                    mockSender,
                    new String[]{"command", "a"}
            ));
            command.tabComplete(new ContextBuilder(
                    mockSender,
                    new String[]{"command", ""}
            ));
        }
        Assertions.assertTrue(true);
    }

    @Test
    void tree() {
        Player mockSender = Mockito.mock(Player.class);
        Mockito.spy(Bukkit.class);
        //noinspection resource
        Mockito.mockStatic(Bukkit.class).when(() -> Bukkit.getPlayer("")).thenReturn(mockSender);
        CommandTree tree = CommandTree.root();
        tree.add("command trust player{type:player}", c -> System.out.println("command a"));
        tree.add("command trustlist", c -> System.out.println("command b"));
        ArgumentParserProvider provider = new ArgumentParserProvider();
        Arrays.stream(DefaultArgumentParsers.values()).forEach(p -> provider.register(p.getKey(), p));
        List<? extends CommandNode> commands = tree.getChildCommands(provider).stream().map(CommandBuilder::build).toList();
        for (CommandNode command : commands) {
            if (!(command instanceof LiteralCommandNode)) {
                throw new RuntimeException("CommandBuilder is not a LiteralCommandBuilder!");
            }
            ContextBuilder context = new ContextBuilder(
                    mockSender,
                    new String[]{"command", "trust", ""}
            );
            System.out.println(Arrays.toString(context.build().input()));
            command.tabComplete(context).forEach(System.out::println);
        }
        Assertions.assertTrue(true);
    }

    @Test
    void defaultExecutor() {
        Player sender = Mockito.mock(Player.class);
        CommandTree tree = CommandTree.root();
        tree.add("command a", c -> System.out.println("command a"));
        tree.add("command b c", c -> System.out.println("command b c"));
        tree.add("command b{default_executor:true}", c -> System.out.println("command b"));
        ArgumentParserProvider provider = new ArgumentParserProvider();
        List<? extends CommandNode> commands = tree.getChildCommands(provider).stream().map(CommandBuilder::build).toList();
        for (CommandNode command : commands) {
            if (!(command instanceof LiteralCommandNode literalCommandNode)) {
                throw new RuntimeException("CommandBuilder is not a LiteralCommandBuilder!");
            }
            System.out.println(literalCommandNode);
            System.out.println("  command b x");
            command.execute(new ContextBuilder(
                    sender,
                    new String[]{"command", "b", "x"}
            ));
            System.out.println("  command b c");
            command.execute(new ContextBuilder(
                    sender,
                    new String[]{"command", "b", "c"}
            ));
            System.out.println("  command a x");
            command.execute(new ContextBuilder(
                    sender,
                    new String[]{"command", "a", "x"}
            ));
            System.out.println("  command a");
            command.execute(new ContextBuilder(
                    sender,
                    new String[]{"command", "a"}
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

        List<? extends CommandNode> commands = tree.getChildCommands(provider).stream().map(CommandBuilder::build).toList();
        for (CommandNode command : commands) {
            if (!(command instanceof LiteralCommandNode)) {
                throw new RuntimeException("CommandBuilder is not a LiteralCommandBuilder!");
            }
            command.execute(new ContextBuilder(
                    mockSender,
                    new String[]{"command", "false"}
            ));
            command.execute(new ContextBuilder(
                    mockSender,
                    new String[]{"command"}
            ));
            command.execute(new ContextBuilder(
                    mockSender,
                    new String[]{"command", "true", "true"}
            ));
        }
        Assertions.assertTrue(true);
    }
}