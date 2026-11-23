package slimeknights.tconstruct.library.modifiers.modules.build;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import slimeknights.mantle.data.loadable.primitive.EnumLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.stack.ToolContext;
import slimeknights.tconstruct.library.tools.stack.VolatileBuilder;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Module for setting tool's display name rarity
 * TODO: consider modifier level/tool conditions
 */
public record RarityModule(Rarity rarity) implements VolatileDataModifierHook, ModifierModule {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<RarityModule>defaultHooks(ModifierHooks.VOLATILE_DATA);
  public static final RecordLoadable<RarityModule> LOADER = RecordLoadable.create(new EnumLoadable<>(Rarity.class).requiredField("rarity", RarityModule::rarity), RarityModule::new);

  @Override
  public void addVolatileData(ToolContext context, ModifierEntry modifier, VolatileBuilder builder, MutableDataComponentHolder volatileData) {
    setRarity(volatileData, rarity);
  }

  @Override
  public RecordLoadable<RarityModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  /**
   * Sets the rarity of the stack
   * @param volatileData     NBT
   * @param rarity  Rarity, largest index will be kept (so modded rarities tend to beat out vanilla).
   */
  public static void setRarity(MutableDataComponentHolder volatileData, Rarity rarity) {
    volatileData.set(DataComponents.RARITY, max(volatileData.get(DataComponents.RARITY), rarity));
  }

  /**
   * Gets the larger rarity
   * @param prev    Previous maximum.
   * @param rarity  New rarity to check.
   * @return Larger rarity between the two.
   */
  public static Rarity max(@Nullable Rarity prev, Rarity rarity) {
    // TODO: consider a sorted list of rarity values that JSON can set
    if (prev == null || rarity.ordinal() > prev.ordinal()) {
      return rarity;
    }
    return prev;
  }
}
