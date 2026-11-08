package slimeknights.tconstruct.gadgets.item;

import com.mojang.datafixers.util.Function3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.shared.item.SelfTooltipItem;

import javax.annotation.Nonnull;

public class FancyItemFrameItem extends SelfTooltipItem {
  private final Function3<Level, BlockPos, Direction, ? extends HangingEntity> entityProvider;
  public FancyItemFrameItem(Properties props, Function3<Level, BlockPos, Direction, ? extends HangingEntity> entityProvider) {
    super(props);
    this.entityProvider = entityProvider;
  }

  @Override
  @Nonnull
  public InteractionResult useOn(UseOnContext context) {
    BlockPos pos = context.getClickedPos();
    Direction facing = context.getClickedFace();
    BlockPos placeLocation = pos.relative(facing);
    Player player = context.getPlayer();
    ItemStack stack = context.getItemInHand();
    if (player != null && !this.canPlace(player, facing, stack, placeLocation)) {
      return InteractionResult.FAIL;
    }

    Level world = context.getLevel();
    HangingEntity frame = this.entityProvider.apply(world, placeLocation, facing);
    CustomData customData = stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
    if (!customData.isEmpty()) {
      EntityType.updateCustomEntityTag(world, player, frame, customData);
    }

    if (frame.survives()) {
      if (!world.isClientSide) {
        frame.playPlacementSound();
        world.addFreshEntity(frame);
      }
      stack.shrink(1);
      return InteractionResult.sidedSuccess(world.isClientSide);
    }
    return InteractionResult.CONSUME;
  }

  private boolean canPlace(Player player, Direction facing, ItemStack stack, BlockPos pos) {
    return !player.level().isOutsideBuildHeight(pos) && player.mayUseItemAt(pos, facing, stack);
  }
}
