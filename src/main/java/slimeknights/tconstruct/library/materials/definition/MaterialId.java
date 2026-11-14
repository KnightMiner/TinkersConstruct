package slimeknights.tconstruct.library.materials.definition;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.resource.ResourceId;
import slimeknights.tconstruct.library.resource.ResourceIdLoadable;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.library.utils.SimpleCache;

import javax.annotation.Nullable;

/**
 * This is just a copy of ResourceLocation for type safety in material JSON.
 */
public record MaterialId(ResourceLocation id) implements ResourceId<IMaterial>, MaterialVariantId {
  private static final SimpleCache<ResourceLocation,MaterialId> CACHE = new SimpleCache<>(MaterialId::new);
  /** Loader instance */
  public static final ResourceIdLoadable<MaterialId> LOADABLE = new ResourceIdLoadable<>(CACHE);
  /** Codec instance */
  public static final Codec<MaterialId> CODEC = ResourceId.codec(CACHE);
  /** Stream codec instance for network buffer */
  public static final StreamCodec<ByteBuf,MaterialId> STREAM_CODEC = ResourceId.streamCodec(CACHE);

  /** ID of fallback material */
  public static final MaterialId UNKNOWN = create(TConstruct.MOD_ID, "unknown");

  /** @apiNote use {@link #create(ResourceLocation)} */
  @Internal
  public MaterialId {}

  /** Checks if this ID matches the given material */
  public boolean matches(IMaterial material) {
    return this.equals(material.getId());
  }

  /** Checks if this ID matches the given stack */
  public boolean matches(ItemStack stack) {
    return !stack.isEmpty() && this.equals(IMaterialItem.getMaterialFromStack(stack));
  }

  @Override
  public MaterialId material() {
    return this;
  }

  @Override
  public String variant() {
    return "";
  }

  @Override
  public boolean hasVariant() {
    return false;
  }

  @Override
  public boolean isDefaultVariant() {
    return false;
  }

  @Override
  public ResourceLocation getLocation(char separator) {
    return id;
  }

  @Override
  public String getSuffix() {
    return id.getNamespace() + '_' + id.getPath();
  }

  @Override
  public boolean matchesVariant(MaterialVariantId other) {
    return this.equals(other.material());
  }

  @Override
  public String toString() {
    return id.toString();
  }


  /* Constructors */

  /** Parses a pattern from the given string */
  public static MaterialId create(ResourceLocation id) {
    return CACHE.apply(id);
  }

  /** {@return Material ID, or throws if invalid} */
  public static MaterialId create(String domain, String name) {
    return create(ResourceLocation.fromNamespaceAndPath(domain, name));
  }

  /** {@return Material ID, or throws if invalid} */
  public static MaterialId parse(String string) {
    return create(ResourceLocation.parse(string));
  }

  /** {@return Material ID, or null if invalid} */
  @Nullable
  public static MaterialId tryParse(String string) {
    return LOADABLE.tryParse(string);
  }
}
