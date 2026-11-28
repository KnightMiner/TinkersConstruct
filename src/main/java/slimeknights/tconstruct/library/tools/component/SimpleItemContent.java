package slimeknights.tconstruct.library.tools.component;

import com.mojang.serialization.Codec;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;
import java.util.function.Predicate;

/** Same as {@link slimeknights.tconstruct.library.fluid.item.SimpleFluidContent} but holds an item. */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SimpleItemContent implements DataComponentHolder {
  /** Instance holding an empty stack */
  public static final SimpleItemContent EMPTY = new SimpleItemContent(ItemStack.EMPTY);
  /** Codec for NBT */
  public static final Codec<SimpleItemContent> CODEC;
  /** Stream codec for network */
  public static final StreamCodec<RegistryFriendlyByteBuf,SimpleItemContent> STREAM_CODEC;
  static {
    Function<ItemStack,SimpleItemContent> constructor = SimpleItemContent::of;
    Function<SimpleItemContent,ItemStack> getter = c -> c.stack;
    CODEC = ItemStack.OPTIONAL_CODEC.xmap(constructor, getter);
    STREAM_CODEC = ItemStack.OPTIONAL_STREAM_CODEC.map(constructor, getter);
  }

  private final ItemStack stack;

  /** Creates a new instance by copying the given stack. */
  private static SimpleItemContent of(ItemStack stack) {
    return stack.isEmpty() ? EMPTY : new SimpleItemContent(stack);
  }

  /** Creates a new instance by copying the given stack. */
  public static SimpleItemContent copyOf(ItemStack stack) {
    return stack.isEmpty() ? EMPTY : new SimpleItemContent(stack.copy());
  }

  /** Gets a copy of the contained stack */
  public ItemStack copy() {
    return this.stack.copy();
  }


  /* Stack properties */

  /** @see ItemStack#isEmpty() */
  public boolean isEmpty() {
    return this.stack.isEmpty();
  }

  /** @see ItemStack#getCount() */
  public int getCount() {
    return this.stack.getCount();
  }

  /** @see ItemStack#getItem() */
  public Item getItem() {
    return stack.getItem();
  }

  /** @see ItemStack#getItemHolder() */
  public Holder<Item> getItemHolder() {
    return stack.getItemHolder();
  }

  /** @see ItemStack#getComponents() */
  @Override
  public DataComponentMap getComponents() {
    return stack.getComponents();
  }

  /** @see ItemStack#getComponentsPatch() */
  public DataComponentPatch getComponentsPatch() {
    return stack.getComponentsPatch();
  }

  /** Gets the display name for the contained stack. */
  public Component getDisplayName() {
    return stack.getDisplayName();
  }


  /* Item checks */

  /** @see ItemStack#is(TagKey) */
  public boolean is(TagKey<Item> tag) {
    return stack.is(tag);
  }

  /** @see ItemStack#is(Item) */
  public boolean is(Item item) {
    return stack.is(item);
  }

  /** @see ItemStack#is(Predicate) */
  public boolean is(Predicate<Holder<Item>> predicate) {
    return stack.is(predicate);
  }

  /** @see ItemStack#is(Holder) */
  public boolean is(Holder<Item> holder) {
    return stack.is(holder);
  }

  /** @see ItemStack#is(HolderSet) */
  public boolean is(HolderSet<Item> holders) {
    return stack.is(holders);
  }


  /* Stack checks */

  /** @see ItemStack#matches(ItemStack, ItemStack) */
  public boolean matches(ItemStack other) {
    return ItemStack.matches(stack, other);
  }

  /** @see ItemStack#isSameItem(ItemStack, ItemStack) */
  public boolean isSameItem(ItemStack other) {
    return ItemStack.isSameItem(stack, other);
  }

  /** @see ItemStack#isSameItemSameComponents(ItemStack, ItemStack)  */
  public boolean isSameItemSameComponents(ItemStack other) {
    return ItemStack.isSameItemSameComponents(stack, other);
  }

  /** @see ItemStack#isSameItemSameComponents(ItemStack, ItemStack)  */
  public boolean isSameItemSameComponents(SimpleItemContent content) {
    return isSameItemSameComponents(content.stack);
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof SimpleItemContent other && ItemStack.matches(this.stack, other.stack);
  }

  @Override
  public int hashCode() {
    return this.stack.getCount() * 31 + ItemStack.hashItemAndComponents(this.stack);
  }
}
