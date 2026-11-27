package slimeknights.tconstruct.library.modifiers.modules.build;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.json.LevelingValue;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.ranged.ProjectileShootModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.stack.ToolContext;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;
import slimeknights.tconstruct.library.tools.stack.VolatileBuilder;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Module that just sets a float on a tool to the given value.
 * TODO: just use tool stats I think, though need to figure out how projectiles do it - maybe just copy specific values?
 * @see VolatileFlagModule
 * @see VolatileIntModule
 * @see VolatileDataModule
 */
public record VolatileFloatModule(ResourceLocation flag, LevelingValue value, ModifierCondition<ToolContext> condition) implements VolatileDataModifierHook, ProjectileShootModifierHook, ModifierModule, ConditionalModule<ToolContext> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<VolatileFloatModule>defaultHooks(ModifierHooks.VOLATILE_DATA);
  public static final RecordLoadable<VolatileFloatModule> LOADER = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("flag", VolatileFloatModule::flag),
    LevelingValue.LOADABLE.directField(VolatileFloatModule::value),
    ModifierCondition.CONTEXT_FIELD, VolatileFloatModule::new);

  public VolatileFloatModule(ResourceLocation flag, LevelingValue value) {
    this(flag, value, ModifierCondition.ANY_CONTEXT);
  }

  @Override
  public void addVolatileData(ToolContext context, ModifierEntry modifier, VolatileBuilder builder, MutableDataComponentHolder volatileData) {
    if (condition.matches(context, modifier)) {
      volatileData.putFloat(flag, volatileData.getFloat(flag) + value.compute(modifier.getEffectiveLevel()));
    }
  }

  @Override
  public void onProjectileShoot(ToolStackView tool, ModifierEntry modifier, @Nullable LivingEntity shooter, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
    if (condition.matches(tool, modifier)) {
      persistentData.putFloat(flag, persistentData.getFloat(flag) + value.compute(modifier.getEffectiveLevel()));
    }
  }

  @Override
  public RecordLoadable<VolatileFloatModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }
}
