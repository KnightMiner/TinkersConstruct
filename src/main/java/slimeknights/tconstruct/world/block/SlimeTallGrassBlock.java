package slimeknights.tconstruct.world.block;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.IShearable;
import slimeknights.tconstruct.common.TinkerTags;

import javax.annotation.Nullable;
import java.util.List;

/** Variant of {@link net.minecraft.world.level.block.TallGrassBlock} for slime foliage */
public class SlimeTallGrassBlock extends BushBlock implements IShearable {
  private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

  @Getter
  private final FoliageType foliageType;
  public SlimeTallGrassBlock(Properties properties, FoliageType foliageType) {
    super(properties);
    this.foliageType = foliageType;
  }

  @Override
  protected MapCodec<? extends BushBlock> codec() {
    throw new UnsupportedOperationException("Mojang deletes this method shortly after they add it.");
  }

  @Deprecated
  @Override
  public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
    return SHAPE;
  }

  @Override
  public List<ItemStack> onSheared(@Nullable Player player, ItemStack item, Level level, BlockPos pos) {
    return Lists.newArrayList(new ItemStack(this, 1));
  }

  @Override
  protected boolean mayPlaceOn(BlockState state, BlockGetter worldIn, BlockPos pos) {
    return state.is(TinkerTags.Blocks.SLIMY_SOIL);
  }
}
