package slimeknights.tconstruct.fluids;

import net.minecraft.core.Registry;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import slimeknights.mantle.data.client.FluidBlockstateModelProvider;
import slimeknights.mantle.data.client.FluidBucketModelProvider;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.fluid.InvertedFluid;
import slimeknights.mantle.fluid.MantleFluids;
import slimeknights.mantle.fluid.texture.FluidTextureCameraProvider;
import slimeknights.mantle.item.ConstantFluidHandlerItem;
import slimeknights.mantle.item.EdibleItem;
import slimeknights.mantle.registration.holder.DeferredEntity;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.FlowingFluidObject;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.common.registration.FluidDeferredRegisterExtension;
import slimeknights.tconstruct.fluids.data.FluidEffectProvider;
import slimeknights.tconstruct.fluids.data.FluidTextureProvider;
import slimeknights.tconstruct.fluids.data.FluidTooltipProvider;
import slimeknights.tconstruct.fluids.entity.FluidEffectProjectile;
import slimeknights.tconstruct.fluids.fluids.SlimeFluid;
import slimeknights.tconstruct.fluids.item.BottleItem;
import slimeknights.tconstruct.fluids.item.MagmaBottleItem;
import slimeknights.tconstruct.fluids.util.EmptyBottleIntoEmpty;
import slimeknights.tconstruct.fluids.util.EmptyBottleIntoWater;
import slimeknights.tconstruct.fluids.util.FillBottle;
import slimeknights.tconstruct.fluids.util.FluidDataSerializer;
import slimeknights.tconstruct.library.TinkerEffects;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.shared.TinkerFood;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock.TankType;
import slimeknights.tconstruct.smeltery.item.CopperCanItem;
import slimeknights.tconstruct.smeltery.item.TankItem;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tools.data.material.MaterialIds;
import slimeknights.tconstruct.world.TinkerWorld;

import java.util.Map;
import java.util.Objects;

import static slimeknights.mantle.Mantle.commonResource;
import static slimeknights.mantle.block.fluid.BurningLiquidBlock.createBurning;
import static slimeknights.mantle.block.fluid.MobEffectLiquidBlock.createEffect;
import static slimeknights.mantle.fluid.MantleFluids.DISPENCE_BUCKET;

/**
 * Contains all fluids used throughout the mod
 */
@SuppressWarnings("unused")
public final class TinkerFluids extends TinkerModule {
  private static final FluidDeferredRegisterExtension FLUIDS = new FluidDeferredRegisterExtension(TConstruct.MOD_ID);

  public TinkerFluids(IEventBus bus) {
    FLUIDS.register(bus);
    NeoForgeMod.enableMilkFluid();
    MantleFluids.registerPotion(bus);
    MantleFluids.registerPowderSnow(bus);
    MantleFluids.registerHoney(bus);
    MantleFluids.registerStew(bus);
    NeoForge.EVENT_BUS.addListener(RegisterBrewingRecipesEvent.class, this::registerBrewing);
    // aliases
    ENTITIES.addAlias(TConstruct.getResource("fluid_spit"), fluidProjectile.getId());
    // unplaceable
    FLUIDS.addAliasWithType(TConstruct.getResource("potion"), MantleFluids.potion.id(), true);
    // used vanilla bucket
    FLUIDS.addAliasWithType(TConstruct.getResource("powdered_snow"), MantleFluids.powderSnow.id(), false);
    FLUIDS.addFlowingAlias(TConstruct.getResource("honey"), MantleFluids.honey.id(), true);
    FLUIDS.addFlowingAlias(TConstruct.getResource("beetroot_soup"), MantleFluids.beetrootSoup.id(), true);
    FLUIDS.addFlowingAlias(TConstruct.getResource("mushroom_stew"), MantleFluids.mushroomStew.id(), true);
    FLUIDS.addFlowingAlias(TConstruct.getResource("rabbit_stew"), MantleFluids.rabbitStew.id(), true);
  }

  /** Creative tab for general items, or those that lack another tab */
  public static final DeferredHolder<CreativeModeTab,CreativeModeTab> tabFluids = CREATIVE_TABS.register(
    "fluids", () -> CreativeModeTab.builder().title(TConstruct.makeTranslation("itemGroup", "fluids"))
                                   .icon(() -> TankItem.fillTank(TinkerSmeltery.searedTank, TankType.FUEL_GAUGE, TinkerFluids.moltenCobalt.get()))
                                   .displayItems(TinkerFluids::addFilledContainers)
                                   .withTabsBefore(TinkerTables.tabTables.getId())
                                   .withSearchBar()
                                   .build());

  // entity
  public static final DeferredEntity<FluidEffectProjectile> fluidProjectile = ENTITIES.registerNoEgg("fluid_projectile", () ->
    EntityType.Builder.<FluidEffectProjectile>of(FluidEffectProjectile::new, MobCategory.MISC)
      .sized(0.25F, 0.25F)
      .clientTrackingRange(4)
      .updateInterval(10)
      .setShouldReceiveVelocityUpdates(false));

  // basic
  public static final FlowingFluidObject<BaseFlowingFluid> venom = FLUIDS.register("venom").type(slime("venom").temperature(310)).bucket().block(createEffect(MapColor.QUARTZ, 0, () -> new MobEffectInstance(MobEffects.POISON, 5*20))).flowing();
  public static final DeferredItem<Item> venomBottle = ITEMS.register("venom_bottle", () -> new EdibleItem(UseAnim.DRINK, new Item.Properties().food(TinkerFood.VENOM_BOTTLE).stacksTo(16).craftRemainder(Items.GLASS_BOTTLE)));

  // slime -  note second name parameter is forge tag name
  public static final FlowingFluidObject<SlimeFluid> earthSlime = FLUIDS.registerSlime("earth_slime", slime("earth_slime").temperature(350)).bucket().block(createEffect(MapColor.GRASS, 0, () -> new MobEffectInstance(TinkerEffects.bouncy, 5*20))).commonTag("slime").flowing(SlimeFluid.Source::new, SlimeFluid.Flowing::new);
  public static final FlowingFluidObject<SlimeFluid> skySlime   = FLUIDS.registerSlime("sky_slime",   slime("sky_slime"  ).temperature(310)).bucket().block(createEffect(MapColor.DIAMOND, 0, () -> new MobEffectInstance(TinkerEffects.ricochet, 5*20))).flowing(SlimeFluid.Source::new, SlimeFluid.Flowing::new);
  public static final FlowingFluidObject<SlimeFluid> enderSlime = FLUIDS.registerSlime("ender_slime", slime("ender_slime").temperature(370)).bucket().block(createEffect(MapColor.COLOR_PURPLE, 0, () -> new MobEffectInstance(TinkerEffects.enderference, 5 * 20))).flowing(SlimeFluid.Source::new, SlimeFluid.Flowing::new);
  public static final FlowingFluidObject<SlimeFluid> magma      = FLUIDS.registerSlime("magma",       slime("magma").temperature(600).lightLevel(3)).bucket().commonTag().block(createBurning(MapColor.NETHER, 3, 8, 3f)).flowing(SlimeFluid.Source::new, SlimeFluid.Flowing::new);
  public static final FlowingFluidObject<InvertedFluid> ichor   = FLUIDS.registerSlime("ichor",       slime("ichor").temperature(1000).density(-1600)).bucket().block(MapColor.COLOR_ORANGE, 0).invertedFlowing();
  public static final EnumObject<SlimeType, Fluid> slime = EnumObject.builder(SlimeType.class, (FluidObject<?> fluid) -> fluid.getHolder())
    .put(SlimeType.EARTH, earthSlime)
    .put(SlimeType.SKY, skySlime)
    .put(SlimeType.ENDER, enderSlime)
    .put(SlimeType.ICHOR, ichor)
    .build();
  // bottles of slime
  public static final EnumObject<SlimeType, Item> slimeBottle = ITEMS.registerEnum(SlimeType.values(), "slime_bottle", type -> new EdibleItem(UseAnim.DRINK, new Item.Properties().food(TinkerFood.getBottle(type)).stacksTo(16).craftRemainder(Items.GLASS_BOTTLE)));
  public static final DeferredItem<Item> magmaBottle = ITEMS.register("magma_bottle", () -> new MagmaBottleItem(new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE), 15));

  // foods
  public static FlowingFluidObject<BaseFlowingFluid> meatSoup = FLUIDS.register("meat_soup").type(cool("meat_soup").temperature(400)).bucket().block(MapColor.CRIMSON_NYLIUM, 0).flowing();
  public static final DeferredItem<Item> meatSoupBowl = ITEMS.register("meat_soup", () -> new EdibleItem(UseAnim.DRINK, new Item.Properties().food(TinkerFood.MEAT_SOUP).stacksTo(1).craftRemainder(Items.BOWL)));

  // potion
  public static final DeferredItem<Item> splashBottle = ITEMS.register("splash_bottle", () -> new BottleItem(Items.SPLASH_POTION, ITEM_PROPS));
  public static final DeferredItem<Item> lingeringBottle = ITEMS.register("lingering_bottle", () -> new BottleItem(Items.LINGERING_POTION, ITEM_PROPS));

  // base molten fluids
  public static final FlowingFluidObject<BaseFlowingFluid> searedStone   = FLUIDS.registerStone("seared_stone").type(hot("seared_stone").temperature(900).lightLevel(6)).block(createBurning(MapColor.DEEPSLATE, 6, 8, 2f)).bucket().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> scorchedStone = FLUIDS.registerStone("scorched_stone").type(hot("scorched_stone").temperature(800).lightLevel(4)).block(createBurning(MapColor.TERRACOTTA_BROWN, 4, 7, 2f)).bucket().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenClay    = FLUIDS.registerStone("molten_clay").type(hot("molten_clay").temperature(750).lightLevel(3)).block(createBurning(MapColor.COLOR_ORANGE, 3, 5, 2f)).bucket().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenGlass   = FLUIDS.registerGlass("molten_glass").type(hot("molten_glass").temperature(1050).lightLevel(1)).block(createBurning(MapColor.ICE, 1, 5, 2f)).bucket().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> liquidSoul    = FLUIDS.registerGlass("liquid_soul").type(hot("liquid_soul").temperature(700).lightLevel(2)).block(createEffect(MapColor.COLOR_BROWN, 2, () -> new MobEffectInstance(MobEffects.BLINDNESS, 5 * 20))).bucket().flowing();
  // ceramics compat
  public static final FlowingFluidObject<BaseFlowingFluid> moltenPorcelain = FLUIDS.registerStone("molten_porcelain").type(hot("molten_porcelain").temperature(1000).lightLevel(2)).block(createBurning(MapColor.QUARTZ, 2, 5, 2f)).bucket().flowing();
  // fancy molten fluids
  public static final FlowingFluidObject<BaseFlowingFluid> moltenObsidian = FLUIDS.registerStone("molten_obsidian").type(hot("molten_obsidian").temperature(1300).lightLevel(3)).block(createBurning(MapColor.COLOR_BLACK, 3, 12, 4f)).bucket().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenEnder    = FLUIDS.registerStone("molten_ender").type(hot("molten_ender").temperature(777).lightLevel(5)).block(createEffect(MapColor.PLANT, 5, () -> new MobEffectInstance(TinkerEffects.enderference, 5 * 20))).bucket().commonTag("ender").flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> blazingBlood   = FLUIDS.register("blazing_blood").type(hot("blazing_blood").temperature(1800).lightLevel(15).density(3500)).block(createBurning(MapColor.COLOR_ORANGE, 15, 15, 5f)).bucket().flowing();

  // ores
  public static final FlowingFluidObject<BaseFlowingFluid> moltenEmerald  = FLUIDS.registerGem("molten_emerald").type(hot("molten_emerald").temperature(1234).lightLevel(9)).block(createBurning(MapColor.EMERALD, 9, 10, 6f)).bucket().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenQuartz   = FLUIDS.registerGem("molten_quartz").type(hot("molten_quartz").temperature(937).lightLevel(6)).block(createBurning(MapColor.QUARTZ, 6, 10, 5f)).bucket().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenAmethyst = FLUIDS.registerGem("molten_amethyst").type(hot("molten_amethyst").temperature(1250).lightLevel(11)).block(createBurning(MapColor.COLOR_PURPLE, 11, 10, 5f)).bucket().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenDiamond  = FLUIDS.registerGem("molten_diamond").type(hot("molten_diamond").temperature(1750).lightLevel(13)).block(createBurning(MapColor.DIAMOND, 13, 10, 7f)).bucket().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenDebris   = FLUIDS.registerGem("molten_debris").type(hot("molten_debris").temperature(1475).lightLevel(14)).block(createBurning(MapColor.COLOR_BLACK, 14, 10, 8f)).bucket().flowing();
  // metal ores
  public static final FlowingFluidObject<BaseFlowingFluid> moltenIron   = FLUIDS.registerMetal("molten_iron").type(hot("molten_iron").temperature(1100).lightLevel(12)).block(createBurning(MapColor.RAW_IRON, 12, 10, 5f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenGold   = FLUIDS.registerMetal("molten_gold").type(hot("molten_gold").temperature(1000).lightLevel(12)).block(createBurning(MapColor.GOLD, 12, 10, 5f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenCopper = FLUIDS.registerMetal("molten_copper").type(hot("molten_copper").temperature(800).lightLevel(12)).block(createBurning(MapColor.COLOR_ORANGE, 12, 10, 5f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenCobalt = FLUIDS.registerMetal("molten_cobalt").type(hot("molten_cobalt").temperature(1250).lightLevel(8)).block(createBurning(MapColor.WATER, 8, 10, 6f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenSteel  = FLUIDS.registerMetal("molten_steel").type(hot("molten_steel").temperature(1250).lightLevel(13)).block(createBurning(MapColor.STONE, 13, 10, 6f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenKnightmetal = FLUIDS.registerMetal("molten_knightmetal").type(hot("molten_knightmetal").temperature(1600).lightLevel(10)).block(createBurning(MapColor.GRASS, 10, 10, 8f)).bucket().flowing();
  // alloys
  public static final FlowingFluidObject<BaseFlowingFluid> moltenSlimesteel     = FLUIDS.registerMetal("molten_slimesteel").type(hot("molten_slimesteel").temperature(1200).lightLevel(10)).block(createBurning(MapColor.DIAMOND, 10, 10, 6f)).bucket().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenAmethystBronze = FLUIDS.registerMetal("molten_amethyst_bronze").type(hot("molten_amethyst_bronze").temperature(1120).lightLevel(12)).block(createBurning(MapColor.COLOR_MAGENTA, 12, 10, 6f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenRoseGold       = FLUIDS.registerMetal("molten_rose_gold").type(hot("molten_rose_gold").temperature(850).lightLevel(12)).block(createBurning(MapColor.COLOR_PINK, 12, 10, 6f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenPigIron        = FLUIDS.registerMetal("molten_pig_iron").type(hot("molten_pig_iron").temperature(1111).lightLevel(10)).block(createBurning(MapColor.TERRACOTTA_WHITE, 10, 10, 6f)).bucket().flowing();

  public static final FlowingFluidObject<BaseFlowingFluid> moltenManyullyn   = FLUIDS.registerMetal("molten_manyullyn").type(hot("molten_manyullyn").temperature(1500).lightLevel(11)).block(createBurning(MapColor.COLOR_PURPLE, 11, 10, 8f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenHepatizon   = FLUIDS.registerMetal("molten_hepatizon").type(hot("molten_hepatizon").temperature(1700).lightLevel(8)).block(createBurning(MapColor.TERRACOTTA_BLUE, 8, 10, 7f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<InvertedFluid>    moltenCinderslime = FLUIDS.registerMetal("molten_cinderslime").type(hot("molten_cinderslime").temperature(1350).lightLevel(10).density(-2000)).burningBlock(MapColor.COLOR_RED, 10, 10, 7f).bucket().invertedFlowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenQueensSlime = FLUIDS.registerMetal("molten_queens_slime").type(hot("molten_queens_slime").temperature(1450).lightLevel(9)).block(createBurning(MapColor.COLOR_GREEN, 9, 10, 6f)).bucket().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenNetherite   = FLUIDS.registerMetal("molten_netherite").type(hot("molten_netherite").temperature(1550).lightLevel(14)).block(createBurning(MapColor.COLOR_BLACK, 14, 10, 10f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenKnightslime = FLUIDS.registerMetal("molten_knightslime").type(hot("molten_knightslime").temperature(1425).lightLevel(12)).block(createBurning(MapColor.COLOR_MAGENTA, 12, 10, 8f)).bucket().flowing();

  // compat ores
  public static final FlowingFluidObject<BaseFlowingFluid> moltenTin      = FLUIDS.registerMetal("molten_tin").type(hot("molten_tin").temperature(525).lightLevel(12)).block(createBurning(MapColor.COLOR_CYAN, 12, 10, 5f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenAluminum = FLUIDS.registerMetal("molten_aluminum").type(hot("molten_aluminum").temperature(725).lightLevel(12)).block(createBurning(MapColor.METAL, 12, 10, 5f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenLead     = FLUIDS.registerMetal("molten_lead").type(hot("molten_lead").temperature(630).lightLevel(12)).block(createBurning(MapColor.TERRACOTTA_BLUE, 12, 10, 5f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenSilver   = FLUIDS.registerMetal("molten_silver").type(hot("molten_silver").temperature(1090).lightLevel(12)).block(createBurning(MapColor.METAL, 12, 10, 5f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenNickel   = FLUIDS.registerMetal("molten_nickel").type(hot("molten_nickel").temperature(1250).lightLevel(12)).block(createBurning(MapColor.WOOD, 12, 10, 5f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenZinc     = FLUIDS.registerMetal("molten_zinc").type(hot("molten_zinc").temperature(720).lightLevel(12)).block(createBurning(MapColor.TERRACOTTA_CYAN, 12, 10, 5f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenPlatinum = FLUIDS.registerMetal("molten_platinum").type(hot("molten_platinum").temperature(1270).lightLevel(12)).block(createBurning(MapColor.DIAMOND, 12, 10, 5f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenTungsten = FLUIDS.registerMetal("molten_tungsten").type(hot("molten_tungsten").temperature(1250).lightLevel(12)).block(createBurning(MapColor.TERRACOTTA_BLACK, 12, 10, 5f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenOsmium   = FLUIDS.registerMetal("molten_osmium").type(hot("molten_osmium").temperature(1275).lightLevel(4)).block(createBurning(MapColor.CLAY, 4, 10, 5f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenUranium  = FLUIDS.registerMetal("molten_uranium").type(hot("molten_uranium").temperature(1130).lightLevel(15)).block(createBurning(MapColor.TERRACOTTA_GREEN, 15, 10, 5f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenChromium = FLUIDS.registerMetal("molten_chromium").type(hot("molten_chromium").temperature(1200).lightLevel(13)).block(createBurning(MapColor.COLOR_CYAN, 13, 10, 5f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenCadmium  = FLUIDS.registerMetal("molten_cadmium").type(hot("molten_cadmium").temperature(594).lightLevel(10)).block(createBurning(MapColor.COLOR_BROWN, 10, 10, 5f)).bucket().commonTag().flowing();

  // compat alloys
  public static final FlowingFluidObject<BaseFlowingFluid> moltenBronze     = FLUIDS.registerMetal("molten_bronze").type(hot("molten_bronze").temperature(1000).lightLevel(10)).block(createBurning(MapColor.TERRACOTTA_ORANGE, 10, 10, 6f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenBrass      = FLUIDS.registerMetal("molten_brass").type(hot("molten_brass").temperature(905).lightLevel(10)).block(createBurning(MapColor.TERRACOTTA_YELLOW, 10, 10, 6f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenElectrum   = FLUIDS.registerMetal("molten_electrum").type(hot("molten_electrum").temperature(1060).lightLevel(10)).block(createBurning(MapColor.GOLD, 10, 10, 6f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenInvar      = FLUIDS.registerMetal("molten_invar").type(hot("molten_invar").temperature(1200).lightLevel(10)).block(createBurning(MapColor.GLOW_LICHEN, 10, 10, 6f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenConstantan = FLUIDS.registerMetal("molten_constantan").type(hot("molten_constantan").temperature(1220).lightLevel(10)).block(createBurning(MapColor.TERRACOTTA_RED, 10, 10, 6f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenPewter     = FLUIDS.registerMetal("molten_pewter").type(hot("molten_pewter").temperature(700).lightLevel(10)).block(createBurning(MapColor.COLOR_GRAY, 10, 10, 6f)).bucket().commonTag().flowing();

  // mod-specific compat
  // thermal
  public static final FlowingFluidObject<BaseFlowingFluid> moltenEnderium = FLUIDS.registerMetal("molten_enderium").type(hot("molten_enderium").temperature(1650).lightLevel(12)).block(createBurning(MapColor.COLOR_CYAN, 12, 10, 7f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenLumium   = FLUIDS.registerMetal("molten_lumium").type(hot("molten_lumium").temperature(1350).lightLevel(15)).block(createBurning(MapColor.GOLD, 15, 10, 7f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenSignalum = FLUIDS.registerMetal("molten_signalum").type(hot("molten_signalum").temperature(1299).lightLevel(13)).block(createBurning(MapColor.FIRE, 13, 10, 7f)).bucket().commonTag().flowing();
  // mekanism
  public static final FlowingFluidObject<BaseFlowingFluid> moltenRefinedGlowstone = FLUIDS.registerMetal("molten_refined_glowstone").type(hot("molten_refined_glowstone").temperature(1125).lightLevel(15)).block(createBurning(MapColor.COLOR_YELLOW, 15, 10, 7f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenRefinedObsidian  = FLUIDS.registerMetal("molten_refined_obsidian").type(hot("molten_refined_obsidian").temperature(1775).lightLevel(7)).block(createBurning(MapColor.TERRACOTTA_BLUE, 7, 10, 7f)).bucket().commonTag().flowing();
  // cosmere metals
  public static final FlowingFluidObject<BaseFlowingFluid> moltenNicrosil = FLUIDS.registerMetal("molten_nicrosil").type(hot("molten_nicrosil").temperature(1400).lightLevel(14)).block(createBurning(MapColor.SNOW, 12, 10, 6f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenDuralumin = FLUIDS.registerMetal("molten_duralumin").type(hot("molten_duralumin").temperature(925).lightLevel(10)).block(createBurning(MapColor.COLOR_LIGHT_GREEN, 10, 10, 6f)).bucket().commonTag().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> moltenBendalloy = FLUIDS.registerMetal("molten_bendalloy").type(hot("molten_bendalloy").temperature(400).lightLevel(9)).block(createBurning(MapColor.SNOW, 9, 10, 6f)).bucket().commonTag().flowing();
  // twilight
  public static final FlowingFluidObject<BaseFlowingFluid> moltenSteeleaf = FLUIDS.registerMetal("molten_steeleaf").type(hot("molten_steeleaf").temperature(1234).lightLevel(10)).block(createBurning(MapColor.COLOR_GREEN, 10, 10, 6f)).bucket().flowing();
  public static final FlowingFluidObject<BaseFlowingFluid> fieryLiquid = FLUIDS.register("fiery_liquid").type(hot("fiery_liquid").temperature(1800).lightLevel(15)).block(createBurning(MapColor.CRIMSON_HYPHAE, 15, 20, 6f)).tickRate(30).bucket().flowing();

  /** Creates a builder for a cool fluid with sounds */
  private static FluidType.Properties cool() {
    return FluidType.Properties.create()
                               .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                               .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                               .motionScale(0.0023333333333333335D)
                               .canExtinguish(true);

  }

  /** Creates a builder for a cool fluid with sounds and description */
  private static FluidType.Properties cool(String name) {
    return cool().descriptionId(TConstruct.makeDescriptionId("fluid", name))
                 .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                 .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY);
  }

  /** Creates a builder for a cool fluid with sounds and description */
  private static FluidType.Properties slime(String name) {
    return cool(name).density(1600).viscosity(1600);
  }

  /** Creates a builder for a cool fluid with sounds and description */
  @SuppressWarnings("SameParameterValue")
  private static FluidType.Properties powder(String name) {
    return FluidType.Properties.create().descriptionId(TConstruct.makeDescriptionId("fluid", name))
                               .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_POWDER_SNOW)
                               .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_POWDER_SNOW);
  }

  /** Creates a builder for a hot with sounds and description */
  private static FluidType.Properties hot(String name) {
    return FluidType.Properties.create().density(2000).viscosity(10000).temperature(1000)
      .descriptionId(TConstruct.makeDescriptionId("fluid", name))
      .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
      .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
      // from forge lava type
      .motionScale(0.0023333333333333335D)
      .canSwim(false).canDrown(false)
      .pathType(PathType.LAVA).adjacentPathType(null);
  }

  @SubscribeEvent
  void registerAdditional(RegisterEvent event) {
    if (event.getRegistryKey() == NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS) {
      Registry<EntityDataSerializer<?>> registry = Objects.requireNonNull(event.getRegistry(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS));
      Registry.register(registry, TConstruct.getResource("fluid"), FluidDataSerializer.INSTANCE);
    }
  }

  @SubscribeEvent
  void gatherData(final GatherDataEvent event) {
    DataGenerator generator = event.getGenerator();
    PackOutput packOutput = generator.getPackOutput();
    generator.addProvider(event.includeServer(), new FluidEffectProvider(packOutput, event.getLookupProvider()));
    boolean client = event.includeClient();
    generator.addProvider(client, new FluidTooltipProvider(packOutput));
    FluidTextureProvider textureProvider = new FluidTextureProvider(packOutput);
    generator.addProvider(client, textureProvider);
    generator.addProvider(client, new FluidTextureCameraProvider(packOutput, event.getExistingFileHelper(), textureProvider));
    generator.addProvider(client, new FluidBucketModelProvider(packOutput, TConstruct.MOD_ID));
    generator.addProvider(client, new FluidBlockstateModelProvider(packOutput, TConstruct.MOD_ID));
  }

  @SubscribeEvent
  void registerCapabilities(RegisterCapabilitiesEvent event) {
    slimeBottle.forEach((type, item) -> {
      event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) -> new ConstantFluidHandlerItem(new FluidStack(slime.get(type), FluidValues.BOTTLE), stack), item);
    });
    event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) -> new ConstantFluidHandlerItem(new FluidStack(magma.get(), FluidValues.BOTTLE), stack), magmaBottle);
    event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) -> new ConstantFluidHandlerItem(new FluidStack(venom.get(), FluidValues.BOTTLE), stack), venomBottle);
  }

  @SubscribeEvent
  void commonSetup(final FMLCommonSetupEvent event) {
    event.enqueueWork(() -> {
      Map<Item,CauldronInteraction> water = CauldronInteraction.WATER.map();
      water.put(splashBottle.get(), new FillBottle(Items.SPLASH_POTION));
      water.put(lingeringBottle.get(), new FillBottle(Items.LINGERING_POTION));
      water.put(Items.SPLASH_POTION,    new EmptyBottleIntoWater(splashBottle,    water.get(Items.SPLASH_POTION)));
      water.put(Items.LINGERING_POTION, new EmptyBottleIntoWater(lingeringBottle, water.get(Items.LINGERING_POTION)));
      Map<Item,CauldronInteraction> empty = CauldronInteraction.EMPTY.map();
      empty.put(Items.SPLASH_POTION,    new EmptyBottleIntoEmpty(splashBottle,    empty.get(Items.SPLASH_POTION)));
      empty.put(Items.LINGERING_POTION, new EmptyBottleIntoEmpty(lingeringBottle, empty.get(Items.LINGERING_POTION)));
    });

    // dispense buckets
    event.enqueueWork(() -> {
      // slime
      DispenserBlock.registerBehavior(venom, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(earthSlime, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(skySlime, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(enderSlime, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(magma, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(meatSoup, DISPENCE_BUCKET);
      // base molten fluids
      DispenserBlock.registerBehavior(searedStone, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(scorchedStone, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenClay, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenGlass, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(liquidSoul, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenPorcelain, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenObsidian, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenEnder, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(blazingBlood, DISPENCE_BUCKET);
      // ores
      DispenserBlock.registerBehavior(moltenEmerald, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenQuartz, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenAmethyst, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenDiamond, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenDebris, DISPENCE_BUCKET);
      // metal ores
      DispenserBlock.registerBehavior(moltenIron, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenGold, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenCopper, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenCobalt, DISPENCE_BUCKET);
      // alloys
      DispenserBlock.registerBehavior(moltenSlimesteel, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenAmethystBronze, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenRoseGold, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenPigIron, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenManyullyn, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenHepatizon, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenQueensSlime, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenNetherite, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenKnightslime, DISPENCE_BUCKET);
      // compat ores
      DispenserBlock.registerBehavior(moltenTin, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenAluminum, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenLead, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenSilver, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenNickel, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenZinc, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenPlatinum, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenTungsten, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenOsmium, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenUranium, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenChromium, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenCadmium, DISPENCE_BUCKET);
      // compat alloys
      DispenserBlock.registerBehavior(moltenBronze, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenBrass, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenElectrum, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenInvar, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenConstantan, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenPewter, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenSteel, DISPENCE_BUCKET);
      // mod-specific compat alloys
      DispenserBlock.registerBehavior(moltenEnderium, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenLumium, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenSignalum, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenRefinedGlowstone, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenRefinedObsidian, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenNicrosil, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenDuralumin, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenBendalloy, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(moltenSteeleaf, DISPENCE_BUCKET);
      DispenserBlock.registerBehavior(fieryLiquid, DISPENCE_BUCKET);
    });
  }

  // registered with the neoforge bus
  private void registerBrewing(RegisterBrewingRecipesEvent event) {
    PotionBrewing.Builder builder = event.getBuilder();

    // brew bottles into each other, a bit weird but feels better than shapeless
    // TODO: can we softcode this somehow for the sake of people that change brewing recipes? no longer have access to the recipes to copy standard bottles
    builder.addRecipe(Ingredient.of(Items.GLASS_BOTTLE), Ingredient.of(Items.GUNPOWDER), splashBottle.toStack());
    builder.addRecipe(Ingredient.of(MantleTags.Items.SPLASH_BOTTLE), Ingredient.of(Items.DRAGON_BREATH), lingeringBottle.toStack());

    // brew congealed slime into bottles to get slime bottles, easy melting
    for (SlimeType slime : SlimeType.values()) {
      builder.addRecipe(new BrewingRecipe(Ingredient.of(Items.GLASS_BOTTLE), Ingredient.of(TinkerWorld.congealedSlime.get(slime)), new ItemStack(TinkerFluids.slimeBottle.get(slime))));
    }
    builder.addRecipe(new BrewingRecipe(Ingredient.of(Items.GLASS_BOTTLE), Ingredient.of(Blocks.MAGMA_BLOCK), TinkerFluids.magmaBottle.toStack()));
  }

  /** Adds all relevant items to the creative tab, called by smeltery */
  public static void addTabItems(ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output) {
    // containers
    output.accept(splashBottle);
    output.accept(lingeringBottle);
    // slime
    output.accept(earthSlime);
    output.accept(skySlime);
    output.accept(ichor);
    output.accept(enderSlime);
    accept(output, slimeBottle);
    output.accept(magma);
    output.accept(magmaBottle);
    output.accept(venom);
    output.accept(venomBottle);

    // food
    output.accept(meatSoup);
    output.accept(meatSoupBowl);

    // stone
    output.accept(searedStone);
    output.accept(scorchedStone);
    output.accept(moltenClay);
    if (ModList.get().isLoaded("ceramics")) {
      output.accept(moltenPorcelain);
    }
    output.accept(moltenGlass);
    output.accept(moltenObsidian);
    output.accept(liquidSoul);
    output.accept(moltenEnder);
    output.accept(blazingBlood);

    // ores
    output.accept(moltenEmerald);
    output.accept(moltenQuartz);
    output.accept(moltenAmethyst);
    output.accept(moltenDiamond);
    output.accept(moltenDebris);
    // metal ores
    output.accept(moltenCopper);
    output.accept(moltenIron);
    output.accept(moltenGold);
    output.accept(moltenCobalt);
    output.accept(moltenSteel);

    // overworld alloys
    output.accept(moltenSlimesteel);
    output.accept(moltenAmethystBronze);
    output.accept(moltenRoseGold);
    output.accept(moltenPigIron);
    // nether alloys
    output.accept(moltenCinderslime);
    output.accept(moltenQueensSlime);
    output.accept(moltenManyullyn);
    output.accept(moltenHepatizon);
    output.accept(moltenNetherite);
    output.accept(moltenKnightmetal);
    output.accept(moltenKnightslime);

    // compat ores
    acceptMolten(output, moltenTin);
    acceptCompat(output, moltenAluminum, MaterialIds.aluminum);
    acceptCompat(output, moltenLead, MaterialIds.lead);
    acceptCompat(output, moltenSilver, MaterialIds.silver);
    acceptMolten(output, moltenNickel);
    acceptMolten(output, moltenZinc);
    acceptMolten(output, moltenPlatinum);
    acceptMolten(output, moltenTungsten);
    acceptCompat(output, moltenOsmium, MaterialIds.osmium);
    acceptMolten(output, moltenUranium, MaterialIds.necronium);
    acceptMolten(output, moltenChromium);
    acceptMolten(output, moltenCadmium);
    // compat alloys
    acceptCompat(output, moltenBronze, MaterialIds.bronze);
    acceptMolten(output, moltenBrass, MaterialIds.platedSlimewood);
    acceptCompat(output, moltenElectrum, MaterialIds.electrum);
    acceptCompat(output, moltenInvar, MaterialIds.invar);
    acceptCompat(output, moltenConstantan, MaterialIds.constantan);
    acceptCompat(output, moltenPewter, MaterialIds.pewter);
    acceptMolten(output, moltenNicrosil, MaterialIds.nicrosil);
    acceptMolten(output, moltenEnderium);
    acceptMolten(output, moltenLumium);
    acceptMolten(output, moltenSignalum);
    acceptMolten(output, moltenRefinedGlowstone);
    acceptMolten(output, moltenRefinedObsidian);
    acceptMolten(output, moltenDuralumin);
    acceptMolten(output, moltenBendalloy);
    acceptCompat(output, moltenSteeleaf, MaterialIds.steeleaf);
    acceptCompat(output, fieryLiquid, "fiery", MaterialIds.fiery);
  }

  /** Adds all filled containers to the fluids tab. */
  private static void addFilledContainers(ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output) {
    // add copper cans, tanks, and lanterns for all the fluids
    CopperCanItem.addFilledVariants(output::accept);
    TankItem.addFilledVariants(output::accept);
  }

  /**
   * Accepts the given item if the passed ingot is present
   */
  private static void acceptCompat(Output output, ItemLike item, String ingot) {
    acceptIfTag(output, item, ItemTags.create(commonResource("ingots/" + ingot)));
  }

  /** Accepts the given item if the passed ingot or material is present */
  private static void acceptCompat(CreativeModeTab.Output output, ItemLike item, String ingot, MaterialId material) {
    if (!acceptIfMaterial(output, item, material)) {
      acceptCompat(output, item, ingot);
    }
  }

  /** Accepts the given item if the passed material or same named ingot is present */
  private static void acceptCompat(CreativeModeTab.Output output, ItemLike item, MaterialId material) {
    acceptCompat(output, item, material.getPath(), material);
  }

  /** Accepts the given item if the ingot named after the fluid is present */
  private static void acceptMolten(CreativeModeTab.Output output, FluidObject<?> fluid) {
    acceptCompat(output, fluid, withoutMolten(fluid));
  }

  /** Accepts the given item if the ingot named after the fluid or the material is present */
  private static void acceptMolten(CreativeModeTab.Output output, FluidObject<?> fluid, MaterialId material) {
    acceptCompat(output, fluid, withoutMolten(fluid), material);
  }

  /** Length of the molten prefix */
  private static final int MOLTEN_LENGTH = "molten_".length();

  /** Removes the "molten_" prefix from the fluids ID */
  public static String withoutMolten(FluidObject<?> fluid) {
    return fluid.id().getPath().substring(MOLTEN_LENGTH);
  }
}
