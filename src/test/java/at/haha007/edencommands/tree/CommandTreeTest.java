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
        List<AsyncTabCompleteEvent.Completion> complets = node.tabComplete(new InternalContext(sender, new String[]{"test", ""}, 0, new LinkedHashMap<>()));
        Assertions.assertEquals(complets.size(), 1);
        Assertions.assertEquals(complets.get(0).suggestion(), "arg");
    }

    @Test
    void tabArg() {
        DoubleArgument argument = DoubleArgument.builder()
                .notDoubleMessage(s -> Component.text("Argument must be of type double"))
                .completion(new Completion<>(.1, Component.text("meow")))
                .completion(new Completion<>(1.))
                .completion(new Completion<>(10.01))
                .completion(new Completion<>(0.1 + 0.2))
                .limitation(new DoubleArgument.MinimumFilter(Component.text("text"), 0))
                .build();

        LiteralCommandNode node = LiteralCommandNode.builder("test").then(ArgumentCommandNode.builder("arg", argument)).build();
        List<AsyncTabCompleteEvent.Completion> complets = node.tabComplete(new InternalContext(sender, new String[]{"test", ""}, 0, new LinkedHashMap<>()));
        Assertions.assertEquals(complets.size(), 4);
        Assertions.assertEquals(complets.get(0).suggestion(), "0.1");
    }
}
