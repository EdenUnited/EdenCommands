package at.haha007.edencommands.annotations;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.annotations.annotations.Command;
import at.haha007.edencommands.tree.CommandBuilder;
import at.haha007.edencommands.tree.CommandNode;
import at.haha007.edencommands.tree.ContextBuilder;
import at.haha007.edencommands.tree.LiteralCommandNode;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

class CommandTreeTest {
    private static final class TestCommand {
        @Command("command a")
        private void root(CommandContext context) {
            System.out.println("a");
        }

        @Command("command b")
        private void b(CommandContext context) {
            System.out.println("b");
        }

        @Command("command b c")
        private void bc(CommandContext context) {
            System.out.println("bc");
        }

        @Command("command b d")
        private void bd(CommandContext context) {
            System.out.println("bd");
        }

        @Command("command b e")
        private void be(CommandContext context) {
            System.out.println("be");
        }
    }

    @Test
    void add() {
        JavaPlugin mockPlugin = Mockito.mock(JavaPlugin.class);
        AnnotatedCommandLoader loader = new AnnotatedCommandLoader(mockPlugin);
        loader.addAnnotated(new TestCommand());
        loader.getCommands().forEach(System.out::println);
        System.out.println("----------------");
        Assertions.assertTrue(true);
    }


    @Test
    void asCommand() {
        CommandSender mockSender = Mockito.mock(CommandSender.class);
        JavaPlugin mockPlugin = Mockito.mock(JavaPlugin.class);
        AnnotatedCommandLoader loader = new AnnotatedCommandLoader(mockPlugin);
        loader.addAnnotated(new TestCommand());
        List<? extends CommandNode> commands = loader.getCommands().stream().map(CommandBuilder::build).toList();
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
}