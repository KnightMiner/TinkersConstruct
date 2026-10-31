package slimeknights.tconstruct.tools.recipe.severing;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.library.recipe.modifiers.severing.SeveringRecipe;

import java.util.Objects;
import java.util.function.BiFunction;

/** Builder for severing recipes that have only the base chance and looting bonus as fields */
@Setter
@Accessors(chain = true)
@RequiredArgsConstructor(staticName = "serializer")
public class SpecialSeveringRecipeBuilder extends AbstractRecipeBuilder<SpecialSeveringRecipeBuilder> {
  private final BiFunction<Float, Float, ? extends SeveringRecipe> constructor;
  private float baseChance = 0.05f;
  private float lootingBonus = 0.01f;

  /** Doubles the drop chances for this rare mob */
  public SpecialSeveringRecipeBuilder rareMob() {
    baseChance = 0.1f;
    lootingBonus = 0.02f;
    return this;
  }

  /** Common saving logic */
  private void save(RecipeOutput output, ResourceLocation id, SeveringRecipe recipe) {
    output.accept(id, recipe, buildOptionalAdvancement(output, id, "severing"));
  }

  @Override
  public void save(RecipeOutput output) {
    SeveringRecipe recipe = constructor.apply(baseChance, lootingBonus);
    save(output, Objects.requireNonNull(BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipe.getSerializer())), recipe);
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    save(output, id, constructor.apply(baseChance, lootingBonus));
  }
}
