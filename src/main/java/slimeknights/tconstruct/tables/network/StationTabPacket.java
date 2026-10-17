package slimeknights.tconstruct.tables.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.mantle.network.PacketHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.tables.block.ITabbedBlock;

/** Packet sent to switch tabs on a tinkers table. */
public record StationTabPacket(BlockPos pos) implements ISimplePacket {
  public static final Type<StationTabPacket> TYPE = new Type<>(TConstruct.getResource("station_tab"));
  public static final StreamCodec<ByteBuf,StationTabPacket> CODEC = BlockPos.STREAM_CODEC.map(StationTabPacket::new, StationTabPacket::pos);

  @Override
  public Type<StationTabPacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    Player player = context.player();
    Level level = player.level();
    if (!level.hasChunkAt(pos)) {
      return;
    }
    // if carrying an item, store it so we can place it in the new container
    ItemStack heldStack = player.containerMenu.getCarried();
    if (!heldStack.isEmpty()) {
      // ensure it does not drop on closing
      player.containerMenu.setCarried(ItemStack.EMPTY);
    }
    BlockState state = level.getBlockState(pos);
    if (state.getBlock() instanceof ITabbedBlock block) {
      block.openGui(player, player.getCommandSenderWorld(), pos);
    } else {
      MenuProvider provider = state.getMenuProvider(level, pos);
      if (provider != null) {
        player.openMenu(provider, pos);
      }
    }
    // place the stack in the new container
    if (!heldStack.isEmpty()) {
      player.containerMenu.setCarried(heldStack);
      PacketHelper.sendVanillaPacket(new ClientboundContainerSetSlotPacket(-1, -1, -1, heldStack), player);
    }
  }
}
