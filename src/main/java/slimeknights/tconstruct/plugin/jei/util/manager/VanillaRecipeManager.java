package slimeknights.tconstruct.plugin.jei.util.manager;

import lombok.Getter;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import slimeknights.tconstruct.library.recipe.display.FilteredItemRecipe;
import slimeknights.tconstruct.library.recipe.display.FilteredRecipe;

import java.util.List;

/** Recipe manager handling filtering vanilla recipes, which use {@link RecipeHolder} but also never go beyond items. */
public class VanillaRecipeManager<T extends Recipe<?> & FilteredItemRecipe> implements ISimpleRecipeManagerPlugin<RecipeHolder<T>> {
  @Getter
  private final List<RecipeHolder<T>> allRecipes;
  private final VanillaRecipeCache<T> inputCache, outputCache;

  /** Gets a list of always visible recipes from the list of filterable recipes. */
  private static <T extends Recipe<?> & FilteredRecipe> List<RecipeHolder<T>> alwaysVisible(List<RecipeHolder<T>> recipes) {
    List<RecipeHolder<T>> filtered = recipes.stream().filter(r -> r.value().showUnfocused()).toList();
    // save memory: return the same list if the size is the same
    return filtered.size() < recipes.size() ? filtered : recipes;
  }

  private VanillaRecipeManager(IIngredientManager ingredientManager, List<RecipeHolder<T>> recipes) {
    this.allRecipes = alwaysVisible(recipes);
    IIngredientHelper<ItemStack> itemHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);
    inputCache = new VanillaRecipeCache<>(itemHelper, recipes, false);
    outputCache = new VanillaRecipeCache<>(itemHelper, recipes, true);
  }

  @Override
  public boolean isHandledInput(ITypedIngredient<?> input) {
    return input.getType() == VanillaTypes.ITEM_STACK;
  }

  @Override
  public boolean isHandledOutput(ITypedIngredient<?> output) {
    return output.getType() == VanillaTypes.ITEM_STACK;
  }

  @Override
  public List<RecipeHolder<T>> getRecipesForInput(ITypedIngredient<?> input) {
    ITypedIngredient<ItemStack> item = input.cast(VanillaTypes.ITEM_STACK);
    if (item != null) {
      return inputCache.filterRecipes(item.getIngredient());
    }
    return List.of();
  }

  @Override
  public List<RecipeHolder<T>> getRecipesForOutput(ITypedIngredient<?> output) {
    ITypedIngredient<ItemStack> item = output.cast(VanillaTypes.ITEM_STACK);
    if (item != null) {
      return outputCache.filterRecipes(item.getIngredient());
    }
    return List.of();
  }

  /** Creates an instance of the category for the crafting table. */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static <T extends CraftingRecipe & FilteredItemRecipe> ISimpleRecipeManagerPlugin<RecipeHolder<CraftingRecipe>> createCrafting(IIngredientManager ingredientManager, List<RecipeHolder<T>> recipes) {
    return (ISimpleRecipeManagerPlugin<RecipeHolder<CraftingRecipe>>) new VanillaRecipeManager(ingredientManager, recipes);
  }
}
