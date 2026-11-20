package slimeknights.tconstruct.library.json.predicate.material;

import net.minecraft.tags.TagKey;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.loadable.record.SingletonLoader;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.TagPredicateRegistry;
import slimeknights.tconstruct.library.json.TinkerLoadables;
import slimeknights.tconstruct.library.materials.MaterialVariant;
import slimeknights.tconstruct.library.materials.MaterialVariantId;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialCastingLookup;

import java.util.List;
import java.util.function.Predicate;

/** Predicate that checks against a material variant */
public interface MaterialPredicate extends IJsonPredicate<MaterialVariant> {
  /** Instance that always returns true */
  MaterialPredicate ANY = simple(material -> true);
  /** Instance that always returns false */
  MaterialPredicate NONE = simple(material -> false);
  /** Loader for material predicates */
  TagPredicateRegistry<IMaterial,MaterialVariant> LOADER = new TagPredicateRegistry<>("Material Predicate", ANY, NONE, TinkerLoadables.MATERIAL_TAGS, (tag, material) -> material.is(tag));

  /** Gets an inverted condition */
  @Override
  default IJsonPredicate<MaterialVariant> inverted() {
    return LOADER.invert(this);
  }

  @Override
  RecordLoadable<? extends MaterialPredicate> getLoader();


  /* Singleton */

  /** Matches any materials that have a casting recipe */
  MaterialPredicate CASTABLE = simple(material -> !MaterialCastingLookup.getCastingFluids(material.id()).isEmpty());
  /** Matches any materials that have a composite recipe */
  MaterialPredicate COMPOSITE = simple(material -> !MaterialCastingLookup.getCompositeFluids(material.id()).isEmpty());
  /** Checks that the material is not hidden */
  MaterialPredicate NOT_HIDDEN = simple(material -> !material.get().isHidden());
  /** Checks that the material can be crafted */
  MaterialPredicate CRAFTABLE = simple(material -> material.get().isCraftable());
  /** Checks that the material exists in the material registry */
  MaterialPredicate REGISTERED = simple(MaterialVariant::isBound);

  /** Creates a new simple predicate */
  static MaterialPredicate simple(Predicate<MaterialVariant> predicate) {
    return SingletonLoader.singleton(loader -> new MaterialPredicate() {
      @Override
      public boolean matches(MaterialVariant material) {
        return predicate.test(material);
      }

      @Override
      public RecordLoadable<? extends MaterialPredicate> getLoader() {
        return loader;
      }
    });
  }


  /* Helper methods */

  /** Creates a classic style predicate from a material, handling exact ID and the default variant. */
  static MaterialPredicate variant(MaterialVariantId material) {
    String variant = material.variant();
    if (variant.isEmpty()) {
      return new MaterialIdPredicate(material.material());
    }
    // default variant means exact match on ID
    if (MaterialVariantId.DEFAULT_VARIANT.equals(variant)) {
      return new MaterialVariantPredicate(material.material());
    }
    // anything else is exact match on material
    return new MaterialVariantPredicate(material);
  }

  /** Creates a tag predicate */
  static IJsonPredicate<MaterialVariant> tag(TagKey<IMaterial> tag) {
    return LOADER.tag(tag);
  }

  /** Creates an and predicate */
  @SafeVarargs
  static IJsonPredicate<MaterialVariant> and(IJsonPredicate<MaterialVariant>... predicates) {
    return LOADER.and(List.of(predicates));
  }

  /** Creates an or predicate */
  @SafeVarargs
  static IJsonPredicate<MaterialVariant> or(IJsonPredicate<MaterialVariant>... predicates) {
    return LOADER.or(List.of(predicates));
  }
}
