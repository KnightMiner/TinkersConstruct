package slimeknights.tconstruct.library.tools.nbt;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.codec.StreamCodec;
import slimeknights.tconstruct.library.codec.TinkerCodecs;
import slimeknights.tconstruct.library.codec.TinkerStreamCodecs;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.SlotType.SlotCount;

import java.util.List;
import java.util.function.Function;

/** Handles free and used modifier slots on tools */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SlotsNBT implements CollectionComponent {
  /** Instance with no entries. */
  public static final SlotsNBT EMPTY = new SlotsNBT(Object2IntMaps.emptyMap());

  public static final Codec<SlotsNBT> CODEC;
  public static final StreamCodec<ByteBuf,SlotsNBT> STREAM_CODEC;
  static {
    Function<Object2IntMap<SlotType>,SlotsNBT> constructor = SlotsNBT::create;
    Function<SlotsNBT,Object2IntMap<SlotType>> getter = s -> s.slots;
    CODEC = TinkerCodecs.object2IntMap(SlotType.CODEC).xmap(constructor, getter);
    STREAM_CODEC = TinkerStreamCodecs.object2IntMap(SlotType.STREAM_CODEC).map(constructor, getter);
  }

  /** Slot map */
  private final Object2IntMap<SlotType> slots;

  /** Creates a new map with the given slots */
  private static SlotsNBT create(Object2IntMap<SlotType> slots) {
    if (slots.isEmpty()) {
      return EMPTY;
    }
    return new SlotsNBT(Object2IntMaps.unmodifiable(slots));
  }

  /** Gets the number of slots of the given type. */
  public int get(SlotType slot) {
    return slots.getOrDefault(slot, 0);
  }

  @Override
  public boolean isEmpty() {
    return slots.isEmpty();
  }

  /** Creates a copy of this data with the given number of slots */
  public SlotsNBT with(SlotType type, int count) {
    Object2IntMap<SlotType> slots = new Object2IntOpenHashMap<>(this.slots);
    if (count == 0) {
      slots.removeInt(type);
    } else {
      slots.put(type, count);
    }
    return create(slots);
  }

  /** Creates a copy of this data with the given slots added */
  public SlotsNBT with(SlotCount count) {
    return with(count.type(), count.count());
  }

  /** Creates a copy of this data with the given slots removed */
  public SlotsNBT without(SlotCount count) {
    return with(count.type(), -count.count());
  }

  /** Creates a copy of this data adding the given number of slots */
  public SlotsNBT add(SlotType slot, int count) {
    return with(slot, get(slot) + count);
  }

  /** Creates a new builder from this slot data */
  public Builder toBuilder() {
    return builder().add(this);
  }


  /* Builder */

  /** Creates a new builder */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final Object2IntMap<SlotType> slots = new Object2IntOpenHashMap<>();

    private Builder() {}

    /** Adds the given number of slots to the builder */
    public Builder add(SlotType slot, int count) {
      count += slots.getOrDefault(slot, 0);
      if (count == 0) {
        slots.removeInt(slot);
      } else {
        slots.put(slot, count);
      }
      return this;
    }

    /** Adds all slots from the given object */
    public Builder add(SlotsNBT slots) {
      for (Object2IntMap.Entry<SlotType> entry : slots.slots.object2IntEntrySet()) {
        add(entry.getKey(), entry.getIntValue());
      }
      return this;
    }

    /** Builds the final slots instance */
    public SlotsNBT build() {
      return create(slots);
    }

    /** Builds as a list of slot counts */
    public List<SlotCount> buildCounts() {
      return slots.object2IntEntrySet().stream()
        .map(entry -> new SlotCount(entry.getKey(), entry.getIntValue()))
        .toList();
    }
  }
}
