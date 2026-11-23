package slimeknights.tconstruct.library.modifiers.modules.build;

import com.google.common.collect.ImmutableSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.VolatileFlagModifierHook;
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
 * Module that just sets a boolean flag to true on a tool.
 * @see slimeknights.tconstruct.library.tools.definition.module.build.VolatileFlagModule
 * @see VolatileIntModule
 * @see VolatileFloatModule
 */
public record VolatileFlagModule(ResourceLocation flag, ModifierCondition<ToolContext> condition) implements VolatileFlagModifierHook, ProjectileShootModifierHook, ModifierModule, ConditionalModule<ToolContext> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<VolatileFlagModule>defaultHooks(ModifierHooks.VOLATILE_FLAGS);
  public static final RecordLoadable<VolatileFlagModule> LOADER = RecordLoadable.create(Loadables.RESOURCE_LOCATION.requiredField("flag", VolatileFlagModule::flag), ModifierCondition.CONTEXT_FIELD, VolatileFlagModule::new);

  public VolatileFlagModule(ResourceLocation flag) {
    this(flag, ModifierCondition.ANY_CONTEXT);
  }

  @Override
  public void addVolatileFlags(ToolContext context, ModifierEntry modifier, ImmutableSet.Builder<ResourceLocation> builder) {
    if (condition.matches(context, modifier)) {
      builder.add(flag);
    }
  }

  @Override
  public void onProjectileShoot(ToolStackView tool, ModifierEntry modifier, @Nullable LivingEntity shooter, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
    if (condition.matches(tool, modifier)) {
      persistentData.putBoolean(flag, true);
    }
  }

  @Override
  public RecordLoadable<VolatileFlagModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }
}
