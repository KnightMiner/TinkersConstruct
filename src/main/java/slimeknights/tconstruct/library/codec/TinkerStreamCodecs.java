package slimeknights.tconstruct.library.codec;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/** Extra stream codecs used by tinkers. */
public class TinkerStreamCodecs {
  /** Codec syncing a block state as an integer. */
  public static final StreamCodec<ByteBuf, BlockState> BLOCK_STATE = ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY);

  /** Stream codec for a unit, used in many data components. */
  public static final StreamCodec<ByteBuf, Unit> UNIT = StreamCodec.unit(Unit.INSTANCE);

  /** Codec for a holder set of instruments. Used in {@link slimeknights.tconstruct.library.recipe.ingredient.InstrumentIngredient} */
  public static final StreamCodec<RegistryFriendlyByteBuf, HolderSet<Instrument>> INSTRUMENT_SET = ByteBufCodecs.holderSet(Registries.INSTRUMENT);

  /** Creates a codec of a map from key to value. Simplifies generics on {@link ByteBufCodecs} */
  public static <B extends ByteBuf,K,V> StreamCodec<B, Map<K,V>> map(StreamCodec<? super B,K> keyCodec, StreamCodec<? super B,V> valueCodec) {
    return ByteBufCodecs.map(HashMap::new, keyCodec, valueCodec);
  }

  /** Creates a codec of an object to int map */
  public static <B extends ByteBuf,T> StreamCodec<B,Object2IntMap<T>> object2IntMap(StreamCodec<B,T> codec) {
    return ByteBufCodecs.map(Object2IntOpenHashMap::new, codec, ByteBufCodecs.INT);
  }
}
