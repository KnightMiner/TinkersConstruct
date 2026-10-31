package slimeknights.tconstruct.library.recipe.casting.potion;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.library.recipe.casting.ICastingRecipe;

import javax.annotation.Nullable;

/**
 * Builder for a potion bottle filling recipe. Takes a fluid and optional cast to create an item that copies the fluid NBT
 */
@SuppressWarnings({"WeakerAccess", "unused", "UnusedReturnValue"})
@RequiredArgsConstructor(staticName = "castingRecipe")
public class PotionCastingRecipeBuilder extends AbstractRecipeBuilder<PotionCastingRecipeBuilder> {
  private final RecipeType<? extends ICastingRecipe> type;
  private final RecipeSerializer<? extends PotionCastingRecipe> serializer;
  private final Item result;
  @Nullable
  private final ModifierId modifier;
  private Ingredient bottle = Ingredient.EMPTY;
  private SizedFluidIngredient fluid = null;
  @Setter @Accessors(chain = true)
  private int coolingTime = 5;

  /** Base method to create a table recipe */
  private static PotionCastingRecipeBuilder tableRecipe(RecipeSerializer<? extends PotionCastingRecipe> serializer, ItemLike result, @Nullable ModifierId modifier) {
    return castingRecipe(TinkerRecipes.castingTableType.get(), serializer, result.asItem(), modifier);
  }

  /** Base method to create a table recipe */
  private static PotionCastingRecipeBuilder tableRecipe(RecipeSerializer<? extends PotionCastingRecipe> serializer, ModifierId modifier) {
    return tableRecipe(serializer, Items.AIR, modifier);
  }

  /** Base method to create a basin recipe */
  private static PotionCastingRecipeBuilder basinRecipe(RecipeSerializer<? extends PotionCastingRecipe> serializer, ItemLike result, @Nullable ModifierId modifier) {
    return castingRecipe(TinkerRecipes.castingBasinType.get(), serializer, result.asItem(), modifier);
  }

  /** Base method to create a basin recipe */
  private static PotionCastingRecipeBuilder basinRecipe(RecipeSerializer<? extends PotionCastingRecipe> serializer, ModifierId modifier) {
    return basinRecipe(serializer, Items.AIR, modifier);
  }


  /* Bottle filling */

  /** Creates a new table casting recipe for a bottle */
  public static PotionCastingRecipeBuilder tableRecipe(ItemLike result) {
    return tableRecipe(TinkerRecipes.potionCasting.getTable(), result, null);
  }

  /** Creates a new basin casting recipe for a bottle */
  public static PotionCastingRecipeBuilder basinRecipe(ItemLike result) {
    return basinRecipe(TinkerRecipes.potionCasting.getBasin(), result, null);
  }


  /* Modifier casting */

  /** Creates a recipe for tipping a tool in a table */
  public static PotionCastingRecipeBuilder tableTipping(ModifierId modifier) {
    return tableRecipe(TinkerRecipes.toolTipping.getTable(), modifier);
  }

  /** Creates a recipe for tipping a basin in a table */
  public static PotionCastingRecipeBuilder basinTipping(ModifierId modifier) {
    return basinRecipe(TinkerRecipes.toolTipping.getBasin(), modifier);
  }

  /** Creates a recipe for clearing a tools potion in a table */
  public static PotionCastingRecipeBuilder tableClearing(ModifierId modifier) {
    return tableRecipe(TinkerRecipes.toolTippedClearing.getTable(), modifier);
  }

  /** Creates a recipe for clearing a basins potion in a table */
  public static PotionCastingRecipeBuilder basinClearing(ModifierId modifier) {
    return basinRecipe(TinkerRecipes.toolTippedClearing.getBasin(), modifier);
  }


  /* Fluids */

  /**
   * Sets the fluid for this recipe
   * @param tag   Tag<Fluid> instance
   * @param amount  amount of fluid
   * @return  Builder instance
   */
  public PotionCastingRecipeBuilder setFluid(TagKey<Fluid> tag, int amount) {
    return setFluid(SizedFluidIngredient.of(tag, amount));
  }

  /**
   * Sets the fluid ingredient
   * @param fluid  Fluid ingredient instance
   * @return  Builder instance
   */
  public PotionCastingRecipeBuilder setFluid(SizedFluidIngredient fluid) {
    this.fluid = fluid;
    return this;
  }


  /* Cast */

  /**
   * Sets the cast from a tag, bottles are always consumed
   * @param tagIn     Cast tag
   * @return  Builder instance
   */
  public PotionCastingRecipeBuilder setBottle(TagKey<Item> tagIn) {
    return this.setBottle(Ingredient.of(tagIn));
  }

  /**
   * Sets the bottle from an item, bottles are always consumed
   * @param itemIn    Cast item
   * @return  Builder instance
   */
  public PotionCastingRecipeBuilder setBottle(ItemLike itemIn) {
    return this.setBottle(Ingredient.of(itemIn));
  }

  /**
   * Sets the bottle from an ingredient, bottles are always consumed
   * @param ingredient  Cast ingredient
   * @return  Builder instance
   */
  public PotionCastingRecipeBuilder setBottle(Ingredient ingredient) {
    this.bottle = ingredient;
    return this;
  }

  @Override
  public void save(RecipeOutput output) {
    this.save(output, Loadables.ITEM.getKey(this.result));
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
    if (modifier != null) {
      // tip casting and tip clearing use the same fields, so this should be fine
      output.accept(id, new TippingCastingRecipe(type, serializer, group, bottle, fluid, coolingTime, modifier), advancement);
    } else {
      output.accept(id, new PotionCastingRecipe(type, serializer, group, bottle, fluid, result, coolingTime), advancement);
    }
  }
}
