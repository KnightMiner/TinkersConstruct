package slimeknights.tconstruct.common.registration.object;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.registration.object.EnumObject;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Extension of {@link EnumObject} for {@link Item} that allows using a specific type. */
public class EnumItem<E extends Enum<E>,T extends Item> extends EnumObject<E,Item> {
  protected EnumItem(Map<E,Holder<Item>> map) {
    super(map);
  }

  public EnumItem(EnumObject<E,Item> object) {
    super(object);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T get(E value) {
    return (T) super.get(value);
  }

  @SuppressWarnings("unchecked")
  @Nullable
  @Override
  public T getOrNull(E value) {
    return (T) super.getOrNull(value);
  }

  /** Same as {@link #forEach(Consumer)} but uses the type. */
  @SuppressWarnings("unchecked")
  public void forEachTyped(Consumer<? super T> consumer) {
    forEach(value -> consumer.accept((T) value));
  }

  /** Same as {@link #forEach(BiConsumer)} but uses the type. */
  @SuppressWarnings("unchecked")
  public void forEachTyped(BiConsumer<E, ? super T> consumer) {
    forEach((key, value) -> consumer.accept(key, (T) value));
  }


  /** Creates a new builder for an {@link EnumItem} */
  public static <E extends Enum<E>, T extends Item> Builder<E,T> itemBuilder(Class<E> clazz) {
    return new Builder<>(clazz);
  }

  /**
   * Enum object builder, to more conveniently create it from blocks, a map, or another enum object
   * @param <E>  Enum type
   * @param <T>  Item type
   */
  public static class Builder<E extends Enum<E>,T extends Item> {
    private final Map<E, Holder<Item>> map;

    private Builder(Class<E> clazz) {
      this.map = new EnumMap<>(clazz);
    }

    /**
     * Adds the given key and value to the object
     * @param key    Key
     * @param value  Value
     * @return  Builder instance
     */
    public Builder<E,T> put(E key, DeferredHolder<Item,? extends T> value) {
      this.map.put(key, value);
      return this;
    }

    /**
     * Adds all values from the given enum object
     * @param object  Enum object
     * @return  Builder instance
     */
    public Builder<E,T> putAll(EnumItem<E,? extends T> object) {
      for (Map.Entry<E,Holder<Item>> entry : object.entries()) {
        map.put(entry.getKey(), entry.getValue());
      }
      return this;
    }

    /**
     * Creates the final enum object
     * @return  Constructed enum object
     */
    public EnumItem<E,T> build() {
      return new EnumItem<>(map);
    }
  }
}
