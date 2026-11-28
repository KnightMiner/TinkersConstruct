package slimeknights.tconstruct.smeltery.block.component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import slimeknights.mantle.fluid.FluidTransferHelper;
import slimeknights.tconstruct.library.fluid.item.SimpleFluidContent;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.smeltery.block.entity.ITankBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.component.TankBlockEntity;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.function.ToIntFunction;

/** Basic tank block that works in smeltery walls */
public class SearedTankBlock extends SearedBlock implements ITankBlock, EntityBlock {
  public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);
  public static final ToIntFunction<BlockState> LIGHT_GETTER = state -> state.getValue(SearedTankBlock.LIGHT);

  @Getter
  private final int capacity;
  private final PushReaction pushReaction;
  public SearedTankBlock(Properties properties, int capacity, PushReaction pushReaction) {
    super(properties, true);
    this.capacity = capacity;
    this.pushReaction = pushReaction;
    registerDefaultState(defaultBlockState().setValue(LIGHT, 0));
  }

  public SearedTankBlock(Properties properties, int capacity) {
    this(properties, capacity, PushReaction.BLOCK);
  }

  @Override
  protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
    super.createBlockStateDefinition(builder);
    builder.add(LIGHT);
  }

  @Override
  public PushReaction getPistonPushReaction(BlockState pState) {
    return pushReaction;
  }

  @Deprecated
  @Override
  protected float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos) {
    return 1.0F;
  }

  @Override
  @Nullable
  public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
    return new TankBlockEntity(pPos, pState, this);
  }

  @Override
  protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
    if (FluidTransferHelper.interactWithTank(level, pos, player, hand, hit)) {
      return ItemInteractionResult.SUCCESS;
    }
    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
  }

  /** Helper for setting the light level on placement */
  public static BlockState setLightLevel(BlockState state, BlockPlaceContext context) {
    ItemStack stack = context.getItemInHand();
    SimpleFluidContent content = SimpleFluidContent.get(stack);
    if (!content.isEmpty()) {
      state = state.setValue(LIGHT, content.getFluid().getFluidType().getLightLevel(content.copy()));
    }
    return state;
  }

  @Nullable
  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return setLightLevel(this.defaultBlockState(), context);
  }

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

  @AllArgsConstructor
  public enum TankType implements StringRepresentable {
    FUEL_TANK(ITankBlock.DEFAULT_CAPACITY),
    FUEL_GAUGE(ITankBlock.DEFAULT_CAPACITY),
    INGOT_TANK(FluidValues.INGOT * 48),
    INGOT_GAUGE(FluidValues.INGOT * 48);

    @Getter
    private final int capacity;

    @Override
    public String getSerializedName() {
      return this.toString().toLowerCase(Locale.US);
    }
  }
}
