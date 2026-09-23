package slimeknights.tconstruct.common;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Effect extension with a few helpers */
public class TinkerEffect extends MobEffect {
  /** If true, effect is visible, false for hidden */
  private final boolean show;
  public TinkerEffect(MobEffectCategory typeIn, boolean show) {
    this(typeIn, 0xffffff, show);
  }

  public TinkerEffect(MobEffectCategory typeIn, int color, boolean show) {
    super(typeIn, color);
    this.show = show;
  }

  // override to change return type
  @Override
  public TinkerEffect addAttributeModifier(Attribute pAttribute, String pUuid, double pAmount, Operation pOperation) {
    super.addAttributeModifier(pAttribute, pUuid, pAmount, pOperation);
    return this;
  }

  /* Visibility */

  @Override
  public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
    consumer.accept(new IClientMobEffectExtensions() {
      @Override
      public boolean isVisibleInInventory(MobEffectInstance effect) {
        return show;
      }

      @Override
      public boolean isVisibleInGui(MobEffectInstance effect) {
        return show;
      }
    });
  }

  /* Helpers */

  /**
   * Gets the level of the effect on the entity starting from 1, or 0 if not active
   * @param entity  Entity to check
   * @return  Level, or 0 if inactive
   */
  public static int getLevel(LivingEntity entity, MobEffect effect) {
    return getAmplifier(entity, effect) + 1;
  }

  /**
   * Gets the level of the effect on the entity starting from 1, or 0 if not active
   * @param entity  Entity to check
   * @return  Level, or 0 if inactive
   */
  public static int getLevel(LivingEntity entity, Supplier<? extends MobEffect> effect) {
    return getAmplifier(entity, effect.get()) + 1;
  }

  /**
   * Gets the amplifier of the effect on the entity starting from 0, or -1 if not active
   * @param entity  Entity to check
   * @return  Amplifier, or -1 if inactive
   */
  public static int getAmplifier(LivingEntity entity, MobEffect effect) {
    MobEffectInstance instance = entity.getEffect(effect);
    if (instance != null) {
      return instance.getAmplifier();
    }
    return -1;
  }
}
