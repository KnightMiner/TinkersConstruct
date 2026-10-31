package slimeknights.tconstruct.library.recipe.casting.item;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.library.recipe.casting.ICastingRecipe;

import static slimeknights.tconstruct.library.recipe.melting.IMeltingRecipe.getTemperature;

/**
 * Builder for an item casting recipe. Takes a fluid and optional cast to create an item
 */
@SuppressWarnings({"WeakerAccess", "unused", "UnusedReturnValue"})
@RequiredArgsConstructor(staticName = "castingRecipe")
public class ItemCastingRecipeBuilder extends AbstractRecipeBuilder<ItemCastingRecipeBuilder> {
  private final RecipeType<? extends ICastingRecipe> type;
  private final RecipeSerializer<? extends ItemCastingRecipe> serializer;
  private final ItemOutput result;
  private Ingredient cast = Ingredient.EMPTY;
  private SizedFluidIngredient fluid = null;
  @Setter @Accessors(chain = true)
  private int coolingTime = -1;
  private boolean consumed = false;
  private boolean switchSlots = false;

  /** Base method to create a basin recipe */
  public static ItemCastingRecipeBuilder tableRecipe(RecipeSerializer<? extends ItemCastingRecipe> serializer, ItemOutput result) {
    return castingRecipe(TinkerRecipes.castingTableType.get(), serializer, result);
  }

  /** Base method to create a basin recipe */
  public static ItemCastingRecipeBuilder basinRecipe(RecipeSerializer<? extends ItemCastingRecipe> serializer, ItemOutput result) {
    return castingRecipe(TinkerRecipes.castingBasinType.get(), serializer, result);
  }

  /* Item casting */

  /** Creates a standard item casting table recipe */
  public static ItemCastingRecipeBuilder tableRecipe(ItemOutput result) {
    return tableRecipe(TinkerRecipes.itemCasting.getTable(), result);
  }

  /** Creates a standard item casting table recipe */
  public static ItemCastingRecipeBuilder tableRecipe(ItemLike resultIn) {
    return tableRecipe(ItemOutput.fromItem(resultIn));
  }

  /** Creates a standard item casting table recipe */
  public static ItemCastingRecipeBuilder tableRecipe(TagKey<Item> result) {
    return tableRecipe(ItemOutput.fromTag(result));
  }

  /** Creates a standard item casting basin recipe */
  public static ItemCastingRecipeBuilder basinRecipe(ItemOutput result) {
    return basinRecipe(TinkerRecipes.itemCasting.getBasin(), result);
  }

  /** Creates a standard item casting basin recipe */
  public static ItemCastingRecipeBuilder basinRecipe(ItemLike resultIn) {
    return basinRecipe(ItemOutput.fromItem(resultIn));
  }

  /** Creates a standard item casting table recipe */
  public static ItemCastingRecipeBuilder basinRecipe(TagKey<Item> result) {
    return basinRecipe(ItemOutput.fromTag(result));
  }


  /* Retextured casting */

  /** Creates a casting basin recipe for a retextured block, like the smeltery controller */
  public static ItemCastingRecipeBuilder retexturedTableRecipe(ItemOutput result) {
    return tableRecipe(TinkerRecipes.retexturedCasting.getTable(), result);
  }

  /** Creates a casting basin recipe for a retextured block, like the smeltery controller */
  public static ItemCastingRecipeBuilder retexturedBasinRecipe(ItemOutput result) {
    return basinRecipe(TinkerRecipes.retexturedCasting.getBasin(), result);
  }


  /* Cast duplication */

  /** Creates a new casting table recipe that duplicates the input  */
  public static ItemCastingRecipeBuilder tableDuplication() {
    return tableRecipe(TinkerRecipes.castDuplication.getTable(), ItemOutput.EMPTY);
  }

  /** Creates a new casting basin recipe that duplicates the input  */
  public static ItemCastingRecipeBuilder basinDuplication() {
    return basinRecipe(TinkerRecipes.castDuplication.getBasin(), ItemOutput.EMPTY);
  }


  /* Fluids */

  /**
   * Sets the fluid for this recipe
   * @param fluid   Fluid instance
   * @param amount  amount of fluid
   * @return  Builder instance
   */
  public ItemCastingRecipeBuilder setFluid(Fluid fluid, int amount) {
    return this.setFluid(SizedFluidIngredient.of(fluid, amount));
  }

  /**
   * Sets the fluid for this recipe
   * @param tagIn   Tag<Fluid> instance
   * @param amount  amount of fluid
   * @return  Builder instance
   */
  public ItemCastingRecipeBuilder setFluid(TagKey<Fluid> tagIn, int amount) {
    return this.setFluid(SizedFluidIngredient.of(tagIn, amount));
  }

  /**
   * Sets the fluid ingredient
   * @param fluid  Fluid ingredient instance
   * @return  Builder instance
   */
  public ItemCastingRecipeBuilder setFluid(SizedFluidIngredient fluid) {
    this.fluid = fluid;
    return this;
  }

  /**
   * Sets the recipe cooling time
   * @param temperature  Recipe temperature
   * @param amount       Recipe amount
   */
  public ItemCastingRecipeBuilder setCoolingTime(int temperature, int amount) {
    return setCoolingTime(ICastingRecipe.calcCoolingTime(temperature, amount));
  }

  /**
   * Sets the fluid for this recipe, and cooling time if unset.
   * @param fluidStack  Fluid input
   * @return  Builder instance
   */
  public ItemCastingRecipeBuilder setFluidAndTime(FluidStack fluidStack) {
    this.fluid = SizedFluidIngredient.of(fluidStack);
    if (this.coolingTime == -1) {
      this.coolingTime = ICastingRecipe.calcCoolingTime(fluidStack);
    }
    return this;
  }

  /**
   * Sets the fluid for this recipe, and cooling time
   * @param fluid      Fluid object instance
   * @param amount     amount of fluid
   */
  public ItemCastingRecipeBuilder setFluidAndTime(FluidObject<?> fluid, int amount) {
    setFluid(fluid.ingredient(amount));
    setCoolingTime(getTemperature(fluid), amount);
    return this;
  }

  /* Cast */

  /**
   * Sets the cast from a tag
   * @param tagIn     Cast tag
   * @param consumed  If true, the cast is consumed
   * @return  Builder instance
   */
  public ItemCastingRecipeBuilder setCast(TagKey<Item> tagIn, boolean consumed) {
    return this.setCast(Ingredient.of(tagIn), consumed);
  }

  /**
   * Sets the cast from a tag
   * @param itemIn    Cast item
   * @param consumed  If true, the cast is consumed
   * @return  Builder instance
   */
  public ItemCastingRecipeBuilder setCast(ItemLike itemIn, boolean consumed) {
    return this.setCast(Ingredient.of(itemIn), consumed);
  }

  /**
   * Sets the cast from an ingredient
   * @param ingredient  Cast ingredient
   * @param consumed    If true, the cast is consumed
   * @return  Builder instance
   */
  public ItemCastingRecipeBuilder setCast(Ingredient ingredient, boolean consumed) {
    this.cast = ingredient;
    this.consumed = consumed;
    return this;
  }

  /**
   * Set output of recipe to be put into the input slot.
   * Mostly used for cast creation
   * @return  Builder instance
   */
  public ItemCastingRecipeBuilder setSwitchSlots() {
    this.switchSlots = true;
    return this;
  }

  @Override
  public void save(RecipeOutput output) {
    this.save(output, Loadables.ITEM.getKey(this.result.get().getItem()));
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    if (this.fluid == null) {
      throw new IllegalStateException("Casting recipes require a fluid input");
    }
    if (this.coolingTime < 0) {
      throw new IllegalStateException("Cooling time is too low, must be at least 0");
    }
    AdvancementHolder advancement = this.buildOptionalAdvancement(output, id, "casting");
    // empty result is useless normally, so assume it's the duplication recipe
    if (result == ItemOutput.EMPTY) {
      if (consumed) {
        throw new IllegalStateException("Cannot consume cast on a duplication recipe");
      }
      output.accept(id, new CastDuplicationRecipe(type, serializer, group, cast, fluid, coolingTime), advancement);
    } else {
      // yeah, retextured recipes have their own constructor, does not matter as long as we pass the right serializer in
      // you can use this for your custom recipe extensions too if you don't change the JSON :)
      output.accept(id, new ItemCastingRecipe(type, serializer, group, cast, fluid, result, coolingTime, consumed && cast != Ingredient.EMPTY, switchSlots), advancement);
    }
  }
}
