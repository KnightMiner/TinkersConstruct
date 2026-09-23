package slimeknights.tconstruct.shared.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.common.TinkerEffect;

/** Effect that pushes mobs from the player */
public class RepulsiveEffect extends TinkerEffect {
  public RepulsiveEffect() {
    super(MobEffectCategory.BENEFICIAL, 0x727272, false);
  }

  @Override
  public boolean isDurationEffectTick(int duration, int amplifier) {
    return (duration & 1) == 0;
  }

  @Override
  public void applyEffectTick(LivingEntity entity, int amplifier) {
    MagneticEffect.applyVelocity(entity, amplifier, LivingEntity.class, 2, -0.1f, 10);
  }
}
