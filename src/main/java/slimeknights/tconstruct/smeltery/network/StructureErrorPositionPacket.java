package slimeknights.tconstruct.smeltery.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.BlockEntityPacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.smeltery.block.entity.controller.HeatingStructureBlockEntity;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Packet to tell a multiblock to render a specific position as the cause of the error, or to clear the error.
 */
public record StructureErrorPositionPacket(BlockPos controllerPos, Optional<BlockPos> errorPos) implements BlockEntityPacket<HeatingStructureBlockEntity> {
  public static final Type<StructureErrorPositionPacket> TYPE = new Type<>(TConstruct.getResource("structure_error_position"));
  public static final StreamCodec<RegistryFriendlyByteBuf,StructureErrorPositionPacket> CODEC = StreamCodec.composite(
    BlockPos.STREAM_CODEC, StructureErrorPositionPacket::controllerPos,
    ByteBufCodecs.optional(BlockPos.STREAM_CODEC), StructureErrorPositionPacket::errorPos,
    StructureErrorPositionPacket::new);

  public StructureErrorPositionPacket(BlockPos controllerPos, @Nullable BlockPos errorPos) {
    this(controllerPos, Optional.ofNullable(errorPos));
  }

  @Override
  public Type<StructureErrorPositionPacket> type() {
    return TYPE;
  }

  @Override
  public BlockPos pos() {
    return controllerPos;
  }

  @Override
  public Class<HeatingStructureBlockEntity> blockEntityType() {
    return HeatingStructureBlockEntity.class;
  }

  @Override
  public void handleBlockEntity(IPayloadContext context, HeatingStructureBlockEntity be) {
    be.setErrorPos(errorPos.orElse(null));
  }
}
