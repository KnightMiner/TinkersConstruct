package slimeknights.tconstruct.library.materials;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * Class for a material variant, lazily loads the material instance and stores the variant string.
 * For a default instance, see {@link MaterialHolder#UNKNOWN}.
 */
public interface MaterialVariant extends Comparable<MaterialVariant> {
  /** Loadable instance for JSON serializing */
  Loadable<MaterialVariant> LOADABLE = MaterialVariantId.LOADABLE.map(MaterialVariant::create, MaterialVariant::serializedId);
  /** Codec instance for general serializing */
  Codec<MaterialVariant> CODEC = MaterialVariantId.CODEC.xmap(MaterialVariant::create, MaterialVariant::serializedId);
  /** Loadable instance for JSON serializing */
  StreamCodec<ByteBuf,MaterialVariant> STREAM_CODEC = MaterialVariantId.STREAM_CODEC.map(MaterialVariant::create, MaterialVariant::serializedId);


  /* Constructors */

  /** Creates an instance for the given material ID */
  static MaterialVariant create(MaterialId id) {
    if (id.equals(MaterialId.UNKNOWN)) {
      return MaterialHolder.UNKNOWN;
    }
    return MaterialHolder.create(id.material());
  }

  /** Creates an instance for the given material ID */
  static MaterialVariant create(MaterialVariantId id) {
    if (id.equals(MaterialId.UNKNOWN)) {
      return MaterialHolder.UNKNOWN;
    }
    MaterialHolder holder = MaterialHolder.create(id.material());
    return id.hasVariant() ? new MaterialVariantImpl(id, holder) : holder;
  }

  /** Creates an instance for the given material holder and variant */
  static MaterialVariant create(MaterialHolder material, String variant) {
    if (material == MaterialHolder.UNKNOWN) {
      return MaterialHolder.UNKNOWN;
    }
    // no variant? just stick with the holder
    if (variant.isEmpty()) {
      return material;
    }
    return new MaterialVariantImpl(MaterialVariantId.create(material.id(), variant), material);
  }

  /** Creates a new lazy material variant with the given ID and variant */
  static MaterialVariant create(IMaterial material) {
    if (material == IMaterial.UNKNOWN) {
      return MaterialHolder.UNKNOWN;
    }
    return create(material.getId());
  }


  /* ID */

  /** Gets the ID for this material */
  MaterialVariantId id();

  /** Gets the ID to use for serializing, using the resolved material ID if fetched. */
  MaterialVariantId serializedId();

  /** Gets the material ID */
  MaterialId material();


  /* Variant */

  /** Gets the variant string */
  String variant();

  /** Checks if this material has a variant */
  boolean hasVariant();

  /** Replaces the default variant with the ID by itself */
  MaterialVariant normalizeVariant();


  /* Material */

  /** Gets the holder for this material */
  MaterialHolder holder();

  /** Gets the material instance */
  IMaterial get();


  /* Stats and traits */

  /** Checks if the given stats ID is supported by this material */
  boolean hasStats(MaterialStatsId stats);

  /**
   * Gets the stats for the given stats ID, returning null if undefined.
   * @return stats, or null if the stats ID is invalid or the stats are not defined for this material
   * @see #hasStats(MaterialStatsId)
   */
  @Nullable
  <T extends IMaterialStats> T getStats(MaterialStatsId stats);

  /**
   * Gets the stats for the given stats ID, returning default stats if undefined.
   * @return stats, or null if the stats ID is invalid.
   * @see #hasStats(MaterialStatsId)
   */
  @Nullable
  default <T extends IMaterialStats> T getStatsOrDefault(MaterialStatsId id) {
    T stats = getStats(id);
    return stats != null ? stats : MaterialRegistry.getInstance().getDefaultStats(id);
  }

  /** Gets all stats for the given material */
  Collection<IMaterialStats> getAllStats();

  /** Gets the traits for the given stats ID */
  List<ModifierEntry> getTraits(MaterialStatsId id);


  /* Presence */

  /**
   * Checks if this is not the unknown material
   * @see #isBound()
   */
  default boolean isPresent() {
    return !material().equals(MaterialId.UNKNOWN);
  }

  /**
   * Returns true if this material exists either statically or in a datapack. A return of true here means a useful value is returned by {@link #get()}
   * @see #isPresent()
   */
  boolean isBound();


  /* Matching */

  /** Checks if the material is in the given ID */
  default boolean is(MaterialId id) {
    return material().equals(id);
  }

  /** Checks if the modifier is in the given tag */
  default boolean is(TagKey<IMaterial> tag) {
    return MaterialRegistry.getInstance().isInTag(material(), tag);
  }

  /** Checks if two material variants match. If this has no variant, matches any variant of the same material */
  default boolean matchesVariant(MaterialVariantId variantId) {
    return id().matchesVariant(variantId);
  }

  /** Checks if two material variants match. If this has no variant, matches any variant of the same material */
  default boolean matchesVariant(MaterialVariant variant) {
    return id().matchesVariant(variant);
  }

  /** Checks if two material variants match. If this has no variant, matches any variant of the same material */
  default boolean matchesVariant(ItemStack stack) {
    return id().matchesVariant(stack);
  }

  /** Checks if this matches the given variant ID */
  default boolean sameVariant(MaterialVariantId variantId) {
    return id().sameVariant(variantId);
  }

  /** Checks if this matches the given variant ID */
  default boolean sameVariant(MaterialVariant variant) {
    return id().sameVariant(variant.id());
  }


  /* Object */

  @Override
  default int compareTo(MaterialVariant other) {
    // quick exit for a match
    if (this == other) return 0;
    // if material IDs are different, compare materials by sort order
    MaterialVariantId thisId = this.id();
    MaterialVariantId otherId = other.id();
    if (!thisId.equals(otherId)) {
      return this.get().compareTo(other.get());
    }
    // otherwise, compare variants
    return thisId.variant().compareTo(otherId.variant());
  }
}
