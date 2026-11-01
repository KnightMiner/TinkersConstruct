package slimeknights.tconstruct.library.recipe.worktable;

import net.neoforged.neoforge.common.crafting.SizedIngredient;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.library.recipe.SizedIngredientRecipeBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for a recipe that uses sized ingredients.
 */
public abstract class AbstractSizedIngredientRecipeBuilder<T extends AbstractSizedIngredientRecipeBuilder<T>> extends AbstractRecipeBuilder<T> implements SizedIngredientRecipeBuilder<T> {
  protected final List<SizedIngredient> inputs = new ArrayList<>();

  @Override
  @SuppressWarnings("unchecked")
  public T input(SizedIngredient ingredient) {
    this.inputs.add(ingredient);
    return (T)this;
  }
}
