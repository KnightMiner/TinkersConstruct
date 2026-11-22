package slimeknights.tconstruct.library.tools.component;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.tconstruct.library.tools.stat.INumericToolStat;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

/**
 * Generic container for tool stats, allows addons to select which stats they wish to use
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode
@ToString
public class StatMultipliers implements CollectionComponent {
  /** Codec instance for NBT */
  public static final Codec<StatMultipliers> CODEC;
  /** Stream codec instance for network syncing */
  public static final StreamCodec<ByteBuf, StatMultipliers> STREAM_CODEC;
  /** Loadable instance for recipes */
  public static final Loadable<StatMultipliers> LOADABLE;
  static {
    Function<StatMultipliers,Map<INumericToolStat<?>,Float>> getter = m -> m.stats;
    Function<Map<INumericToolStat<?>,Float>, StatMultipliers> constructor = StatMultipliers::new;
    CODEC = Codec.unboundedMap(ToolStats.NUMERIC_CODEC, Codec.FLOAT).xmap(constructor, getter);
    StreamCodec<ByteBuf,Map<INumericToolStat<?>,Float>> mapStream = ByteBufCodecs.map(Object2FloatOpenHashMap::new, ToolStats.NUMERIC_STREAM_CODEC, ByteBufCodecs.FLOAT);
    STREAM_CODEC = mapStream.map(constructor, getter);
    LOADABLE  = ToolStats.NUMERIC_LOADER.mapWithValues(FloatLoadable.ANY, 0).map(constructor, getter);
  }

  /** Empty stats */
  public static final StatMultipliers EMPTY = new StatMultipliers(ImmutableMap.of());

  /** All currently contained multipliers */
  private final Map<INumericToolStat<?>, Float> stats;

  /** Creates a new builder */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public boolean isEmpty() {
    return stats.isEmpty();
  }

  /**
   * Gets a set of all stats contained
   * @return  Stat type set
   */
  public Set<INumericToolStat<?>> getContainedStats() {
    return stats.keySet();
  }

  /**
   * Checks if the NBT contains the given stat
   * @param stat  Stat to check for
   * @return  True if the stat is contained
   */
  public boolean hasStat(INumericToolStat<?> stat) {
    return stats.containsKey(stat);
  }

  /**
   * Gets the given multiplier
   * @param stat  Stat
   * @return  Value, or default if the stat is missing
   */
  public float get(INumericToolStat<?> stat) {
    return stats.getOrDefault(stat, 1f);
  }


  /* NBT */

  /** Reads the multipliers from NBT */
  public static StatMultipliers readFromNBT(@Nullable Tag inbt) {
    if (inbt == null || inbt.getId() != Tag.TAG_COMPOUND) {
      return EMPTY;
    }
    // simply try each key as a tool stat
    Builder builder = builder();
    CompoundTag nbt = (CompoundTag)inbt;
    for (String key : nbt.getAllKeys()) {
      if (nbt.contains(key, Tag.TAG_ANY_NUMERIC) && StatValues.readStatIdFromNBT(key) instanceof INumericToolStat<?> stat) {
        builder.set(stat, nbt.getFloat(key));
      }
    }
    return builder.build();
  }

  /** Writes these stats to NBT */
  public CompoundTag serializeToNBT() {
    CompoundTag nbt = new CompoundTag();
    for (Entry<INumericToolStat<?>,Float> entry : stats.entrySet()) {
      nbt.putFloat(entry.getKey().getId().toString(), entry.getValue());
    }
    return nbt;
  }


  /** Builder for a multiplier, mostly prevents nulls from being added */
  public static class Builder {
    private final ImmutableMap.Builder<INumericToolStat<?>, Float> builder = ImmutableMap.builder();

    private Builder() {}

    /** Sets the given stat in the builder */
    public Builder set(INumericToolStat<?> stat, float value) {
      if (value != 1f) {
        builder.put(stat, Math.max(value, 0));
      }
      return this;
    }

    /** Builds the stats from the given values */
    public StatMultipliers build() {
      Map<INumericToolStat<?>,Float> map = builder.build();
      if (map.isEmpty()) {
        return EMPTY;
      }
      return new StatMultipliers(map);
    }
  }
}
