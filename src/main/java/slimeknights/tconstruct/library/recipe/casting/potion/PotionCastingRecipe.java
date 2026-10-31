package slimeknights.tconstruct.library.recipe.casting.potion;

import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
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
import slimeknights.mantle.recipe.MultiNamedRecipe;
import slimeknights.mantle.recipe.NamedRecipe;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.tconstruct.library.recipe.casting.CastingRecipeInput;
import slimeknights.tconstruct.library.recipe.casting.CastingRecipeLookup;
import slimeknights.tconstruct.library.recipe.casting.DisplayCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.ICastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.IDisplayableCastingRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Recipe for casting a fluid onto an item, copying the fluid NBT to the item.
 */
public class PotionCastingRecipe implements ICastingRecipe, MultiNamedRecipe<IDisplayableCastingRecipe> {
  protected static final LoadableField<SizedFluidIngredient, PotionCastingRecipe> FLUID_FIELD = Loadables.SIZED_FLUID_INGREDIENT.requiredField("fluid", r -> r.fluid);
  protected static final LoadableField<Integer, PotionCastingRecipe> COOLING_TIME_FIELD = IntLoadable.FROM_ONE.defaultField("cooling_time", 5, r -> r.coolingTime);
  public static final RecordLoadable<PotionCastingRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.TYPE.requiredField(), LoadableRecipeSerializer.SERIALIZER.requiredField(), LoadableRecipeSerializer.RECIPE_GROUP,
    Loadables.ITEM_INGREDIENT_NONEMPTY.requiredField("bottle", r -> r.bottle),
    FLUID_FIELD,
    Loadables.ITEM.requiredField("result", r -> r.result),
    COOLING_TIME_FIELD,
    PotionCastingRecipe::new);

  @Getter
  protected final RecipeType<?> type;
  @Getter
  protected final RecipeSerializer<?> serializer;
  @Getter
  protected final String group;
  /** Input on the casting table, always consumed */
  protected final Ingredient bottle;
  /** Potion ingredient, typically just the potion tag */
  protected final SizedFluidIngredient fluid;
  /** Potion item result, will be given the proper NBT */
  protected final Item result;
  /** Cooling time for this recipe, used for tipped arrows */
  protected final int coolingTime;

  public PotionCastingRecipe(RecipeType<?> type, RecipeSerializer<?> serializer, String group, Ingredient bottle, SizedFluidIngredient fluid, Item result, int coolingTime) {
    this.type = type;
    this.serializer = serializer;
    this.group = group;
    this.bottle = bottle;
    this.fluid = fluid;
    this.result = result;
    this.coolingTime = coolingTime;
    CastingRecipeLookup.registerCastable(result);
  }

  @Override
  public boolean matches(CastingRecipeInput inv, Level level) {
    return bottle.test(inv.getItem()) && fluid.test(inv.getFluid());
  }

  @Override
  public int getFluidAmount(CastingRecipeInput inv) {
    return fluid.amount();
  }

  @Override
  public boolean isConsumed() {
    return true;
  }

  @Override
  public boolean switchSlots() {
    return false;
  }

  @Override
  public int getCoolingTime(CastingRecipeInput inv) {
    return coolingTime;
  }

  @Override
  public ItemStack assemble(CastingRecipeInput inv, HolderLookup.Provider registries) {
    ItemStack result = new ItemStack(this.result);
    result.applyComponents(inv.getFluid().getComponentsPatch());
    return result;
  }


  /* JEI */
  // TODO 1.21: consider making this a display recipe instead of a multirecipe
  protected List<NamedRecipe<IDisplayableCastingRecipe>> displayRecipes = null;

  @Override
  public List<NamedRecipe<IDisplayableCastingRecipe>> getRecipes(ResourceLocation id, HolderLookup.Provider registries) {
    if (displayRecipes == null) {
      // first, make all the potion items
      List<ItemStack> results = getPotions().map(potion -> {
        ItemStack stack = new ItemStack(this.result);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        return stack;
      }).filter(stack -> !stack.isEmpty()).toList();
      // next, it's time to do the fluids
      // we want an order of Mod 1 Potion 1, Mod 1 Potion 2, ..., Mod 2 Potion 1, ...
      // this is potentially displaying multiple fluids from the fluid tag
      FluidStack[] potionFluids = this.fluid.getFluids();
      int totalSize = results.size() * potionFluids.length;
      List<ItemStack> displayResults = new ArrayList<>(totalSize);
      List<FluidStack> displayFluids = new ArrayList<>(totalSize);
      for (FluidStack potionFluid : potionFluids) {
        displayResults.addAll(results);
        for (ItemStack result : results) {
          FluidStack fluid = potionFluid.copy();
          fluid.applyComponents(result.getComponentsPatch());
          displayFluids.add(fluid);
        }
      }
      this.displayRecipes = List.of(DisplayCastingRecipe.id(id)
        .cast(bottle).consumed()
        .fluids(List.copyOf(displayFluids))
        .results(List.copyOf(displayResults)).linkFluidsToOutput()
        .coolingTime(coolingTime)
        .build());
    }
    return displayRecipes;
  }


  /* Recipe interface methods */

  @Override
  public NonNullList<Ingredient> getIngredients() {
    return NonNullList.of(Ingredient.EMPTY, bottle);
  }

  /** @deprecated use {@link #assemble(CastingRecipeInput, HolderLookup.Provider)} */
  @Deprecated
  @Override
  public ItemStack getResultItem(HolderLookup.Provider registries) {
    return new ItemStack(this.result);
  }


  /* JEI helpers */

  /** Gets a list of all potion IDs ready for usage in building NBT */
  public static Stream<Holder.Reference<Potion>> getPotions() {
    return BuiltInRegistries.POTION.holders().filter(holder -> !holder.equals(Potions.WATER));
  }
}
