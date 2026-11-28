package slimeknights.tconstruct.library.fluid;

import lombok.Getter;
import lombok.Setter;
import slimeknights.mantle.block.entity.MantleBlockEntity;

/** Fluid tank that smoothly updates the fluid position */
public class FluidTankAnimated extends FluidTankBase<MantleBlockEntity> {
  @Getter @Setter
  private float renderOffset;

  public FluidTankAnimated(int capacity, MantleBlockEntity parent) {
    super(capacity, parent);
  }
}
