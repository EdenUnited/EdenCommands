package at.haha007.edencommands.argument;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A helper class to only accept the used <T> in tab-completions for FloatArgument etc.
 * Needs to be converted to strings in the implementation though.
 * @param <T>
 */
@Getter
@Accessors(fluent = true)
public class Completion<T> {
    @NotNull
    private final T completion;
    @Nullable
    private final Component tooltip;

    /**
     * A Completion without tooltip
     * @param completion The instance to be completed
     */
    public Completion(@NotNull T completion) {
        this(completion, null);
    }

    /**
     * A Completion without tooltip if provided.
     * @param completion The instance to be completed
     * @param tooltip The tooltip to show when hovering over the tab-completion
     */
    public Completion(@NotNull T completion, @Nullable Component tooltip) {
        this.completion = completion;
        this.tooltip = tooltip;
    }
}
