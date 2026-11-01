package slimeknights.tconstruct.library.recipe.modifiers.adding;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.util.LazyModifier;
import slimeknights.tconstruct.library.recipe.SizedIngredientRecipeBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for a standard {@link ModifierRecipe}.
 * @see IncrementalModifierRecipeBuilder
 * @see SwappableModifierRecipeBuilder
 * @see MultilevelModifierRecipeBuilder
 * @see MultilevelIncrementalModifierRecipeBuilder
 */
public class ModifierRecipeBuilder extends AbstractModifierRecipeBuilder<ModifierRecipeBuilder> implements SizedIngredientRecipeBuilder<ModifierRecipeBuilder> {
  protected final List<SizedIngredient> inputs = new ArrayList<>();
  protected ModifierRecipeBuilder(ModifierId result) {
    super(result);
  }

  /**
   * Creates a new recipe for 1 level of a modifier
   * @param modifier  Modifier
   * @return  Recipe for 1 level of the modifier
   */
  public static ModifierRecipeBuilder modifier(ModifierId modifier) {
    return new ModifierRecipeBuilder(modifier);
  }

  /**
   * Creates a new recipe for 1 level of a modifier
   * @param modifier  Modifier
   * @return  Recipe for 1 level of the modifier
   */
  public static ModifierRecipeBuilder modifier(LazyModifier modifier) {
    return modifier(modifier.id());
  }


  /* Inputs */

  /**
   * Adds an input to the recipe
   * @param ingredient  Input
   * @return  Builder instance
   */
  @Override
  public ModifierRecipeBuilder input(SizedIngredient ingredient) {
    this.inputs.add(ingredient);
    return this;
  }


  /* Building */

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    if (inputs.isEmpty() && !allowCrystal) {
      throw new IllegalStateException("Must have at least 1 input");
    }
    output.accept(id, new ModifierRecipe(inputs, tools, maxToolSize, result, ModifierEntry.VALID_LEVEL.range(minLevel, maxLevel), slots, allowCrystal, checkTraitLevel), buildOptionalAdvancement(output, id, "modifiers"));
  }
}
