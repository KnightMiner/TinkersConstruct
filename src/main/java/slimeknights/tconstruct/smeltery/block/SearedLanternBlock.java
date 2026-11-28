package slimeknights.tconstruct.smeltery.block;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import slimeknights.tconstruct.smeltery.block.component.ITankBlock;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock;
import slimeknights.tconstruct.smeltery.block.entity.ITankBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.LanternBlockEntity;

import javax.annotation.Nullable;

import static slimeknights.tconstruct.smeltery.block.component.SearedTankBlock.LIGHT;

/** Light giving block that holds fluid */
public class SearedLanternBlock extends LanternBlock implements ITankBlock, EntityBlock {
  @Getter
  private final int capacity;
  public SearedLanternBlock(Properties properties, int capacity) {
    super(properties);
    this.capacity = capacity;
    registerDefaultState(defaultBlockState().setValue(LIGHT, 0));
  }

  @Override
  protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(LIGHT);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new LanternBlockEntity(pos, state, this);
  }

  @Nullable
  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    BlockState state = super.getStateForPlacement(context);
    if (state != null) {
      return SearedTankBlock.setLightLevel(state, context);
    }
    return null;
  }

  @Override
  protected boolean hasAnalogOutputSignal(BlockState state) {
    return true;
  }

  @Override
  protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
    return ITankBlockEntity.getComparatorInputOverride(level, pos);
  }

  @Override
  public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
    return ITankBlockEntity.getCloneItemStack(new ItemStack(this), level, pos);
  }
}
