package slimeknights.tconstruct.library.recipe.tinkerstation;

import slimeknights.tconstruct.library.recipe.TinkerableRecipeInput;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;

import javax.annotation.Nullable;

/**
 * Inventory interface for all Tinker Station containers to use
 */
public interface TinkerStationRecipeInput extends TinkerableRecipeInput {
  /**
   * Gets material recipe for the given slot
   * @return material recipe for the given slot, null if the slot has no material recipe
   */
  @Nullable
  MaterialRecipe getInputMaterial(int index);

  /** Simply combines {@link TinkerStationRecipeInput} and {@link TinkerableRecipeInput.Mutable} */
  interface Mutable extends TinkerStationRecipeInput, TinkerableRecipeInput.Mutable {}
}
