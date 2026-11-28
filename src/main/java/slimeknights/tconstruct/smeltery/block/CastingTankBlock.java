package slimeknights.tconstruct.smeltery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import slimeknights.mantle.block.InventoryBlock;
import slimeknights.tconstruct.smeltery.block.component.ITankBlock;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock;
import slimeknights.tconstruct.smeltery.block.entity.CastingTankBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.ITankBlockEntity;

import javax.annotation.Nullable;

import static slimeknights.tconstruct.smeltery.block.component.SearedTankBlock.LIGHT;

/** Tank that can extract fluids from and insert fluids into items */
public class CastingTankBlock extends InventoryBlock implements ITankBlock, EntityBlock {
  public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

  public CastingTankBlock(Properties properties) {
    super(properties);
    registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIGHT, 0));
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return SearedTankBlock.setLightLevel(defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()), context);
  }

  @Override
  protected BlockState rotate(BlockState state, Rotation rot) {
    return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
  }

  @Override
  protected BlockState mirror(BlockState state, Mirror mirrorIn) {
    return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING, LIGHT);
  }

  @Override
  protected InteractionResult openGui(BlockState state, Level world, BlockPos pos, Player player) {
    return InteractionResult.PASS;
  }

  @Override
  protected float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos) {
    return 1.0F;
  }

  @Override
  @Nullable
  public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
    return new CastingTankBlockEntity(pPos, pState, this);
  }

  @Override
  protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
    if (level.getBlockEntity(pos) instanceof CastingTankBlockEntity tank) {
      tank.interact(player, stack, hand, hit.getLocation().y - pos.getY() < 0.6875);
      return ItemInteractionResult.SUCCESS;
    }
    return ItemInteractionResult.FAIL;
  }


  /* Redstone interaction */

  @Override
  protected void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
    if (!worldIn.isClientSide() && worldIn.getBlockEntity(pos) instanceof CastingTankBlockEntity tank) {
      tank.handleRedstone(worldIn.hasNeighborSignal(pos));
    }
  }

  @Override
  protected void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
    if (!worldIn.isClientSide() && worldIn.getBlockEntity(pos) instanceof CastingTankBlockEntity tank) {
      tank.swap();
    }
  }
  

  /* Comparator support */

  @Override
  protected boolean hasAnalogOutputSignal(BlockState state) {
    return true;
  }

  @Override
  protected int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
    return ITankBlockEntity.getComparatorInputOverride(worldIn, pos);
  }

  @Override
  public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
    return ITankBlockEntity.getCloneItemStack(new ItemStack(this), level, pos);
  }

  @Override
  public int getCapacity() {
    return CastingTankBlockEntity.DEFAULT_CAPACITY;
  }
}
