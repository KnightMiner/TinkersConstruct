package slimeknights.tconstruct.library.recipe;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/** Recipe builder for a recipe taking sized ingredients. Can be a single or multiple inputs. */
public interface SizedIngredientRecipeBuilder<T> {
  /**
   * Adds an input to the recipe
   * @param ingredient  Input
   * @return  Builder instance
   */
  T input(SizedIngredient ingredient);

  /**
   * Adds an input to the recipe
   * @param ingredient  Input
   * @return  Builder instance
   */
  default T input(Ingredient ingredient) {
    return input(new SizedIngredient(ingredient, 1));
  }

  /**
   * Adds an input with the given amount, does not affect the salvage builder
   * @param item    Item
   * @param amount  Amount
   * @return  Builder instance
   */
  default T input(ItemLike item, int amount) {
    return input(SizedIngredient.of(item, amount));
  }

  /**
   * Adds an input with a size of 1, does not affect the salvage builder
   * @param item    Item
   * @return  Builder instance
   */
  default T input(ItemLike item) {
    return input(item, 1);
  }

  /**
   * Adds an input to the recipe
   * @param tag     Tag input
   * @param amount  Amount required
   * @return  Builder instance
   */
  default T input(TagKey<Item> tag, int amount) {
    return input(SizedIngredient.of(tag, amount));
  }

  /**
   * Adds an input to the recipe
   * @param tag     Tag input
   * @return  Builder instance
   */
  default T input(TagKey<Item> tag) {
    return input(tag, 1);
  }
}
