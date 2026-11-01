package slimeknights.tconstruct.tools.recipe;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.library.recipe.SizedIngredientRecipeBuilder;

/** Builder for modifier sorting recipes */
@RequiredArgsConstructor(staticName = "sorting")
public class ModifierSortingRecipeBuilder extends AbstractRecipeBuilder<ModifierSortingRecipeBuilder> implements SizedIngredientRecipeBuilder<ModifierSortingRecipeBuilder> {
  @Accessors(fluent = true)
  @Setter
  private SizedIngredient input = null;

  @Override
  public void save(RecipeOutput output) {
    save(output, BuiltInRegistries.ITEM.getKey((input.getItems()[0].getItem())));
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    if (input == null) {
      throw new IllegalStateException("Must set the input ingredient");
    }
    output.accept(id, new ModifierSortingRecipe(input), buildOptionalAdvancement(output, id, "modifiers"));
  }
}
