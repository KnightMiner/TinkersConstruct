package slimeknights.tconstruct.library.modifiers.modules.capacity;

import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.special.CapacityBarHook;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.library.tools.stat.impl.IntegerToolStat;
import slimeknights.tconstruct.tools.TinkerModifiers;

import java.util.List;

/**
 * Module implementing overslime and overslime helpers.
 * TODO 1.21: register as a singleton loader and migrate OverslimeModifier to JSON.
 */
public class OverslimeModule extends CapacityBarHook.PersistentDataCapacityBar {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<OverslimeModule>defaultHooks(ModifierHooks.CAPACITY_BAR);
  /** Singleton instance */
  public static final OverslimeModule INSTANCE = new OverslimeModule();

  /** Stat for the overslime cap, copies the durability global multiplier on build */
  public static final FloatToolStat OVERSLIME_STAT = new IntegerToolStat(TConstruct.resourceId("overslime"), 0xFF71DC85, 0, 0, Short.MAX_VALUE, TinkerTags.Items.DURABILITY) {
    @Override
    public Float build(ModifierStatsBuilder parent, Object builderObj) {
      return super.build(parent, builderObj) * parent.getMultiplier(ToolStats.DURABILITY);
    }
  };

  private OverslimeModule() {
    super(TinkerModifiers.overslime.id().id());
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  /**
   * Sets the shield, bypassing the capacity.
   * @param persistentData  Persistent data
   * @param amount          Amount to set
   */
  public void setAmountRaw(ModDataNBT persistentData, int amount) {
    if (amount <= 0) {
      persistentData.remove(key);
    } else {
      persistentData.putInt(key, amount);
    }
  }

  /** Gets the overslime capacity on the tool */
  public static int getCapacity(ToolStackView tool) {
    return tool.getStats().getInt(OVERSLIME_STAT);
  }

  @Override
  public int getCapacity(ToolStackView tool, ModifierEntry entry) {
    return getCapacity(tool);
  }

  /** Gets the boost to apply from overworked */
  public static int getOverworkedBonus(ToolStackView tool) {
    return (1 + tool.getModifierLevel(TinkerModifiers.overworked.id()));
  }

  @Override
  public void addAmount(ToolStackView tool, ModifierEntry entry, int amount) {
    // yeah, I am hardcoding overworked. If you need something similar, put in an issue request on github
    // grants +100% restoring per level
    super.addAmount(tool, entry, amount * getOverworkedBonus(tool));
  }

  /** Helper to add amount without worrying about the modifier entry */
  public void addAmount(ToolStackView tool, int amount) {
    addAmount(tool, ModifierEntry.EMPTY, amount);
  }

  /** Removes overslime from a tool. Unlike {@link #addAmount(ToolStackView, int)}, does not trigger overworked */
  public void removeAmount(ToolStackView tool, int amount) {
    removeAmount(tool, ModifierEntry.EMPTY, amount);
  }
}
