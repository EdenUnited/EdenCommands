package at.haha007.edencommands.annotations.annotations;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.argument.ParsedArgument;

import java.util.function.Function;

public interface ArgumentParser extends Function<CommandContext, ParsedArgument<?>> {
}
