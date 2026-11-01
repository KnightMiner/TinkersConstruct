package slimeknights.tconstruct.plugin.jei.util.manager;

import mezz.jei.api.ingredients.IIngredientHelper;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.recipe.NamedRecipe;
import slimeknights.tconstruct.library.recipe.display.FilteredFluidRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** Cache for searching for recipes from a list of {@link FilteredFluidRecipe}. */
public class FluidRecipeCache<T extends FilteredFluidRecipe> extends AbstractRecipeCache<NamedRecipe<T>, FluidStack> {
  private final boolean output;
  public FluidRecipeCache(IIngredientHelper<FluidStack> helper, List<NamedRecipe<T>> recipes, boolean output) {
    super(helper, recipes);
    this.output = output;
  }

  @Override
  protected boolean matches(NamedRecipe<T> recipe, Predicate<FluidStack> predicate) {
    return recipe.recipe().matchesFluid(predicate, this.output);
  }

  @Override
  public List<NamedRecipe<T>> filterRecipes(FluidStack focus) {
    List<NamedRecipe<T>> recipes = matchingRecipes(focus);
    if (recipes.isEmpty()) {
      return List.of();
    }
    List<NamedRecipe<T>> filtered = new ArrayList<>(recipes.size());
    for (NamedRecipe<T> recipe : recipes) {
      if (recipe.recipe().isVisibleFromFluid(focus, output)) {
        filtered.add(recipe);
      }
    }
    return filtered;
  }
}
