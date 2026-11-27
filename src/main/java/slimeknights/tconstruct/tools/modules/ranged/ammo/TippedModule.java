package slimeknights.tconstruct.tools.modules.ranged.ammo;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.EntityHitResult;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.loadable.record.SingletonLoader;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHolder;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ModifierRemovalHook;
import slimeknights.tconstruct.library.modifiers.hook.display.DisplayNameModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.ranged.ProjectileShootModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.component.ModifierEntries;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;
import slimeknights.tconstruct.library.utils.RomanNumeralHelper;

import javax.annotation.Nullable;
import java.util.List;

/** Module allowing arrows to be tipped, applying their effect to the target */
public enum TippedModule implements ModifierModule, ProjectileShootModifierHook, ProjectileHitModifierHook, ModifierRemovalHook, DisplayNameModifierHook, TooltipModifierHook {
  INSTANCE;

  private static final String FORMAT = TConstruct.makeTranslationKey("modifier", "tipped.format");
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<TippedModule>defaultHooks(ModifierHooks.PROJECTILE_LAUNCH, ModifierHooks.PROJECTILE_SHOT, ModifierHooks.PROJECTILE_THROWN, ModifierHooks.PROJECTILE_HIT, ModifierHooks.DISPLAY_NAME, ModifierHooks.TOOLTIP, ModifierHooks.REMOVE);
  public static final RecordLoadable<TippedModule> LOADER = new SingletonLoader<>(INSTANCE);

  @Override
  public RecordLoadable<TippedModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }


  /* Data */

  @Override
  public void onProjectileShoot(ToolStackView tool, ModifierEntry modifier, @Nullable LivingEntity shooter, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
    PotionContents contents = tool.get(DataComponents.POTION_CONTENTS);
    if (contents != null) {
      projectile.setData(TinkerData.PROJECTILE_POTION, contents);
    }
  }

  @Nullable
  @Override
  public Component onRemoved(ToolStackView tool, ModifierHolder modifier) {
    tool.remove(DataComponents.POTION_CONTENTS);
    return null;
  }


  /* Effects */

  /** Gets the divisor for the duration */
  private static int getDivisor(ModifierEntry modifier) {
    return 1 << Math.max(4 - modifier.intEffectiveLevel(), 0);
  }

  @Override
  public boolean unblockedProjectileHitEntity(ModifierEntries modifiers, ModDataNBT persistentData, ModifierEntry modifier, Projectile projectile, EntityHitResult hit, @Nullable LivingEntity attacker, @Nullable LivingEntity target) {
    if (target != null) {
      PotionContents contents = projectile.getExistingDataOrNull(TinkerData.PROJECTILE_POTION);
      if (contents != null) {
        Entity source = projectile.getEffectSource();
        int divisor = getDivisor(modifier);
        int oldHurtTime = target.invulnerableTime;
        target.invulnerableTime = 0;
        // not a problem if the ID is invalid, will just do nothing
        for (MobEffectInstance instance : contents.getAllEffects()) {
          Holder<MobEffect> holder = instance.getEffect();
          MobEffect effect = holder.value();
          if (effect.isInstantenous()) {
            effect.applyInstantenousEffect(projectile, projectile.getOwner(), target, instance.getAmplifier(), 1f / (divisor * 0.75f));
          } else {
            target.addEffect(new MobEffectInstance(holder, Math.max(instance.mapDuration(i -> i / divisor), 1), instance.getAmplifier(), instance.isAmbient(), instance.isVisible()), source);
          }
        }
        target.invulnerableTime = oldHurtTime;
      }
    }
    return false;
  }


  /* Display */

  @Override
  public void addTooltip(ToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
    PotionContents contents = tool.get(DataComponents.POTION_CONTENTS);
    if (contents != null) {
      // TODO: this should use tooltipcontext
      PotionContents.addPotionTooltip(contents.getAllEffects(), tooltip::add, 1f / getDivisor(modifier), player != null ? player.level().tickRateManager().tickrate() : 20);
    }
  }

  @Override
  public Component getDisplayName(ToolStackView tool, ModifierEntry entry, Component name, @Nullable RegistryAccess access) {
    PotionContents contents = tool.get(DataComponents.POTION_CONTENTS);
    if (contents != null) {
      // formats as Tipped <level> (<potion>)
      return Component.translatable(FORMAT,
        RomanNumeralHelper.getNumeral(entry.getLevel()),
        Component.translatable(Potion.getName(contents.potion(), "item.minecraft.potion.effect."))
      ).withStyle(style -> style.withColor(contents.getColor()));
    }
    return name;
  }
}
