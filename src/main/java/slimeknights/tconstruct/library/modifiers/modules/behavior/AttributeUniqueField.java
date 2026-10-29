package slimeknights.tconstruct.library.modifiers.modules.behavior;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.function.Function;

/** Field for the unique key for an attribute. If the key is unset in JSON, defaults to the modifier name */
public record AttributeUniqueField<P>(String key, Function<P,ResourceLocation> getter) implements LoadableField<ResourceLocation,P> {
  public AttributeUniqueField(Function<P, ResourceLocation> getter) {
    this("unique", getter);
  }

  @Override
  public ResourceLocation get(JsonObject json, String key, TypedMap context) {
    if (json.has(key)) {
      return JsonHelper.getResourceLocation(json, key);
    }
    ResourceLocation id = context.get(ContextKey.ID);
    if (id == null) {
      throw new JsonParseException("Missing modifier ID in context, cannot default " + key);
    }
    return id;
  }

  @Override
  public void serializeInto(P parent, JsonObject json, TypedMap context) {
    ResourceLocation unique = getter.apply(parent);
    // TODO: maybe use context here?
    if (unique != null) {
      json.addProperty(key, unique.toString());
    }
  }

  @Override
  public ResourceLocation decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return buffer.readResourceLocation();
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, P parent, TypedMap context) {
    buffer.writeResourceLocation(getter.apply(parent));
  }
}
