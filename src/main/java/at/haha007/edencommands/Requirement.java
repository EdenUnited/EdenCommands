package at.haha007.edencommands;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public interface Requirement extends Predicate<CommandContext> {

    default Requirement or(@NotNull Requirement other) {
        return (t) -> test(t) || other.test(t);
    }

    default Requirement and(@NotNull Requirement other) {
        return (t) -> test(t) && other.test(t);
    }

    default Requirement negate() {
        return (t) -> !test(t);
    }
}
