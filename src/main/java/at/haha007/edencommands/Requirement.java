package at.haha007.edencommands;

import org.jetbrains.annotations.NotNull;

public interface Requirement {
    static Requirement alwaysTrue() {
        return new TrueRequirement();
    }

    static Requirement alwaysFalse() {
        return new FalseRequirement();
    }

    default Requirement or(@NotNull Requirement other) {
        return t -> test(t) || other.test(t);
    }

    default Requirement and(@NotNull Requirement other) {
        return t -> test(t) && other.test(t);
    }

    boolean test(CommandContext t);

    @NotNull
    default Requirement negate() {
        return t -> !test(t);
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
