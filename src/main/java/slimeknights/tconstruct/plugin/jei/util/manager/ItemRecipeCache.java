package slimeknights.tconstruct.plugin.jei.util.manager;

import mezz.jei.api.ingredients.IIngredientHelper;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.recipe.NamedRecipe;
import slimeknights.tconstruct.library.recipe.display.FilteredItemRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** Cache for searching for recipes from a list of {@link FilteredItemRecipe} */
public class ItemRecipeCache<T extends FilteredItemRecipe> extends AbstractRecipeCache<NamedRecipe<T>, ItemStack> {
  private final boolean output;
  public ItemRecipeCache(IIngredientHelper<ItemStack> helper, List<NamedRecipe<T>> recipes, boolean output) {
    super(helper, recipes);
    this.output = output;
  }

  @Override
  protected boolean matches(NamedRecipe<T> recipe, Predicate<ItemStack> predicate) {
    return recipe.recipe().matchesItem(predicate, output);
  }

  @Override
  public List<NamedRecipe<T>> filterRecipes(ItemStack focus) {
    List<NamedRecipe<T>> recipes = matchingRecipes(focus);
    if (recipes.isEmpty()) {
      return List.of();
    }
    List<NamedRecipe<T>> filtered = new ArrayList<>(recipes.size());
    for (NamedRecipe<T> recipe : recipes) {
      if (recipe.recipe().isVisibleFromItem(focus, output)) {
        filtered.add(recipe);
      }
    }
    return filtered;
  }
}
