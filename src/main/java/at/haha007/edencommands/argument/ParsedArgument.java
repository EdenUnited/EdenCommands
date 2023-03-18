package at.haha007.edencommands.argument;

/**
 * @param result The parsed argument
 * @param pointerIncrements how many subcommands to jump forwards
 */
public record ParsedArgument<T>(T result, int pointerIncrements) {
}
