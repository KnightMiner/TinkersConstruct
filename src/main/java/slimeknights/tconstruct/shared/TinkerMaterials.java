package slimeknights.tconstruct.shared;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.RegisterEvent;
import slimeknights.mantle.registration.object.FenceBuildingBlockObject;
import slimeknights.mantle.registration.object.MetalItemObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.library.json.predicate.material.MaterialDefinitionPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialHasPartPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialIdPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialStatTypePredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialTierPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialVariantPredicate;
import slimeknights.tconstruct.shared.block.KnightMetalBlock;
import slimeknights.tconstruct.shared.block.OrientableBlock;
import slimeknights.tconstruct.shared.block.SlimesteelBlock;
import slimeknights.tconstruct.shared.block.TaggedTooltipBlockItem;
import slimeknights.tconstruct.tools.TinkerToolParts;
import slimeknights.tconstruct.tools.data.material.MaterialIds;

import java.util.function.Consumer;
import java.util.function.Function;

import static slimeknights.tconstruct.TConstruct.getResource;

/**
 * Contains common blocks and items used in crafting materials
 */
@SuppressWarnings("unused")
public final class TinkerMaterials extends TinkerModule {
  public static final MetalItemObject cobalt, steel;
  public static final MetalItemObject slimesteel, amethystBronze, roseGold, pigIron;
  public static final MetalItemObject cinderslime, queensSlime, manyullyn, hepatizon;
  public static final MetalItemObject knightmetal, knightslime;

  static {
    Component metal = TConstruct.makeTranslation("block", "beacon_base").withStyle(ChatFormatting.GRAY);
    Component slime = TConstruct.makeTranslation("block", "beacon_base.slime").withStyle(ChatFormatting.GRAY);
    Function<Block, BlockItem> metalItem = block -> TaggedTooltipBlockItem.beaconBase(block, ITEM_PROPS, metal);
    Function<Block, BlockItem> slimeItem = block -> TaggedTooltipBlockItem.beaconBase(block, ITEM_PROPS, slime);
    // ores
    cobalt = BLOCKS.registerMetal("cobalt").block(metalBuilder(MapColor.COLOR_BLUE));
    steel = BLOCKS.registerMetal("steel").block(metalBuilder(MapColor.STONE));
    // tier 3
    slimesteel     = BLOCKS.registerMetal("slimesteel"     ).blockItem(slimeItem).block(() -> new SlimesteelBlock(metalBuilder(MapColor.WARPED_WART_BLOCK).noOcclusion()));
    amethystBronze = BLOCKS.registerMetal("amethyst_bronze").blockItem(metalItem).block(metalBuilder(MapColor.COLOR_PURPLE));
    roseGold       = BLOCKS.registerMetal("rose_gold"      ).blockItem(metalItem).block(metalBuilder(MapColor.TERRACOTTA_WHITE));
    pigIron        = BLOCKS.registerMetal("pig_iron"       ).blockItem(metalItem).block(() -> new OrientableBlock(metalBuilder(MapColor.COLOR_PINK)));
    // tier 4
    cinderslime = BLOCKS.registerMetal("cinderslime" ).blockItem(slimeItem).block(() -> new SlimesteelBlock(metalBuilder(MapColor.COLOR_ORANGE).sound(SoundType.NETHERITE_BLOCK).noOcclusion()));
    queensSlime = BLOCKS.registerMetal("queens_slime").blockItem(metalItem).block(metalBuilder(MapColor.COLOR_GREEN).sound(SoundType.NETHERITE_BLOCK));
    manyullyn   = BLOCKS.registerMetal("manyullyn"   ).blockItem(metalItem).block(metalBuilder(MapColor.COLOR_PURPLE).sound(SoundType.NETHERITE_BLOCK));
    hepatizon   = BLOCKS.registerMetal("hepatizon"   ).blockItem(metalItem).block(metalBuilder(MapColor.TERRACOTTA_BLUE).sound(SoundType.NETHERITE_BLOCK));
    // tier 4 end
    knightmetal = BLOCKS.registerMetal("knightmetal")
      .blockItem(block -> TaggedTooltipBlockItem.beaconBase(block, ITEM_PROPS, TConstruct.makeTranslation("block", "beacon_base.spiky").withStyle(ChatFormatting.GRAY)))
      .block(() -> new KnightMetalBlock(metalBuilder(MapColor.GRASS).sound(SoundType.NETHERITE_BLOCK).noOcclusion()));
    knightslime = BLOCKS.registerMetal("knightslime").blockItem(slimeItem).block(() -> new SlimesteelBlock(metalBuilder(MapColor.COLOR_BLACK).noOcclusion()));
  }

  // nuggets
  public static final DeferredItem<Item> copperNugget = ITEMS.register("copper_nugget", ITEM_PROPS);
  public static final DeferredItem<Item> netheriteNugget = ITEMS.register("netherite_nugget", ITEM_PROPS);
  public static final DeferredItem<Item> debrisNugget = ITEMS.registerFixedTooltip("debris_nugget");

  // non-metal
  public static final DeferredItem<Item> necroticBone = ITEMS.registerFixedTooltip("necrotic_bone");
  public static final DeferredItem<Item> venombone = ITEMS.register("venombone");
  public static final DeferredItem<Item> blazingBone = ITEMS.register("blazing_bone");
  public static final DeferredItem<Item> necroniumBone = ITEMS.register("necronium_bone");
  public static final FenceBuildingBlockObject nahuatl = BLOCKS.registerBuilding("nahuatl", builder(MapColor.COLOR_PURPLE, SoundType.WOOD).instrument(NoteBlockInstrument.BASS).requiresCorrectToolForDrops().strength(25f, 300f)).fence();
  public static final FenceBuildingBlockObject blazewood = BLOCKS.registerBuilding("blazewood", woodBuilder(MapColor.TERRACOTTA_RED).requiresCorrectToolForDrops().strength(25f, 300f).lightLevel(s -> 7)).fence();

  /*
   * Serializers
   */
  @SubscribeEvent
  void registerSerializers(RegisterEvent event) {
    if (event.getRegistryKey() == Registries.RECIPE_SERIALIZER) {
      MaterialPredicate.LOADER.register(getResource("variant"), MaterialVariantPredicate.LOADER);
      MaterialPredicate.LOADER.register(getResource("id"), MaterialIdPredicate.LOADER);
      MaterialPredicate.LOADER.register(getResource("has_part"), MaterialHasPartPredicate.LOADER);
      MaterialPredicate.LOADER.register(getResource("stat_type"), MaterialStatTypePredicate.LOADER);
      MaterialPredicate.LOADER.register(getResource("castable"), MaterialPredicate.CASTABLE.getLoader());
      MaterialPredicate.LOADER.register(getResource("composite"), MaterialPredicate.COMPOSITE.getLoader());
      MaterialPredicate.LOADER.register(getResource("craftable"), MaterialDefinitionPredicate.CRAFTABLE.getLoader());
      MaterialPredicate.LOADER.register(getResource("not_hidden"), MaterialDefinitionPredicate.NOT_HIDDEN.getLoader());
      MaterialPredicate.LOADER.register(getResource("registered"), MaterialDefinitionPredicate.REGISTERED.getLoader());
      MaterialPredicate.LOADER.register(getResource("tier"), MaterialTierPredicate.LOADER);
    }
  }

  /** Adds all relevant items to the creative tab, called by general tab */
  public static void addTabItems(ItemDisplayParameters itemDisplayParameters, Output output) {
    // non-metals
    // necrotic bone is in world
    output.accept(venombone);
    output.accept(blazingBone);
    acceptIfMaterial(output, necroniumBone, MaterialIds.necronium);
    accept(output, nahuatl);
    accept(output, blazewood);

    // natural ores
    output.accept(copperNugget);
    accept(output, cobalt);
    accept(output, steel);
    output.accept(debrisNugget);
    // mod alloys
    accept(output, slimesteel);
    accept(output, amethystBronze);
    accept(output, roseGold);
    accept(output, pigIron);
    accept(output, cinderslime);
    accept(output, queensSlime);
    accept(output, manyullyn);
    accept(output, hepatizon);
    output.accept(netheriteNugget);
    accept(output, knightmetal);
    accept(output, knightslime);
    // fake storage items make more sense here than tool parts
    Consumer<ItemStack> consumer = output::accept;
    TinkerToolParts.fakeIngot.get().addVariants(consumer, "");
    TinkerToolParts.fakeStorageBlockItem.get().addVariants(consumer, "");
  }

  /** Adds a metal to the tab */
  private static void accept(Output output, MetalItemObject metal) {
    output.accept(metal.getIngot());
    output.accept(metal.getNugget());
    output.accept(metal.get());
  }
}
