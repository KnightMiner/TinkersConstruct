package slimeknights.tconstruct.library.recipe.molding;

import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.BooleanLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.ICommonRecipe;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;

/** Recipe to combine two items on the top of a casting table, changing the first */
public class MoldingRecipe implements ICommonRecipe<MoldingRecipeInput> {
  public static final RecordLoadable<MoldingRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.TYPE.requiredField(),
    LoadableRecipeSerializer.SERIALIZER.requiredField(),
    Loadables.ITEM_INGREDIENT_NONEMPTY.requiredField("material", MoldingRecipe::getMaterial),
    Loadables.ITEM_INGREDIENT.defaultField("pattern", Ingredient.EMPTY, MoldingRecipe::getPattern),
    BooleanLoadable.INSTANCE.defaultField("pattern_consumed", false, false, MoldingRecipe::isPatternConsumed),
    ItemOutput.REQUIRED_ITEM_LOADABLE.requiredField("result", r -> r.recipeOutput),
    MoldingRecipe::new);

  @Getter
  private final RecipeType<?> type;
  @Getter
  private final RecipeSerializer<?> serializer;
  @Getter
  private final Ingredient material;
  @Getter
  private final Ingredient pattern;
  @Getter
  private final boolean patternConsumed;
  private final ItemOutput recipeOutput;

  public MoldingRecipe(RecipeType<?> type, RecipeSerializer<?> serializer, Ingredient material, Ingredient pattern, boolean patternConsumed, ItemOutput recipeOutput) {
    this.type = type;
    this.serializer = serializer;
    this.material = material;
    this.pattern = pattern;
    this.patternConsumed = pattern != Ingredient.EMPTY && patternConsumed;
    this.recipeOutput = recipeOutput;
  }

  @Override
  public boolean matches(MoldingRecipeInput input, Level worldIn) {
    return material.test(input.getMaterial()) && pattern.test(input.getPattern());
  }

  @Override
  public NonNullList<Ingredient> getIngredients() {
    return NonNullList.of(Ingredient.EMPTY, material, pattern);
  }

  @Override
  public ItemStack getResultItem(HolderLookup.Provider access) {
    return recipeOutput.get();
  }

  @Override
  public ItemStack getToastSymbol() {
    return ICommonRecipe.super.getToastSymbol();
  }
}
