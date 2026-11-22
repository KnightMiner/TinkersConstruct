package slimeknights.tconstruct.library.tools.stack;

import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import slimeknights.tconstruct.library.materials.MaterialVariant;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.component.ModifierEntries;
import slimeknights.tconstruct.library.tools.component.ToolMaterials;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.definition.ToolDefinitionData;
import slimeknights.tconstruct.library.tools.nbt.IModDataView;

import java.util.List;

/**
 * Provides partial access to tool data, essentially a bridge between {@link ToolStackView} and {@link slimeknights.tconstruct.library.tools.context.ToolRebuildContext}
 */
public interface ToolContext extends DataComponentHolder {
  /** Gets the item contained in this tool */
  Item getItem();

  /** Gets the tool definition */
  ToolDefinition getDefinition();

  /** Gets the tool definition data */
  default ToolDefinitionData getDefinitionData() {
    return getDefinition().getData();
  }

  /** Checks if the tool has the given tag */
  @SuppressWarnings("deprecation")
  default boolean hasTag(TagKey<Item> tag) {
    return getItem().builtInRegistryHolder().is(tag);
  }

  /** Gets the given hook from the tool */
  default <T> T getHook(ModuleHook<T> hook) {
    return getDefinition().getData().getHook(hook);
  }


  /* Materials */

  /** Gets the list of current materials making this tool */
  ToolMaterials getMaterials();

  /**
   * Gets the material at the given index
   * @param index  Index
   * @return  Material, or unknown if index is invalid
   */
  default MaterialVariant getMaterial(int index) {
    return getMaterials().get(index);
  }


  /* Modifiers */

  /**
   * Gets a list of modifiers added from recipes.
   * In general, you should use {@link #getModifiers()} when performing modifier actions to include traits.
   * @return  Crafted modifier list
   */
  ModifierEntries getCraftedModifiers();

  /** Gets a full list of effective modifiers on this tool, from both upgrades/abilities and material traits */
  ModifierEntries getModifiers();

  /**
   * Helper to get a list of all modifiers on the tool. Note this list is already sorted by priority
   * @return  List of all modifiers
   */
  default List<ModifierEntry> getModifierList() {
    return getModifiers().modifiers();
  }

  /**
   * Gets the modifier entry for the given modifier ID
   * @param modifier  Modifier
   * @return  Modifier entry, or {@link ModifierEntry#EMPTY} if missing.
   */
  default ModifierEntry getModifier(ModifierId modifier) {
    return getModifiers().getEntry(modifier);
  }

  /**
   * Gets the modifier entry for the given modifier ID
   * @param modifier  Modifier
   * @return  Modifier entry, or {@link ModifierEntry#EMPTY} if missing.
   */
  default ModifierEntry getModifier(Modifier modifier) {
    return getModifiers().getEntry(modifier.id());
  }

  /**
   * Gets the level of a modifier on this tool. Will consider both raw modifiers and material traits
   * @param modifier  Modifier
   * @return  Level of modifier, 0 if the modifier is not on the tool
   */
  default int getModifierLevel(ModifierId modifier) {
    return getModifiers().getLevel(modifier);
  }

  /**
   * Gets the level of a modifier on this tool. Will consider both raw modifiers and material traits
   * @param modifier  Modifier
   * @return  Level of modifier, 0 if the modifier is not on the tool
   */
  default int getModifierLevel(Modifier modifier) {
    return getModifiers().getLevel(modifier.id());
  }


  /* Tool data */

  /**
   * Gets persistent modifier data from the tool.
   * This data may be edited by modifiers and will persist when stats rebuild
   */
  @Deprecated(forRemoval = true)
  default IModDataView getPersistentData() {
    return IModDataView.EMPTY;
  }
}
