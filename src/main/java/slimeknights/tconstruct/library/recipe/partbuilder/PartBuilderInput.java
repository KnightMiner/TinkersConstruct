package slimeknights.tconstruct.library.recipe.partbuilder;

import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.recipe.input.SingleItemInput;
import slimeknights.tconstruct.library.recipe.material.IMaterialValue;

import javax.annotation.Nullable;

/**
 * Inventory interface for the part builder
 */
public interface PartBuilderInput extends SingleItemInput {
  /**
   * Gets the material recipe based on the current slot contents
   * @return  Material recipe, or null if the slot contents are not a valid material
   */
  @Nullable
  IMaterialValue getMaterial();

  /**
   * Gets the stack in the pattern slot
   * @return  Pattern slot stack
   */
  ItemStack getPatternStack();

  /*
   * Gets the currently selected pattern item
   * @return
   */
  //ResourceLocation getPattern();

  /** @deprecated use {{@link #getItem()}} */
  @Deprecated
  @Override
  default ItemStack getItem(int index) {
    return switch (index) {
      case 0 -> getItem();
      case 1 -> getPatternStack();
      default -> ItemStack.EMPTY;
    };
  }

  @Override
  default boolean isEmpty() {
    return getItem().isEmpty() && getPatternStack().isEmpty();
  }

  /** @deprecated always 2, not useful */
  @Deprecated
  @Override
  default int size() {
    return 2;
  }
}
