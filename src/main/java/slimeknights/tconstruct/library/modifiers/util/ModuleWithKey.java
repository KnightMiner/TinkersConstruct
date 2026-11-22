package slimeknights.tconstruct.library.modifiers.util;

import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.ResourceLocationLoadable;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHolder;

import javax.annotation.Nullable;

/**
 * Shared boilerplate for a module with a nullable key that can alternatively be the modifier ID
 */
public interface ModuleWithKey {
  /** Field for building loadables */
  LoadableField<ResourceLocation,ModuleWithKey> KEY_FIELD = ResourceLocationLoadable.DEFAULT.nullableField("key", ModuleWithKey::key);

  /** Gets the key for the module */
  default ResourceLocation getKey(Modifier modifier) {
    ResourceLocation key = key();
    if (key != null) {
      return key;
    }
    return modifier.id().id();
  }

  /** Gets the key for the module */
  default ResourceLocation getKey(ModifierEntry modifier) {
    ResourceLocation key = key();
    if (key != null) {
      return key;
    }
    return modifier.id().id();
  }

  /** Gets the key for the module */
  default ResourceLocation getKey(ModifierHolder modifier) {
    ResourceLocation key = key();
    if (key != null) {
      return key;
    }
    return modifier.id().id();
  }

  /** Gets the key field from the record */
  @Nullable
  ResourceLocation key();
}
