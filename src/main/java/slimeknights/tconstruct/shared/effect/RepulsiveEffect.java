package slimeknights.tconstruct.shared.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** Effect that pushes mobs from the player */
public class RepulsiveEffect extends MobEffect {
  public RepulsiveEffect() {
    super(MobEffectCategory.BENEFICIAL, 0x727272);
  }

  @Override
  public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
    return (duration & 1) == 0;
  }

  @Override
  public boolean applyEffectTick(LivingEntity entity, int amplifier) {
    MagneticEffect.applyVelocity(entity, amplifier, LivingEntity.class, 2, -0.1f, 10);
    return true;
  }
}
