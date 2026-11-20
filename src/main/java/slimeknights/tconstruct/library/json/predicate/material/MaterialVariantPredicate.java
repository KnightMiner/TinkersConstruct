package slimeknights.tconstruct.library.json.predicate.material;

import slimeknights.mantle.data.loadable.array.ArrayLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.materials.MaterialVariant;
import slimeknights.tconstruct.library.materials.MaterialVariantId;

import java.util.Set;

/** Predicate matching any material variant from a set */
public record MaterialVariantPredicate(Set<MaterialVariantId> values) implements MaterialPredicate {
  public static final RecordLoadable<MaterialVariantPredicate> LOADER = RecordLoadable.create(MaterialVariantId.LOADABLE.set(ArrayLoadable.COMPACT).requiredField("variant", MaterialVariantPredicate::values), MaterialVariantPredicate::new);

  public MaterialVariantPredicate(MaterialVariantId... variants) {
    this(Set.of(variants));
  }

  @Override
  public boolean matches(MaterialVariant variant) {
    return values.contains(variant.id());
  }

  @Override
  public RecordLoadable<? extends MaterialPredicate> getLoader() {
    return LOADER;
  }
}
