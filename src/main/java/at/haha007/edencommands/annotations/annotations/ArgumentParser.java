package at.haha007.edencommands.annotations.annotations;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import at.haha007.edencommands.argument.ParsedArgument;

import java.lang.reflect.InvocationTargetException;

public interface ArgumentParser<T> {
    ParsedArgument<T> parse(CommandContext context) throws InvocationTargetException, IllegalAccessException, CommandException;
}
