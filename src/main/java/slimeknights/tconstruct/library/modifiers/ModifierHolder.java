package slimeknights.tconstruct.library.modifiers;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.tconstruct.TConstruct;

import java.util.function.Function;

/**
 * Contains a reference to a modifier, which is lazily fetched and automatically reset when datapacks reload.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ModifierHolder {
  /** Loadable instance for JSON serializing */
  public static final Loadable<ModifierHolder> LOADABLE;
  /** Codec instance for general serializing */
  public static final Codec<ModifierHolder> CODEC;
  /** Loadable instance for JSON serializing */
  public static final StreamCodec<ByteBuf,ModifierHolder> STREAM_CODEC;
  static {
    Function<ModifierId,ModifierHolder> factory = ModifierHolder::create;
    Function<ModifierHolder,ModifierId> id = ModifierHolder::serializedId;
    LOADABLE =  ModifierId.LOADABLE.map(factory, id);
    CODEC =  ModifierId.CODEC.xmap(factory, id);
    STREAM_CODEC = ModifierId.STREAM_CODEC.map(factory, id);
  }

  /** Default instance returning an empty modifier */
  public static final ModifierHolder EMPTY = new ModifierHolder(ModifierId.EMPTY, ModifierManager.INSTANCE.getDefaultValue());

  @Getter @Accessors(fluent = true)
  private final ModifierId id;
  private Modifier modifier;

  /** Gets a modifier holder for the given ID */
  public static ModifierHolder create(ModifierId id) {
    return ModifierManager.INSTANCE.getHolder(id);
  }

  /** Gets the ID to use for serializing, using the modifier ID if fetched. */
  protected ModifierId serializedId() {
    return modifier != null && modifier != ModifierManager.INSTANCE.getDefaultValue() ? modifier.id() : id;
  }

  /** Gets the modifier */
  public Modifier get() {
    if (modifier == null) {
      if (!ModifierManager.INSTANCE.isDynamicModifiersLoaded()) {
        TConstruct.LOG.error("Attempting to fetch modifier {} before the modifier registry is loaded.", id);
        return ModifierManager.INSTANCE.getDefaultValue();
      }
      modifier = ModifierManager.INSTANCE.get(id);
    }
    return modifier;
  }

  /** Clears the cached modifier instance */
  void clearCache() {
    modifier = null;
  }

  /**
   * Checks if this is not the empty modifier
   * @see #isBound()
   */
  public boolean isPresent() {
    return !id.equals(ModifierId.EMPTY);
  }

  /**
   * Returns true if this modifier exists either statically or in a datapack. A return of true here means a useful value is returned by {@link #get()}
   * @see #isPresent()
   */
  public boolean isBound() {
    return ModifierManager.INSTANCE.isDynamicModifiersLoaded() && get() != ModifierManager.INSTANCE.getDefaultValue();
  }

  /** Gets a holder instance after applying redirects. */
  public ModifierHolder resolve() {
    ModifierId resolved = get().id();
    if (resolved.equals(id)) {
      return this;
    }
    return ModifierManager.INSTANCE.getHolder(resolved);
  }

  /** Checks if the modifier is in the given tag */
  public boolean is(ModifierId id) {
    return this.id.equals(id);
  }

  /** Checks if the modifier is in the given tag */
  public boolean is(Modifier modifier) {
    return this.id.equals(modifier.id());
  }

  /** Checks if the modifier is in the given tag */
  public boolean is(TagKey<Modifier> tag) {
    return ModifierManager.isInTag(id, tag);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    return other instanceof ModifierHolder holder && this.id.equals(holder.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public String toString() {
    return "ModifierHolder{" + id + '}';
  }
}
