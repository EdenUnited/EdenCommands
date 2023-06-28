package at.haha007.edencommands.annotations;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.annotations.annotations.Command;
import at.haha007.edencommands.annotations.annotations.CommandArgument;
import at.haha007.edencommands.annotations.annotations.DefaultExecutor;
import at.haha007.edencommands.annotations.annotations.TabCompleter;
import at.haha007.edencommands.argument.ParsedArgument;
import at.haha007.edencommands.tree.CommandBuilder;
import at.haha007.edencommands.tree.CommandNode;
import at.haha007.edencommands.tree.ContextBuilder;
import at.haha007.edencommands.tree.LiteralCommandNode;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent.Completion;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.stream.Stream;

class CommandTreeTest {
    private static final class SimpleTestCommand {
        private static String lastExecuted = "";

        @Command("command")
        @DefaultExecutor
        private void command(CommandContext context) {
            lastExecuted = "command";
        }

        @Command("command a")
        private void a(CommandContext context) {
            lastExecuted = "command a";
        }

        @Command("command b")
        private void b(CommandContext context) {
            lastExecuted = "command b";
        }

        @Command("command b c")
        private void bc(CommandContext context) {
            lastExecuted = "command b c";
        }

        @Command("command b d")
        private void bd(CommandContext context) {
            lastExecuted = "command b d";
        }

        @Command("command b e")
        private void be(CommandContext context) {
            lastExecuted = "command b e";
        }
    }

    private static final class ArgumentTestCommand {
        static String state;

        @TabCompleter("§arg")
        private List<Completion> complete(CommandContext context) {
            return Stream.of("a", "b", "c").map(Completion::completion).toList();
        }

        @CommandArgument("§arg")
        private ParsedArgument<String> parse(CommandContext context) {
            return new ParsedArgument<>(context.input()[context.pointer()], 1);
        }

        @TabCompleter("§argb")
        private List<Completion> completeb(CommandContext context) {
            return Stream.of("d", "e", "f").map(Completion::completion).toList();
        }

        @CommandArgument("§argb")
        private ParsedArgument<String> parseb(CommandContext context) {
            return new ParsedArgument<>(context.input()[context.pointer()], 1);
        }


        @Command("cmd")
        @DefaultExecutor
        private void root(CommandContext context) {
            state = null;
        }


        @Command("cmd §arg")
        private void arg(CommandContext context) {
            state = context.parameter("§arg");
        }

        @Command("cmd §arg §argb")
        private void argb(CommandContext context) {
            String a = context.parameter("§arg");
            String b = context.parameterString("§argb");
            System.out.printf("a: %s         b: %s%n", a, b);
        }
    }

    @Test
    void print() {
        JavaPlugin mockPlugin = Mockito.mock(JavaPlugin.class);
        AnnotatedCommandLoader loader = new AnnotatedCommandLoader(mockPlugin);
        loader.addAnnotated(new SimpleTestCommand());
        Assertions.assertEquals(1, loader.getCommands().size());
        loader.getCommands().stream().map(CommandBuilder::build).forEach(System.out::println);
        System.out.println("----------------");
        Assertions.assertTrue(true);
    }

    @Test
    void simpleExecute() {
        CommandSender mockSender = Mockito.mock(CommandSender.class);
        JavaPlugin mockPlugin = Mockito.mock(JavaPlugin.class);
        AnnotatedCommandLoader loader = new AnnotatedCommandLoader(mockPlugin);
        loader.addAnnotated(new SimpleTestCommand());
        List<? extends CommandNode> commands = loader.getCommands().stream().map(CommandBuilder::build).toList();
        for (CommandNode command : commands) {
            if (!(command instanceof LiteralCommandNode)) {
                throw new RuntimeException("CommandBuilder is not a LiteralCommandBuilder!");
            }
            command.execute(new ContextBuilder(
                    mockSender,
                    new String[]{"command", "b", "c"}
            ));
            Assertions.assertEquals("command b c", SimpleTestCommand.lastExecuted);

            command.execute(new ContextBuilder(
                    mockSender,
                    new String[]{"command", "b", "d", "f"}
            ));
            Assertions.assertEquals("command", SimpleTestCommand.lastExecuted);

            command.execute(new ContextBuilder(
                    mockSender,
                    new String[]{"command", "a"}
            ));
            Assertions.assertEquals("command a", SimpleTestCommand.lastExecuted);

            command.execute(new ContextBuilder(
                    mockSender,
                    new String[]{"command", ""}
            ));
            Assertions.assertEquals("command", SimpleTestCommand.lastExecuted);
            return;
        }
        Assertions.fail();
    }

    @Test
    void testReplacements() {
        CommandSender mockSender = Mockito.mock(CommandSender.class);
        JavaPlugin mockPlugin = Mockito.mock(JavaPlugin.class);
        AnnotatedCommandLoader loader = new AnnotatedCommandLoader(mockPlugin);
        loader.addAnnotated(new SimpleTestCommand());
        loader.mapLiteral("command", "mycommand");
        loader.mapLiteral("a", "A");
        List<? extends CommandNode> commands = loader.getCommands().stream().map(CommandBuilder::build).toList();
        for (CommandNode command : commands) {
            if (!(command instanceof LiteralCommandNode)) {
                throw new RuntimeException("CommandBuilder is not a LiteralCommandBuilder!");
            }
            SimpleTestCommand.lastExecuted = "";
            command.execute(new ContextBuilder(
                    mockSender,
                    new String[]{"command", "b", "c"}
            ));
            Assertions.assertEquals("", SimpleTestCommand.lastExecuted);

            command.execute(new ContextBuilder(
                    mockSender,
                    new String[]{"mycommand", "b", "d", "f"}
            ));
            Assertions.assertEquals("command", SimpleTestCommand.lastExecuted);

            command.execute(new ContextBuilder(
                    mockSender,
                    new String[]{"mycommand", "a"}
            ));
            Assertions.assertEquals("command a", SimpleTestCommand.lastExecuted);

            command.execute(new ContextBuilder(
                    mockSender,
                    new String[]{"mycommand", ""}
            ));
            Assertions.assertEquals("command", SimpleTestCommand.lastExecuted);
            return;
        }
        Assertions.fail();
    }

    @Test
    void argumentTest() {
        ArgumentTestCommand test = new ArgumentTestCommand();
        CommandSender mockSender = Mockito.mock(CommandSender.class);
        JavaPlugin mockPlugin = Mockito.mock(JavaPlugin.class);
        AnnotatedCommandLoader loader = new AnnotatedCommandLoader(mockPlugin);
        loader.addAnnotated(test);
        CommandBuilder<?> builder = loader.getCommands().get(0);
        Assertions.assertNotNull(builder);
        CommandNode cmd = builder.build();

        List<Completion> tabArgs = cmd.tabComplete(new ContextBuilder(mockSender, new String[]{"cmd", ""}));
        Assertions.assertEquals(3, tabArgs.size());
        Assertions.assertTrue(tabArgs.stream().map(Completion::suggestion).toList().contains("a"));
        Assertions.assertTrue(tabArgs.stream().map(Completion::suggestion).toList().contains("b"));
        Assertions.assertTrue(tabArgs.stream().map(Completion::suggestion).toList().contains("c"));

        cmd.execute(new ContextBuilder(mockSender, new String[]{"cmd", "text"}));
        Assertions.assertEquals("text", ArgumentTestCommand.state);
        cmd.execute(new ContextBuilder(mockSender, new String[]{"cmd"}));
        Assertions.assertNull(ArgumentTestCommand.state);
    }

    @Test
    void argumentTest2() {
        ArgumentTestCommand test = new ArgumentTestCommand();
        CommandSender mockSender = Mockito.mock(CommandSender.class);
        JavaPlugin mockPlugin = Mockito.mock(JavaPlugin.class);
        AnnotatedCommandLoader loader = new AnnotatedCommandLoader(mockPlugin);
        loader.addAnnotated(test);
        CommandBuilder<?> builder = loader.getCommands().get(0);
        Assertions.assertNotNull(builder);
        CommandNode cmd = builder.build();

        List<Completion> tabArgs = cmd.tabComplete(new ContextBuilder(mockSender, new String[]{"cmd", "a", ""}));
        Assertions.assertEquals(3, tabArgs.size());
        Assertions.assertTrue(tabArgs.stream().map(Completion::suggestion).toList().contains("d"));
        Assertions.assertTrue(tabArgs.stream().map(Completion::suggestion).toList().contains("e"));
        Assertions.assertTrue(tabArgs.stream().map(Completion::suggestion).toList().contains("f"));

        cmd.execute(new ContextBuilder(mockSender, new String[]{"cmd", "text", "abc"}));
        cmd.execute(new ContextBuilder(mockSender, new String[]{"cmd", "text"}));
        Assertions.assertEquals("text", ArgumentTestCommand.state);
        cmd.execute(new ContextBuilder(mockSender, new String[]{"cmd"}));
        Assertions.assertNull(ArgumentTestCommand.state);
    }
}