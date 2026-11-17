package slimeknights.tconstruct.library.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.json.IntRange;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.utils.TagUtil;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Data class holding a modifier with a level
 */
@Getter
@RequiredArgsConstructor
public class ModifierEntry implements Comparable<ModifierEntry> {
  /** Key for modifier IDs in NBT and JSON */
  public static final String TAG_MODIFIER = "name";
  /** Key for modifier levels in NBT and JSON */
  public static final String TAG_LEVEL = "level";
  /** Key for incremental amount */
  public static final String TAG_AMOUNT = "amount";
  /** Key for incremental need */
  public static final String TAG_NEEDED = "needed";

  /** Empty modifier instance, default for many methods */
  public static final ModifierEntry EMPTY = new ModifierEntry(ModifierId.EMPTY, 0);

  /** Range of levels for a modifier including 0 (not on the tool) */
  public static final IntRange ANY_LEVEL = new IntRange(0, Short.MAX_VALUE);
  /** Range of levels for a modifier on a tool */
  public static final IntRange VALID_LEVEL = new IntRange(1, Short.MAX_VALUE);

  /** Loadable instance for recipe parsing. Does not handle incremental, we currently disallow incremental as traits */
  public static final RecordLoadable<ModifierEntry> LOADABLE;
  /** Codec instance for NBT parsing. Handles incremental using the amount and needed fields */
  public static final Codec<ModifierEntry> CODEC;
  /** Codec instance for NBT parsing. Handles incremental using the amount and needed fields */
  public static final StreamCodec<FriendlyByteBuf,ModifierEntry> STREAM_CODEC = new StreamCodec<>() {
    @Override
    public ModifierEntry decode(FriendlyByteBuf buffer) {
      ModifierHolder holder = ModifierHolder.STREAM_CODEC.decode(buffer);
      int level = buffer.readVarInt();
      int needed = buffer.readVarInt();
      // if needed is 0, no amount is coming
      if (needed == 0) {
        return new ModifierEntry(holder, level);
      }
      return IncrementalModifierEntry.of(holder, level, buffer.readVarInt(), needed);
    }

    @Override
    public void encode(FriendlyByteBuf buffer, ModifierEntry value) {
      ModifierHolder.STREAM_CODEC.encode(buffer, value.getHolder());
      buffer.writeVarInt(value.getLevel());
      int needed = value.getNeeded();
      buffer.writeVarInt(needed);
      // if needed is 0, no need to sync over amount, its just going to be ignored
      if (needed > 0) {
        buffer.writeVarInt(value.getAmount(0));
      }
    }
  };
  static {
    // using the holder means writing to NBT will write the resolved redirect
    Function<ModifierEntry,ModifierHolder> modifier = ModifierEntry::getHolder;
    Function<ModifierEntry,Integer> level = ModifierEntry::getLevel;
    LOADABLE = RecordLoadable.create(
      ModifierHolder.LOADABLE.requiredField(TAG_MODIFIER, modifier),
      IntLoadable.FROM_ONE.defaultField(TAG_LEVEL, 1, true, level),
      ModifierEntry::new);
    CODEC = RecordCodecBuilder.create(instance -> instance.group(
      ModifierHolder.CODEC.fieldOf(TAG_MODIFIER).forGetter(modifier),
      ExtraCodecs.POSITIVE_INT.fieldOf(TAG_LEVEL).orElse(1).forGetter(level),
      ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf(TAG_AMOUNT, 0).forGetter(entry -> entry.getAmount(0)),
      ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf(TAG_NEEDED, 0).forGetter(ModifierEntry::getNeeded)
    ).apply(instance, instance.stable(ModifierEntry::of)));
  }

  /** Modifier instance */
  protected final ModifierHolder holder;
  /** Current level */
  @With
  protected final int level;

  public ModifierEntry(ModifierId id, int level) {
    this(ModifierHolder.create(id), level);
  }

  public ModifierEntry(Modifier modifier, int level) {
    this(ModifierHolder.from(modifier), level);
  }


  /* Incremental - in this class as we need it for the codecs */

  /**
   * Creates a new incremental modifier entry
   * @param modifier  Modifier instance
   * @param level     Modifier level, will be effectively as low as 1 less based on amount
   * @param amount    Amount of progress towards the level
   * @param needed    Amount of progress needed for a full level
   * @return  Either an incremental or a regular modifier entry based on parameters
   * @see #ModifierEntry(ModifierHolder,int)
   */
  public static ModifierEntry of(ModifierHolder modifier, int level, int amount, int needed) {
    // If we have a full level, no need to be incremental
    if (needed == 0 || amount >= needed) {
      return new ModifierEntry(modifier, level);
    }
    // no sense saving the needed if amount is 0, that's just the lower level
    if (amount <= 0) {
      return new ModifierEntry(modifier, level - 1);
    }
    return new IncrementalModifierEntry(modifier, level, amount, needed);
  }

  /**
   * Creates a new incremental modifier entry
   * @param modifier  Modifier instance
   * @param level     Modifier level, will be effectively as low as 1 less based on amount
   * @param amount    Amount of progress towards the level
   * @param needed    Amount of progress needed for a full level
   * @return  Either an incremental or a regular modifier entry based on parameters
   * @see #ModifierEntry(ModifierId,int)
   */
  public static ModifierEntry of(ModifierId modifier, int level, int amount, int needed) {
    return of(ModifierHolder.create(modifier), level, amount, needed);
  }

  /**
   * Creates a new incremental modifier entry
   * @param modifier  Modifier instance
   * @param level     Modifier level, will be effectively as low as 1 less based on amount
   * @param amount    Amount of progress towards the level
   * @param needed    Amount of progress needed for a full level, if 0 will be treated as full progress
   * @return  Either an incremental or a regular modifier entry based on parameters
   * @see #ModifierEntry(Modifier,int)
   */
  public static ModifierEntry of(Modifier modifier, int level, int amount, int needed) {
    return of(ModifierHolder.from(modifier), level, amount, needed);
  }


  /* Methods */

  /** Checks if the given modifier is bound */
  public boolean isBound() {
    return holder.isBound();
  }

  /** Gets the contained modifier ID, prevents resolving the lazy modifier if not needed */
  public ModifierId id() {
    return holder.id();
  }

  /** Gets the contained modifier */
  public Modifier getModifier() {
    return holder.get();
  }

  /** Gets the given hook from the modifier, returning default instance if not present */
  public final <T> T getHook(ModuleHook<T> hook) {
    return holder.get().getHook(hook);
  }


  /* Levels */

  /**
   * {@return Entry level, possibly reduced due to the incremental amount.}
   */
  public float getEffectiveLevel() {
    return level;
  }

  /**
   * Returns {@code level - 1} if we have an incremental amount level, level otherwise. Equivalent to flooring {@link #getEffectiveLevel()}
   * @return  Level of the modifier, possibly reduced due to a partial level
   */
  public int intEffectiveLevel() {
    return level;
  }


  /* Incremental */

  /**
   * Gets the incremental amount
   * @param fallback  Value to return if not incremental
   */
  public int getAmount(int fallback) {
    return fallback;
  }

  /** Gets the amount needed for this incremental level, will be 0 for non-incremental */
  public int getNeeded() {
    return 0;
  }

  /** Gets the display name for this entry */
  public Component getDisplayName() {
    return holder.get().getDisplayName(level);
  }


  /* Withers */

  /**
   * Adds the given amount to the modifier
   * @param amount  Amount to add, if 0 no change will be made
   * @param needed  Amount needed, if 0 no change will be made
   * @return  Modifier entry, will be
   */
  public ModifierEntry addAmount(int amount, int needed) {
    // if a need but no amount, the level won't change so don't make a new instance
    if (needed <= 0 || amount <= 0) {
      return this;
    }
    // static constructor automatically handles cases of needed being 0/amount being full (inon-incremental level raise) and amount being 0 (ignore level raise)
    return IncrementalModifierEntry.of(holder, level + 1, amount, needed);
  }

  /**
   * Combines this entry with the other entry
   * @param other  Other entry to merge, precondition is the same modifier type
   * @return  New merged modifier
   */
  public ModifierEntry merge(ModifierEntry other) {
    if (!this.id().equals(other.id())) {
      throw new IllegalArgumentException("Modifiers do not match, have " + id() + " but was given " + other.id());
    }
    // we know we are not incremental, so the only thing to merge is our level with their level
    // this automatically handles other being incremental
    return other.withLevel(level + other.level);
  }


  /* Comparison */

  /** Checks if this entry matches the given modifier */
  public boolean matches(ModifierId id) {
    return holder.id().equals(id);
  }

  /** Checks if this entry matches the given modifier */
  public boolean matches(Modifier modifier) {
    return matches(modifier.id());
  }

  /** Checks if the modifier is in the given tag */
  public boolean matches(TagKey<Modifier> tag) {
    return holder.is(tag);
  }

  @Override
  public int compareTo(ModifierEntry other) {
    Modifier mod1 = this.getModifier(), mod2 = other.getModifier();
    int priority1 = mod1.getPriority(), priority2 = mod2.getPriority();
    // sort by priority first if different
    if (priority1 != priority2) {
      // reversed order so higher goes first
      return Integer.compare(priority2, priority1);
    }
    // fallback to ID path, approximates localized name so we get mostly alphabetical sort in the tooltip
    return mod1.id().getPath().compareTo(mod2.id().getPath());
  }


  /* Serializing */

  /** Reads a modifier entry from NBT */
  public static ModifierEntry readFromNBT(Tag tag) {
    return TagUtil.decode(CODEC, "modifier entry", tag).orElse(EMPTY);
  }

  /** Writes this tag to NBT */
  public Tag serializeToNBT() {
    return CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow();
  }


  /** Object */

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ModifierEntry entry = (ModifierEntry)o;
    return this.matches(entry.id()) && level == entry.level;
  }

  @Override
  public int hashCode() {
    return 31 * holder.hashCode() + level;
  }

  @Override
  public String toString() {
    return "ModifierEntry{" + holder.id() + ",level=" + level + '}';
  }
}
