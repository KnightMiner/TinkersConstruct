package slimeknights.tconstruct.library.recipe.tinkerstation.building;

import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.common.ItemStackLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.tconstruct.library.json.TinkerLoadables;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;

import java.util.List;

/**
 * Shapeless recipe with a number of {@link slimeknights.tconstruct.library.recipe.ingredient.MaterialIngredient} and
 * {@link slimeknights.tconstruct.library.recipe.ingredient.MaterialValueIngredient} to set the materials of the result.
 * @see ShapedMaterialsRecipe
 * @see ShapelessMaterialsRecipeBuilder
 */
public class ShapelessMaterialsRecipe extends ShapelessRecipe implements MaterialsCraftingTableRecipe {
  public static final RecordLoadable<ShapelessMaterialsRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.RECIPE_GROUP,
    TinkerLoadables.CRAFTING_BOOK_CATEGORY.defaultField("category", CraftingBookCategory.MISC, true, CraftingRecipe::category),
    ItemStackLoadable.REQUIRED_STACK_DATA.requiredField("result", r -> r.result),
    Loadables.ITEM_INGREDIENT_NONEMPTY.list(1).mapChecked(
      (list, error) -> {
        int max = ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth();
        if (list.size() > max) {
          throw error.create("Too many ingredients for shapeless forge recipe. The maximum is: " + max);
        }
        return new NonNullList<>(list, Ingredient.EMPTY);
      },
      (list, error) -> list
    ).requiredField("ingredients", r -> r.ingredients),
    IntLoadable.FROM_ONE.requiredField("parts", r -> r.partCount),
    MaterialVariantId.LOADABLE.list(0).defaultField("extra_materials", List.of(), r -> r.extraMaterials),
    ShapelessMaterialsRecipe::new);

  /** Number of parts to match */
  @Getter
  private final int partCount;
  /** List of additional materials to add beyond the parts */
  @Getter
  private final List<MaterialVariantId> extraMaterials;

  public ShapelessMaterialsRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients, int partCount, List<MaterialVariantId> extraMaterials) {
    super(group, category, result, ingredients);
    this.partCount = partCount;
    this.extraMaterials = extraMaterials;
  }

  @Override
  public List<Ingredient> getParts() {
    return getIngredients();
  }

  /** Sets the material for the given stack */
  @Override
  public void setMaterial(ItemStack stack, MaterialVariantId material) {
    ShapedMaterialsRecipe.setMaterial(stack, material, extraMaterials);
  }

  @Override
  public ItemStack assemble(CraftingInput inventory, HolderLookup.Provider registries) {
    return ShapedMaterialsRecipe.assemble(super.assemble(inventory, registries), inventory, getIngredients(), partCount, false, extraMaterials);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerRecipes.shapelessMaterials.get();
  }
}
