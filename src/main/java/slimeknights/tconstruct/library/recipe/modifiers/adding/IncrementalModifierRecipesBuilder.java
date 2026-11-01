package slimeknights.tconstruct.library.recipe.modifiers.adding;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.recipe.helper.ItemOutput;

/** Common logic for {@link IncrementalModifierRecipeBuilder} and {@link MultilevelIncrementalModifierRecipeBuilder} */
public interface IncrementalModifierRecipesBuilder<T> {
  /**
   * Adds an input to the recipe
   * @param input          Input
   * @param amountPerItem  Amount each item matches
   * @param neededPerLevel Total number needed for this modifier
   * @return  Builder instance
   */
  T input(Ingredient input, int amountPerItem, int neededPerLevel);

  /**
   * Adds an input to the recipe
   * @param item           Item input
   * @param amountPerItem  Amount each item matches
   * @param neededPerLevel Total number needed for this modifier
   * @return  Builder instance
   */
  default T input(ItemLike item, int amountPerItem, int neededPerLevel) {
    return input(Ingredient.of(item), amountPerItem, neededPerLevel);
  }

  /**
   * Adds an input to the recipe
   * @param tag            Tag input
   * @param amountPerItem  Amount each item matches
   * @param neededPerLevel Total number needed for this modifier
   * @return  Builder instance
   */
  default T input(TagKey<Item> tag, int amountPerItem, int neededPerLevel) {
    return input(Ingredient.of(tag), amountPerItem, neededPerLevel);
  }
  
  /** Valiates the arguments to {@link #input(Ingredient, int, int)} */
  static void validate(int amountPerItem, int neededPerLevel) {
    if (amountPerItem < 1) {
      throw new IllegalArgumentException("Amount per item must be at least 1");
    }
    if (neededPerLevel <= amountPerItem) {
      throw new IllegalArgumentException("Needed per level must be greater than amount per item");
    }
  }

  /** Sets the leftover to the given output */
  T leftover(ItemOutput leftover);

  /** Sets the leftover to the given stack */
  default T leftover(ItemStack stack) {
    return leftover(ItemOutput.fromStack(stack));
  }

  /** Sets the leftover to the given item */
  default T leftover(ItemLike item) {
    return leftover(ItemOutput.fromItem(item));
  }
}
