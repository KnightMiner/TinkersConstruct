package slimeknights.tconstruct.library.tools.stack;


import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.component.ModifierSlots;
import slimeknights.tconstruct.library.tools.component.StatMultipliers;
import slimeknights.tconstruct.library.tools.component.StatValues;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IModDataView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.stat.INumericToolStat;

/**
 * Provides mostly read only access to {@link ToolStack}.
 * Used since modifiers should not be modifying the tool materials or modifiers in their behaviors.
 * If you receive an instance of this interface a parameter, do NOT use an instanceof check and cast it to a ToolStack. Don't make me use a private wrapper class.
 */
public interface ToolStackView extends ToolContext, MutableDataComponentHolder {
  /* Item stack */

  /**
   * Checks if this tool stack represents the same item stack instance as the passed stack.
   * In other words, this is a shallow comparison, not deep comparison.
   * Condition compares based on same stack tag instance. */
  default boolean isSameStack(ItemStack stack) {
    return false;
  }

  /* Stats */

  /** On built tools, contains the full tool stats. During tool rebuild, contains the base stats before considering modifiers. */
  StatValues getStats();

  /**
   * Gets the tool stats if parsed, or parses from NBT if not yet parsed
   * @return stats
   */
  StatMultipliers getMultipliers();

  /** Commonly used operation, getting a stat multiplier */
  default float getMultiplier(INumericToolStat<?> stat) {
    return getMultipliers().get(stat);
  }


  /* Durability */

  /** Gets the current lost durability of the tool */
  int getDamage();

  /** Gets the current durability remaining for this tool */
  int getCurrentDurability();

  /** Checks whether the tool is broken */
  boolean isBroken();

  /** If true, tool is marked unbreakable by vanilla NBT. This is distinct from {@link slimeknights.tconstruct.tools.data.ModifierIds#unbreakable} */
  boolean isUnbreakable();

  /**
   * Sets the tools current damage.
   * Note in general you should use {@link ToolDamageUtil#damage(ToolStackView, int, LivingEntity, ItemStack)} or {@link ToolDamageUtil#repair(ToolStackView, int)} as they handle modifiers.
   * @param damage  New damage
   */
  void setDamage(int damage);

  /** Gets persistent slot count from the tool. This data will persistent on stat rebuild. */
  ModifierSlots getPersistentSlots();

  /** Gets volatile slot counts from the tool. This data is volatile and will reset stat rebuild. */
  ModifierSlots getVolatileSlots();

  @Deprecated(forRemoval = true)
  @Override
  default ModDataNBT getPersistentData() {
    return new ModDataNBT();
  }

  @Deprecated(forRemoval = true)
  default IModDataView getVolatileData() {
    return IModDataView.EMPTY;
  }


  /* Helpers */

  /**
   * Gets the free upgrade slots remaining on the tool
   * @return  Free upgrade slots
   */
  default int getFreeSlots(SlotType type) {
    return getPersistentSlots().get(type) + getVolatileSlots().get(type);
  }
}
