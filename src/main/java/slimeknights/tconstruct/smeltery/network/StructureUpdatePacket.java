package slimeknights.tconstruct.smeltery.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.BlockEntityPacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.smeltery.block.entity.controller.HeatingStructureBlockEntity;

import java.util.List;

/**
 * Packet sent when the smeltery or foundry structure changes
 */
public record StructureUpdatePacket(BlockPos pos, BlockPos minPos, BlockPos maxPos, List<BlockPos> tanks) implements BlockEntityPacket<HeatingStructureBlockEntity> {
  public static final Type<StructureUpdatePacket> TYPE = new Type<>(TConstruct.getResource("structure_update"));
  public static final StreamCodec<ByteBuf,StructureUpdatePacket> CODEC = StreamCodec.composite(
    BlockPos.STREAM_CODEC, StructureUpdatePacket::pos,
    BlockPos.STREAM_CODEC, StructureUpdatePacket::minPos,
    BlockPos.STREAM_CODEC, StructureUpdatePacket::maxPos,
    BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), StructureUpdatePacket::tanks,
    StructureUpdatePacket::new);

  @Override
  public Type<StructureUpdatePacket> type() {
    return TYPE;
  }

  @Override
  public Class<HeatingStructureBlockEntity> blockEntityType() {
    return HeatingStructureBlockEntity.class;
  }

  @Override
  public void handleBlockEntity(IPayloadContext context, HeatingStructureBlockEntity be) {
    be.setStructureSize(minPos, maxPos, tanks);
  }
}
