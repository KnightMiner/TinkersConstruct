package slimeknights.tconstruct.library.modifiers.hook.mining;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed;

import javax.annotation.Nullable;

/** Context for {@link BreakSpeedModifierHook} */
public sealed interface BreakSpeedContext {
  /** Player breaking the block. */
  Player player();

  /** State being broken. May be {@link net.minecraft.world.level.block.Blocks#AIR} when querying block insensitive break speed. */
  BlockState state();

  /** Block position that was hit, will be null when querying speed without a specific position. */
  @Nullable
  BlockPos pos();

  /** Side of the block that was hit. Value is undefined if {@link #pos()} is {@code null}. */
  Direction sideHit();

  /** If true, the tool is effective against this block type */
  boolean isEffective();

  /** Original mining speed before modifiers applied. Includes modifiers from other listeners to {@link net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed}. */
  float originalSpeed();

  /** Calculated modifier from potion effects such as haste and environment such as water, use for additive bonuses to ensure consistency with the mining speed stat. */
  float miningSpeedMultiplier();


  /* Helpers */

  /**
   * Gets the mining speed modifier for the current conditions, notably potions and armor enchants.
   * @param entity  Entity to check
   * @return  Mining speed modifier
   */
  static float getMiningModifier(LivingEntity entity) {
    float modifier = 1.0f;
    // haste effect
    if (MobEffectUtil.hasDigSpeed(entity)) {
      modifier *= 1.0F + (MobEffectUtil.getDigSpeedAmplification(entity) + 1) * 0.2f;
    }
    // mining fatigue
    MobEffectInstance miningFatigue = entity.getEffect(MobEffects.DIG_SLOWDOWN);
    if (miningFatigue != null) {
      switch (miningFatigue.getAmplifier()) {
        case 0 -> modifier *= 0.3F;
        case 1 -> modifier *= 0.09F;
        case 2 -> modifier *= 0.0027F;
        default -> modifier *= 8.1E-4F;
      }
    }
    // water
    if (entity.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(entity)) {
      modifier /= 5.0F;
    }
    if (!entity.onGround()) {
      modifier /= 5.0F;
    }
    return modifier;
  }


  /* Impl */

  /** Break speed context for {@link BreakSpeed} */
  record Event(BreakSpeed event, Direction sideHit, boolean isEffective, float miningSpeedMultiplier) implements BreakSpeedContext {
    @Override
    public Player player() {
      return event.getEntity();
    }

    @Override
    public BlockState state() {
      return event.getState();
    }

    @Nullable
    @Override
    public BlockPos pos() {
      return event.getPosition().orElse(null);
    }

    @Override
    public float originalSpeed() {
      return event.getOriginalSpeed();
    }
  }

  /** Implementation of break speed context. */
  record Direct(
    Player player, BlockState state, @Nullable BlockPos pos, Direction sideHit,
    boolean isEffective, float originalSpeed, float miningSpeedMultiplier
  ) implements BreakSpeedContext {}
}
