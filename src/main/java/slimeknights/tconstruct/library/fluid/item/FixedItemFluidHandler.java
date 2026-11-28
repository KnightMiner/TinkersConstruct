package slimeknights.tconstruct.library.fluid.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import slimeknights.tconstruct.library.TinkerData;

/** Fluid handler for an item with a fixed fluid tank size using {@link FluidComponents} and {@link slimeknights.tconstruct.library.TinkerData#FLUID}. */
@AllArgsConstructor
public class FixedItemFluidHandler implements IFluidHandlerItem {
  @Getter
  private final ItemStack container;
  private final int capacity;


  /* Tank properties */

  @Override
  public int getTanks() {
    return 1;
  }

  @Override
  public boolean isFluidValid(int tank, FluidStack stack) {
    return true;
  }

  /** Gets the stack size sensitive capacity of the container */
  private int getCapacity() {
    // scale up by the stack size to prevent dupes with people trying to fill a stack of containers
    return capacity * container.getCount();
  }

  @Override
  public int getTankCapacity(int tank) {
    return getCapacity();
  }

  /** Gets the current tank contents */
  private FluidComponents getContents() {
    return FluidComponents.get(container);
  }

  @Override
  public FluidStack getFluidInTank(int tank) {
    FluidComponents fluid = getContents();
    return fluid.isEmpty() ? FluidStack.EMPTY : fluid.toStack(getCapacity());
  }


  /* Interaction */

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    // must not be filled, must have enough
    int capacity = getCapacity();
    if (!getContents().isEmpty() || resource.getAmount() < capacity) {
      return 0;
    }
    // update fluid and return
    if (action.execute()) {
      // this is not size sensitive so no need to shrink resource for stack size
      container.set(TinkerData.FLUID, FluidComponents.of(resource));
    }
    return capacity;
  }

  @Override
  public FluidStack drain(FluidStack resource, FluidAction action) {
    // must be draining at least an ingot
    int capacity = getCapacity();
    if (resource.isEmpty() || resource.getAmount() < capacity) {
      return FluidStack.EMPTY;
    }
    // must have a fluid, must match what they are draining (including components)
    FluidComponents fluid = getContents();
    if (fluid.isEmpty() || !fluid.isSameFluidSameComponents(resource)) {
      return FluidStack.EMPTY;
    }
    // output 1 ingot times stack size
    FluidStack output = fluid.toStack(capacity);
    if (action.execute()) {
      container.remove(TinkerData.FLUID);
    }
    return output;
  }

  @Override
  public FluidStack drain(int maxDrain, FluidAction action) {
    // must be draining at least an ingot
    int capacity = getCapacity();
    if (maxDrain < capacity) {
      return FluidStack.EMPTY;
    }
    // must have a fluid
    FluidComponents fluid = getContents();
    if (fluid.isEmpty()) {
      return FluidStack.EMPTY;
    }
    // output 1 ingot
    FluidStack output = fluid.toStack(capacity);
    if (action.execute()) {
      container.remove(TinkerData.FLUID);
    }
    return output;
  }
}
