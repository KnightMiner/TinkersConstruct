package slimeknights.tconstruct.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.codec.TinkerStreamCodecs;

/**
 * Packet to notify neighbors that a block changed, used when breaking blocks in weird contexts that vanilla suppresses updates in for some reason
 */
public record UpdateNeighborsPacket(BlockState state, BlockPos pos) implements ISimplePacket {
  public static final Type<UpdateNeighborsPacket> TYPE = new Type<>(TConstruct.getResource("update_neighbors"));
  public static final StreamCodec<ByteBuf,UpdateNeighborsPacket> CODEC = StreamCodec.composite(
    TinkerStreamCodecs.BLOCK_STATE, UpdateNeighborsPacket::state,
    BlockPos.STREAM_CODEC, UpdateNeighborsPacket::pos,
    UpdateNeighborsPacket::new);

  @Override
  public Type<UpdateNeighborsPacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    Level level = context.player().level();
    state.updateNeighbourShapes(level, pos, Block.UPDATE_CLIENTS, 511);
    state.updateIndirectNeighbourShapes(level, pos, Block.UPDATE_CLIENTS, 511);
  }
}
