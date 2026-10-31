package slimeknights.tconstruct.library.recipe.casting.item;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.tconstruct.library.recipe.casting.CastingRecipeInput;

import java.util.List;

/** Recipe which duplicates the input cast using a fluid */
public class CastDuplicationRecipe extends ItemCastingRecipe {
  public static final RecordLoadable<CastDuplicationRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.TYPE.requiredField(), LoadableRecipeSerializer.SERIALIZER.requiredField(),
    LoadableRecipeSerializer.RECIPE_GROUP,
    Loadables.ITEM_INGREDIENT_NONEMPTY.requiredField("cast", CastDuplicationRecipe::getCast),
    FLUID_FIELD, COOLING_TIME_FIELD,
    CastDuplicationRecipe::new);

  public CastDuplicationRecipe(RecipeType<?> type, RecipeSerializer<?> serializer, String group, Ingredient cast, SizedFluidIngredient fluid, int coolingTime) {
    super(type, serializer, group, cast, fluid, ItemOutput.EMPTY, coolingTime, false, false);
  }

  @Override
  public ItemStack assemble(CastingRecipeInput inv, HolderLookup.Provider registries) {
    return inv.getItem().copy();
  }

  @Override
  public ItemStack getResultItem(HolderLookup.Provider registries) {
    ItemStack[] items = getCast().getItems();
    return items.length == 0 ? ItemStack.EMPTY : items[0];
  }


  /* JEI */

  @Override
  public List<ItemStack> getOutputs() {
    return List.of(getCast().getItems());
  }
}
