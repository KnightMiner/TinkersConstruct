package slimeknights.tconstruct.library.resource;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.registration.object.IdAwareObject;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Represents a type specific ID of an object. Unlike {@link net.minecraft.resources.ResourceLocation}, provides additional validation.
 * Intentionally distinct from {@link net.minecraft.resources.ResourceKey} for two reasons: makes it easier to add helpers and makes it clear vanilla registry methods do not work.
 * Note for compatability with previous ID expectations, you should override {@code #toString()} to return {@link ResourceLocation#toString()}.
 */
public interface ResourceId<T> extends IdAwareObject, Comparable<ResourceId<T>> {
  /* Helpers */

  /** Gets the namespace for this ID */
  default String getNamespace() {
    return id().getNamespace();
  }

  /** Gets the path for this ID */
  default String getPath() {
    return id().getPath();
  }

  @Override
  default int compareTo(ResourceId<T> other) {
    return id().compareTo(other.id());
  }

  /* Constructors */

  /** Creates a resource ID of the given type */
  static <T> ResourceId<T> create(ResourceLocation id) {
    return new GenericResourceId<>(id);
  }

  /** Creates a resource ID from the given domain and name */
  static <T> ResourceId<T> build(String domain, String name) {
    return create(ResourceLocation.fromNamespaceAndPath(domain, name));
  }

  /**
   * Creates a resource ID from the given string
   * @throws net.minecraft.ResourceLocationException if invalid.
   */
  static <T> ResourceId<T> parse(String string) {
    return create(ResourceLocation.parse(string));
  }

  /** Creates a resource ID from the given string, returning null if invalid */
  @Nullable
  static <T> ResourceId<T> tryParse(String string) {
    ResourceLocation location = ResourceLocation.tryParse(string);
    return location == null ? null : create(location);
  }


  /* Codecs */

  /** Creates a codec for a resource ID */
  static <T extends ResourceId<?>> Codec<T> codec(Function<ResourceLocation,T> constructor) {
    return ResourceLocation.CODEC.xmap(constructor, ResourceId::id);
  }

  /** Gets a codec for a generic resource ID using {@link #create(ResourceLocation)} */
  @SuppressWarnings("unchecked")
  static <T> Codec<ResourceId<T>> codec() {
    return (Codec<ResourceId<T>>) GenericResourceId.CODEC;
  }

  /** Creates a stream codec for a resource ID */
  static <T extends ResourceId<?>> StreamCodec<ByteBuf,T> streamCodec(Function<ResourceLocation,T> constructor) {
    return ResourceLocation.STREAM_CODEC.map(constructor, ResourceId::id);
  }

  /** Gets a stream codec for a generic resource ID using {@link #create(ResourceLocation)} */
  @SuppressWarnings("unchecked")
  static <T> StreamCodec<ByteBuf,ResourceId<T>> streamCodec() {
    return (StreamCodec<ByteBuf,ResourceId<T>>) GenericResourceId.STREAM_CODEC; 
  }
}
