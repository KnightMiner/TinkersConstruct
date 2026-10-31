package slimeknights.tconstruct.library.recipe.casting;

import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.recipe.input.SingleItemInput;

/**
 * Inventory containing a single item and a fluid
 */
public interface CastingRecipeInput extends SingleItemInput {
  /**
   * Gets the contained fluid in this inventory
   * @return  Contained fluid
   */
  FluidStack getFluid();
}
