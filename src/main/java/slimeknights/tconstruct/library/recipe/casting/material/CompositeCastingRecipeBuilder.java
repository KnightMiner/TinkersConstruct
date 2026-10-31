package slimeknights.tconstruct.library.recipe.casting.material;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.library.recipe.casting.ICastingRecipe;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;

import javax.annotation.Nullable;

/** Builder for a composite part recipe, should exist for each part */
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "composite")
public class CompositeCastingRecipeBuilder extends AbstractRecipeBuilder<CompositeCastingRecipeBuilder> {
  private final RecipeType<? extends ICastingRecipe> type;
  private final RecipeSerializer<? extends CompositeCastingRecipe> serializer;
  private final IMaterialItem result;
  private final int itemCost;
  @Setter @Nullable
  private MaterialStatsId castingStatConflict = null;
  @Setter
  private IJsonPredicate<MaterialVariantId> allowedMaterials = MaterialPredicate.ANY;

  /** Creates a builder for a table */
  public static CompositeCastingRecipeBuilder table(IMaterialItem result, int itemCost) {
    return composite(TinkerRecipes.castingTableType.get(), TinkerRecipes.compositeCasting.getTable(), result, itemCost);
  }

  /** Creates a builder for a basin */
  public static CompositeCastingRecipeBuilder basin(IMaterialItem result, int itemCost) {
    return composite(TinkerRecipes.castingBasinType.get(), TinkerRecipes.compositeCasting.getBasin(), result, itemCost);
  }

  @Override
  public void save(RecipeOutput output) {
    save(output, Loadables.ITEM.getKey(result.asItem()));
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    output.accept(id, new CompositeCastingRecipe(type, serializer, group, itemCost, result, allowedMaterials, castingStatConflict), buildOptionalAdvancement(output, id, "casting"));
  }
}
