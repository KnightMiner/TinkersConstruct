package slimeknights.tconstruct.library.recipe.modifiers.adding.variant;

import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import slimeknights.tconstruct.library.modifiers.ModifierHolder;
import slimeknights.tconstruct.library.tools.component.ResourceMap;

import java.util.Objects;

/** Implementation of swappable variant using {@link ResourceMap} components. */
public interface SwappableModifierMap<T> extends SwappableVariant {
  /** Gets the value to apply */
  T value();

  /** Component to assign the value into */
  DataComponentType<ResourceMap<T>> component();

  @Override
  default boolean isPresent(DataComponentHolder tool, ModifierHolder modifier) {
    return Objects.equals(ResourceMap.get(tool, component()).get(modifier.id()), value());
  }

  @Override
  default void applyToTool(MutableDataComponentHolder tool, ModifierHolder modifier) {
    ResourceMap.put(tool, component(), modifier.id(), value());
  }

  @Override
  default void removeFromTool(MutableDataComponentHolder tool, ModifierHolder modifier) {
    ResourceMap.remove(tool, component(), modifier.id());
  }
}
