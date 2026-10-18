package slimeknights.tconstruct.shared.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.EffectCure;

import java.util.Set;

/**
 * Effect that cannot be cured with milk
 */
public class NoMilkEffect extends MobEffect {
  public NoMilkEffect(MobEffectCategory typeIn, int color) {
    super(typeIn, color);
  }

  @Override
  public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {}
}
