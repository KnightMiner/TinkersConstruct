package slimeknights.tconstruct.library.tools.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.registration.object.IdAwareObject;
import slimeknights.tconstruct.library.codec.TinkerStreamCodecs;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/** Holds data as a map from modifier ID to entry. */
public record ResourceMap<T>(Map<ResourceLocation,T> map) implements CollectionComponent {
  private static final ResourceMap<?> EMPTY = new ResourceMap<>(Map.of());

  /** @apiNote use {@link #create(Map)} */
  @Internal
  public ResourceMap {}

  /** Gets the empty instance for a given type */
  @SuppressWarnings("unchecked")
  public static <T> ResourceMap<T> empty() {
    return (ResourceMap<T>) EMPTY;
  }

  /** Creates a new instance from the given map */
  public static <T> ResourceMap<T> create(Map<ResourceLocation,T> map) {
    if (map.isEmpty()) {
      return empty();
    }
    return new ResourceMap<>(Map.copyOf(map));
  }

  /** Checks if the map has the given key */
  public boolean has(ResourceLocation id) {
    return map.containsKey(id);
  }

  /** Checks if the map has the given key */
  public boolean has(IdAwareObject id) {
    return has(id.id());
  }

  /** Gets the value at the given modifier ID, or null if absent */
  @Nullable
  public T get(ResourceLocation id) {
    return map.get(id);
  }

  /** Gets the value at the given modifier ID, or null if absent */
  @Nullable
  public T get(IdAwareObject id) {
    return get(id.id());
  }

  /** Gets the value at the given modifier ID, or {@code defaultValue} if absent. */
  public T getOrDefault(ResourceLocation id, T defaultValue) {
    return map.getOrDefault(id, defaultValue);
  }

  /** Gets the value at the given modifier ID, or {@code defaultValue} if absent. */
  public T getOrDefault(IdAwareObject id, T defaultValue) {
    return getOrDefault(id.id(), defaultValue);
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  /** Creates a copy of the map with the given id and value */
  public ResourceMap<T> with(ResourceLocation id, T value) {
    // don't create a new instance if the value is already in the map
    if (Objects.equals(get(id), value)) {
      return this;
    }
    Map<ResourceLocation,T> map = new HashMap<>(this.map);
    map.put(id, value);
    return create(map);
  }

  /** Creates a copy of the map with the given id and value */
  public ResourceMap<T> with(IdAwareObject id, T value) {
    return with(id.id(), value);
  }

  /** Creates a copy of the map without the given id */
  public ResourceMap<T> without(ResourceLocation id) {
    if (isEmpty()) {
      return empty();
    }
    // don't create a new instance of the ID is already absent
    if (!has(id)) {
      return this;
    }
    Map<ResourceLocation,T> map = new HashMap<>(this.map);
    map.remove(id);
    return create(map);
  }

  /** Creates a copy of the map without the given modifier */
  public ResourceMap<T> without(IdAwareObject id) {
    return without(id.id());
  }

  /** Creates a new builder from this slot data */
  public Builder<T> toBuilder() {
    return new Builder<T>().putAll(this);
  }


  /* Component helpers */

  /** Gets the map from the given component holder */
  public static <T> ResourceMap<T> get(DataComponentHolder holder, DataComponentType<ResourceMap<T>> component) {
    return holder.getOrDefault(component, ResourceMap.empty());
  }

  /** Gets the map from the given component holder */
  public static <T> ResourceMap<T> get(DataComponentHolder holder, Supplier<DataComponentType<ResourceMap<T>>> component) {
    return get(holder, component.get());
  }

  // put

  /** Updates the given modifier in the component map */
  public static <T> void put(MutableDataComponentHolder holder, DataComponentType<ResourceMap<T>> component, ResourceLocation id, T value) {
    holder.set(component, get(holder, component).with(id, value));
  }

  /** Updates the given modifier in the component map */
  public static <T> void put(MutableDataComponentHolder holder, DataComponentType<ResourceMap<T>> component, IdAwareObject id, T value) {
    put(holder, component, id.id(), value);
  }

  /** Updates the given modifier in the component map */
  public static <T> void put(MutableDataComponentHolder holder, Supplier<DataComponentType<ResourceMap<T>>> component, ResourceLocation id, T value) {
    put(holder, component.get(), id, value);
  }

  /** Updates the given modifier in the component map */
  public static <T> void put(MutableDataComponentHolder holder, Supplier<DataComponentType<ResourceMap<T>>> component, IdAwareObject id, T value) {
    put(holder, component.get(), id.id(), value);
  }

  // remove

  /** Removes the given modifier from the component map */
  public static <T> void remove(MutableDataComponentHolder holder, DataComponentType<ResourceMap<T>> component, ResourceLocation id) {
    CollectionComponent.set(holder, component, get(holder, component).without(id));
  }

  /** Removes the given modifier from the component map */
  public static <T> void remove(MutableDataComponentHolder holder, DataComponentType<ResourceMap<T>> component, IdAwareObject id) {
    remove(holder, component, id.id());
  }

  /** Removes the given modifier from the component map */
  public static <T> void remove(MutableDataComponentHolder holder, Supplier<DataComponentType<ResourceMap<T>>> component, ResourceLocation id) {
    remove(holder, component.get(), id);
  }

  /** Removes the given modifier from the component map */
  public static <T> void remove(MutableDataComponentHolder holder, Supplier<DataComponentType<ResourceMap<T>>> component, IdAwareObject id) {
    remove(holder, component.get(), id.id());
  }


  /* Codec */

  /** Creates a codec for the given value type */
  public static <T> Codec<ResourceMap<T>> codec(Codec<T> valueCodec) {
    return Codec.unboundedMap(ResourceLocation.CODEC, valueCodec).xmap(ResourceMap::create, m -> m.map);
  }

  /** Creates a stream codec for the given value type */
  public static <B extends ByteBuf, T> StreamCodec<B, ResourceMap<T>> streamCodec(StreamCodec<B,T> valueCodec) {
    return TinkerStreamCodecs.map(ResourceLocation.STREAM_CODEC, valueCodec).map(ResourceMap::create, m -> m.map);
  }


  /* Builder */

  /** Creates a new builder */
  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  /** Builder for the map */
  public static class Builder<T> {
    private final Map<ResourceLocation,T> map = new HashMap<>();

    private Builder() {}

    /** Adds the given value to the builder */
    public Builder<T> put(ResourceLocation id, T value) {
      map.put(id, value);
      return this;
    }

    /** Adds the given value to the builder */
    public Builder<T> put(IdAwareObject id, T value) {
      return put(id.id(), value);
    }

    /** Removes the given modifier from the builder */
    public Builder<T> remove(ResourceLocation id) {
      map.remove(id);
      return this;
    }

    /** Removes the given modifier from the builder */
    public Builder<T> remove(IdAwareObject id) {
      return remove(id.id());
    }

    /** Adds all values from the given map */
    public Builder<T> putAll(Map<ResourceLocation,T> map) {
      this.map.putAll(map);
      return this;
    }

    /** Adds all values from the given map */
    public Builder<T> putAll(ResourceMap<T> map) {
      return putAll(map.map);
    }

    /** Builds the final slots instance */
    public ResourceMap<T> build() {
      return create(map);
    }
  }
}
