package slimeknights.tconstruct.library.recipe.modifiers.adding;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.util.LazyModifier;

/**
 * Recipe that supports not just adding multiple of an item, but also adding a partial amount
 * @see IncrementalModifierRecipe
 * @see MultilevelIncrementalModifierRecipeBuilder
 * @see ModifierRecipeBuilder
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class IncrementalModifierRecipeBuilder extends AbstractModifierRecipeBuilder<IncrementalModifierRecipeBuilder> implements IncrementalModifierRecipesBuilder<IncrementalModifierRecipeBuilder> {
  private Ingredient input = Ingredient.EMPTY;
  private int amountPerItem;
  private int neededPerLevel;
  @Accessors(fluent = true)
  @Setter
  private ItemOutput leftover = ItemOutput.EMPTY;

  protected IncrementalModifierRecipeBuilder(ModifierId result) {
    super(result);
  }

  /**
   * Creates a new recipe for 1 level of a modifier
   * @param modifier  Modifier
   * @return  Recipe for 1 level of the modifier
   */
  public static IncrementalModifierRecipeBuilder modifier(ModifierId modifier) {
    return new IncrementalModifierRecipeBuilder(modifier);
  }

  /**
   * Creates a new recipe for 1 level of a modifier
   * @param modifier  Modifier
   * @return  Recipe for 1 level of the modifier
   */
  public static IncrementalModifierRecipeBuilder modifier(LazyModifier modifier) {
    return modifier(modifier.id());
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
  public IncrementalModifierRecipeBuilder input(Ingredient input, int amountPerItem, int neededPerLevel) {
    IncrementalModifierRecipesBuilder.validate(amountPerItem, neededPerLevel);
    this.input = input;
    this.amountPerItem = amountPerItem;
    this.neededPerLevel = neededPerLevel;
    return this;
  }


  /* Building */

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    if (input == Ingredient.EMPTY) {
      throw new IllegalStateException("Must set input");
    }
    output.accept(id, new IncrementalModifierRecipe(input, amountPerItem, neededPerLevel, tools, maxToolSize, result, ModifierEntry.VALID_LEVEL.range(minLevel, maxLevel), slots, leftover, allowCrystal, checkTraitLevel), buildOptionalAdvancement(output, id, "modifiers"));
  }
}
