package slimeknights.tconstruct.library.codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Extra stream codecs used by tinkers. */
public class TinkerStreamCodecs {
  /** Codec syncing a block state as an integer. */
  public static final StreamCodec<ByteBuf, BlockState> BLOCK_STATE = ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY);

  /** Codec for a holder set of instruments. Used in {@link slimeknights.tconstruct.library.recipe.ingredient.InstrumentIngredient} */
  public static final StreamCodec<RegistryFriendlyByteBuf, HolderSet<Instrument>> INSTRUMENT_SET = ByteBufCodecs.holderSet(Registries.INSTRUMENT);
}
