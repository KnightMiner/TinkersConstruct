package slimeknights.tconstruct.library.recipe;

import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.recipe.input.NoItemInput;

/**
 * Inventory containing just a single fluid
 */
public interface SingleFluidInput extends NoItemInput {
  /**
   * Gets the fluid contained in this inventory
   * @return  Contained fluid
   */
  FluidStack getFluid();
}
