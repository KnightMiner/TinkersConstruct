package slimeknights.tconstruct.tools.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.fluid.block.MoveBlocksFluidEffect;

/** Packet handling {@link MoveBlocksFluidEffect} syncing to the client */
public record PushBlockRowPacket(BlockPos pos, Direction direction, boolean push, int moving) implements ISimplePacket {
  public static final Type<PushBlockRowPacket> TYPE = new Type<>(TConstruct.getResource("push_block_row"));
  public static final StreamCodec<ByteBuf,PushBlockRowPacket> CODEC = StreamCodec.composite(
    BlockPos.STREAM_CODEC, PushBlockRowPacket::pos,
    Direction.STREAM_CODEC, PushBlockRowPacket::direction,
    ByteBufCodecs.BOOL, PushBlockRowPacket::push,
    ByteBufCodecs.VAR_INT, PushBlockRowPacket::moving,
    PushBlockRowPacket::new);

  @Override
  public Type<PushBlockRowPacket> type() {
    return TYPE;
  }

  /** Gets the facing value for this packet */
  private Direction facing() {
    return push ? direction : direction.getOpposite();
  }

  @Override
  public void handle(IPayloadContext context) {
    Level level = context.player().level();
    MoveBlocksFluidEffect.moveBlocks(level, pos, level.getBlockState(pos), facing(), direction, moving);
  }
}
