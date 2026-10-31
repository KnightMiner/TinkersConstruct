package slimeknights.tconstruct.smeltery.block.entity.inventory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.tconstruct.library.recipe.casting.CastingRecipeInput;
import slimeknights.tconstruct.smeltery.block.entity.CastingBlockEntity;

/**
 * Provides read only access to the input of a casting table. Prevents extra data from leaking
 */
@RequiredArgsConstructor
public class CastingRecipeInputWrapper implements CastingRecipeInput {
  private final CastingBlockEntity tile;
  @Setter @Getter
  private FluidStack fluid;
  private boolean switchSlots = false;

  @Override
  public ItemStack getItem() {
    ItemStack stack = tile.getItem(switchSlots ? CastingBlockEntity.OUTPUT : CastingBlockEntity.INPUT);
    if (stack.is(tile.getEmptyCastTag())) {
      return ItemStack.EMPTY;
    }
    return stack;
  }

  /** Uses the input for input (default) */
  public void useInput() {
    switchSlots = false;
  }

  /** Uses the output for input (for multistep casting) */
  public void useOutput() {
    switchSlots = true;
  }
}
