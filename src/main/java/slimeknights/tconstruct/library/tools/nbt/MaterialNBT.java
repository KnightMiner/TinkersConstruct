package slimeknights.tconstruct.library.tools.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.materials.MaterialHolder;
import slimeknights.tconstruct.library.materials.MaterialVariant;
import slimeknights.tconstruct.library.materials.MaterialVariantId;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.utils.TagUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * All the materials contained within the tool. Determines a portion of the modifiers, along with the stats.
 */
@EqualsAndHashCode
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MaterialNBT implements Iterable<MaterialVariant>, CollectionComponent {
  /** Instance containing no materials, for errors with parsing NBT */
  public final static MaterialNBT EMPTY = new MaterialNBT(List.of());

  /** Codec instance for NBT */
  public static final Codec<MaterialNBT> CODEC;
  /** Stream codec instance for network */
  public static final StreamCodec<ByteBuf,MaterialNBT> STREAM_CODEC;
  static {
    Function<List<MaterialVariant>,MaterialNBT> constuctor = MaterialNBT::of;
    Function<MaterialNBT,List<MaterialVariant>> getter = m -> m.list;
    CODEC = MaterialVariant.CODEC.listOf().xmap(constuctor, getter);
    STREAM_CODEC = MaterialVariant.STREAM_CODEC.apply(ByteBufCodecs.list()).map(constuctor, getter);
  }

  /** List of materials contained in this NBT */
  @Getter
  private final List<MaterialVariant> list;

  /** Creates a new instance, returning {@link #EMPTY} if no materials are passed. */
  public static MaterialNBT of(List<MaterialVariant> list) {
    if (list.isEmpty()) {
      return EMPTY;
    }
    return new MaterialNBT(List.copyOf(list));
  }

  /** Creates a new material NBT */
  @VisibleForTesting
  public static MaterialNBT of(IMaterial... materials) {
    return of(Arrays.stream(materials).map(MaterialVariant::create).toList());
  }

  /** Creates a new material NBT */
  public static MaterialNBT of(MaterialVariant... materials) {
    return of(List.of(materials));
  }

  /** Creates a new material NBT */
  public static MaterialNBT of(MaterialVariantId... materials) {
    return of(Arrays.stream(materials).map(MaterialVariant::create).toList());
  }

  /** Creates a builder for this NBT */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Gets the material at the given index
   * @param index  Index
   * @return  Material, or unknown if index is invalid
   */
  public MaterialVariant get(int index) {
    if (index >= list.size() || index < 0) {
      return MaterialHolder.UNKNOWN;
    }
    return list.get(index);
  }

  /** Gets the number of materials in this list */
  public int size() {
    return list.size();
  }

  @Override
  public boolean isEmpty() {
    return list.isEmpty();
  }

  /**
   * Creates a copy of this material list with the material at the given index substituted
   * @param index        Index to replace. Can be greater than the material list size
   * @param replacement  New material for that index
   * @return  Copy of NBt with the new material
   * @throws IndexOutOfBoundsException  If the index is invalid
   */
  public MaterialNBT replaceMaterial(int index, MaterialVariant replacement) {
    if (index < 0) {
      throw new IndexOutOfBoundsException("Material index is out of bounds");
    }
    // start by copying all materials over
    int size = list.size();
    ArrayList<MaterialVariant> list = new ArrayList<>(Math.max(size, index + 1));
    for (int i = 0; i < size; i++) {
      if (i == index) {
        list.add(replacement);
      } else {
        list.add(this.list.get(i));
      }
    }

    // if the index is bigger, copy in unknown materials until we reach it
    // handles the case where a tool has broken material NBT without crashing
    if (index >= size) {
      for (int i = size; i < index; i++) {
        list.add(MaterialHolder.UNKNOWN);
      }
      list.add(replacement);
    }
    return of(list);
  }

  /**
   * Creates a copy of this material list with the material at the given index substituted
   * @param index        Index to replace. Can be greater than the material list size
   * @param replacement  New material for that index
   * @return  Copy of NBt with the new material
   * @throws IndexOutOfBoundsException  If the index is invalid
   */
  public MaterialNBT replaceMaterial(int index, MaterialVariantId replacement) {
    return replaceMaterial(index, MaterialVariant.create(replacement));
  }

  /** @deprecated use {@link #CODEC} */
  @Deprecated
  public static MaterialNBT readFromNBT(@Nullable Tag tag) {
    if (tag == null || tag.getId() != Tag.TAG_LIST) {
      return EMPTY;
    }
    ListTag listNBT = (ListTag) tag;
    if (listNBT.getElementType() != Tag.TAG_STRING || listNBT.isEmpty()) {
      return EMPTY;
    }
    return TagUtil.decode(CODEC, "material nbt", tag).orElse(EMPTY);
  }

  /** @deprecated use {@link #CODEC} */
  @Deprecated
  public Tag serializeToNBT() {
    return TagUtil.encode(CODEC, this);
  }


  /* Iterator */

  @Nonnull
  @Override
  public Iterator<MaterialVariant> iterator() {
    return list.iterator();
  }

  @Override
  public void forEach(Consumer<? super MaterialVariant> action) {
    list.forEach(action);
  }

  @Override
  public Spliterator<MaterialVariant> spliterator() {
    return list.spliterator();
  }


  /* Helpers */

  /** Gets the materials from a stack */
  public static MaterialNBT get(ItemStack stack) {
    return stack.getOrDefault(TinkerData.MATERIALS, EMPTY);
  }

  /** Helper to quickly fetch a single material from a stack. Use {@link #get(ItemStack)} and {@link #get(int)} instead if you need to get multiple. */
  public static MaterialVariant get(ItemStack stack, int index) {
    return get(stack).get(index);
  }

  /** Writes this material list to the given stack */
  public ItemStack updateStack(ItemStack stack) {
    stack.set(TinkerData.MATERIALS, this);
    return stack;
  }


  /** Builder for material NBT */
  public static class Builder {
    private final ImmutableList.Builder<MaterialVariant> builder = ImmutableList.builder();

    /** Adds a material to the builder */
    public Builder add(MaterialVariant variant) {
      builder.add(variant);
      return this;
    }

    /** Adds a material to the builder */
    public Builder add(MaterialVariantId variantId) {
      return add(MaterialVariant.create(variantId));
    }

    /** Adds a material to the builder */
    public Builder add(IMaterial material) {
      return add(MaterialVariant.create(material));
    }

    /** Adds all materials from the given list */
    public Builder add(List<MaterialVariantId> materials) {
      for (MaterialVariantId material : materials) {
        add(material);
      }
      return this;
    }

    /** Adds all materials from the given list */
    public Builder addAll(List<MaterialVariant> materials) {
      builder.addAll(materials);
      return this;
    }

    /** Adds all materials from the given NBT */
    public Builder addAll(MaterialNBT existing) {
      return addAll(existing.list);
    }

    /** Builds the final list */
    public MaterialNBT build() {
      return of(builder.build());
    }
  }
}
