package slimeknights.tconstruct.library.modifiers;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.resource.ResourceId;
import slimeknights.tconstruct.library.resource.ResourceIdLoadable;
import slimeknights.tconstruct.library.utils.SimpleCache;

import javax.annotation.Nullable;

/**
 * {@link ResourceId} for {@link Modifier}
 */
public record ModifierId(ResourceLocation id) implements ResourceId<Modifier> {
  private static final SimpleCache<ResourceLocation,ModifierId> CACHE = new SimpleCache<>(ModifierId::new);
  /** {@link slimeknights.mantle.data.loadable.Loadable} instance. */
  public static final ResourceIdLoadable<ModifierId> LOADABLE = new ResourceIdLoadable<>(CACHE);
  /** {@link Codec} instance. */
  public static final Codec<ModifierId> CODEC = ResourceId.codec(CACHE);
  /** {@link StreamCodec} instance. */
  public static final StreamCodec<ByteBuf, ModifierId> STREAM_CODEC = ResourceId.streamCodec(CACHE);

  /** ID of the default modifier. Used in a few contexts to indicate "no modifier" instead of using null. */
  public static final ModifierId EMPTY = create(TConstruct.MOD_ID, "empty");
  /** Context key for loadable parsing. Used in {@link slimeknights.tconstruct.library.client.modifiers.ModifierModelMapManager} and {@link ModifierManager} */
  public static final ContextKey<ModifierId> CONTEXT_KEY = new ContextKey<>("modifier");

  /** @apiNote use {@link #create(ResourceLocation)} */
  @Internal
  public ModifierId {}

  @Override
  public String toString() {
    return id.toString();
  }


  /* Constructors */

  /** Parses a pattern from the given string */
  public static ModifierId create(ResourceLocation id) {
    return CACHE.apply(id);
  }

  /** Parses a pattern from the given string */
  public static ModifierId create(String domain, String name) {
    return create(ResourceLocation.fromNamespaceAndPath(domain, name));
  }

  /** Parses a pattern from the given string */
  public static ModifierId parse(String string) {
    return create(ResourceLocation.parse(string));
  }

  /** Parses a pattern from the given string */
  @Nullable
  public static ModifierId tryParse(String string) {
    return LOADABLE.tryParse(string);
  }
}
