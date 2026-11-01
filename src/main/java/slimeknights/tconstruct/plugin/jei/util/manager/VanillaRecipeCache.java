package slimeknights.tconstruct.plugin.jei.util.manager;

import mezz.jei.api.ingredients.IIngredientHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import slimeknights.tconstruct.library.recipe.display.FilteredItemRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** Cache for searching for recipes from a list of {@link FilteredItemRecipe} */
public class VanillaRecipeCache<T extends Recipe<?> & FilteredItemRecipe> extends AbstractRecipeCache<RecipeHolder<T>, ItemStack> {
  private final boolean output;
  public VanillaRecipeCache(IIngredientHelper<ItemStack> helper, List<RecipeHolder<T>> recipes, boolean output) {
    super(helper, recipes);
    this.output = output;
  }

  @Override
  protected boolean matches(RecipeHolder<T> recipe, Predicate<ItemStack> predicate) {
    return recipe.value().matchesItem(predicate, output);
  }

  @Override
  public List<RecipeHolder<T>> filterRecipes(ItemStack focus) {
    List<RecipeHolder<T>> recipes = matchingRecipes(focus);
    if (recipes.isEmpty()) {
      return List.of();
    }
    List<RecipeHolder<T>> filtered = new ArrayList<>(recipes.size());
    for (RecipeHolder<T> recipe : recipes) {
      if (recipe.value().isVisibleFromItem(focus, output)) {
        filtered.add(recipe);
      }
    }
    return filtered;
  }
}
