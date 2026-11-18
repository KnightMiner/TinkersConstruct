package slimeknights.tconstruct.library.materials;

import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;

import javax.annotation.Nullable;
import java.util.List;

/** Implementation of {@link MaterialVariant} for {@link MaterialVariantId} */
record MaterialVariantImpl(MaterialVariantId id, MaterialHolder holder) implements MaterialVariant {
  @Override
  public MaterialId material() {
    return id.material();
  }

  @Override
  public MaterialVariantId serializedId() {
    return MaterialVariantId.create(holder.serializedId(), id.variant());
  }

  @Override
  public String variant() {
    return id.variant();
  }

  @Override
  public boolean hasVariant() {
    return true;
  }

  @Override
  public MaterialVariant normalizeVariant() {
    if (id.isDefaultVariant()) {
      return holder;
    }
    return this;
  }


  /* Material properties */

  @Override
  public IMaterial get() {
    return holder.get();
  }

  @Override
  public boolean isBound() {
    return holder.isBound();
  }

  @Override
  public boolean hasStats(MaterialStatsId stats) {
    return holder.hasStats(stats);
  }

  @Override
  @Nullable
  public <T extends IMaterialStats> T getStats(MaterialStatsId stats) {
    return holder.getStats(stats);
  }

  @Override
  public List<ModifierEntry> getTraits(MaterialStatsId id) {
    return holder.getTraits(id);
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
    return other instanceof MaterialVariantImpl otherVariant && this.id.equals(otherVariant.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
