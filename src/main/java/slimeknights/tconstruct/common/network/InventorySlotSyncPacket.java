package slimeknights.tconstruct.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.TConstruct;

/** Packet for syncing a visible item on change. */
public record InventorySlotSyncPacket(ItemStack stack, int slot, BlockPos pos) implements ISimplePacket {
  public static final Type<InventorySlotSyncPacket> TYPE = new Type<>(TConstruct.getResource("inventory_slot_sync"));
  public static final StreamCodec<RegistryFriendlyByteBuf,InventorySlotSyncPacket> CODEC = StreamCodec.composite(
    ItemStack.OPTIONAL_STREAM_CODEC, InventorySlotSyncPacket::stack,
    ByteBufCodecs.VAR_INT, InventorySlotSyncPacket::slot,
    BlockPos.STREAM_CODEC, InventorySlotSyncPacket::pos,
    InventorySlotSyncPacket::new);

  @Override
  public Type<InventorySlotSyncPacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    Level level = context.player().level();
    if (level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null) instanceof IItemHandlerModifiable handler) {
      handler.setStackInSlot(slot, stack.copy());
      BlockState state = level.getBlockState(pos);
      level.sendBlockUpdated(pos, state, state, 0);
    }
  }
}
