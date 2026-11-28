package slimeknights.tconstruct.library.fluid.item;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.data.codec.MantleCodecs;
import slimeknights.mantle.network.codec.MantleStreamCodecs;
import slimeknights.mantle.util.DataComponentHelper;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.tools.component.CollectionComponent;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Simple component holding a fluid and components with no size. Used in {@link slimeknights.tconstruct.smeltery.item.CopperCanItem}.
 * @see SimpleFluidContent
 */
public record FluidComponents(Fluid fluid, DataComponentPatch components) implements CollectionComponent {
  /** Empty instance */
  public static final FluidComponents EMPTY = new FluidComponents(Fluids.EMPTY, DataComponentPatch.EMPTY);
  /** Codec instance for serialization. Serializes lone fluids as a string. */
  public static final Codec<FluidComponents> CODEC = Codec.either(
    MantleCodecs.NON_EMPTY_FLUID,
    RecordCodecBuilder.<FluidComponents>create(instance -> instance.group(
      BuiltInRegistries.FLUID.byNameCodec().optionalFieldOf("id", Fluids.EMPTY).forGetter(FluidComponents::fluid),
      DataComponentPatch.CODEC.fieldOf("components").forGetter(FluidComponents::components)
    ).apply(instance, FluidComponents::of))
  ).xmap(
    either -> either.map(FluidComponents::of, Function.identity()),
    fluid -> fluid.hasComponents() ? Either.right(fluid) : Either.left(fluid.fluid)
  );
  /** Stream codec instance for network buffer */
  public static final StreamCodec<RegistryFriendlyByteBuf,FluidComponents> STREAM_CODEC = StreamCodec.composite(
    MantleStreamCodecs.FLUID, FluidComponents::fluid,
    DataComponentPatch.STREAM_CODEC, FluidComponents::components,
    FluidComponents::of);

  /** Creates a new instance from the given fluid and component */
  public static FluidComponents of(Fluid fluid, DataComponentPatch components) {
    return fluid == Fluids.EMPTY ? EMPTY : new FluidComponents(fluid, components);
  }

  /** Creates a new instance from the given fluid and component */
  public static FluidComponents of(Fluid fluid) {
    return of(fluid, DataComponentPatch.EMPTY);
  }

  /** Creates a new instance from the given fluid stack, discarding amount */
  public static FluidComponents of(FluidStack stack) {
    return of(stack.getFluid(), stack.getComponentsPatch());
  }


  /** Checks if this component is empty */
  @Override
  public boolean isEmpty() {
    return this.fluid == Fluids.EMPTY;
  }

  /** Checks if the fluid has components */
  public boolean hasComponents() {
    return !this.components.isEmpty();
  }

  /** Creates a fluid stack with the given size */
  public FluidStack toStack(int amount) {
    return amount <= 0 || fluid == Fluids.EMPTY ? FluidStack.EMPTY : DataComponentHelper.makeStack(fluid, amount, components);
  }

  /** @see FluidStack#isSameFluid(FluidStack, FluidStack) */
  public boolean isSameFluid(FluidStack stack) {
    return stack.is(fluid);
  }

  /** @see FluidStack#isSameFluidSameComponents(FluidStack, FluidStack) */
  public boolean isSameFluidSameComponents(FluidStack other) {
    return isSameFluid(other) && ((isEmpty() && other.isEmpty()) || Objects.equals(components, other.getComponentsPatch()));
  }


  /* Static helpers */

  /** Gets the fluid from the given stack */
  public static FluidComponents get(DataComponentHolder stack) {
    return stack.getOrDefault(TinkerData.FLUID, EMPTY);
  }

  /** Updates a stack with the given fluid components */
  public static <T extends MutableDataComponentHolder> T set(T stack, FluidComponents fluid) {
    CollectionComponent.set(stack, TinkerData.FLUID, fluid);
    return stack;
  }

  /** Updates a stack with the given fluid */
  public static <T extends MutableDataComponentHolder> T set(T stack, FluidStack fluid) {
    return set(stack, of(fluid));
  }

  /** Updates a stack with the given fluid */
  public static <T extends MutableDataComponentHolder> T set(T stack, Fluid fluid) {
    return set(stack, of(fluid));
  }

  /** Updates a stack with the given fluid and components */
  public static <T extends MutableDataComponentHolder> T set(T stack, Fluid fluid, DataComponentPatch components) {
    return set(stack, of(fluid, components));
  }

  /** Adds filled variants of the copper can to the given consumer */
  public static void addFilledVariants(ItemLike item, Consumer<ItemStack> output) {
    BuiltInRegistries.FLUID.holders().filter(holder -> {
      Fluid fluid = holder.value();
      return fluid.isSource(fluid.defaultFluidState()) && !holder.is(TinkerTags.Fluids.HIDE_IN_CREATIVE_TANKS);
    }).forEachOrdered(holder -> {
      output.accept(set(new ItemStack(item), holder.value()));
    });
  }
}
