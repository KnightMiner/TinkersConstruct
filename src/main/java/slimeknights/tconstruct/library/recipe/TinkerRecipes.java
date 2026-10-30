package slimeknights.tconstruct.library.recipe;

import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.registration.deferred.IngredientDeferredRegister;
import slimeknights.mantle.registration.deferred.RecipeTypeDeferredRegister;
import slimeknights.mantle.registration.holder.DeferredIngredient;
import slimeknights.mantle.registration.holder.DeferredRecipeType;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.registration.CastingRecipeSerializers;
import slimeknights.tconstruct.common.registration.RecipeSerializerDeferredRegisterExtension;
import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipe;
import slimeknights.tconstruct.library.recipe.casting.ICastingRecipe;
import slimeknights.tconstruct.library.recipe.entitymelting.EntityMeltingRecipe;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuel;
import slimeknights.tconstruct.library.recipe.ingredient.InstrumentIngredient;
import slimeknights.tconstruct.library.recipe.ingredient.MaterialIngredient;
import slimeknights.tconstruct.library.recipe.ingredient.MaterialValueIngredient;
import slimeknights.tconstruct.library.recipe.ingredient.NoContainerIngredient;
import slimeknights.tconstruct.library.recipe.ingredient.ToolHookIngredient;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;
import slimeknights.tconstruct.library.recipe.melting.IMeltingRecipe;
import slimeknights.tconstruct.library.recipe.modifiers.severing.SeveringRecipe;
import slimeknights.tconstruct.library.recipe.molding.MoldingRecipe;
import slimeknights.tconstruct.library.recipe.partbuilder.IPartBuilderRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.recipe.worktable.IModifierWorktableRecipe;

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

  public static final DeferredRecipeType<IPartBuilderRecipe> PART_BUILDER = TYPES.registerType("part_builder");
  public static final DeferredRecipeType<MaterialRecipe> MATERIAL = TYPES.registerType("material");
  public static final DeferredRecipeType<ITinkerStationRecipe> TINKER_STATION = TYPES.registerType("tinker_station");
  public static final DeferredRecipeType<IModifierWorktableRecipe> MODIFIER_WORKTABLE = TYPES.registerType("modifier_worktable");

  /*
   * smeltery
   */
  // casting
  public static final DeferredRecipeType<ICastingRecipe> CASTING_BASIN = TYPES.registerType("casting_basin");
  public static final DeferredRecipeType<ICastingRecipe> CASTING_TABLE = TYPES.registerType("casting_table");
  // smeltery
  public static final DeferredRecipeType<IMeltingRecipe> MELTING = TYPES.registerType("melting");
  public static final DeferredRecipeType<EntityMeltingRecipe> ENTITY_MELTING = TYPES.registerType("entity_melting");
  public static final DeferredRecipeType<MeltingFuel> FUEL = TYPES.registerType("fuel");
  public static final DeferredRecipeType<AlloyRecipe> ALLOYING = TYPES.registerType("alloying");
  // molding
  public static final DeferredRecipeType<MoldingRecipe> moldingTableType = TYPES.registerType("molding_table");
  public static final DeferredRecipeType<MoldingRecipe> moldingBasinType = TYPES.registerType("molding_basin");
  public static final CastingRecipeSerializers<MoldingRecipe> molding = new CastingRecipeSerializers<>(
    SERIALIZERS.registerLoadable("molding_table", MoldingRecipe.LOADER, moldingTableType),
    SERIALIZERS.registerLoadable("molding_basin", MoldingRecipe.LOADER, moldingBasinType)
  );

  // modifiers
  public static final DeferredRecipeType<SeveringRecipe> SEVERING = TYPES.registerType("severing");

  /** Internal recipe type for recipes that are not pulled by any specific crafting block */
  public static final DeferredRecipeType<Recipe<?>> DATA = TYPES.registerType("data");

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
