package at.haha007.edencommands;

import java.util.List;
import java.util.function.Function;

import static com.destroystokyo.paper.event.server.AsyncTabCompleteEvent.Completion;

public interface TabCompleter extends Function<CommandContext, List<Completion>> {
}
