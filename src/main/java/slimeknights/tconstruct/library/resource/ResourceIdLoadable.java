package slimeknights.tconstruct.library.resource;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.primitive.ResourceLocationLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.function.Function;

/** {@link ResourceLocationLoadable} for {@link ResourceId} and extensions. */
public record ResourceIdLoadable<T extends ResourceId<?>>(Function<ResourceLocation,T> constructor) implements ResourceLocationLoadable<T> {
  private static final ResourceLocationLoadable<?> DEFAULT = new ResourceIdLoadable<>(ResourceId::create);

  /** Gets a loadable using the default {@link ResourceId#create(ResourceLocation)} */
  @SuppressWarnings("unchecked")
  public static <T> ResourceIdLoadable<ResourceId<T>> loadable() {
    return (ResourceIdLoadable<ResourceId<T>>) DEFAULT;
  }

  /** Attempts to parse an ID from JSON, returning null if invalid */
  @Nullable
  public T tryParse(String string) {
    ResourceLocation location = ResourceLocation.tryParse(string);
    return location == null ? null : constructor.apply(location);
  }

  @Override
  public T fromKey(ResourceLocation name, String key, TypedMap context) {
    return constructor.apply(name);
  }

  @Override
  public ResourceLocation getKey(T id, TypedMap context) {
    return id.id();
  }

  @Override
  public T decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return constructor.apply(buffer.readResourceLocation());
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, T value, TypedMap context) {
    buffer.writeResourceLocation(value.id());
  }
}
