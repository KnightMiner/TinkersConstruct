package slimeknights.tconstruct.tools.modules.ranged.ammo;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.loadable.record.SingletonLoader;
import slimeknights.mantle.util.TranslationHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.fluid.item.FluidComponents;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHolder;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectContext;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectManager;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffects;
import slimeknights.tconstruct.library.modifiers.hook.build.ModifierRemovalHook;
import slimeknights.tconstruct.library.modifiers.hook.build.ValidateModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.DisplayNameModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.ranged.ProjectileFuseModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.ranged.ProjectileShootModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.library.tools.capability.fluid.ToolFluidCapability;
import slimeknights.tconstruct.library.tools.capability.fluid.ToolFluidCapability.FluidModifierHook;
import slimeknights.tconstruct.library.tools.capability.fluid.ToolTankHelper;
import slimeknights.tconstruct.library.tools.component.ModifierEntries;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.stack.ToolContext;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;
import slimeknights.tconstruct.library.tools.stack.VolatileBuilder;

import javax.annotation.Nullable;
import java.util.List;

/** Module that allows arrows to perform fluid effect on hit */
public enum SmashingModule implements ModifierModule, FluidModifierHook, ProjectileShootModifierHook, ProjectileHitModifierHook, ProjectileFuseModifierHook, VolatileDataModifierHook, ValidateModifierHook, ModifierRemovalHook, DisplayNameModifierHook, TooltipModifierHook {
  INSTANCE;

  /** Projectile boolean marking that a fluid effect happened */
  private static final ResourceLocation KEY_USED = TConstruct.getResource("smashing_used");
  private static final String FORMAT = TConstruct.makeTranslationKey("modifier", "smashing.format");
  private static final Component EMPTY_TO_SWAP = TConstruct.makeTranslation("modifier", "smashing.empty_to_swap");
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<SmashingModule>defaultHooks(ToolFluidCapability.HOOK, ModifierHooks.PROJECTILE_LAUNCH, ModifierHooks.PROJECTILE_SHOT, ModifierHooks.PROJECTILE_HIT, ModifierHooks.PROJECTILE_FUSE, ModifierHooks.VOLATILE_DATA, ModifierHooks.VALIDATE, ModifierHooks.REMOVE, ModifierHooks.DISPLAY_NAME, ModifierHooks.TOOLTIP);
  public static final RecordLoadable<SmashingModule> LOADER = new SingletonLoader<>(INSTANCE);

  @Override
  public RecordLoadable<SmashingModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public Integer getPriority() {
    // after bounce, before explosive
    return 50;
  }


  /* Tank helpers */

  /** Gets the capacity for the given fluid */
  private static int getAmount(Fluid fluid) {
    FluidEffects effects = FluidEffectManager.INSTANCE.find(fluid);
    return effects.hasEffects() ? effects.fluids().amount() : 0;
  }

  /** Gets the capacity for the given fluid */
  private static int getAmount(ModifierEntry modifier, Fluid fluid) {
    return getAmount(fluid) * modifier.getLevel();
  }

  /** Gets the amount to store in NBT to ensure no funny business with part swapping causes dupes */
  private static float getValidationAmount(ToolStackView tool, ModifierEntry modifier) {
    float level = modifier.getEffectiveLevel();
    for (ModifierEntry entry : tool.getModifiers()) {
      level = entry.getHook(ModifierHooks.CRAFT_COUNT).modifyCraftCount(tool, modifier, level);
    }
    return level;
  }

  /* Tank filling/draining */

  @Override
  public int fill(ToolStackView tool, ModifierEntry modifier, FluidStack resource, FluidAction action) {
    if (resource.isEmpty()) {
      return 0;
    }
    FluidComponents current = FluidComponents.get(tool);
    // already has fluid? can't fill
    if (!current.isEmpty()) {
      return 0;
    }
    int amount = getAmount(modifier, resource.getFluid());
    // if the fluid is invalid, or not enough fluid is offered, give up
    if (amount == 0 || resource.getAmount() < amount) {
      return 0;
    }
    // success! we can fill
    if (action.execute()) {
      tool.set(TinkerData.FLUID, FluidComponents.of(resource));
      // we want to store a fixed size, but its possible part swapping changes our capacity, so keep track of our capacity at the time of storing
      tool.set(TinkerData.SMASHING_VALIDATION, getValidationAmount(tool, modifier));
    }
    return amount;
  }

  @Override
  public FluidStack drain(ToolStackView tool, ModifierEntry modifier, int maxDrain, FluidAction action) {
    if (maxDrain > 0) {
      FluidComponents fluid = FluidComponents.get(tool);
      if (!fluid.isEmpty()) {
        int amount = getAmount(modifier, fluid.fluid());
        if (amount <= 0) {
          // 0 amount with a fluid means datapacks changed, best we can do is delete what we have
          tool.remove(TinkerData.FLUID);
          // ensure we requested enough
        } else if (amount <= maxDrain) {
          FluidStack result = fluid.toStack(amount);
          if (action.execute()) {
            tool.remove(TinkerData.FLUID);
          }
          return result;
        }
      }
    }
    return FluidStack.EMPTY;
  }

  @Override
  public FluidStack drain(ToolStackView tool, ModifierEntry modifier, FluidStack resource, FluidAction action) {
    if (!resource.isEmpty()) {
      FluidComponents fluid = FluidComponents.get(tool);
      // ensure we have a valid fluid
      if (!fluid.isEmpty() && fluid.isSameFluidSameComponents(resource)) {
        int amount = getAmount(modifier, fluid.fluid());
        if (amount <= 0) {
          // 0 amount with a fluid means datapacks changed, best we can do is delete what we have
          tool.remove(TinkerData.FLUID);
          // ensure we requested enough
        } else if (amount <= resource.getAmount()) {
          // ensure the tag matches
          FluidStack result = fluid.toStack(amount);
          if (action.execute()) {
            tool.remove(TinkerData.FLUID);
          }
          return result;
        }
      }
    }
    return FluidStack.EMPTY;
  }


  /* Other tank behaviors */

  @Override
  public FluidStack getFluidInTank(ToolStackView tool, ModifierEntry modifier, int tank) {
    FluidComponents fluid = FluidComponents.get(tool);
    if (!fluid.isEmpty()) {
      int amount = getAmount(modifier, fluid.fluid());
      if (amount > 0) {
        return fluid.toStack(amount);
      } else {
        // invalid, nothing more to do
        tool.remove(TinkerData.FLUID);
      }
    }
    return FluidStack.EMPTY;
  }

  @Override
  public int getTankCapacity(ToolStackView tool, ModifierEntry modifier, int tank) {
    FluidComponents fluid = FluidComponents.get(tool);
    if (!fluid.isEmpty()) {
      return getAmount(modifier, fluid.fluid());
    }
    // TODO: should we return something else? this number when empty is really meaningless
    return FluidValues.BOTTLE;
  }

  @Override
  public boolean isFluidValid(ToolStackView tool, ModifierEntry modifier, int tank, FluidStack fluid) {
    return getAmount(fluid.getFluid()) > 0;
  }


  /* Tool data */

  @Override
  public void addVolatileData(ToolContext context, ModifierEntry modifier, VolatileBuilder builder, MutableDataComponentHolder volatileData) {
    ToolFluidCapability.addTanks(this, context, modifier, builder);
  }

  @Nullable
  @Override
  public Component validate(ToolStackView tool, ModifierEntry modifier) {
    FluidComponents fluid = FluidComponents.get(tool);
    if (!fluid.isEmpty()) {
      // if our new level is larger, error to prevent a fluid dupe
      float level = getValidationAmount(tool, modifier);
      if (tool.getOrDefault(TinkerData.SMASHING_VALIDATION, 0f) < level) {
        return EMPTY_TO_SWAP;
      }
      // delete some fluid to match new level
      tool.set(TinkerData.SMASHING_VALIDATION, level);
    }
    return null;
  }

  @Nullable
  @Override
  public Component onRemoved(ToolStackView tool, ModifierHolder modifier) {
    // delete fluid on swap, such a waste
    tool.remove(TinkerData.FLUID);
    tool.remove(TinkerData.SMASHING_VALIDATION);
    return null;
  }


  /* Fluid effects */

  @Override
  public void onProjectileShoot(ToolStackView tool, ModifierEntry modifier, @Nullable LivingEntity shooter, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
    // if firing an arrow via multishot, don't fill the extra projectiles with fluid to prevent some dupes with effects
    if (arrow == null || arrow.pickup == Pickup.ALLOWED || shooter instanceof Player player && player.getAbilities().instabuild) {
      FluidComponents fluid = FluidComponents.get(tool);
      if (!fluid.isEmpty()) {
        int amount = getAmount(modifier, fluid.fluid());
        if (amount > 0) {
          projectile.setData(TinkerData.PROJECTILE_FLUID, fluid.toStack(amount));
        }
      }
    }
  }

  @Override
  public boolean onProjectileHitEntity(ModifierEntries modifiers, ModDataNBT persistentData, ModifierEntry modifier, Projectile projectile, EntityHitResult hit, @Nullable LivingEntity attacker, @Nullable LivingEntity target, boolean unblocked) {
    // find our fluid and ensure it affects blocks
    FluidStack fluid = projectile.getData(TinkerData.PROJECTILE_FLUID);
    if (!fluid.isEmpty()) {
      FluidEffects effects = FluidEffectManager.INSTANCE.find(fluid.getFluid());
      if (effects.hasEntityEffects()) {
        // apply the effect
        int drained = effects.applyToEntity(fluid, modifier.getEffectiveLevel(),
          FluidEffectContext.builder(projectile.level()).user(attacker).projectile(projectile).location(hit.getLocation()).target(hit.getEntity(), target),
          FluidAction.EXECUTE
        );
        // drain the fluid
        if (drained > 0) {
          fluid.shrink(drained);
          if (!fluid.isEmpty()) {
            projectile.setData(TinkerData.PROJECTILE_FLUID, fluid);
          } else {
            projectile.removeData(TinkerData.PROJECTILE_FLUID);
          }
          projectile.playSound(SoundEvents.SPLASH_POTION_BREAK);
          // does not need to check if we exceeded our piercing count; the arrow will deal with removing in that case
          if (projectile instanceof Arrow arrow && arrow.getPierceLevel() > 0) {
            // mark as used to prevent it from dropping later
            persistentData.putBoolean(KEY_USED, true);
          } else {
            projectile.playSound(SoundEvents.SPLASH_POTION_BREAK);
            projectile.discard();
          }
        }
      } else {
        projectile.removeData(TinkerData.PROJECTILE_FLUID);
      }
    }
    return false;
  }

  @Override
  public boolean onProjectileHitBlock(ModifierEntries modifiers, ModDataNBT persistentData, ModifierEntry modifier, Projectile projectile, BlockHitResult hit, @Nullable LivingEntity attacker) {
    // find our fluid and ensure it affects blocks
    FluidStack fluid = projectile.getData(TinkerData.PROJECTILE_FLUID);
    boolean used = false;
    if (!fluid.isEmpty()) {
      FluidEffects effects = FluidEffectManager.INSTANCE.find(fluid.getFluid());
      if (effects.hasBlockEffects()) {
        // apply the effect
        int drained = effects.applyToBlock(fluid, modifier.getEffectiveLevel(),
          FluidEffectContext.builder(projectile.level()).user(attacker).projectile(projectile).location(hit.getLocation()).block(hit),
          FluidAction.EXECUTE
        );
        // drain the fluid
        if (drained > 0) {
          fluid.shrink(drained);
          used = true;
          if (!fluid.isEmpty()) {
            // TODO: ideally, would like the arrow to live past this point to hit another block if the block is gone
            // but we don't have hit canceling tech right now due to an API change
            projectile.setData(TinkerData.PROJECTILE_FLUID, fluid);
          } else {
            projectile.removeData(TinkerData.PROJECTILE_FLUID);
          }
        }
      } else {
        projectile.removeData(TinkerData.PROJECTILE_FLUID);
      }
    }
    // if the arrow is stopping, discard it to prevent a fluid dupe
    if (used || persistentData.getBoolean(KEY_USED)) {
      projectile.playSound(SoundEvents.SPLASH_POTION_BREAK);
      projectile.discard();
    }
    return false;
  }

  @Override
  public void onProjectileFuseFinish(ModifierEntries modifiers, ModDataNBT persistentData, ModifierEntry modifier, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow) {
    boolean used = false;
    FluidStack fluid = projectile.getData(TinkerData.PROJECTILE_FLUID);
    if (!fluid.isEmpty()) {
      FluidEffects effects = FluidEffectManager.INSTANCE.find(fluid.getFluid());
      if (effects.hasBlockEffects()) {
        // apply the effect at the location of the projectile
        Vec3 position = projectile.position();
        int drained = effects.applyToBlock(fluid, modifier.getEffectiveLevel(),
          FluidEffectContext.builder(projectile.level()).user(projectile.getOwner()).projectile(projectile).location(position)
            .block(new BlockHitResult(position, projectile.getDirection(), projectile.blockPosition(), false)),
          FluidAction.EXECUTE
        );
        // drain the fluid
        if (drained > 0) {
          fluid.shrink(drained);
          used = true;
          if (!fluid.isEmpty()) {
            projectile.setData(TinkerData.PROJECTILE_FLUID, fluid);
          } else {
            projectile.removeData(TinkerData.PROJECTILE_FLUID);
          }
        }
      } else {
        projectile.removeData(TinkerData.PROJECTILE_FLUID);
      }
    }
    // if the arrow is stopping, discard it to prevent a fluid dupe
    if (used || persistentData.getBoolean(KEY_USED)) {
      projectile.playSound(SoundEvents.SPLASH_POTION_BREAK);
      projectile.discard(); // ensure discarded, smashing overrides reusable
    }
  }


  /* Tooltip */

  @Override
  public Component getDisplayName(ToolStackView tool, ModifierEntry entry, Component name, @Nullable RegistryAccess access) {
    FluidComponents contents = FluidComponents.get(tool);
    if (!contents.isEmpty()) {
      // formats as <name> <level> (<fluid>)
      return Component.translatable(FORMAT, name, contents.toStack(FluidValues.BOTTLE).getHoverName()).withStyle(name.getStyle());
    }
    return name;
  }

  @Override
  public void addTooltip(ToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
    FluidComponents contents = FluidComponents.get(tool);
    if (!contents.isEmpty()) {
      int amount = getAmount(modifier, contents.fluid());
      if (amount > 0) {
        // formats as <fluid>: <amount> mb
        tooltip.add(modifier.getModifier().applyStyle(contents.toStack(amount).getHoverName().copy()
          .append(": ").append(Component.translatable(ToolTankHelper.MB_FORMAT, TranslationHelper.COMMA_FORMAT.format(amount)))));
      }
    }
  }


  /* Helper */

  /** Helper for working with models. Not designed to be used directly. */
  public static ToolTankHelper TANK_HELPER = new ToolTankHelper() {
    @Override
    public FluidStack getFluid(ToolStackView tool) {
      FluidComponents contents = FluidComponents.get(tool);
      if (!contents.isEmpty()) {
        int amount = getAmount(contents.fluid());
        if (amount > 0) {
          return contents.toStack(amount);
        }
      }
      return FluidStack.EMPTY;
    }

    @Deprecated
    @Override
    public FluidStack setFluid(ToolStackView tool, FluidStack fluid) {
      // disallowed currently, as we don't have a quick way to validate capacity for multiple levels
      return FluidStack.EMPTY;
    }

    @Override
    public int getCapacity(ToolStackView tool) {
      FluidComponents contents = FluidComponents.get(tool);
      if (!contents.isEmpty()) {
        return getAmount(contents.fluid());
      }
      return FluidValues.BOTTLE;
    }
  };
}
