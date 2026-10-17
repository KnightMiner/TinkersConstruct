package slimeknights.tconstruct.smeltery.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.BlockEntityPacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.smeltery.block.entity.ChannelBlockEntity;

/**
 * Packet for when the flowing state changes on a channel side.
 */
@Internal
public record ChannelFlowPacket(BlockPos pos, Direction side, boolean flow) implements BlockEntityPacket<ChannelBlockEntity> {
  public static final Type<ChannelFlowPacket> TYPE = new Type<>(TConstruct.getResource("channel_flow"));
  public static final StreamCodec<ByteBuf,ChannelFlowPacket> CODEC = StreamCodec.composite(
    BlockPos.STREAM_CODEC, ChannelFlowPacket::pos,
    Direction.STREAM_CODEC, ChannelFlowPacket::side,
    ByteBufCodecs.BOOL, ChannelFlowPacket::flow,
    ChannelFlowPacket::new);

  @Override
  public Type<ChannelFlowPacket> type() {
    return TYPE;
  }

  @Override
  public Class<ChannelBlockEntity> blockEntityType() {
    return ChannelBlockEntity.class;
  }

  @Override
  public void handleBlockEntity(IPayloadContext context, ChannelBlockEntity be) {
    be.setFlow(side, flow);
  }
}
