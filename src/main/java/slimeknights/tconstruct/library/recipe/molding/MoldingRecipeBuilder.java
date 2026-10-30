package slimeknights.tconstruct.library.recipe.molding;

import lombok.RequiredArgsConstructor;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;

/** Builder for {@link MoldingRecipe} */
@SuppressWarnings("unused")
@RequiredArgsConstructor(staticName = "molding")
public class MoldingRecipeBuilder extends AbstractRecipeBuilder<MoldingRecipeBuilder> {
  private final RecipeType<? extends MoldingRecipe> type;
  private final RecipeSerializer<? extends MoldingRecipe> serializer;
  private final ItemOutput result;
  private Ingredient material = Ingredient.EMPTY;
  private Ingredient pattern = Ingredient.EMPTY;
  private boolean patternConsumed = false;

  /** Creates a casting table builder for an {@link ItemOutput} result */
  public static MoldingRecipeBuilder moldingTable(ItemOutput result) {
    return molding(TinkerRecipes.moldingTableType.get(), TinkerRecipes.molding.table().get(), result);
  }

  /** Creates a casting table builder for an {@link ItemLike} result */
  public static MoldingRecipeBuilder moldingTable(ItemLike item) {
    return moldingTable(ItemOutput.fromItem(item));
  }

  /** Creates a casting basin builder for an {@link ItemOutput} result */
  public static MoldingRecipeBuilder moldingBasin(ItemOutput result) {
    return molding(TinkerRecipes.moldingBasinType.get(), TinkerRecipes.molding.basin().get(), result);
  }

  /** Creates a casting basin builder for an {@link ItemLike} result */
  public static MoldingRecipeBuilder moldingBasin(ItemLike item) {
    return moldingBasin(ItemOutput.fromItem(item));
  }


  /* Inputs */

  /** Sets the material item, on the table */
  public MoldingRecipeBuilder material(Ingredient ingredient) {
    this.material = ingredient;
    return this;
  }

  /** Sets the material item, on the table */
  public MoldingRecipeBuilder material(ItemLike item) {
    return material(Ingredient.of(item));
  }

  /** Sets the material item, on the table */
  public MoldingRecipeBuilder material(TagKey<Item> tag) {
    return material(Ingredient.of(tag));
  }

  /** Sets the mold item, in the players hand */
  public MoldingRecipeBuilder pattern(Ingredient ingredient, boolean consumed) {
    this.pattern = ingredient;
    this.patternConsumed = consumed;
    return this;
  }

  /** Sets the mold item, in the players hand */
  public MoldingRecipeBuilder pattern(ItemLike item, boolean consumed) {
    return pattern(Ingredient.of(item), consumed);
  }

  /** Sets the mold item, in the players hand */
  public MoldingRecipeBuilder pattern(TagKey<Item> tag, boolean consumed) {
    return pattern(Ingredient.of(tag), consumed);
  }


  /* Building */

  @Override
  public void save(RecipeOutput output) {
    save(output, Loadables.ITEM.getKey(result.get().getItem()));
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    if (material == Ingredient.EMPTY) {
      throw new IllegalStateException("Missing material for molding recipe");
    }
    output.accept(id, new MoldingRecipe(type, serializer, material, pattern, patternConsumed, result), buildOptionalAdvancement(output, id, "molding"));
  }
}
