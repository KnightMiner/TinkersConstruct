package slimeknights.tconstruct.library.recipe.casting.item;

import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.tconstruct.library.recipe.casting.AbstractCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.CastingRecipeInput;
import slimeknights.tconstruct.library.recipe.casting.CastingRecipeLookup;
import slimeknights.tconstruct.library.recipe.casting.IDisplayableCastingRecipe;

import java.util.Arrays;
import java.util.List;

/** Casting recipe that takes a fluid and optional cast and outputs an item. */
@Getter
public class ItemCastingRecipe extends AbstractCastingRecipe implements IDisplayableCastingRecipe {
  /* Shared fields */
  protected static final LoadableField<SizedFluidIngredient,ItemCastingRecipe> FLUID_FIELD = Loadables.SIZED_FLUID_INGREDIENT.requiredField("fluid", ItemCastingRecipe::getFluid);
  protected static final LoadableField<ItemOutput,ItemCastingRecipe> RESULT_FIELD = ItemOutput.REQUIRED_ITEM_LOADABLE.requiredField("result", r -> r.result);
  protected static final LoadableField<Integer,ItemCastingRecipe> COOLING_TIME_FIELD = IntLoadable.FROM_ONE.requiredField("cooling_time", ItemCastingRecipe::getCoolingTime);
  /** Loader instance */
  public static final RecordLoadable<ItemCastingRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.TYPE.requiredField(), LoadableRecipeSerializer.SERIALIZER.requiredField(),
    LoadableRecipeSerializer.RECIPE_GROUP, CAST_FIELD, FLUID_FIELD, RESULT_FIELD, COOLING_TIME_FIELD, CAST_CONSUMED_FIELD, SWITCH_SLOTS_FIELD,
    ItemCastingRecipe::new);

  private final RecipeSerializer<?> serializer;
  protected final SizedFluidIngredient fluid;
  protected final ItemOutput result;
  protected final int coolingTime;
  public ItemCastingRecipe(RecipeType<?> type, RecipeSerializer<?> serializer, String group, Ingredient cast, SizedFluidIngredient fluid, ItemOutput result, int coolingTime, boolean consumed, boolean switchSlots) {
    super(type, group, cast, consumed, switchSlots);
    this.serializer = serializer;
    this.fluid = fluid;
    this.result = result;
    this.coolingTime = coolingTime;
    CastingRecipeLookup.registerCastable(result);
  }

  @Override
  public int getFluidAmount(CastingRecipeInput inv) {
    return this.fluid.amount();
  }

  @Override
  public boolean matches(CastingRecipeInput inv, Level worldIn) {
    return getCast().test(inv.getItem()) && fluid.ingredient().test(inv.getFluid());
  }

  @Override
  public ItemStack getResultItem(HolderLookup.Provider registries) {
    return this.result.get();
  }

  @Override
  public int getCoolingTime(CastingRecipeInput inv) {
    return this.coolingTime;
  }


  /* JEI */

  @Override
  public boolean hasCast() {
    return getCast() != Ingredient.EMPTY;
  }

  @Override
  public List<ItemStack> getCastItems() {
    return Arrays.asList(getCast().getItems());
  }

  @Override
  public List<ItemStack> getOutputs() {
    return List.of(this.result.get());
  }

  @Override
  public List<FluidStack> getFluids() {
    return List.of(this.fluid.getFluids());
  }
}
