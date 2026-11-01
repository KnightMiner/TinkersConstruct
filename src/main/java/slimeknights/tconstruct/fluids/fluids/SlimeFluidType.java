package slimeknights.tconstruct.fluids.fluids;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.tconstruct.common.TinkerTags;

/** Fluid Type that does not affect slimes */
public class SlimeFluidType extends FluidType {
  public SlimeFluidType(Properties properties) {
    super(properties);
  }

  @Override
  public boolean canDrownIn(LivingEntity entity) {
    return !entity.getType().is(TinkerTags.EntityTypes.SLIMES);
  }
}
