package slimeknights.tconstruct.library.recipe.modifiers.adding;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.SizedIngredientRecipeBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Modifier recipe that changes max level and slot behavior each level. Used for a single input recipe that has multiple slot requirements
 * @see MultilevelModifierRecipe
 * @see MultilevelIncrementalModifierRecipeBuilder
 * @see IncrementalModifierRecipeBuilder
 */
public class MultilevelModifierRecipeBuilder extends AbstractMultilevelModifierRecipeBuilder<MultilevelModifierRecipeBuilder> implements SizedIngredientRecipeBuilder<MultilevelModifierRecipeBuilder> {
  // inputs
  private final List<SizedIngredient> inputs = new ArrayList<>();

  protected MultilevelModifierRecipeBuilder(ModifierId result) {
    super(result);
  }

  /** Creates a new builder instance */
  public static MultilevelModifierRecipeBuilder modifier(ModifierId result) {
    return new MultilevelModifierRecipeBuilder(result);
  }


  /* Inputs */

  /**
   * Adds an input to the recipe
   * @param ingredient  Input
   * @return  Builder instance
   */
  @Override
  public MultilevelModifierRecipeBuilder input(SizedIngredient ingredient) {
    this.inputs.add(ingredient);
    return this;
  }


  /* Saving */

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    if (inputs.isEmpty() && !allowCrystal) {
      throw new IllegalStateException("Must either have at least 1 input or allow crystal");
    }
    if (levels.isEmpty()) {
      throw new IllegalStateException("Must have at least 1 level");
    }
    output.accept(id, new MultilevelModifierRecipe(inputs, tools, maxToolSize, result, allowCrystal, levels, checkTraitLevel), buildOptionalAdvancement(output, id, "modifiers"));
  }
}
