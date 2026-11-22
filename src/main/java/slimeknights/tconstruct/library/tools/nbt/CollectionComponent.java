package slimeknights.tconstruct.library.tools.nbt;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.common.MutableDataComponentHolder;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/** Interface for a component that may be empty */
public interface CollectionComponent {

  /** If true, this component is empty and should not be serialized. */
  boolean isEmpty();

  /**
   * Sets the component value, or removes it if its empty.
   * @return Previous component value
   */
  @Nullable
  static <T extends CollectionComponent> T set(MutableDataComponentHolder holder, DataComponentType<T> type, T value) {
    if (value.isEmpty()) {
      return holder.remove(type);
    } else {
      return holder.set(type, value);
    }
  }

  /**
   * Sets the component value, or removes it if its empty.
   * @return Previous component value
   */
  @Nullable
  static <T extends CollectionComponent> T set(MutableDataComponentHolder holder, Supplier<? extends DataComponentType<T>> type, T value) {
    return set(holder, type.get(), value);
  }
}
