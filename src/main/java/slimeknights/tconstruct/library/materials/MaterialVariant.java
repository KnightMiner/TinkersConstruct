package slimeknights.tconstruct.library.materials;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/** Class for a material variant, lazily loads the material instance and stores the variant string */
@Accessors(fluent = true)
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MaterialVariant implements Comparable<MaterialVariant> {
  /** Loadable instance for JSON serializing */
  public static final Loadable<MaterialVariant> LOADABLE;
  /** Codec instance for general serializing */
  public static final Codec<MaterialVariant> CODEC;
  /** Loadable instance for JSON serializing */
  public static final StreamCodec<ByteBuf,MaterialVariant> STREAM_CODEC;
  static {
    Function<MaterialVariantId,MaterialVariant> factory = MaterialVariant::create;
    Function<MaterialVariant,MaterialVariantId> id = MaterialVariant::serializedId;
    LOADABLE =  MaterialVariantId.LOADABLE.map(factory, id);
    CODEC =  MaterialVariantId.CODEC.xmap(factory, id);
    STREAM_CODEC = MaterialVariantId.STREAM_CODEC.map(factory, id);
  }

  /** Unknown material variant, as it comes up a lot */
  public static final MaterialVariant UNKNOWN = new MaterialVariant(MaterialId.UNKNOWN, MaterialHolder.UNKNOWN);

  private final MaterialVariantId id;
  private final MaterialHolder holder;

  /** Creates an instance for the given material ID */
  public static MaterialVariant create(MaterialVariantId id) {
    if (id.equals(MaterialId.UNKNOWN)) {
      return UNKNOWN;
    }
    return new MaterialVariant(id, MaterialHolder.create(id.material()));
  }

  /** Creates an instance for the given material holder and variant */
  public static MaterialVariant create(MaterialHolder material, String variant) {
    if (material == MaterialHolder.UNKNOWN) {
      return UNKNOWN;
    }
    return new MaterialVariant(MaterialVariantId.create(material.id(), variant), material);
  }

  /** Creates an instance for the given material holder and variant */
  public static MaterialVariant create(MaterialHolder material) {
    if (material == MaterialHolder.UNKNOWN) {
      return UNKNOWN;
    }
    return new MaterialVariant(material.id(), material);
  }

  /** Creates a new lazy material variant with the given ID and variant */
  public static MaterialVariant create(IMaterial material) {
    if (material == IMaterial.UNKNOWN) {
      return UNKNOWN;
    }
    return create(material.getId());
  }

  /** Gets the ID to use for serializing, using the resolved material ID if fetched. */
  protected MaterialVariantId serializedId() {
    return MaterialVariantId.create(holder.serializedId(), id.variant());
  }


  /* Helpers */

  /** Gets the material instance */
  public IMaterial get() {
    return holder.get();
  }

  /** Gets the material ID */
  public MaterialId material() {
    return id.material();
  }

  /** Gets the variant string */
  public String variant() {
    return id.variant();
  }

  /** Gets the traits for the given stats ID */
  @Nullable
  public <T extends IMaterialStats> T getStats(MaterialStatsId stats) {
    return holder.getStats(stats);
  }

  /** Gets the traits for the given stats ID */
  public List<ModifierEntry> getTraits(MaterialStatsId id) {
    return holder.getTraits(id);
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
    return holder.isBound();
  }


  /* Matching */

  /** Checks if the material is in the given ID */
  public boolean is(MaterialId id) {
    return material().equals(id);
  }

  /** Checks if the modifier is in the given tag */
  public boolean is(TagKey<IMaterial> tag) {
    return MaterialRegistry.getInstance().isInTag(id.material(), tag);
  }

  /** Checks if two material variants match. If this has no variant, matches any variant of the same material */
  public boolean matchesVariant(MaterialVariantId variantId) {
    return this.id.matchesVariant(variantId);
  }

  /** Checks if two material variants match. If this has no variant, matches any variant of the same material */
  public boolean matchesVariant(MaterialVariant variant) {
    return this.id.matchesVariant(variant);
  }

  /** Checks if two material variants match. If this has no variant, matches any variant of the same material */
  public boolean matchesVariant(ItemStack stack) {
    return this.id.matchesVariant(stack);
  }

  /** Checks if this matches the given variant ID */
  public boolean sameVariant(MaterialVariantId variantId) {
    return this.id.sameVariant(variantId);
  }


  /* Object */

  @Override
  public String toString() {
    return "MaterialVariant{" + id + '}';
  }

  @Override
  public boolean equals(@Nullable Object other) {
    if (this == other) {
      return true;
    }
    return other instanceof MaterialVariant otherVariant && this.id.equals(otherVariant.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public int compareTo(MaterialVariant other) {
    // if material IDs are different, compare materials by sort order
    if (!this.id.equals(other.id)) {
      return this.get().compareTo(other.get());
    }
    // otherwise, compare variants
    return this.id.variant().compareTo(other.id.variant());
  }
}
