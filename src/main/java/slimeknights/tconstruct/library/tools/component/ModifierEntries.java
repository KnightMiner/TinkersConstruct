package slimeknights.tconstruct.library.tools.component;

import com.mojang.serialization.Codec;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * NBT object containing all current modifiers.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ModifierEntries implements Iterable<ModifierEntry>, CollectionComponent {
  /** Instance containing no modifiers */
  public static final ModifierEntries EMPTY = new ModifierEntries(List.of(), Map.of());

  /** Codec for NBT serializing */
  public static final Codec<ModifierEntries> CODEC;
  /** Stream for NBT syncing */
  public static final StreamCodec<FriendlyByteBuf, ModifierEntries> STREAM_CODEC;
  static {
    Function<ModifierEntries,List<ModifierEntry>> getter = m -> m.modifiers;
    Function<List<ModifierEntry>, ModifierEntries> constructor = ModifierEntries::createUnchecked;
    CODEC = ModifierEntry.CODEC.listOf().xmap(constructor, getter);
    STREAM_CODEC = ModifierEntry.STREAM_CODEC.apply(ByteBufCodecs.list()).map(constructor, getter);
  }

  @Accessors(fluent = true)
  @Getter
  private final List<ModifierEntry> modifiers;
  private final Map<ModifierId,ModifierEntry> modifierLookup;

  /** Creates a new modifier NBT from a list of modifiers that may need merging */
  public static ModifierEntries create(List<ModifierEntry> modifiers) {
    if (modifiers.isEmpty()) {
      return EMPTY;
    }
    return new ModifierEntries(List.copyOf(modifiers), modifiers.stream().collect(Collectors.toUnmodifiableMap(ModifierEntry::id, Function.identity())));
  }

  /** Creates a new instance for the given entries */
  public static ModifierEntries create(ModifierEntry... modifiers) {
    return create(Arrays.asList(modifiers));
  }

  /** Creates a new modifier NBT from a list of modifiers that may need merging */
  public static ModifierEntries createUnchecked(List<ModifierEntry> modifiers) {
    if (modifiers.isEmpty()) {
      return EMPTY;
    }
    // using a linked map to preserve the order
    Map<ModifierId,ModifierEntry> entries = new LinkedHashMap<>();
    for (ModifierEntry entry : modifiers) {
      entries.computeIfPresent(entry.id(), (id, existing) -> existing.merge(entry));
    }
    return new ModifierEntries(List.copyOf(entries.values()), Map.copyOf(entries));
  }
  
  @Override
  public boolean isEmpty() {
    return modifiers.isEmpty();
  }

  /**
   * Gets the modifier entry for a modifier
   * @param modifier  Modifier to check
   * @return  Modifier entry, or {@link ModifierEntry#EMPTY} if absent
   */
  public ModifierEntry getEntry(ModifierId modifier) {
    return modifierLookup.getOrDefault(modifier, ModifierEntry.EMPTY);
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

  /** Updates the entry for the given modifier in the map */
  private ModifierEntries updateEntry(ModifierId modifier, UnaryOperator<ModifierEntry> updater, @Nullable Supplier<ModifierEntry> ifAbsent) {
    // locate the existing entry
    ModifierEntry entry = getEntry(modifier);
    // duplicate data structures to copy, will be doing a patch approach rather than recreating entirely
    List<ModifierEntry> entries = new ArrayList<>(this.modifiers);
    Map<ModifierId, ModifierEntry> lookup = new HashMap<>(modifierLookup);
    // if its absent, just add to both structures
    if (entry != ModifierEntry.EMPTY) {
      // if present, need to swap out the entry in the list
      int index = entries.indexOf(entry);
      entry = updater.apply(entry);
      // remove if the new level is 0
      if (entry.getLevel() > 0) {
        if (index == -1) {
          entries.set(index, entry);
        } else {
          entries.add(entry);
        }
      } else if (index != -1) {
        entries.remove(index);
      }
      lookup.put(modifier, entry);
    } else if (ifAbsent != null) {
      entry = ifAbsent.get();
      entries.add(entry);
      lookup.put(modifier, entry);
    }
    return new ModifierEntries(List.copyOf(entries), Map.copyOf(lookup));
  }

  /**
   * Creates a copy of this NBT with the given modifier added. Result will be unsorted
   * Do not use if you need to make multiple additions, use {@link ModifierEntries.Builder}
   * @param modifier  Modifier
   * @param level     Levels of the modifier to add
   * @return  Instance with the given modifier
   */
  public ModifierEntries withModifier(ModifierId modifier, int level) {
    if (level <= 0) {
      throw new IllegalArgumentException("Invalid level, must be above zero");
    }
    return updateEntry(
      modifier,
      entry -> entry.withLevel(entry.getLevel() + level),
      () -> new ModifierEntry(modifier, level)
    );
  }

  /**
   * Creates a copy of this NBT with the given incremental modifier amount added.
   * Do not use if you need to make multiple additions, use {@link ModifierEntries.Builder}
   * @param modifier  Modifier
   * @param amount    Increments to add
   * @param needed    Increment scale to reach a full level
   * @return  Instance with the given modifier
   */
  public ModifierEntries addAmount(ModifierId modifier, int amount, int needed) {
    // no need to do anything if amount or need is not at least 1
    if (amount <= 0 || needed <= 0) {
      return this;
    }
    return updateEntry(
      modifier,
      entry -> entry.addAmount(amount, needed),
      () -> IncrementalModifierEntry.of(modifier, 1, amount, needed)
    );
  }

  /**
   * Creates a copy of this NBT without the given modifier
   * @param modifier  Modifier to remove
   * @param level     Level to remove
   * @return  ModifierNBT without the given modifier
   */
  public ModifierEntries withoutModifier(ModifierId modifier, int level) {
    if (level <= 0) {
      throw new IllegalArgumentException("Invalid level, must be above zero");
    }
    return updateEntry(
      modifier,
      entry -> entry.withLevel(entry.getLevel() - level),
      null
    );
  }


  /* NBT */

  /** Re-adds the modifier list from NBT */
  @Deprecated
  public static ModifierEntries readFromNBT(@Nullable Tag tag) {
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


  /* Equality */

  @Override
  public boolean equals(Object obj) {
    return this == obj || (obj instanceof ModifierEntries other && this.modifiers.equals(other.modifiers));
  }

  @Override
  public int hashCode() {
    return modifiers.hashCode();
  }
  

  /* Helpers */

  /** Gets the modifiers from a stack */
  public static ModifierEntries get(ItemStack stack) {
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

    /** Builds the modifiers without applying modifier priority sorting */
    public ModifierEntries buildUnsorted() {
      if (modifiers.isEmpty()) {
        return EMPTY;
      }
      return new ModifierEntries(List.copyOf(modifiers.values()), Map.copyOf(modifiers));
    }

    @Override
    public ModifierEntries build() {
      if (modifiers.isEmpty()) {
        return EMPTY;
      }
      // converts the map into a list of entries, priority sorted
      // note priority is negated so higher numbers go first
      return new ModifierEntries(modifiers.values().stream()
        // sort on priority, falls back to the order they were added
        .sorted(Comparator.comparingInt(entry -> -entry.getModifier().getPriority()))
        .toList(), Map.copyOf(modifiers));
    }
  }
}
