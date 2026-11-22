package slimeknights.tconstruct.library.recipe.modifiers.adding.variant;

import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import slimeknights.mantle.data.registry.NamedComponentRegistry;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.client.materials.MaterialTooltipCache;
import slimeknights.tconstruct.library.materials.MaterialVariant;
import slimeknights.tconstruct.library.modifiers.ModifierHolder;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.component.ResourceMap;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/** Formats variant data on a tool. Used in {@link slimeknights.tconstruct.library.modifiers.modules.display.ModifierVariantNameModule}. */
@FunctionalInterface
public interface VariantFormatter {
  /** Loader instance */
  NamedComponentRegistry<VariantFormatter> LOADER = new NamedComponentRegistry<>("Unknown variant formatter");

  /** Formats the variant on the tool. Returns null if there is no variant */
  @Nullable
  Component format(DataComponentHolder tool, ModifierHolder modifier);


  /* Instances */

  /** Formats a string using it as the key for a modifier */
  ModifierMapFormatter<String> STRING = modifierMap(TinkerData.MODIFIER_STRING, (modifier, value) -> Component.translatable(modifier.get().getTranslationKey() + "." + value));
  /** Formats an integer using it as the parameter to translation */
  ModifierMapFormatter<Integer> INTEGER = modifierMap(TinkerData.MODIFIER_INTEGER, (modifier, value) -> Component.translatable(modifier.get().getTranslationKey() + ".variant", value));
  /** Formats a material using the material display name */
  ModifierMapFormatter<MaterialVariant> MATERIAL = modifierMap(TinkerData.MODIFIER_MATERIAL, (modifier, value) -> MaterialTooltipCache.getDisplayName(value.id()));
  /** Formats a slot type using its uppercase name */
  ModifierMapFormatter<SlotType> SLOT_TYPE = modifierMap(TinkerData.MODIFIER_SLOT_TYPE, (modifier, value) -> value.getUppercaseName());

  /** Creates a formatter for a modifier map */
  static <T> ModifierMapFormatter<T> modifierMap(Supplier<DataComponentType<ResourceMap<T>>> component, BiFunction<ModifierHolder, T, Component> formatter) {
    return new ModifierMapFormatter<T>() {
      @Override
      public DataComponentType<ResourceMap<T>> component() {
        return component.get();
      }

      @Override
      public Component format(ModifierHolder modifier, T value) {
        return formatter.apply(modifier, value);
      }
    };
  }

  /** Helper for variant formatting data from a modifier map */
  interface ModifierMapFormatter<T> extends VariantFormatter {
    /** Component to assign the value into */
    DataComponentType<ResourceMap<T>> component();

    /** Formats the given value */
    Component format(ModifierHolder modifier, T value);

    @Nullable
    @Override
    default Component format(DataComponentHolder tool, ModifierHolder modifier) {
      T value = ResourceMap.get(tool, component()).get(modifier.id());
      return value == null ? null : format(modifier, value);
    }
  }
}
