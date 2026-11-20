package slimeknights.tconstruct.library.tools.nbt;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.ApiStatus.Internal;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.modifiers.IncrementalModifierEntry;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.tools.helper.ModifierBuilder;
import slimeknights.tconstruct.library.utils.TagUtil;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * NBT object containing all current modifiers.
 * @param modifiers  List of modifiers in order. Should be immutable.
 */
public record ModifierNBT(List<ModifierEntry> modifiers) implements Iterable<ModifierEntry> {
  /** Instance containing no modifiers */
  public static final ModifierNBT EMPTY = new ModifierNBT(Collections.emptyList());

  /** Codec for NBT serializing */
  public static final Codec<ModifierNBT> CODEC;
  /** Stream for NBT syncing */
  public static final StreamCodec<FriendlyByteBuf,ModifierNBT> STREAM_CODEC;
  static {
    Function<ModifierNBT,List<ModifierEntry>> getter = ModifierNBT::modifiers;
    Function<List<ModifierEntry>,ModifierNBT> constructor = ModifierNBT::create;
    CODEC = ModifierEntry.CODEC.listOf().xmap(constructor, getter);
    STREAM_CODEC = ModifierEntry.STREAM_CODEC.apply(ByteBufCodecs.list()).map(constructor, getter);
  }

  /** @apiNote use {@link #create(List)} */
  @Internal
  public ModifierNBT {}
  
  /** Creates a new modifier NBT */
  public static ModifierNBT create(List<ModifierEntry> modifiers) {
    return modifiers.isEmpty() ? EMPTY : new ModifierNBT(modifiers);
  }
  
  /**
   * Checks if the NBT has no modifiers
   * @return  True if there are no modifiers
   */
  public boolean isEmpty() {
    return modifiers.isEmpty();
  }

  /**
   * Gets the modifier entry for a modifier
   * @param modifier  Modifier to check
   * @return  Modifier entry, or {@link ModifierEntry#EMPTY} if absent
   */
  public ModifierEntry getEntry(ModifierId modifier) {
    for (ModifierEntry entry : modifiers) {
      if (entry.matches(modifier)) {
        return entry;
      }
    }
    return ModifierEntry.EMPTY;
  }

  /**
   * Gets the level of a modifier
   * @param modifier  Modifier to check
   * @return  Modifier level, or 0 if modifier is missing
   */
  public int getLevel(ModifierId modifier) {
    return getEntry(modifier).getLevel();
  }

  /**
   * Checks if the listing has the given modifier tag.
   * To check if it has a specific modifier, use {@link #getLevel(ModifierId)}.
   * @param tag  Modifier tag
   * @return  True if any modifier in the tag is present
   */
  public boolean has(TagKey<Modifier> tag) {
    for (ModifierEntry entry : modifiers) {
      if (ModifierManager.isInTag(entry.id(), tag)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the listing has a modifier matching the given predicate.
   * To check if it has a specific modifier, use {@link #getLevel(ModifierId)}.
   * @param predicate  Predicate to test
   * @return  True if any modifier in the tag is present
   */
  public boolean has(IJsonPredicate<ModifierId> predicate) {
    for (ModifierEntry entry : modifiers) {
      if (predicate.matches(entry.id())) {
        return true;
      }
    }
    return false;
  }


  /* Iterator */

  @Override
  public Iterator<ModifierEntry> iterator() {
    return modifiers.iterator();
  }

  @Override
  public void forEach(Consumer<? super ModifierEntry> action) {
    modifiers.forEach(action);
  }

  @Override
  public Spliterator<ModifierEntry> spliterator() {
    return modifiers.spliterator();
  }


  /* Withers */

  /**
   * Creates a copy of this NBT with the given modifier added. Result will be unsorted
   * Do not use if you need to make multiple additions, use {@link ModifierNBT.Builder}
   * @param modifier  Modifier
   * @param level     Levels of the modifier to add
   * @return  Instance with the given modifier
   */
  public ModifierNBT withModifier(ModifierId modifier, int level) {
    if (level <= 0) {
      throw new IllegalArgumentException("Invalid level, must be above zero");
    }

    // rather than using the builder, just use a raw list builder
    // easier for adding a single entry, and the cases that call this method don't care about sorting
    ImmutableList.Builder<ModifierEntry> builder = ImmutableList.builder();
    boolean found = false;
    for (ModifierEntry entry : this.modifiers) {
      // first match increases the level
      // shouldn't be a second match (all the methods are protected), but just in case we prevent modifier duplication
      if (!found && entry.matches(modifier)) {
        builder.add(entry.withLevel(entry.getLevel() + level));
        found = true;
      } else {
        builder.add(entry);
      }
    }
    // if no matching modifier, create a new entry
    if (!found) {
      builder.add(new ModifierEntry(modifier, level));
    }
    // guaranteed to have at least 1 modifier so no need for #create's empty check
    return new ModifierNBT(builder.build());
  }

  /**
   * Creates a copy of this NBT with the given incremental modifier amount added.
   * Do not use if you need to make multiple additions, use {@link ModifierNBT.Builder}
   * @param modifier  Modifier
   * @param amount    Increments to add
   * @param needed    Increment scale to reach a full level
   * @return  Instance with the given modifier
   */
  public ModifierNBT addAmount(ModifierId modifier, int amount, int needed) {
    // no need to do anything if amount or need is not at least 1
    if (amount <= 0 || needed <= 0) {
      return this;
    }
    // no shortcut here as amount being greater than need should lead to a full level
    // rather than using the builder, just use a raw list builder
    // easier for adding a single entry, and the cases that call this method don't care about sorting
    ImmutableList.Builder<ModifierEntry> builder = ImmutableList.builder();
    boolean found = false;
    for (ModifierEntry entry : this.modifiers) {
      // first match increases the level
      // shouldn't be a second match (all the methods are protected), but just in case we prevent modifier duplication
      if (!found && entry.matches(modifier)) {
        builder.add(entry.addAmount(amount, needed));
        found = true;
      } else {
        builder.add(entry);
      }
    }
    // if no matching modifier, create a new entry
    if (!found) {
      builder.add(IncrementalModifierEntry.of(modifier, 1, amount, needed));
    }
    // guaranteed to have at least 1 modifier so no need for #create's empty check
    return new ModifierNBT(builder.build());
  }

  /**
   * Creates a copy of this NBT without the given modifier
   * @param modifier  Modifier to remove
   * @param level     Level to remove
   * @return  ModifierNBT without the given modifier
   */
  public ModifierNBT withoutModifier(ModifierId modifier, int level) {
    if (level <= 0) {
      throw new IllegalArgumentException("Invalid level, must be above zero");
    }

    // rather than using the builder, just use a raw list builder
    // easier for adding a single entry, and the cases that call this method don't care about sorting
    ImmutableList.Builder<ModifierEntry> builder = ImmutableList.builder();
    for (ModifierEntry entry : this.modifiers) {
      if (entry.matches(modifier) && level > 0) {
        if (entry.getLevel() > level) {
          builder.add(entry.withLevel(entry.getLevel() - level));
          level = 0;
        } else {
          level -= entry.getLevel();
        }
      } else {
        builder.add(entry);
      }
    }
    return create(builder.build());
  }


  /* NBT */

  /** Re-adds the modifier list from NBT */
  @Deprecated
  public static ModifierNBT readFromNBT(@Nullable Tag tag) {
    if (tag == null || tag.getId() != Tag.TAG_LIST) {
      return EMPTY;
    }
    ListTag listNBT = (ListTag)tag;
    if (listNBT.getElementType() != Tag.TAG_COMPOUND) {
      return EMPTY;
    }
    return TagUtil.decode(CODEC, "modifier nbt", tag).orElse(EMPTY);
  }

  /** Writes these modifiers to NBT */
  @Deprecated
  public Tag serializeToNBT() {
    return TagUtil.encode(CODEC, this);
  }


  /* Helpers */

  /** Gets the modifiers from a stack */
  public static ModifierNBT get(ItemStack stack) {
    return stack.getOrDefault(TinkerData.ALL_MODIFIERS, EMPTY);
  }

  /** Helper to quickly fetch a single modifier entry from a stack. Use {@link #get(ItemStack)} if you need to get multiple. */
  public static ModifierEntry getEntry(ItemStack stack, ModifierId id) {
    return get(stack).getEntry(id);
  }

  /** Helper to quickly fetch a single modifier level from a stack. Use {@link #get(ItemStack)} if you need to get multiple. */
  public static int getLevel(ItemStack stack, ModifierId id) {
    return get(stack).getLevel(id);
  }

  /** Checks if the given stack has upgrades */
  public static boolean hasCrafted(ItemStack stack) {
    return !stack.getOrDefault(TinkerData.CRAFTED_MODIFIERS, EMPTY).isEmpty();
  }


  /* Builder */

  /**
   * Creates a new builder for modifier NBT
   * @return  Builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder class for creating a modifier list with multiple additions. Builder results will be sorted
   */
  public static class Builder implements ModifierBuilder {
    /** Intentionally using modifiers to ensure they are resolved */
    private final Map<ModifierId, ModifierEntry> modifiers = new LinkedHashMap<>();

    private Builder() {}

    @Override
    public Builder add(ModifierEntry entry) {
      if (entry != ModifierEntry.EMPTY && entry.isBound()) {
        ModifierId id = entry.id();
        ModifierEntry current = modifiers.get(id);
        if (current != null) {
          entry = current.merge(entry);
        }
        modifiers.put(id, entry);
      }
      return this;
    }

    @Override
    public ModifierNBT build() {
      // converts the map into a list of entries, priority sorted
      // note priority is negated so higher numbers go first
      return create(modifiers.values().stream()
        // sort on priority, falls back to the order they were added
        .sorted(Comparator.comparingInt(entry -> -entry.getModifier().getPriority()))
        .toList());
    }
  }
}
