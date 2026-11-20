package slimeknights.tconstruct.tables.block;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.util.RetexturedHelper;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity;

import javax.annotation.Nullable;

public class TinkerStationBlock extends RetexturedTableBlock {
  @Getter
  private final int slotCount;

  public TinkerStationBlock(Properties builder, int slotCount) {
    super(builder);
    this.slotCount = slotCount;
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
    return new TinkerStationBlockEntity(pPos, pState, getSlotCount());
  }


  /* Material texture */

  @Override
  public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
    ItemStack stack = new ItemStack(state.getBlock());
    if (level.getBlockEntity(pos) instanceof TinkerStationBlockEntity be) {
      Block block = be.getTexture();
      if (block != Blocks.AIR) {
        RetexturedHelper.setTexture(stack, block);
      } else {
        stack = IMaterialItem.withMaterial(stack, be.getMaterial());
      }
    }
    return stack;
  }
}
