package slimeknights.tconstruct.library.materials.definition;

import net.minecraft.resources.ResourceLocation;

/** Internal record to represent a material ID with a variant. Use {@link MaterialVariantId} to create if needed */
record MaterialVariantIdImpl(MaterialId material, String variant) implements MaterialVariantId {
  @Override
  public boolean hasVariant() {
    return true;
  }

  @Override
  public boolean matchesVariant(MaterialVariantId other) {
    // special case: material#default will match against the empty variant, to allow recipes to look for a material with no variant
    if (DEFAULT_VARIANT.equals(variant)) {
      String otherVariant = other.variant();
      return this.material.equals(other.material()) && (otherVariant.isEmpty() || DEFAULT_VARIANT.equals(otherVariant));
    }
    return this.sameVariant(other);
  }

  @Override
  public ResourceLocation getLocation(char separator) {
    return material.id().withSuffix(separator + variant);
  }

  @Override
  public String getSuffix() {
    return material.getNamespace() + '_' + material.getPath() + '_' + variant;
  }

  @Override
  public String toString() {
    return material + "#" + variant;
  }
}
