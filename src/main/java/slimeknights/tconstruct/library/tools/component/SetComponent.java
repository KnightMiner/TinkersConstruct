package slimeknights.tconstruct.library.tools.component;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.codec.MantleStreamCodecs;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/** Holds a set as an immutable component. */
public record SetComponent<T>(Set<T> set) implements CollectionComponent {
  private static final SetComponent<?> EMPTY = new SetComponent<>(Set.of());

  /** @apiNote use {@link #create(Collection)} */
  @Internal
  public SetComponent {}

  /** Gets the empty instance for a given type */
  @SuppressWarnings("unchecked")
  public static <T> SetComponent<T> empty() {
    return (SetComponent<T>) EMPTY;
  }

  /** Creates a new instance from the given set */
  public static <T> SetComponent<T> create(Collection<T> set) {
    if (set.isEmpty()) {
      return empty();
    }
    return new SetComponent<>(ImmutableSet.copyOf(set));
  }

  /** Checks if the map has the given key */
  public boolean contains(T value) {
    return set.contains(value);
  }

  @Override
  public boolean isEmpty() {
    return set.isEmpty();
  }

  /** Creates a copy of the set with the given value */
  public SetComponent<T> with(T value) {
    // don't create a new instance if the value is already in the set
    if (contains(value)) {
      return this;
    }
    Set<T> set = new LinkedHashSet<>(this.set);
    set.add(value);
    return create(set);
  }

  /** Creates a copy of the set without the given value */
  public SetComponent<T> without(T value) {
    if (isEmpty()) {
      return empty();
    }
    // don't create a new instance if its not in the set
    if (!contains(value)) {
      return this;
    }
    Set<T> set = new LinkedHashSet<>(this.set);
    set.remove(value);
    return create(set);
  }


  /* Component helpers */

  /** Gets the map from the given component holder */
  public static <T> SetComponent<T> get(DataComponentHolder holder, DataComponentType<SetComponent<T>> component) {
    return holder.getOrDefault(component, SetComponent.empty());
  }

  /** Gets the map from the given component holder */
  public static <T> SetComponent<T> get(DataComponentHolder holder, Supplier<DataComponentType<SetComponent<T>>> component) {
    return get(holder, component.get());
  }

  /** Updates the given modifier in the component map */
  public static <T> void add(MutableDataComponentHolder holder, DataComponentType<SetComponent<T>> component, T value) {
    holder.set(component, get(holder, component).with(value));
  }

  /** Updates the given modifier in the component map */
  public static <T> void add(MutableDataComponentHolder holder, Supplier<DataComponentType<SetComponent<T>>> component, T value) {
    add(holder, component.get(), value);
  }

  /** Removes the given modifier from the component map */
  public static <T> void remove(MutableDataComponentHolder holder, DataComponentType<SetComponent<T>> component, T value) {
    CollectionComponent.set(holder, component, get(holder, component).without(value));
  }

  /** Removes the given modifier from the component map */
  public static <T> void remove(MutableDataComponentHolder holder, Supplier<DataComponentType<SetComponent<T>>> component, T value) {
    remove(holder, component.get(), value);
  }


  /* Codec */

  /** Creates a codec for the given value type */
  public static <T> Codec<SetComponent<T>> codec(Codec<T> valueCodec) {
    return Codec.list(valueCodec).xmap(SetComponent::create, m -> List.copyOf(m.set));
  }

  /** Creates a stream codec for the given value type */
  public static <B extends ByteBuf, T> StreamCodec<B, SetComponent<T>> streamCodec(StreamCodec<B,T> valueCodec) {
    return valueCodec.apply(MantleStreamCodecs.set()).map(SetComponent::create, m -> m.set);
  }
}
