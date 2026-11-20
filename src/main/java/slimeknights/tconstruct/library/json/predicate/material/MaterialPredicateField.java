package slimeknights.tconstruct.library.json.predicate.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.tconstruct.library.materials.MaterialId;
import slimeknights.tconstruct.library.materials.MaterialVariant;
import slimeknights.tconstruct.library.materials.MaterialVariantId;

import java.util.Set;
import java.util.function.Function;

/** Field for a material predicate that maps primitive strings to classic material predicates instead of unnamed predicates */
public record MaterialPredicateField<P>(String key, Function<P, IJsonPredicate<MaterialVariant>> getter, boolean compact) implements LoadableField<IJsonPredicate<MaterialVariant>,P> {
  public MaterialPredicateField(String key, Function<P, IJsonPredicate<MaterialVariant>> getter) {
    this(key, getter, true);
  }

  @Override
  public IJsonPredicate<MaterialVariant> get(JsonObject json, String key, TypedMap context) {
    if (json.has(key)) {
      JsonElement element = json.get(key);
      // primitive is a material ID
      if (element.isJsonPrimitive()) {
        return MaterialPredicate.variant(MaterialVariantId.LOADABLE.convert(element, key, context));
      } else {
        return MaterialPredicate.LOADER.convert(element, key, context);
      }
    }
    return MaterialPredicate.ANY;
  }

  @Override
  public void serializeInto(P parent, JsonObject json, TypedMap context) {
    IJsonPredicate<MaterialVariant> predicate = getter.apply(parent);
    if (predicate != MaterialPredicate.ANY) {
      // if compact serializing, serialize the legacy style predicate
      if (compact) {
        // ID predicate serializes to a raw ID
        if (predicate instanceof MaterialIdPredicate(Set<MaterialId> ids) && ids.size() == 1) {
          json.addProperty(key, ids.iterator().next().toString());
          return;
        }
        // single variant predicate maps to exact variant match
        if (predicate instanceof MaterialVariantPredicate(Set<MaterialVariantId> values) && values.size() == 1) {
          MaterialVariantId id = values.iterator().next();
          // map empty variant to special default variant string
          if (id.variant().isEmpty()) {
            id = MaterialVariantId.create(id.material(), MaterialVariantId.DEFAULT_VARIANT);
          }
          json.addProperty(key, id.toString());
          return;
        }
      }

      // force serialized form to be a json object, as compact is misleading
      JsonObject serialized = new JsonObject();
      MaterialPredicate.LOADER.serializeInto(predicate, serialized, context);
      json.add(key, serialized);
    }
  }

  @Override
  public IJsonPredicate<MaterialVariant> decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return MaterialPredicate.LOADER.decode(buffer, context);
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, P parent, TypedMap context) {
    MaterialPredicate.LOADER.encode(buffer, getter.apply(parent));
  }
}
