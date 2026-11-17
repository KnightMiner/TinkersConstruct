package slimeknights.tconstruct.library.materials;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Contains a reference to a material and its properties, which is lazily fetched and automatically reset when datapacks reload.
 * @see slimeknights.tconstruct.library.modifiers.ModifierHolder
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class MaterialHolder {
  /** Loadable instance for JSON serializing */
  public static final Loadable<MaterialHolder> LOADABLE;
  /** Codec instance for general serializing */
  public static final Codec<MaterialHolder> CODEC;
  /** Loadable instance for JSON serializing */
  public static final StreamCodec<ByteBuf,MaterialHolder> STREAM_CODEC;
  static {
    Function<MaterialId,MaterialHolder> factory = MaterialHolder::create;
    Function<MaterialHolder,MaterialId> id = MaterialHolder::serializedId;
    LOADABLE =  MaterialId.LOADABLE.map(factory, id);
    CODEC =  MaterialId.CODEC.xmap(factory, id);
    STREAM_CODEC = MaterialId.STREAM_CODEC.map(factory, id);
  }

  /** Default instance returning {@link MaterialId#UNKNOWN}. */
  public static final MaterialHolder UNKNOWN = new MaterialHolder();

  @Getter @Accessors(fluent = true)
  private final MaterialId id;
  private IMaterial material;

  // stats
  /** Cache of fetched stats for this material */
  private final Map<MaterialStatsId, IMaterialStats> stats = new ConcurrentHashMap<>();
  /** Logic to fill in the cache of a stat */
  private final Function<MaterialStatsId, IMaterialStats> statsGetter = statType -> MaterialRegistry.getInstance().getStatsOrDefault(serializedId(), statType);

  // traits
  /** Cache of fetched traits for this material */
  private final Map<MaterialStatsId, List<ModifierEntry>> traits = new ConcurrentHashMap<>();
  /** Logic to fill in the cache of a trait */
  private final Function<MaterialStatsId, List<ModifierEntry>> traitsGetter = statType -> MaterialRegistry.getInstance().getTraits(serializedId(), statType);

  private MaterialHolder() {
    this(MaterialId.UNKNOWN);
    material = IMaterial.UNKNOWN;
  }

  /** Gets a material holder for the given ID */
  public static MaterialHolder create(MaterialId id) {
    return MaterialRegistry.getHolder(id);
  }

  /** Gets the ID to use for serializing, using the resolved material ID if fetched. */
  protected MaterialId serializedId() {
    return material != null && material != IMaterial.UNKNOWN ? material.getId() : id;
  }

  /** Gets the material */
  public IMaterial get() {
    if (material == null) {
      if (!MaterialRegistry.isFullyLoaded()) {
        TConstruct.LOG.error("Attempting to fetch material {} before the material registry is loaded.", id);
        return IMaterial.UNKNOWN;
      }
      material = MaterialRegistry.getMaterial(id);
    }
    return material;
  }

  /** Clears the cached modifier instance */
  void clearCache() {
    material = null;
    stats.clear();
    traits.clear();
  }


  /* Stats and traits */

  /** Gets the traits for the given stats ID */
  @Nullable
  @SuppressWarnings("unchecked")
  public <T extends IMaterialStats> T getStats(MaterialStatsId id) {
    // if the material is not registered, ignore its stats
    if (isBound()) {
      return (T) stats.computeIfAbsent(id, statsGetter);
    }
    return MaterialRegistry.getInstance().getDefaultStats(id);
  }

  /** Gets the traits for the given stats ID */
  public List<ModifierEntry> getTraits(MaterialStatsId id) {
    // if the material is not registered, ignore its traits
    if (isBound()) {
      return traits.computeIfAbsent(id, traitsGetter);
    }
    return List.of();
  }


  /* Presence */

  /**
   * Checks if this is not the unknown material
   * @see #isBound()
   */
  public boolean isPresent() {
    return !id.equals(MaterialId.UNKNOWN);
  }

  /**
   * Returns true if this material exists either statically or in a datapack. A return of true here means a useful value is returned by {@link #get()}
   * @see #isPresent()
   */
  public boolean isBound() {
    return MaterialRegistry.isFullyLoaded() && get() != IMaterial.UNKNOWN;
  }


  /* Matching */

  /** Checks if the material is in the given ID */
  public boolean is(MaterialId id) {
    return this.id.equals(id);
  }

  /** Checks if the modifier is in the given tag */
  public boolean is(TagKey<IMaterial> tag) {
    return MaterialRegistry.getInstance().isInTag(id, tag);
  }


  /* Equality */

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    return other instanceof MaterialHolder holder && this.id.equals(holder.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public String toString() {
    return "MaterialHolder{" + id + '}';
  }
}
