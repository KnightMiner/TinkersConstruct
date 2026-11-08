package slimeknights.tconstruct.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.common.TinkerTags;

/** Sapling that may only be placed on slimy dirt or slimy grass. */
public class SlimeSaplingBlock extends SaplingBlock {
  public SlimeSaplingBlock(TreeGrower tree, Properties properties) {
    super(tree, properties);
  }

  @Override
  protected boolean mayPlaceOn(BlockState state, BlockGetter worldIn, BlockPos pos) {
    return state.is(TinkerTags.BlockItems.SLIMY_SOIL.block());
  }
}
