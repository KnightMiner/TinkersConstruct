package slimeknights.tconstruct.library.recipe.material;

import slimeknights.tconstruct.library.materials.MaterialVariant;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;

/**
 * Constant material value used for tool parts
 */
public record MaterialValue(MaterialVariant getMaterial, int getValue) implements IMaterialValue {
  public MaterialValue(MaterialVariantId material, int value) {
    this(MaterialVariant.create(material), value);
  }
}
