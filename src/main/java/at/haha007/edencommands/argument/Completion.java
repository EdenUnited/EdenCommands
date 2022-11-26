package at.haha007.edencommands.argument;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Accessors(fluent = true)
public class Completion<T> {
    @NotNull
    private final T completion;
    @Nullable
    private final Component tooltip;

    public Completion(@NotNull T completion) {
        this(completion, null);
    }

    public Completion(@NotNull T completion, @Nullable Component tooltip) {
        this.completion = completion;
        this.tooltip = tooltip;
    }
}
