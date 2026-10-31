package slimeknights.tconstruct.library.recipe.casting.material;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;

/** Builder for {@link PartSwapCastingRecipe} */
@Accessors(chain = true)
@RequiredArgsConstructor(staticName = "castingRecipe")
public class PartSwapCastingRecipeBuilder extends AbstractRecipeBuilder<PartSwapCastingRecipeBuilder> {
  private final RecipeType<?> type;
  private final RecipeSerializer<? extends PartSwapCastingRecipe> serializer;
  private final Ingredient tools;
  private final int itemCost;
  @Setter
  @Accessors(fluent = true)
  private int index = 0;
  @Setter
  private IJsonPredicate<MaterialVariantId> allowedMaterials = MaterialPredicate.ANY;

  /** Creates a builder for part swapping in a table */
  public static PartSwapCastingRecipeBuilder tableRecipe(Ingredient tools, int itemCost) {
    return castingRecipe(TinkerRecipes.castingTableType.get(), TinkerRecipes.partSwapCasting.getTable(), tools, itemCost);
  }

  /** Creates a builder for part swapping in a basin */
  public static PartSwapCastingRecipeBuilder basinRecipe(Ingredient tools, int itemCost) {
    return castingRecipe(TinkerRecipes.castingBasinType.get(), TinkerRecipes.partSwapCasting.getBasin(), tools, itemCost);
  }

  @Override
  public void save(RecipeOutput output) {
    save(output, BuiltInRegistries.ITEM.getKey(tools.getItems()[0].getItem()));
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    output.accept(id, new PartSwapCastingRecipe(type, serializer, group, tools, itemCost, index, allowedMaterials), buildOptionalAdvancement(output, id, "materials"));
  }
}
