package slimeknights.tconstruct.library.codec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/** Same as {@link com.mojang.serialization.codecs.DispatchedMapCodec} but for {@link StreamCodec} */
public record DispatchedMapStreamCodec<B extends FriendlyByteBuf,K,V>(StreamCodec<? super B,K> keyCodec, Function<K, StreamCodec<? super B, ? extends V>> valueCodecFunction) implements StreamCodec<B,Map<K,V>> {
  @SuppressWarnings("unchecked")
  @Override
  public void encode(B buffer, Map<K, V> map) {
    buffer.writeVarInt(map.size());
    for (Map.Entry<K, V> entry : map.entrySet()) {
      keyCodec.encode(buffer, entry.getKey());
      ((StreamCodec<B, V>)valueCodecFunction.apply(entry.getKey())).encode(buffer, entry.getValue());
    }
  }

  @Override
  public Map<K, V> decode(B buffer) {
    int size = buffer.readVarInt();
    Map<K, V> result = new HashMap<>(size);
    for (int i = 0; i < size; i++) {
      K key = keyCodec.decode(buffer);
      V value = valueCodecFunction.apply(key).decode(buffer);
      result.put(key, value);
    }
    return Map.copyOf(result);
  }
}
