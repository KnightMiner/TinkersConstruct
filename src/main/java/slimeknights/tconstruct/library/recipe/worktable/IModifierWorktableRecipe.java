package slimeknights.tconstruct.library.recipe.worktable;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import slimeknights.mantle.recipe.ICommonRecipe;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.RecipeResult;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.library.recipe.TinkerableRecipeInput;
import slimeknights.tconstruct.library.recipe.tinkerstation.TinkerStationRecipeInput;
import slimeknights.tconstruct.library.tools.nbt.LazyToolStack;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Main interface for all recipes in the Modifier Worktable
 */
public interface IModifierWorktableRecipe extends ICommonRecipe<TinkerableRecipeInput> {
  @Override
  default RecipeType<?> getType() {
    return TinkerRecipes.modifierWorktableType.get();
  }

  /** If true, this recipe matches the given inputs, does not consider the modifier button yet */
  @Override
  boolean matches(TinkerableRecipeInput inv, Level world);

  /** Gets the title for display in JEI and in the info panel */
  Component getTitle();

  /**
   * Gets the description of this recipe, or display an error if this recipe matches but currently has no modifiers
   * @param inv  Recipe inventory, null when fetching in JEI
   */
  Component getDescription(@Nullable TinkerableRecipeInput inv);

  /**
   * Gets a list of modifier buttons for the given input. May be empty.
   * It will not be automatically sorted, you must ensure the order is consistent on both client and server.
   * @param inv  Recipe inventory, null when fetching in JEI
   */
  List<ModifierEntry> getModifierOptions(@Nullable TinkerableRecipeInput inv);

  /**
   * Gets the tool stack result for this recipe.
   * @param inv             Inventory instance
   * @param modifier        Modifier that was interacted with
   * @return Tool stack result. Can be the same instance as previousResult or a new instance.
   *         Should never share NBT with {@link TinkerableRecipeInput#getTinkerable()}, needs to be a copy.
   */
  RecipeResult<LazyToolStack> getResult(TinkerableRecipeInput inv, ModifierEntry modifier);

  default int shrinkToolSlotBy(LazyToolStack result) {
    return result.getSize();
  }

  /**
   * Updates the input stacks upon crafting this recipe
   * @param result    Result from {@link #getResult(TinkerableRecipeInput, ModifierEntry)}
   * @param inv       Inventory instance to modify inputs
   * @param isServer  If true, this is on the serverside. Use to handle randomness, {@link TinkerStationRecipeInput.Mutable#giveItem(ItemStack)} should handle being called serverside only
   */
  default void updateInputs(LazyToolStack result, TinkerableRecipeInput.Mutable inv, ModifierEntry selected, boolean isServer) {
    // shrink all stacks by 1
    for (int index = 0; index < inv.getInputCount(); index++) {
      inv.shrinkInput(index, 1);
    }
  }

  /** Gets input tool options, need not be rendered with the modifiers, simply be valid tools */
  List<ItemStack> getInputTools();

  /**
   * Gets an ingredients to display in JEI.
   * @param  slot  Slot index to display
   * @return  Display item list, or {@link List#of()} if an invalid index.
   */
  List<ItemStack> getDisplayItems(int slot);

  /** Gets the number of inputs for this recipe */
  int getInputCount();

  /** If true, the input in the tool slot is consumed rather than being a catalyst. */
  default boolean isToolInput() {
    return false;
  }

  /** If true, attempts a focus link on the tools and modifier slots */
  default boolean linkToolsModifiers() {
    return false;
  }

  /** If true, the recipe modifier is an output */
  default boolean isModifierOutput() {
    return false;
  }


  /** Deprecated methods to ignore */

  @Override
  @Deprecated
  default ItemStack getResultItem(HolderLookup.Provider registries) {
    return ItemStack.EMPTY;
  }

  @Deprecated
  @Override
  default ItemStack assemble(TinkerableRecipeInput inv, HolderLookup.Provider registries) {
    return ItemStack.EMPTY;
  }
}
