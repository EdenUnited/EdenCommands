package at.haha007.edencommands.argument;

import lombok.experimental.Accessors;

@Accessors(fluent = true)
public record ParsedArgument<T>(T result, int pointerIncrements) {
}
