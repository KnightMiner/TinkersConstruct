package slimeknights.tconstruct.library.loot;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.registration.deferred.LootConditionDeferredRegister;
import slimeknights.mantle.registration.deferred.LootEntryDeferredRegister;
import slimeknights.mantle.registration.deferred.LootFunctionDeferredRegister;
import slimeknights.mantle.registration.holder.DeferredLootFunction;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.config.ConfigEnabledCondition;
import slimeknights.tconstruct.library.TinkerAttributes;
import slimeknights.tconstruct.library.loot.tool.AddToolDataFunction;
import slimeknights.tconstruct.library.loot.tool.BlockOrEntityCondition;
import slimeknights.tconstruct.library.loot.tool.HasModifierLootCondition;
import slimeknights.tconstruct.library.loot.tool.ModifierBonusLootFunction;
import slimeknights.tconstruct.library.loot.tool.ModifierLootModifier;
import slimeknights.tconstruct.library.loot.tool.ToolPartLootEntry;
import slimeknights.tconstruct.tools.modifiers.loot.ChrysophiliteBonusFunction;
import slimeknights.tconstruct.tools.modifiers.loot.ChrysophiliteLootCondition;

import java.util.Objects;

/** Handles any loot table related registrations */
public class TinkerLoot {
  private static final LootConditionDeferredRegister LOOT_CONDITIONS = new LootConditionDeferredRegister(TConstruct.MOD_ID);
  private static final LootFunctionDeferredRegister LOOT_FUNCTIONS = new LootFunctionDeferredRegister(TConstruct.MOD_ID);
  private static final LootEntryDeferredRegister LOOT_ENTRIES = new LootEntryDeferredRegister(TConstruct.MOD_ID);

  private TinkerLoot() {}

  /** Registers this to the bus */
  @Internal
  public static void init(IEventBus bus) {
    LOOT_CONDITIONS.register(bus);
    LOOT_FUNCTIONS.register(bus);
    LOOT_ENTRIES.register(bus);
    bus.addListener(TinkerLoot::registerCodecs);
  }

  /* Loot conditions */
  /** Condition using {@link slimeknights.tconstruct.common.config.Config}. */
  public static final DeferredHolder<LootItemConditionType,LootItemConditionType> config = LOOT_CONDITIONS.register("config", ConfigEnabledCondition.CODEC);
  /** Condition matching when the context has either a block or entity target. */
  public static final Holder<LootItemConditionType> blockOrEntityCondition = LOOT_CONDITIONS.register("block_or_entity", BlockOrEntityCondition.CODEC);
  /** Condition checking if the tool used for mining has the given modifier */
  public static final Holder<LootItemConditionType> hasModifierCondition = LOOT_CONDITIONS.register("has_modifier", HasModifierLootCondition.CODEC);
  /** Condition checking if the attacking entity has {@link TinkerAttributes#CHRYSOPHILITE} */
  @Internal
  public static final Holder<LootItemConditionType> chrysophiliteLootCondition = LOOT_CONDITIONS.register("has_chrysophilite", ChrysophiliteLootCondition.CODEC);

  /* Loot functions */
  /** Function applying loot bonuses using a modifier. Comparable to {@link ApplyBonusCount} but uses a modifier instead of enchantment. */
  public static final DeferredLootFunction<ModifierBonusLootFunction> modifierBonusFunction = LOOT_FUNCTIONS.register("modifier_bonus", ModifierBonusLootFunction.CODEC);
  /** Function applying loot bonuses {@link TinkerAttributes#CHRYSOPHILITE}. Comparable to {@link ApplyBonusCount} but uses an entity attribute. */
  @Internal
  public static final DeferredLootFunction<ChrysophiliteBonusFunction> chrysophiliteBonusFunction = LOOT_FUNCTIONS.register("chrysophilite_bonus", ChrysophiliteBonusFunction.CODEC);
  /** Loot function type that adds tool materials, damages it, and ensures the tool stats are generated. */
  public static final DeferredLootFunction<AddToolDataFunction> addToolDataFunction = LOOT_FUNCTIONS.register("add_tool_data", AddToolDataFunction.CODEC);

  /* Loot entries */
  /** Entry that adds a tool part, setting its material and randomizing it from a tag */
  public static final Holder<LootPoolEntryType> toolPartEntry = LOOT_ENTRIES.register("tool_part", ToolPartLootEntry.CODEC);

  /** Registers any relevant codecs using the event */
  private static void registerCodecs(RegisterEvent event) {
    // global loot modifiers are just map codecs, no need to create deferred holders
    if (event.getRegistryKey() == NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS) {
      Registry<MapCodec<? extends IGlobalLootModifier>> registry = Objects.requireNonNull(event.getRegistry(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS));
      Registry.register(registry, "modifier_hook", ModifierLootModifier.CODEC);
    }
  }
}
