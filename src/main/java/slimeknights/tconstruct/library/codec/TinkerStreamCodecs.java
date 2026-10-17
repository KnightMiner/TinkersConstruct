package slimeknights.tconstruct.library.codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Extra stream codecs used by tinkers. */
public class TinkerStreamCodecs {
  /** Codec syncing a block state as an integer. */
  public static final StreamCodec<ByteBuf, BlockState> BLOCK_STATE = ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY);
}
