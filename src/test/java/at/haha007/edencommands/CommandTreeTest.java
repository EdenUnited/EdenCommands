package at.haha007.edencommands;

import at.haha007.edencommands.argument.StringArgument;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommandTreeTest {
    private final CommandSender sender = Mockito.mock(CommandSender.class);

    @Test
    void tabLiteral() {
        LiteralCommandNode node = new LiteralCommandNode("test").then(new LiteralCommandNode("arg"));
        List<String> complets = node.tabComplete(new InternalContext(sender, new String[]{"test", ""}, 0, new LinkedHashMap<>()));
        Assertions.assertEquals(complets.size(), 1);
        Assertions.assertEquals(complets.get(0), "arg");
    }

    @Test
    void tabArg() {
        LiteralCommandNode node = new LiteralCommandNode("test").then(new ArgumentCommandNode<>("arg", StringArgument.word().tabCompleter(c -> Arrays.asList("a", "b", "c"))));
        List<String> complets = node.tabComplete(new InternalContext(sender, new String[]{"test", ""}, 0, new LinkedHashMap<>()));
        Assertions.assertEquals(complets.size(), 3);
        Assertions.assertEquals(complets.get(0), "a");
    }
}
