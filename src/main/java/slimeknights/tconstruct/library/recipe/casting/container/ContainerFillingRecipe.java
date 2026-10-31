package slimeknights.tconstruct.library.recipe.casting.container;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.MultiNamedRecipe;
import slimeknights.mantle.recipe.NamedRecipe;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.recipe.casting.CastingRecipeInput;
import slimeknights.tconstruct.library.recipe.casting.DisplayCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.ICastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.IDisplayableCastingRecipe;

import java.util.List;

/**
 * Casting recipe that takes an arbitrary fluid for a given amount and fills a container
 */
@RequiredArgsConstructor
public class ContainerFillingRecipe implements ICastingRecipe, MultiNamedRecipe<IDisplayableCastingRecipe> {
  public static final RecordLoadable<ContainerFillingRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.TYPE.requiredField(), LoadableRecipeSerializer.SERIALIZER.requiredField(), LoadableRecipeSerializer.RECIPE_GROUP,
    IntLoadable.FROM_ONE.requiredField("fluid_amount", r -> r.fluidAmount),
    Loadables.ITEM.requiredField("container", r -> r.container),
    ContainerFillingRecipe::new);

  @Getter
  private final RecipeType<?> type;
  @Getter
  private final RecipeSerializer<?> serializer;
  @Getter
  private final String group;
  private final int fluidAmount;
  private final Item container;

  @Override
  public boolean isConsumed() {
    return true;
  }

  @Override
  public boolean switchSlots() {
    return false;
  }

  @Override
  public int getCoolingTime(CastingRecipeInput inv) {
    return 5;
  }
  @Override
  public int getFluidAmount(CastingRecipeInput input) {
    FluidStack fluid = input.getFluid();
    IFluidHandlerItem handler = input.getItem().getCapability(Capabilities.FluidHandler.ITEM);
    return handler == null ? 0 : handler.fill(fluid.copyWithAmount(this.fluidAmount), FluidAction.SIMULATE);
  }

  @Override
  public boolean matches(CastingRecipeInput inv, Level level) {
    ItemStack stack = inv.getItem();
    if (stack.getItem() != this.container) {
      return false;
    }
    IFluidHandlerItem handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
    return handler != null && handler.fill(inv.getFluid().copyWithAmount(this.fluidAmount), FluidAction.SIMULATE) > 0;
  }

  /** @deprecated use {@link ICastingRecipe#assemble(RecipeInput, HolderLookup.Provider)} */
  @Override
  @Deprecated
  public ItemStack getResultItem(HolderLookup.Provider registries) {
    return new ItemStack(this.container);
  }

  @Override
  public ItemStack assemble(CastingRecipeInput inv, HolderLookup.Provider registries) {
    ItemStack stack = inv.getItem().copyWithCount(1);
    IFluidHandlerItem handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
    if (handler == null) {
      return stack;
    }
    handler.fill(inv.getFluid().copyWithAmount(this.fluidAmount), FluidAction.EXECUTE);
    return handler.getContainer();
  }


  /* Display */
  /** Cache of items to display for this container */
  private List<NamedRecipe<IDisplayableCastingRecipe>> displayRecipes = null;

  @Override
  public List<NamedRecipe<IDisplayableCastingRecipe>> getRecipes(ResourceLocation id, HolderLookup.Provider registries) {
    if (displayRecipes == null) {
      // filter fluid registry to just fluids we care about
      List<FluidStack> fluids = BuiltInRegistries.FLUID.stream()
        .filter(fluid -> {
          // skip flowing fluids (redundant to source), fluids with no bucket (probably internal), and fluids hidden from recipe viewers (they be hidden)
          if (fluid.isSource(fluid.defaultFluidState()) && !fluid.is(TinkerTags.Fluids.HIDE_IN_CREATIVE_TANKS)) {
            try {
              Item bucket = fluid.getBucket();
              return bucket != Items.AIR && !bucket.builtInRegistryHolder().is(TinkerTags.Items.HIDDEN_IN_RECIPE_VIEWERS);
            } catch (Exception e) {
              // Registrate (popular dependency for making registration easier) is broken and throws in getBucket for fluids with no bucket
              // we could just skip the bucket check, but its just going to throw when we try to fill an empty bucket in map below
            }
          }
          return false;
        })
        .map(fluid -> new FluidStack(fluid, fluidAmount))
        .toList();
      // fill the container with each fluid stack
      List<ItemStack> results = fluids.stream().map(fluid -> {
        ItemStack stack = new ItemStack(container);
        return FluidUtil.getFluidHandler(stack).map(handler -> {
          handler.fill(fluid, FluidAction.EXECUTE);
          return handler.getContainer();
        }).orElse(stack);
      }).toList();
      displayRecipes = List.of(DisplayCastingRecipe.id(id)
        // cast is just an empty container, which is "consumed"
        .cast(new ItemStack(container)).consumed()
        .fluids(fluids).results(results).linkFluidsToOutput()
        .coolingTime(5)
        .build());
    }
    return displayRecipes;
  }
}
