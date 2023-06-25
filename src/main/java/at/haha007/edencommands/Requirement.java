package at.haha007.edencommands;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public interface Requirement extends Predicate<CommandContext> {
    static Requirement alwaysTrue() {
        return new TrueRequirement();
    }

    static Requirement alwaysFalse() {
        return new FalseRequirement();
    }

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

final class TrueRequirement implements Requirement {
    @Override
    public boolean test(@NotNull CommandContext context) {
        return true;
    }
}

final class FalseRequirement implements Requirement {
    @Override
    public boolean test(@NotNull CommandContext context) {
        return true;
    }
}
