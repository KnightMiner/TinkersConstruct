package slimeknights.tconstruct.library.recipe.casting.item;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.mantle.util.RetexturedHelper;
import slimeknights.tconstruct.library.recipe.casting.CastingRecipeInput;

/** Extension of item recipe that sets the result block to the input block */
public class RetexturedCastingRecipe extends ItemCastingRecipe {
  /** Loader instance */
  public static final RecordLoadable<RetexturedCastingRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.TYPE.requiredField(), LoadableRecipeSerializer.SERIALIZER.requiredField(),
    LoadableRecipeSerializer.RECIPE_GROUP, CAST_FIELD, FLUID_FIELD, RESULT_FIELD, COOLING_TIME_FIELD, CAST_CONSUMED_FIELD, SWITCH_SLOTS_FIELD,
    RetexturedCastingRecipe::new);

  public RetexturedCastingRecipe(RecipeType<?> type, RecipeSerializer<?> serializer, String group, Ingredient cast, SizedFluidIngredient fluid, ItemOutput result, int coolingTime, boolean consumed, boolean switchSlots) {
    super(type, serializer, group, cast, fluid, result, coolingTime, consumed, switchSlots);
  }

  @Override
  public ItemStack assemble(CastingRecipeInput inv, HolderLookup.Provider registries) {
    ItemStack result = getResultItem(registries).copy();
    if (inv.getItem().getItem() instanceof BlockItem blockItem ) {
      return RetexturedHelper.setTexture(result, blockItem.getBlock());
    }
    return result;
  }
}
