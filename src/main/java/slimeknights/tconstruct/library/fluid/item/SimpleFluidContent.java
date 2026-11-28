package slimeknights.tconstruct.library.fluid.item;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.tools.component.CollectionComponent;

import java.util.function.Function;
import java.util.function.Predicate;

/** Reimplementation of {@link net.neoforged.neoforge.fluids.SimpleFluidContent} but with more accessors for non-mutating data. */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SimpleFluidContent implements DataComponentHolder, CollectionComponent {
  /** Instance holding an empty stack */
  public static final SimpleFluidContent EMPTY = new SimpleFluidContent(FluidStack.EMPTY);
  /** Codec for NBT */
  public static final Codec<SimpleFluidContent> CODEC;
  /** Stream codec for network */
  public static final StreamCodec<RegistryFriendlyByteBuf, SimpleFluidContent> STREAM_CODEC;
  static {
    Function<FluidStack, SimpleFluidContent> constructor = SimpleFluidContent::of;
    Function<SimpleFluidContent,FluidStack> getter = c -> c.stack;
    CODEC = FluidStack.OPTIONAL_CODEC.xmap(constructor, getter);
    STREAM_CODEC = FluidStack.OPTIONAL_STREAM_CODEC.map(constructor, getter);
  }

  private final FluidStack stack;

  /** Creates a new instance by copying the given stack. */
  private static SimpleFluidContent of(FluidStack stack) {
    return stack.isEmpty() ? EMPTY : new SimpleFluidContent(stack);
  }

  /** Creates a new instance by copying the given stack. */
  public static SimpleFluidContent copyOf(FluidStack stack) {
    return stack.isEmpty() ? EMPTY : new SimpleFluidContent(stack.copy());
  }

  /** Creates a new instance by copying the given stack with the given amount. */
  public static SimpleFluidContent copyWithAmount(FluidStack stack, int amount) {
    return stack.isEmpty() ? EMPTY : new SimpleFluidContent(stack.copyWithAmount(amount));
  }

  /** Gets a copy of the contained stack */
  public FluidStack copy() {
    return stack.copy();
  }

  /** Gets a copy of the contained stack with the given amount */
  public FluidStack copyWithAmount(int amount) {
    return stack.copyWithAmount(amount);
  }


  /* Stack properties */

  /** @see FluidStack#isEmpty() */
  @Override
  public boolean isEmpty() {
    return stack.isEmpty();
  }

  /** @see FluidStack#getAmount()  */
  public int getAmount() {
    return stack.getAmount();
  }

  /** @see FluidStack#getFluid() */
  public Fluid getFluid() {
    return stack.getFluid();
  }

  /** @see FluidStack#getFluidHolder() () */
  public Holder<Fluid> getFluidHolder() {
    return stack.getFluidHolder();
  }

  /** @see FluidStack#getComponents() */
  @Override
  public DataComponentMap getComponents() {
    return stack.getComponents();
  }

  /** @see FluidStack#getComponentsPatch() */
  public DataComponentPatch getComponentsPatch() {
    return stack.getComponentsPatch();
  }

  /** Gets the display name for the contained stack. */
  public Component getHoverName() {
    return stack.getHoverName();
  }


  /* Item checks */

  /** @see FluidStack#is(TagKey) */
  public boolean is(TagKey<Fluid> tag) {
    return stack.is(tag);
  }

  /** @see FluidStack#is(Fluid) */
  public boolean is(Fluid fluid) {
    return stack.is(fluid);
  }

  /** @see FluidStack#is(Predicate) */
  public boolean is(Predicate<Holder<Fluid>> predicate) {
    return stack.is(predicate);
  }

  /** @see FluidStack#is(Holder) */
  public boolean is(Holder<Fluid> holder) {
    return stack.is(holder);
  }

  /** @see FluidStack#is(HolderSet) */
  public boolean is(HolderSet<Fluid> holders) {
    return stack.is(holders);
  }


  /* Stack checks */

  /** @see FluidStack#matches(FluidStack, FluidStack) */
  public boolean matches(FluidStack other) {
    return FluidStack.matches(stack, other);
  }

  /** @see FluidStack#isSameFluid(FluidStack, FluidStack)  */
  public boolean isSameFluid(FluidStack other) {
    return FluidStack.isSameFluid(stack, other);
  }

  /** @see FluidStack#isSameFluidSameComponents(FluidStack, FluidStack)   */
  public boolean isSameFluidSameComponents(FluidStack other) {
    return FluidStack.isSameFluidSameComponents(stack, other);
  }

  /** @see FluidStack#isSameFluidSameComponents(FluidStack, FluidStack)  */
  public boolean isSameFluidSameComponents(SimpleFluidContent content) {
    return isSameFluidSameComponents(content.stack);
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof SimpleFluidContent other && FluidStack.matches(this.stack, other.stack);
  }

  @Override
  public int hashCode() {
    return this.stack.getAmount() * 31 + FluidStack.hashFluidAndComponents(this.stack);
  }


  /* Static helpers */

  /** Gets the fluid from the given stack */
  public static SimpleFluidContent get(ItemStack stack) {
    return stack.getOrDefault(TinkerData.TANK, EMPTY);
  }

  /** Updates a stack with the given fluid components */
  public static ItemStack set(ItemStack stack, SimpleFluidContent fluid) {
    CollectionComponent.set(stack, TinkerData.TANK, fluid);
    return stack;
  }

  /** Updates a stack with the given fluid */
  public static ItemStack set(ItemStack stack, FluidStack fluid) {
    return set(stack, of(fluid));
  }
}
