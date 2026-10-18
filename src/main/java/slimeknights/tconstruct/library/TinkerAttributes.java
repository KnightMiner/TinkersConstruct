package slimeknights.tconstruct.library;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.registration.deferred.AttributeDeferredRegister;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.config.Config;

/**
 * Handles registrations of all attributes added by tinkers
 */
public class TinkerAttributes {
  private TinkerAttributes() {}

  private static final AttributeDeferredRegister ATTRIBUTES = new AttributeDeferredRegister(TConstruct.MOD_ID);

  @Internal
  public static void init(IEventBus modBus) {
    ATTRIBUTES.register(modBus);
    modBus.addListener(EntityAttributeModificationEvent.class, TinkerAttributes::addAttributes);
    modBus.addListener(FMLCommonSetupEvent.class, TinkerAttributes::commonSetup);
  }

  // booleans
  /** If true, the entity will bounce. Used to implement slime boots */
  public static final DeferredHolder<Attribute,Attribute> BOUNCY = ATTRIBUTES.register("generic.bouncy").sync().percent(0);

  // stats
  /** Changes the speed debuff percentage when the player moves while using an item */
  public static final DeferredHolder<Attribute,Attribute> USE_ITEM_SPEED = ATTRIBUTES.register("player.use_item_speed").sync().percent(0.2f);
  /** Changes the speed debuff when the player moves while using an item */
  public static final DeferredHolder<Attribute,Attribute> PROTECTION_CAP = ATTRIBUTES.register("generic.protection_cap").sync().ranged(0.8f, 0, 0.95f);
  /** Percentage boost to critical hits for any airborne attacker, used for {@link slimeknights.tconstruct.tools.data.ModifierIds#dragonborn} */
  public static final DeferredHolder<Attribute,Attribute> CRITICAL_DAMAGE = ATTRIBUTES.register("player.critical_damage").ranged(1.5f, 0, 100);
  /** Loot bonus for {@link slimeknights.tconstruct.tools.data.ModifierIds#chrysophilite} */
  public static final DeferredHolder<Attribute,Attribute> CHRYSOPHILITE = ATTRIBUTES.register("generic.chrysophilite").ranged(0, 0, 100);

  // stat bonuses
  /** Bonus jump height in blocks */
  public static final DeferredHolder<Attribute,Attribute> JUMP_BOOST = ATTRIBUTES.register("generic.jump_boost").sync().ranged(0, 0, 100);
  /** Number of jumps the player may perform, used by the double jump modifier. */
  public static final DeferredHolder<Attribute,Attribute> JUMP_COUNT = ATTRIBUTES.register("player.jump_count").sync().ranged(1, 1, 100);

  // stat multipliers
  /** Multiplier for knockback this entity takes. Similar to {@link net.minecraft.world.entity.ai.attributes.Attributes#KNOCKBACK_RESISTANCE} but can be used to increase knockback */
  public static final DeferredHolder<Attribute,Attribute> KNOCKBACK_MULTIPLIER = ATTRIBUTES.register("generic.knockback_multiplier").sync().sentiment(Attribute.Sentiment.NEGATIVE).multiplier();
  /** Player modifier data key for mining speed multiplier as an additive percentage boost on mining speed. Used for armor haste. */
  public static final DeferredHolder<Attribute,Attribute> MINING_SPEED_MULTIPLIER = ATTRIBUTES.register("player.mining_speed_multiplier").sync().multiplier();
  /** Attribute for experience from all sources */
  public static final DeferredHolder<Attribute,Attribute> EXPERIENCE_MULTIPLIER = ATTRIBUTES.register("player.experience_multiplier").multiplier();
  /** Percentage boost to damage while crouching, used by {@link slimeknights.tconstruct.tools.data.ModifierIds#shulking} */
  public static final DeferredHolder<Attribute,Attribute> CROUCH_DAMAGE_MULTIPLIER = ATTRIBUTES.register("generic.crouch_damage_multiplier").multiplier();
  // effect durations
  /** Percentage boost to positive potion effects */
  public static final DeferredHolder<Attribute,Attribute> GOOD_EFFECT_DURATION = ATTRIBUTES.register("generic.good_effect_duration_multiplier").multiplier();
  /** Percentage boost to negative potion effects, used for {@link slimeknights.tconstruct.tools.data.ModifierIds#magicProtection} */
  public static final DeferredHolder<Attribute,Attribute> BAD_EFFECT_DURATION = ATTRIBUTES.register("generic.bad_effect_duration_multiplier").multiplier();


  /** Adds attributes to relevant entities */
  private static void addAttributes(EntityAttributeModificationEvent event) {
    // player attributes
    event.add(EntityType.PLAYER, USE_ITEM_SPEED);
    event.add(EntityType.PLAYER, CRITICAL_DAMAGE);
    event.add(EntityType.PLAYER, MINING_SPEED_MULTIPLIER);
    event.add(EntityType.PLAYER, EXPERIENCE_MULTIPLIER);
    event.add(EntityType.PLAYER, JUMP_COUNT);
    // general attributes
    addToAll(event, BOUNCY);
    addToAll(event, PROTECTION_CAP);
    addToAll(event, CHRYSOPHILITE);
    addToAll(event, JUMP_BOOST);
    addToAll(event, CROUCH_DAMAGE_MULTIPLIER);
    addToAll(event, KNOCKBACK_MULTIPLIER);
    addToAll(event, GOOD_EFFECT_DURATION);
    addToAll(event, BAD_EFFECT_DURATION);
  }

  /** Updates knockback resistance to sync subject to config */
  private static void commonSetup(FMLCommonSetupEvent event) {
    event.enqueueWork(() -> {
      // make knockback resistance syncable, as we need that info clientside
      if (Config.COMMON.syncKnockbackResistance.get()) {
        Attributes.KNOCKBACK_RESISTANCE.value().setSyncable(true);
      }
    });
  }

  /** Adds an attribute to all entities */
  private static void addToAll(EntityAttributeModificationEvent event, DeferredHolder<Attribute,Attribute> attribute, double defaultValue) {
    for (EntityType<? extends LivingEntity> entity : event.getTypes()) {
      event.add(entity, attribute, defaultValue);
    }
  }

  /** Adds an attribute to all entities */
  private static void addToAll(EntityAttributeModificationEvent event, DeferredHolder<Attribute,Attribute> attribute) {
    addToAll(event, attribute, attribute.value().getDefaultValue());
  }
}
