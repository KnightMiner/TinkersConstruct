package slimeknights.tconstruct.library.recipe;

import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.registration.deferred.RecipeTypeDeferredRegister;
import slimeknights.mantle.registration.holder.DeferredRecipeType;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipe;
import slimeknights.tconstruct.library.recipe.casting.ICastingRecipe;
import slimeknights.tconstruct.library.recipe.entitymelting.EntityMeltingRecipe;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuel;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;
import slimeknights.tconstruct.library.recipe.melting.IMeltingRecipe;
import slimeknights.tconstruct.library.recipe.modifiers.severing.SeveringRecipe;
import slimeknights.tconstruct.library.recipe.molding.MoldingRecipe;
import slimeknights.tconstruct.library.recipe.partbuilder.IPartBuilderRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.recipe.worktable.IModifierWorktableRecipe;

/**
 * Class containing all of Tinkers Construct recipe types
 */
public class TinkerRecipeTypes {
  /** Deferred instance */
  private static final RecipeTypeDeferredRegister TYPES = new RecipeTypeDeferredRegister(TConstruct.MOD_ID);

  /** Initializes the deferred register */
  @Internal
  public static void init(IEventBus bus) {
    TYPES.register(bus);
  }

  public static final DeferredRecipeType<IPartBuilderRecipe> PART_BUILDER = TYPES.registerType("part_builder");
  public static final DeferredRecipeType<MaterialRecipe> MATERIAL = TYPES.registerType("material");
  public static final DeferredRecipeType<ITinkerStationRecipe> TINKER_STATION = TYPES.registerType("tinker_station");
  public static final DeferredRecipeType<IModifierWorktableRecipe> MODIFIER_WORKTABLE = TYPES.registerType("modifier_worktable");

  // casting
  public static final DeferredRecipeType<ICastingRecipe> CASTING_BASIN = TYPES.registerType("casting_basin");
  public static final DeferredRecipeType<ICastingRecipe> CASTING_TABLE = TYPES.registerType("casting_table");
  public static final DeferredRecipeType<MoldingRecipe> MOLDING_TABLE = TYPES.registerType("molding_table");
  public static final DeferredRecipeType<MoldingRecipe> MOLDING_BASIN = TYPES.registerType("molding_basin");

  // smeltery
  public static final DeferredRecipeType<IMeltingRecipe> MELTING = TYPES.registerType("melting");
  public static final DeferredRecipeType<EntityMeltingRecipe> ENTITY_MELTING = TYPES.registerType("entity_melting");
  public static final DeferredRecipeType<MeltingFuel> FUEL = TYPES.registerType("fuel");
  public static final DeferredRecipeType<AlloyRecipe> ALLOYING = TYPES.registerType("alloying");

  // modifiers
  public static final DeferredRecipeType<SeveringRecipe> SEVERING = TYPES.registerType("severing");

  /** Internal recipe type for recipes that are not pulled by any specific crafting block */
  public static final DeferredRecipeType<Recipe<?>> DATA = TYPES.registerType("data");
}
