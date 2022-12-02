package at.haha007.edencommands.tree;

import at.haha007.edencommands.argument.Completion;
import at.haha007.edencommands.argument.DoubleArgument;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.util.LinkedHashMap;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommandTreeTest {
    private final CommandSender sender = Mockito.mock(CommandSender.class);

    @Test
    void tabLiteral() {
        LiteralCommandNode node = LiteralCommandNode.builder("test").then(LiteralCommandNode.builder("arg")).build();
        List<AsyncTabCompleteEvent.Completion> completes = node.tabComplete(new InternalContext(sender, new String[]{"test", ""}, 0, new LinkedHashMap<>()));
        Assertions.assertEquals(completes.size(), 1);
        Assertions.assertEquals(completes.get(0).suggestion(), "arg");
    }

    @Test
    void tabArg() {
        DoubleArgument argument = DoubleArgument.builder()
                .notDoubleMessage(s -> Component.text("Argument must be of type double"))
                .completion(new Completion<>(.1, Component.text("meow")))
                .completion(new Completion<>(1.))
                .completion(new Completion<>(10.01))
                .completion(new Completion<>(0.1 + 0.2))
                .filter(new DoubleArgument.MinimumFilter(Component.text("text"), 0))
                .filter(new DoubleArgument.MaximumFilter(Component.text("text"), 10))
                .build();

        LiteralCommandNode node = LiteralCommandNode.builder("test").then(ArgumentCommandNode.builder("arg", argument)).build();
        List<AsyncTabCompleteEvent.Completion> completes = node.tabComplete(new InternalContext(sender, new String[]{"test", ""}, 0, new LinkedHashMap<>()));
        Assertions.assertEquals(3, completes.size());
        Assertions.assertEquals("0.1", completes.get(0).suggestion());
    }
}
