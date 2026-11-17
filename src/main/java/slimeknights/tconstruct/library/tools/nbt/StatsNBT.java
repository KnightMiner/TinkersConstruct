package slimeknights.tconstruct.library.tools.nbt;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.common.CodecLoadable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.codec.DispatchedMapStreamCodec;
import slimeknights.tconstruct.library.resource.ResourceId;
import slimeknights.tconstruct.library.tools.stat.INumericToolStat;
import slimeknights.tconstruct.library.tools.stat.IToolStat;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.library.utils.TagUtil;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Generic container for tool stats, allows addons to select which stats they wish to use
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode
@ToString
public class StatsNBT {
  /** Set of all tool stat IDs that failed to parse, to reduce log spam as they get parsed many times in UIs when dumb mods don't call proper methods */
  static final Set<String> ERRORED_IDS = new HashSet<>();
  /** Empty stats */
  public static final StatsNBT EMPTY = new StatsNBT(Map.of());

  /** Codec instance for NBT and JSON */
  public static final Codec<StatsNBT> CODEC;
  /** Stream codec instance for syncing the component */
  public static final StreamCodec<FriendlyByteBuf,StatsNBT> STREAM_CODEC;
  static {
    Function<StatsNBT,Map<IToolStat<?>,Object>> getter = s -> s.stats;
    Function<Map<IToolStat<?>,Object>,StatsNBT> constructor = StatsNBT::new;
    CODEC = Codec.<IToolStat<?>,Object>dispatchedMap(ToolStats.CODEC, IToolStat::codec).xmap(constructor, getter);
    STREAM_CODEC = new DispatchedMapStreamCodec<FriendlyByteBuf,IToolStat<?>,Object>(ToolStats.STREAM_CODEC, IToolStat::streamCodec).map(constructor, getter);
  }
  /** Loadable instance for tool modules */
  public static final Loadable<StatsNBT> LOADABLE = new CodecLoadable.Direct<>(CODEC, STREAM_CODEC);

  /** All currently contained stats */
  private final Map<IToolStat<?>, Object> stats;

  /** Creates a new stats builder */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Gets a set of all stats contained
   * @return  Stat type set
   */
  public Set<IToolStat<?>> getContainedStats() {
    return stats.keySet();
  }

  /**
   * Checks if the NBT contains the given stat
   * @param stat  Stat to check for
   * @return  True if the stat is contained
   */
  public boolean hasStat(IToolStat<?> stat) {
    return stats.containsKey(stat);
  }

  /**
   * Gets the given tool stat as a float
   * @param stat  Stat
   * @return  Value, or default if the stat is missing
   */
  @SuppressWarnings("unchecked")
  public <T> T get(IToolStat<T> stat) {
    return (T)stats.getOrDefault(stat, stat.getDefaultValue());
  }

  /**
   * Gets the given tool stat as an int
   * @param stat  Stat
   * @return  Value, or default if the stat is missing
   */
  public int getInt(IToolStat<? extends Number> stat) {
    return get(stat).intValue();
  }


  /* NBT parsing */

  /** Reads a tool stat ID from a NBT string */
  @Nullable
  static IToolStat<?> readStatIdFromNBT(String name) {
    ResourceId<IToolStat<?>> statName = ResourceId.tryParse(name);
    if (statName != null) {
      IToolStat<?> stat = ToolStats.getToolStat(statName);
      if (stat != null) {
        return stat;
      }
    }
    if (!StatsNBT.ERRORED_IDS.contains(name)) {
      StatsNBT.ERRORED_IDS.add(name);
      TConstruct.LOG.error("Ignoring unknown stat {} in tool stat NBT", name);
    }
    return null;
  }

  /** use {@link #CODEC} */
  @Deprecated
  public static StatsNBT readFromNBT(@Nullable Tag tag) {
    return tag == null ? EMPTY : TagUtil.decode(CODEC, "tool stats", tag).orElse(EMPTY);
  }

  /** use {@link #CODEC} */
  @Deprecated
  public Tag serializeToNBT() {
    return TagUtil.encode(CODEC, this);
  }


  /* Network */

  /** use {@link #STREAM_CODEC} */
  @Deprecated
  public void toNetwork(FriendlyByteBuf buffer) {
    STREAM_CODEC.encode(buffer, this);
  }

  /** use {@link #STREAM_CODEC} */
  @Deprecated
  public static StatsNBT fromNetwork(RegistryFriendlyByteBuf buffer) {
    return STREAM_CODEC.decode(buffer);
  }

  /** Create a builder for stats, really just safety checks on the generics for the set method */
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final ImmutableMap.Builder<IToolStat<?>, Object> builder = ImmutableMap.builder();

    /** Sets the given stat in the builder */
    public <T> Builder set(IToolStat<T> stat, T value) {
      builder.put(stat, stat.clamp(value));
      return this;
    }

    /** Sets the given stat in the builder */
    public Builder set(INumericToolStat<Float> stat, float value) {
      return set(stat, (Float)value);
    }

    /** Builds the stats from the given values */
    public StatsNBT build() {
      Map<IToolStat<?>,Object> map = builder.build();
      if (map.isEmpty()) {
        return EMPTY;
      }
      return new StatsNBT(map);
    }
  }

}
