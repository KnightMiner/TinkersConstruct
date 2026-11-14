package slimeknights.tconstruct.library.recipe.partbuilder;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.tconstruct.library.resource.ResourceId;
import slimeknights.tconstruct.library.resource.ResourceIdLoadable;
import slimeknights.tconstruct.library.utils.SimpleCache;
import slimeknights.tconstruct.library.utils.Util;

import javax.annotation.Nullable;

/**
 * Resource ID variant for pattern buttons in the part builder. Also used in a few spots for convenient icon loading.
 */
public record Pattern(ResourceLocation id) implements ResourceId<Pattern> {
  private static final SimpleCache<ResourceLocation,Pattern> CACHE = new SimpleCache<>(Pattern::new);
  /** {@link slimeknights.mantle.data.loadable.Loadable} instance. */
  public static final ResourceIdLoadable<Pattern> LOADABLE = new ResourceIdLoadable<>(CACHE);
  /** {@link Codec} instance. */
  public static final Codec<Pattern> CODEC = ResourceId.codec(CACHE);
  /** {@link StreamCodec} instance. */
  public static final StreamCodec<ByteBuf,Pattern> STREAM_CODEC = ResourceId.streamCodec(CACHE);
  
  /** @apiNote use {@link #create(ResourceLocation)} */
  @Internal
  public Pattern {}

  /**
   * Gets the translation key for this pattern
   * @return  Translation key
   */
  public String getTranslationKey() {
    // TODO: cache this?
    return Util.makeTranslationKey("pattern", id);
  }

  /**
   * Gets the display name for this pattern
   * @return  Display name
   */
  public Component getDisplayName() {
    return Component.translatable(getTranslationKey());
  }

  /**
   * Gets the texture for this pattern for rendering
   * @return  Pattern texture
   */
  public ResourceLocation getTexture() {
    // TODO: migrate to GUI sprites
    return id.withPrefix("gui/tinker_pattern/");
  }

  /** Checks if this pattern matches the give item, typically a tool part. */
  public boolean matches(ItemLike item) {
    return id.equals(Loadables.ITEM.getKey(item.asItem()));
  }

  @Override
  public String toString() {
    return id.toString();
  }


  /* Constructors */

  /** Parses a pattern from the given string */
  public static Pattern create(ResourceLocation id) {
    return CACHE.apply(id);
  }

  /** Parses a pattern from the given string */
  public static Pattern create(String domain, String name) {
    return create(ResourceLocation.fromNamespaceAndPath(domain, name));
  }

  /** Parses a pattern from the given string */
  public static Pattern parse(String string) {
    return create(ResourceLocation.parse(string));
  }

  /** Parses a pattern from the given string */
  @Nullable
  public static Pattern tryParse(String string) {
    return LOADABLE.tryParse(string);
  }

  /** Gets the pattern for the given item ID */
  public static Pattern fromItem(ItemLike item) {
    return create(Loadables.ITEM.getKey(item.asItem()));
  }
}
