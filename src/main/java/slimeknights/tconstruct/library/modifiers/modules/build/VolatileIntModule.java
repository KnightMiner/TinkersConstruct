package slimeknights.tconstruct.library.modifiers.modules.build;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.json.LevelingInt;
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

import javax.annotation.Nullable;
import java.util.List;

/**
 * Module that just sets a integer on a tool to the given value
 * @see slimeknights.tconstruct.library.tools.definition.module.build.VolatileIntModule
 * @see VolatileFloatModule
 */
public record VolatileIntModule(ResourceLocation flag, LevelingInt value, ModifierCondition<ToolContext> condition) implements VolatileDataModifierHook, ProjectileShootModifierHook, ModifierModule, ConditionalModule<ToolContext> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<VolatileIntModule>defaultHooks(ModifierHooks.VOLATILE_DATA);
  public static final RecordLoadable<VolatileIntModule> LOADER = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("flag", VolatileIntModule::flag),
    LevelingInt.LOADABLE.directField(VolatileIntModule::value),
    ModifierCondition.CONTEXT_FIELD, VolatileIntModule::new);

  public VolatileIntModule(ResourceLocation flag, LevelingInt value) {
    this(flag, value, ModifierCondition.ANY_CONTEXT);
  }

  @Override
  public void addVolatileData(ToolContext context, ModifierEntry modifier, MutableDataComponentHolder volatileData) {
    if (condition.matches(context, modifier)) {
      volatileData.putInt(flag, volatileData.getInt(flag) + value.compute(modifier.getEffectiveLevel()));
    }
  }

  @Override
  public void onProjectileShoot(ToolStackView tool, ModifierEntry modifier, @Nullable LivingEntity shooter, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
    if (condition.matches(tool, modifier)) {
      persistentData.putInt(flag, persistentData.getInt(flag) + value.compute(modifier.getEffectiveLevel()));
    }
  }

  @Override
  public RecordLoadable<VolatileIntModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }
}
