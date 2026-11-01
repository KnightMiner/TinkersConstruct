package slimeknights.tconstruct.library.recipe.modifiers.adding;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.tconstruct.library.modifiers.ModifierId;

/**
 * Builder for {@link MultilevelIncrementalModifierRecipe}.
 * @see MultilevelIncrementalModifierRecipe
 * @see MultilevelModifierRecipeBuilder
 * @see IncrementalModifierRecipeBuilder
 */
public class MultilevelIncrementalModifierRecipeBuilder extends AbstractMultilevelModifierRecipeBuilder<MultilevelIncrementalModifierRecipeBuilder> implements IncrementalModifierRecipesBuilder<MultilevelIncrementalModifierRecipeBuilder> {
  private Ingredient input = Ingredient.EMPTY;
  private int amountPerItem;
  private int neededPerLevel;
  @Accessors(fluent = true)
  @Setter
  private ItemOutput leftover = ItemOutput.EMPTY;

  protected MultilevelIncrementalModifierRecipeBuilder(ModifierId result) {
    super(result);
  }

  /** Creates a new builder instance */
  public static MultilevelIncrementalModifierRecipeBuilder modifier(ModifierId result) {
    return new MultilevelIncrementalModifierRecipeBuilder(result);
  }


  /* Inputs */

  /**
   * Adds an input to the recipe
   * @param input          Input
   * @param amountPerItem  Amount each item matches
   * @param neededPerLevel Total number needed for this modifier
   * @return  Builder instance
   */
  @Override
  public MultilevelIncrementalModifierRecipeBuilder input(Ingredient input, int amountPerItem, int neededPerLevel) {
    IncrementalModifierRecipesBuilder.validate(amountPerItem, neededPerLevel);
    this.input = input;
    this.amountPerItem = amountPerItem;
    this.neededPerLevel = neededPerLevel;
    return this;
  }


  /* Saving */

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    if (input == Ingredient.EMPTY) {
      throw new IllegalStateException("Must set input");
    }
    if (levels.isEmpty()) {
      throw new IllegalStateException("Must have at least 1 level");
    }
    output.accept(id, new MultilevelIncrementalModifierRecipe(input, amountPerItem, neededPerLevel, tools, maxToolSize, result, leftover, allowCrystal, levels, checkTraitLevel), buildOptionalAdvancement(output, id, "modifiers"));
  }
}
