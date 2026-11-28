package slimeknights.tconstruct.library.fluid.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.tconstruct.library.TinkerData;

/** Same as {@link ItemFluidTank} but fills a stack at a time. */
public class StackedItemFluidTank extends ItemFluidTank {
  private final int maxStackSize;
  public StackedItemFluidTank(ItemStack container, int capacity, int maxStackSize) {
    super(container, capacity);
    this.maxStackSize = maxStackSize;
  }

  @Override
  public void setFluid(FluidStack stack) {
    // when setting, divide the amount to set based on the container count
    if (!container.isEmpty()) {
      container.set(TinkerData.TANK, SimpleFluidContent.copyWithAmount(stack, stack.getAmount() / container.getCount()));
    }
  }

  @Override
  public FluidStack getFluid() {
    // when getting, scale by container amount
    SimpleFluidContent contents = getContents();
    return contents.copyWithAmount(contents.getAmount() * container.getCount());
  }

  @Override
  public int getFluidAmount() {
    return getContents().getAmount() * container.getCount();
  }

  @Override
  public boolean isFluidValid(FluidStack stack) {
    // can only fill if the stack size isn't too large
    return container.getCount() <= maxStackSize || stack.isEmpty();
  }

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    // ensure we do not try and fill a partial amount
    int count = container.getCount();
    if (count > 1) {
      int remainder = resource.getAmount() % count;
      if (remainder != 0) {
        resource = resource.copyWithAmount(resource.getAmount() - remainder);
      }
    }
    return super.fill(resource, action);
  }

  @Override
  public FluidStack drain(FluidStack fluid, int maxDrain, FluidAction action) {
    // ensure we do not try and drain a partial amount
    int count = container.getCount();
    if (count > 1) {
      int remainder = maxDrain % count;
      if (remainder != 0) {
        maxDrain -= remainder;
      }
    }
    return super.drain(fluid, maxDrain, action);
  }
}
