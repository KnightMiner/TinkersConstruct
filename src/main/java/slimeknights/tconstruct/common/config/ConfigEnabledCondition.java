package slimeknights.tconstruct.common.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.tconstruct.library.loot.TinkerLoot;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;

/** Condition that checks the Tinkers' Construct config */
public record ConfigEnabledCondition(String name, BooleanSupplier supplier) implements ICondition, LootItemCondition {
  /* Map of config names to condition cache */
  private static final Map<String,ConfigEnabledCondition> PROPERTIES = new HashMap<>();
  /** Codec mapping from string to condition instance */
  private static final Codec<ConfigEnabledCondition> BY_NAME_CODEC = Codec.stringResolver(ConfigEnabledCondition::name, PROPERTIES::get);
  /** Map codec used for condition registration */
  public static final MapCodec<ConfigEnabledCondition> CODEC = BY_NAME_CODEC.fieldOf("name");

  @Override
  public boolean test(IContext context) {
    return supplier.getAsBoolean();
  }

  @Override
  public boolean test(LootContext lootContext) {
    return supplier.getAsBoolean();
  }

  @Override
  public LootItemConditionType getType() {
    return TinkerLoot.config.value();
  }

  @Override
  public MapCodec<ConfigEnabledCondition> codec() {
    return CODEC;
  }

  @Override
  public String toString() {
    return "ConfigEnabledCondition(\"" + this.name + "\")";
  }


  /**
   * Adds a condition
   * @param prop     Property name
   * @param supplier Boolean supplier
   * @return Added condition
   */
  private static ConfigEnabledCondition add(String prop, BooleanSupplier supplier) {
    ConfigEnabledCondition conf = new ConfigEnabledCondition(prop, supplier);
    PROPERTIES.put(prop.toLowerCase(Locale.ROOT), conf);
    return conf;
  }

  /* Properties */
  public static final ConfigEnabledCondition SPAWN_WITH_BOOK = add("spawn_with_book", Config.COMMON.shouldSpawnWithTinkersBook);
  public static final ConfigEnabledCondition GRAVEL_TO_FLINT = add("gravel_to_flint", Config.COMMON.addGravelToFlintRecipe);
  public static final ConfigEnabledCondition CHEAPER_NETHERITE_ALLOY = add("cheaper_netherite_alloy", Config.COMMON.cheaperNetheriteAlloy);
  public static final ConfigEnabledCondition WITHER_BONE_DROP = add("wither_bone_drop", Config.COMMON.witherBoneDrop);
  public static final ConfigEnabledCondition SLIME_RECIPE_FIX = add("slime_recipe_fix", Config.COMMON.glassRecipeFix);
  public static final ConfigEnabledCondition GLASS_RECIPE_FIX = add("glass_recipe_fix", Config.COMMON.glassRecipeFix);
  public static final ConfigEnabledCondition ALLOW_INGOTLESS_ALLOYS = add("allow_ingotless_alloys", Config.COMMON.allowIngotlessAlloys);
  public static final ConfigEnabledCondition FORCE_INTEGRATION_MATERIALS = add("force_integration_materials", Config.COMMON.forceIntegrationMaterials);
  public static final ConfigEnabledCondition SLIMY_LOOT_CHESTS = add("slimy_loot_chests", Config.COMMON.slimyLootChests);
  public static final ConfigEnabledCondition SYNC_KNOCKBACK_RESISTANCE = add("sync_knockback_resistance", Config.COMMON.syncKnockbackResistance);
}
