package at.haha007.edencommands.argument;

import lombok.experimental.Accessors;

/**
 * @param result The parsed argument
 * @param pointerIncrements how many subcommands to jump forwards
 */
@Accessors(fluent = true)
public record ParsedArgument<T>(T result, int pointerIncrements) {
}
