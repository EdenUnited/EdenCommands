package at.haha007.edencommands.annotations;

import at.haha007.edencommands.argument.EnumArgument;
import at.haha007.edencommands.argument.Filter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class EnumArgumentParser<T extends Enum<T>> implements ArgumentParser<EnumArgument<T>> {

    public static <U extends Enum<U>> Builder<U> builder(Class<U> enumClass) {
        return new Builder<>(enumClass);
    }

    public static EnumArgumentParser<EntityType> entityTypeParser() {
        return builder(EntityType.class).defaultTooltipProvider(t -> Component.translatable(t.translationKey())).build();
    }

    public static EnumArgumentParser<Material> materialParser() {
        return builder(Material.class).defaultTooltipProvider(m -> Component.translatable(m.translationKey())).build();
    }

    public static EnumArgumentParser<Material> blockMaterialParser() {
        return builder(Material.class)
                .defaultTooltipProvider(m -> Component.translatable(m.translationKey()))
                .defaultFilter((s, t) -> t.isBlock() ? null : Component.text("Material is not a block!"))
                .build();
    }

    public static EnumArgumentParser<Material> itemMaterialParser() {
        return builder(Material.class)
                .defaultTooltipProvider(m -> Component.translatable(m.translationKey()))
                .defaultFilter((s, t) -> t.isItem() ? null : Component.text("Material is not an item!"))
                .build();
    }


    private final Class<T> enumClass;
    private final Filter<T> defaultFilter;
    private final Map<String, Filter<T>> filterMap;
    private final Function<T, Component> defaultTooltipProvider;
    private final Map<String, Function<T, Component>> tooltipProvider;

    private EnumArgumentParser(Class<T> enumClass,
                               Filter<T> defaultFilter,
                               Map<String, Filter<T>> filterMap,
                               Function<T, Component> defaultTooltipProvider,
                               Map<String, Function<T, Component>> tooltipProvider) {
        this.enumClass = enumClass;
        this.defaultFilter = defaultFilter;
        this.filterMap = Map.copyOf(filterMap);
        this.defaultTooltipProvider = defaultTooltipProvider;
        this.tooltipProvider = tooltipProvider;
    }

    public EnumArgument<T> parse(Map<String, String> params) {
        EnumArgument.EnumArgumentBuilder<T> builder = EnumArgument.builder(enumClass);
        String filter = params.get("filter");
        builder.filter(filterMap.getOrDefault(filter, defaultFilter));
        String tooltip = params.get("tooltip");
        builder.tooltipProvider(tooltipProvider.getOrDefault(tooltip, defaultTooltipProvider));
        String message = params.getOrDefault("error", "Unknown value: {0}");
        builder.errorMessage(c -> MiniMessage.miniMessage().deserialize(message.replace("{0}", c)));
        return builder.build();
    }

    public static class Builder<T extends Enum<T>> {
        private final Class<T> enumClass;
        private Filter<T> defaultFilter;
        private final Map<String, Filter<T>> filterMap = new HashMap<>();
        private Function<T, Component> defaultTooltipProvider;
        private final Map<String, Function<T, Component>> tooltipProvider = new HashMap<>();

        public Builder(Class<T> enumClass) {
            this.enumClass = enumClass;
        }

        public Builder<T> defaultFilter(Filter<T> defaultFilter) {
            this.defaultFilter = defaultFilter;
            return this;
        }

        public Builder<T> defaultTooltipProvider(Function<T, Component> defaultTooltipProvider) {
            this.defaultTooltipProvider = defaultTooltipProvider;
            return this;
        }

        public Builder<T> filterMap(Map<String, Filter<T>> filterMap) {
            this.filterMap.putAll(filterMap);
            return this;
        }

        public Builder<T> tooltipProvider(Map<String, Function<T, Component>> tooltipProvider) {
            this.tooltipProvider.putAll(tooltipProvider);
            return this;
        }

        public Builder<T> filter(String name, Filter<T> filter) {
            filterMap.put(name, filter);
            return this;
        }

        public Builder<T> tooltipProvider(String name, Function<T, Component> tooltipProvider) {
            this.tooltipProvider.put(name, tooltipProvider);
            return this;
        }

        public EnumArgumentParser<T> build() {
            return new EnumArgumentParser<>(enumClass,
                    defaultFilter == null ? (s, t) -> null : defaultFilter,
                    filterMap,
                    defaultTooltipProvider == null ? t -> null : defaultTooltipProvider,
                    tooltipProvider);
        }
    }
}
