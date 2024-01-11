package at.haha007.edencommands.argument;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A helper class to only accept the used <T> in tab-completions for FloatArgument etc.
 * Needs to be converted to strings in the implementation though.
 * @param <T>
 */
public class Completion<T> {
    @NotNull
    private final T value;
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
        this.value = completion;
        this.tooltip = tooltip;
    }

    public @NotNull T completion() {
        return this.value;
    }

    public @Nullable Component tooltip() {
        return this.tooltip;
    }
}
