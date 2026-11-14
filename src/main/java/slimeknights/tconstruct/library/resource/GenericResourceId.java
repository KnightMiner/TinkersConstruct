package slimeknights.tconstruct.library.resource;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/** Generic implementation of {@link ResourceId}, used when extra methods are not required. */
record GenericResourceId<T>(ResourceLocation id) implements ResourceId<T> {
  /** Resource ID codec instance with no specific type. See {@link ResourceId#codec()}. */
  static Codec<?> CODEC = ResourceId.codec(ResourceId::create);
  /** Resource ID stream codec instance with no specific type. See {@link ResourceId#streamCodec()}. */
  static StreamCodec<ByteBuf,?> STREAM_CODEC = ResourceId.streamCodec(ResourceId::create);

  @Override
  public String toString() {
    return id.toString();
  }
}
