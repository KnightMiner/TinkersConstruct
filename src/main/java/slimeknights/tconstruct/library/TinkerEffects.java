package slimeknights.tconstruct.library;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.registration.deferred.PotionDeferredRegister;
import slimeknights.mantle.registration.deferred.PotionDeferredRegister.PotionType;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.shared.effect.AntigravityEffect;
import slimeknights.tconstruct.shared.effect.BleedingEffect;
import slimeknights.tconstruct.shared.effect.MagneticEffect;
import slimeknights.tconstruct.shared.effect.RepulsiveEffect;
import slimeknights.tconstruct.shared.effect.ReturningEffect;
import slimeknights.tconstruct.tools.modifiers.skull.SelfDestructiveModifier.SelfDestructiveEffect;
import slimeknights.tconstruct.world.TinkerWorld;

import javax.annotation.Nullable;

/**
 * Handles registration for all status effects and potions in the mod.
 */
@EventBusSubscriber(modid = TConstruct.MOD_ID)
public class TinkerEffects extends TinkerModule {
  private static final PotionDeferredRegister POTIONS = new PotionDeferredRegister(TConstruct.MOD_ID);

  private TinkerEffects() {}

  // slimy potions
  public static final DeferredHolder<MobEffect,MobEffect> experienced = MOB_EFFECTS.register("experienced", id -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x82c873)
    .addAttributeModifier(TinkerAttributes.EXPERIENCE_MULTIPLIER, id, 0.25f, Operation.ADD_MULTIPLIED_BASE));
  public static final DeferredHolder<MobEffect,MobEffect> ricochet = MOB_EFFECTS.register("ricochet", id -> new MobEffect(MobEffectCategory.NEUTRAL, 0x01cbcd)
    .addAttributeModifier(TinkerAttributes.KNOCKBACK_MULTIPLIER, id, 0.5f, Operation.ADD_MULTIPLIED_BASE));
  public static final DeferredHolder<MobEffect,MobEffect> enderference = MOB_EFFECTS.register("enderference", () -> new MobEffect(MobEffectCategory.HARMFUL, 0xD37CFF));
  /** Projectile persistent data key to allow ranged modifiers to hit endermen. */
  public static final ResourceLocation ENDERFERENCE_KEY = enderference.getId();

  // slimy cakes
  public static final DeferredHolder<MobEffect,MobEffect> bouncy = MOB_EFFECTS.register("bouncy", id -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x71AC63)
    .addAttributeModifier(TinkerAttributes.BOUNCY, id, 1, Operation.ADD_VALUE));
  public static final DeferredHolder<MobEffect,MobEffect> doubleJump = MOB_EFFECTS.register("double_jump", id -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xA99B87)
    .addAttributeModifier(TinkerAttributes.JUMP_COUNT, id, 1, Operation.ADD_VALUE));
  public static final DeferredHolder<MobEffect,AntigravityEffect> antigravity = MOB_EFFECTS.register("antigravity", AntigravityEffect::new);
  public static final DeferredHolder<MobEffect,ReturningEffect> returning = MOB_EFFECTS.register("returning", ReturningEffect::new);

  // modifier effects
  public static final DeferredHolder<MobEffect,BleedingEffect> bleeding = MOB_EFFECTS.register("bleeding", BleedingEffect::new);
  public static final DeferredHolder<MobEffect,MagneticEffect> magnetic = MOB_EFFECTS.register("magnetic", MagneticEffect::new);
  public static final DeferredHolder<MobEffect,SelfDestructiveEffect> selfDestructing = MOB_EFFECTS.register("self_destructing", SelfDestructiveEffect::new);
  public static final DeferredHolder<MobEffect,RepulsiveEffect> repulsive = MOB_EFFECTS.register("repulsive", RepulsiveEffect::new);
  public static final DeferredHolder<MobEffect,MobEffect> pierce = MOB_EFFECTS.register("pierce", id -> new MobEffect(MobEffectCategory.HARMFUL, 0xD1D37A).addAttributeModifier(Attributes.ARMOR, id, -1, Operation.ADD_VALUE));
  // damage boost
  public static final DeferredHolder<MobEffect,MobEffect> conductive = MOB_EFFECTS.register("conductive", () -> new MobEffect(MobEffectCategory.HARMFUL, 0xF2D500));
  public static final DeferredHolder<MobEffect,MobEffect> venom = MOB_EFFECTS.register("venom", () -> new MobEffect(MobEffectCategory.HARMFUL, 0xA2935E));

  // potions
  public static final EnumObject<PotionType,Potion> experiencedPotion = POTIONS.registerTypes(experienced).withStrong().withLong().build();
  public static final EnumObject<PotionType,Potion> ricochetPotion = POTIONS.registerTypes(ricochet).withStrong().withLong().build();
  public static final EnumObject<PotionType,Potion> levitationPotion = POTIONS.registerTypes("levitation", MobEffects.LEVITATION, 15 * 20, 0).withStrong().withLong(40 * 20, 0).build();
  public static final EnumObject<PotionType,Potion> enderferencePotion = POTIONS.registerTypes(enderference, 90 * 20, 0).withLong().build();

  /** Registers this class with the event bus. */
  @Internal
  public static void register(IEventBus bus) {
    POTIONS.register(bus);
  }

  @SubscribeEvent
  static void registerBrewing(RegisterBrewingRecipesEvent event) {
    PotionBrewing.Builder builder = event.getBuilder();
    brewing(builder, experiencedPotion,  Potions.AWKWARD, TinkerWorld.congealedSlime.get(SlimeType.EARTH));
    brewing(builder, ricochetPotion,     Potions.AWKWARD, TinkerWorld.congealedSlime.get(SlimeType.SKY));
    brewing(builder, levitationPotion,   Potions.AWKWARD, TinkerWorld.congealedSlime.get(SlimeType.ICHOR));
    brewing(builder, enderferencePotion, Potions.AWKWARD, TinkerWorld.congealedSlime.get(SlimeType.ENDER));
  }


  /** Registers recipes for brewing, longer and stronger potions for the given object */
  @SuppressWarnings("SameParameterValue")
  private static void brewing(PotionBrewing.Builder builder, EnumObject<PotionType,Potion> potion, Holder<Potion> base, ItemLike ingredient) {
    Holder<Potion> normal = potion.getHolder(PotionType.NORMAL);
    builder.addMix(base, ingredient.asItem(), normal);
    Holder<Potion> longer = potion.getHolderOrNull(PotionType.LONG);
    if (longer != null) {
      builder.addMix(normal, Items.REDSTONE, longer);
    }
    Holder<Potion> strong = potion.getHolderOrNull(PotionType.STRONG);
    if (strong != null) {
      builder.addMix(normal, Items.GLOWSTONE_DUST, strong);
    }
  }

  /** Checks if the given entity can be hit considering enderman enderference */
  public static boolean canHitWithProjectile(@Nullable LivingEntity living) {
    return living == null || living.getType() != EntityType.ENDERMAN || living.hasEffect(enderference);
  }

  /** Checks if the given entity needs special casing for enderference */
  public static boolean needsEnderferenceOverride(@Nullable Entity entity) {
    return entity != null && entity.getType() == EntityType.ENDERMAN && entity instanceof LivingEntity living && living.hasEffect(enderference);
  }

  /** Checks if the given entity needs special casing for enderference */
  public static boolean needsEnderferenceOverride(@Nullable LivingEntity living) {
    return living != null && living.getType() == EntityType.ENDERMAN && living.hasEffect(enderference);
  }

  /**
   * Gets the level of the effect on the entity starting from 1, or 0 if not active
   * @param entity  Entity to check
   * @return  Level, or 0 if inactive
   */
  public static int getLevel(LivingEntity entity, Holder<MobEffect> effect) {
    return getAmplifier(entity, effect) + 1;
  }

  /**
   * Gets the amplifier of the effect on the entity starting from 0, or -1 if not active
   * @param entity  Entity to check
   * @return  Amplifier, or -1 if inactive
   */
  public static int getAmplifier(LivingEntity entity, Holder<MobEffect> effect) {
    MobEffectInstance instance = entity.getEffect(effect);
    if (instance != null) {
      return instance.getAmplifier();
    }
    return -1;
  }
}
