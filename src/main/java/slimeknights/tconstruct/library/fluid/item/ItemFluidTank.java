package slimeknights.tconstruct.library.fluid.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.fluid.SimpleFluidTank;

/** Simple tank implementation handling unstacked fluid containers. */
@RequiredArgsConstructor
public class ItemFluidTank implements SimpleFluidTank, IFluidHandlerItem {
  @Getter
  protected final ItemStack container;
  @Getter
  private final int capacity;

  /** Gets the contents of the tank */
  protected SimpleFluidContent getContents() {
    return container.getOrDefault(TinkerData.TANK, SimpleFluidContent.EMPTY);
  }

  @Override
  public void setFluid(FluidStack fluid) {
    if (container.isEmpty()) {
      container.remove(TinkerData.TANK);
    } else {
      container.set(TinkerData.TANK, SimpleFluidContent.copyOf(fluid));
    }
  }

  @Override
  public FluidStack getFluid() {
    return getContents().copy();
  }


  /* Make some methods more efficient */

  @Override
  public boolean isEmpty() {
    return getContents().isEmpty();
  }

  @Override
  public int getFluidAmount() {
    return getContents().getAmount();
  }
}
