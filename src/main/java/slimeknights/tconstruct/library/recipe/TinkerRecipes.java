package slimeknights.tconstruct.library.recipe;

import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.registration.deferred.IngredientDeferredRegister;
import slimeknights.mantle.registration.deferred.RecipeTypeDeferredRegister;
import slimeknights.mantle.registration.holder.DeferredIngredient;
import slimeknights.mantle.registration.holder.DeferredRecipeSerializer;
import slimeknights.mantle.registration.holder.DeferredRecipeType;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.registration.CastingRecipeSerializers;
import slimeknights.tconstruct.common.registration.RecipeSerializerDeferredRegisterExtension;
import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipe;
import slimeknights.tconstruct.library.recipe.casting.ICastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.container.ContainerFillingRecipe;
import slimeknights.tconstruct.library.recipe.casting.item.CastDuplicationRecipe;
import slimeknights.tconstruct.library.recipe.casting.item.ItemCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.item.RetexturedCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.material.CompositeCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialFluidRecipe;
import slimeknights.tconstruct.library.recipe.casting.material.PartSwapCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.material.ToolCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.potion.PotionCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.potion.TipClearingCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.potion.TippingCastingRecipe;
import slimeknights.tconstruct.library.recipe.entitymelting.EntityMeltingRecipe;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuel;
import slimeknights.tconstruct.library.recipe.ingredient.InstrumentIngredient;
import slimeknights.tconstruct.library.recipe.ingredient.MaterialIngredient;
import slimeknights.tconstruct.library.recipe.ingredient.MaterialValueIngredient;
import slimeknights.tconstruct.library.recipe.ingredient.NoContainerIngredient;
import slimeknights.tconstruct.library.recipe.ingredient.ToolHookIngredient;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;
import slimeknights.tconstruct.library.recipe.melting.DamageableMeltingRecipe;
import slimeknights.tconstruct.library.recipe.melting.IMeltingRecipe;
import slimeknights.tconstruct.library.recipe.melting.MaterialMeltingRecipe;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipe;
import slimeknights.tconstruct.library.recipe.melting.OreMeltingRecipe;
import slimeknights.tconstruct.library.recipe.modifiers.severing.AgeableSeveringRecipe;
import slimeknights.tconstruct.library.recipe.modifiers.severing.SeveringRecipe;
import slimeknights.tconstruct.library.recipe.molding.MoldingRecipe;
import slimeknights.tconstruct.library.recipe.partbuilder.IPartBuilderRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.recipe.worktable.IModifierWorktableRecipe;
import slimeknights.tconstruct.tools.recipe.severing.MooshroomDemushroomingRecipe;
import slimeknights.tconstruct.tools.recipe.severing.PlayerBeheadingRecipe;
import slimeknights.tconstruct.tools.recipe.severing.SheepShearingRecipe;
import slimeknights.tconstruct.tools.recipe.severing.SnowGolemBeheadingRecipe;

/**
 * Handles registration and access for all Tinkers' Construct recipe types, recipe serializers, and ingredient types.
 */
public class TinkerRecipes {
  private static final RecipeTypeDeferredRegister TYPES = new RecipeTypeDeferredRegister(TConstruct.MOD_ID);
  private static final RecipeSerializerDeferredRegisterExtension SERIALIZERS = new RecipeSerializerDeferredRegisterExtension(TConstruct.MOD_ID);
  private static final IngredientDeferredRegister INGREDIENTS = new IngredientDeferredRegister(TConstruct.MOD_ID);

  /** Initializes the deferred registers */
  @Internal
  public static void init(IEventBus bus) {
    TYPES.register(bus);
    SERIALIZERS.register(bus);
    INGREDIENTS.register(bus);
  }

  /** Internal recipe type for recipes that are not pulled by any specific crafting block */
  public static final DeferredRecipeType<Recipe<?>> dataType = TYPES.registerType("data");

  public static final DeferredRecipeType<IPartBuilderRecipe> PART_BUILDER = TYPES.registerType("part_builder");
  public static final DeferredRecipeType<ITinkerStationRecipe> TINKER_STATION = TYPES.registerType("tinker_station");
  public static final DeferredRecipeType<IModifierWorktableRecipe> MODIFIER_WORKTABLE = TYPES.registerType("modifier_worktable");

  /*
   * materials
   */
  public static final DeferredRecipeType<MaterialRecipe> materialType = TYPES.registerType("material");
  public static final DeferredRecipeSerializer<MaterialRecipe> materialItem = SERIALIZERS.registerLoadable("material", MaterialRecipe.LOADER);
  public static final DeferredRecipeSerializer<MaterialFluidRecipe> materialFluidRecipe = SERIALIZERS.registerLoadable("material_fluid", MaterialFluidRecipe.LOADER);

  /*
   * smeltery
   */
  // fuel
  public static final DeferredRecipeType<MeltingFuel> fuelType = TYPES.registerType("fuel");
  public static final DeferredRecipeSerializer<MeltingFuel> fuel = SERIALIZERS.registerLoadable("melting_fuel", MeltingFuel.LOADER);
  // alloying
  public static final DeferredRecipeType<AlloyRecipe> alloyingType = TYPES.registerType("alloying");
  public static final DeferredRecipeSerializer<AlloyRecipe> alloying = SERIALIZERS.registerLoadable("alloy", AlloyRecipe.LOADER);

  // melting
  public static final DeferredRecipeType<IMeltingRecipe> meltingType = TYPES.registerType("melting");
  public static final DeferredRecipeSerializer<MeltingRecipe> itemMelting = SERIALIZERS.registerLoadable("melting", MeltingRecipe.LOADER);
  public static final DeferredRecipeSerializer<OreMeltingRecipe> oreMelting = SERIALIZERS.registerLoadable("ore_melting", OreMeltingRecipe.LOADER);
  public static final DeferredRecipeSerializer<DamageableMeltingRecipe> damageableMelting = SERIALIZERS.registerLoadable("damageable_melting", DamageableMeltingRecipe.LOADER);
  public static final DeferredRecipeSerializer<MaterialMeltingRecipe> materialMeltingSerializer = SERIALIZERS.registerLoadable("material_melting", MaterialMeltingRecipe.LOADER);
  // entity melting
  public static final DeferredRecipeType<EntityMeltingRecipe> entityMeltingType = TYPES.registerType("entity_melting");
  public static final DeferredRecipeSerializer<EntityMeltingRecipe> entityMelting = SERIALIZERS.registerLoadable("entity_melting", EntityMeltingRecipe.LOADER);

  // molding
  public static final DeferredRecipeType<MoldingRecipe> moldingTableType = TYPES.registerType("molding_table");
  public static final DeferredRecipeType<MoldingRecipe> moldingBasinType = TYPES.registerType("molding_basin");
  public static final CastingRecipeSerializers<MoldingRecipe> molding = new CastingRecipeSerializers<>(
    SERIALIZERS.registerLoadable("molding_table", MoldingRecipe.LOADER, moldingTableType),
    SERIALIZERS.registerLoadable("molding_basin", MoldingRecipe.LOADER, moldingBasinType)
  );

  // casting
  public static final DeferredRecipeType<ICastingRecipe> castingBasinType = TYPES.registerType("casting_basin");
  public static final DeferredRecipeType<ICastingRecipe> castingTableType = TYPES.registerType("casting_table");
  // basic
  public static final CastingRecipeSerializers<ItemCastingRecipe> itemCasting = SERIALIZERS.registerCastingPrefix("casting", ItemCastingRecipe.LOADER);
  public static final CastingRecipeSerializers<CastDuplicationRecipe> castDuplication = SERIALIZERS.registerCastingSuffix("cast_duplication", CastDuplicationRecipe.LOADER);
  public static final CastingRecipeSerializers<RetexturedCastingRecipe> retexturedCasting = SERIALIZERS.registerCastingPrefix("retextured_casting", RetexturedCastingRecipe.LOADER);
  public static final CastingRecipeSerializers<ContainerFillingRecipe> containerFilling = SERIALIZERS.registerCastingSuffix("filling", ContainerFillingRecipe.LOADER);
  // material
  public static final CastingRecipeSerializers<MaterialCastingRecipe> materialCasting = SERIALIZERS.registerCastingSuffix("material_casting", MaterialCastingRecipe.LOADER);
  public static final CastingRecipeSerializers<CompositeCastingRecipe> compositeCasting = SERIALIZERS.registerCastingSuffix("composite_casting", CompositeCastingRecipe.LOADER);
  // tool
  public static final CastingRecipeSerializers<PartSwapCastingRecipe> partSwapCasting = SERIALIZERS.registerCastingSuffix("part_swapping", PartSwapCastingRecipe.LOADER);
  public static final CastingRecipeSerializers<ToolCastingRecipe> toolCasting = SERIALIZERS.registerCastingSuffix("tool_casting", ToolCastingRecipe.LOADER);
  // potion
  public static final CastingRecipeSerializers<PotionCastingRecipe> potionCasting = SERIALIZERS.registerCastingSuffix("potion_casting", PotionCastingRecipe.LOADER);
  public static final CastingRecipeSerializers<TippingCastingRecipe> toolTipping = SERIALIZERS.registerCastingSuffix("tool_tipping", TippingCastingRecipe.LOADER);
  public static final CastingRecipeSerializers<TipClearingCastingRecipe> toolTippedClearing = SERIALIZERS.registerCastingSuffix("tool_tip_clearing", TipClearingCastingRecipe.LOADER);

  // severing
  public static final DeferredRecipeType<SeveringRecipe> severingType = TYPES.registerType("severing");
  public static final DeferredRecipeSerializer<SeveringRecipe> severing = SERIALIZERS.registerLoadable("severing", SeveringRecipe.LOADER);
  public static final DeferredRecipeSerializer<AgeableSeveringRecipe> ageableSevering = SERIALIZERS.registerLoadable("ageable_severing", AgeableSeveringRecipe.LOADER);
  // special severing
  public static final DeferredRecipeSerializer<PlayerBeheadingRecipe> playerBeheading = SERIALIZERS.registerLoadable("player_beheading", PlayerBeheadingRecipe.LOADER);
  public static final DeferredRecipeSerializer<SnowGolemBeheadingRecipe> snowGolemBeheading = SERIALIZERS.registerLoadable("snow_golem_beheading", SnowGolemBeheadingRecipe.LOADER);
  public static final DeferredRecipeSerializer<MooshroomDemushroomingRecipe> mooshroomDemushrooming = SERIALIZERS.registerLoadable("mooshroom_demushrooming", MooshroomDemushroomingRecipe.LOADER);
  public static final DeferredRecipeSerializer<SheepShearingRecipe> sheepShearing = SERIALIZERS.registerLoadable("sheep_shearing", SheepShearingRecipe.LOADER);


  // ingredients
  /** Ingredient that only matches items with no container */
  public static final DeferredIngredient<NoContainerIngredient> noContainerIngredient = INGREDIENTS.register("no_container", NoContainerIngredient.CODEC, NoContainerIngredient.STREAM_CODEC);
  /** Ingredient matching an item with a particular {@link net.minecraft.core.component.DataComponents#INSTRUMENT} */
  public static final DeferredIngredient<InstrumentIngredient> instrumentIngredient = INGREDIENTS.register("instrument", InstrumentIngredient.CODEC, InstrumentIngredient.STREAM_CODEC);
  // tools
  /** Ingredient matching tools with a particular {@link slimeknights.tconstruct.library.module.ModuleHook} present in their data. */
  public static final DeferredIngredient<ToolHookIngredient> toolHookIngredient = INGREDIENTS.register("tool_hook", ToolHookIngredient.LOADABLE);
  /** Ingredient matching a {@link slimeknights.tconstruct.library.tools.part.IMaterialItem} with the given material. */
  public static final DeferredIngredient<MaterialIngredient> materialIngredient = INGREDIENTS.register("material", MaterialIngredient.LOADABLE);
  /** Ingredient matching all items in {@link MaterialRecipe} that match a given {@link slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate} and value range. */
  public static final DeferredIngredient<MaterialValueIngredient> materialValueIngredient = INGREDIENTS.register("material_value", MaterialValueIngredient.LOADABLE);
}
