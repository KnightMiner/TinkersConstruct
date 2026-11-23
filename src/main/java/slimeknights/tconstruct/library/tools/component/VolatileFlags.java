package slimeknights.tconstruct.library.tools.component;

import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.tools.IndestructibleItemEntity;

import static slimeknights.tconstruct.TConstruct.getResource;

/**
 * Contains resource locations for {@link TinkerData#VOLATILE_FLAGS} used in various tool behaviors.
 * Volatile flags do not need to be registered anywhere to be used; simply check for the flag in relevant code.
 * @see slimeknights.tconstruct.library.modifiers.hook.build.VolatileFlagModifierHook
 * @see slimeknights.tconstruct.library.tools.definition.module.build.VolatileFlagToolHook
 */
public interface VolatileFlags {
  /**
   * General flag making the dropped item invulnerable and not despawn.
   * @see IndestructibleItemEntity
   */
  ResourceLocation INDESTRUCTIBLE_ENTITY = getResource("indestructible");
  /**
   * Applies the enchantment glint to the tool as an item and as armor.
   * @see net.minecraft.world.item.Item#isFoil(ItemStack)
   */
  ResourceLocation SHINY = getResource("shiny");
  /** If set, the tool will only be able to use its right click interactions when the offhand is empty. */
  ResourceLocation DEFER_OFFHAND = getResource("defer_offhand");
  /** If set, the tool's offhand interactions will be fully disabled. */
  ResourceLocation NO_INTERACTION = getResource("no_interaction");
  /** If set, the tool will remain in the inventory after death. */
  ResourceLocation SOULBOUND = getResource("soulbound");

  // ranged
  /** Volatile flag activating the ballista functionality on {@link slimeknights.tconstruct.library.tools.item.ranged.ModifiableBowItem}, and persistent int for bow actively firing a ballista */
  ResourceLocation KEY_BALLISTA = getResource("ballista");

  // armor
  /**
   * Chestplate flag to grant elytra flying.
   * @see net.minecraft.world.item.Item#canElytraFly(ItemStack, LivingEntity)
   */
  ResourceLocation ELYTRA = getResource("elyta");
  /**
   * Armor flag to make piglins neutral.
   * @see net.minecraft.world.item.Item#makesPiglinsNeutral(ItemStack, LivingEntity)
   */
  ResourceLocation PIGLIN_NEUTRAL = getResource("piglin_neutral");
  /**
   * Boots flag to allow walking on powder snow. Cold immunity is handled through a tag.
   * @see net.minecraft.world.item.Item#canWalkOnPowderedSnow(ItemStack, LivingEntity)
   */
  ResourceLocation SNOW_BOOTS = getResource("snow_boots");
  /**
   * Helmet flag to make endermen not get angry when stared at.
   * @see net.minecraft.world.item.Item#isEnderMask(ItemStack, Player, EnderMan)
   */
  ResourceLocation ENDERMASK = getResource("endermask");

  // inventory
  /** Causes the offhand slot to show in the tool inventory */
  ResourceLocation INCLUDE_OFFHAND = getResource("inventory_show_offhand");
  /** Enables the 3x3 crafting table in the tool inventory */
  ResourceLocation CRAFTING_TABLE = getResource("crafting_table");
  /** Enables the 2x2 crafting table in the tool inventory. Only shows if {@link #CRAFTING_TABLE} is not enabled. */
  ResourceLocation INVENTORY_CRAFTING = getResource("inventory_crafting");

  /** Checks if the given flag is set on the given tool. */
  static boolean hasFlag(DataComponentHolder holder, ResourceLocation flag) {
    return SetComponent.get(holder, TinkerData.VOLATILE_FLAGS).contains(flag);
  }
}
