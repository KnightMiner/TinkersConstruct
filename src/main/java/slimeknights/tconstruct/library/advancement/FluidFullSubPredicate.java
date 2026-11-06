package slimeknights.tconstruct.library.advancement;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import slimeknights.mantle.data.codec.MantleCodecs;
import slimeknights.mantle.recipe.ingredient.IngredientHelper;

import java.util.List;

/** Checks that the given item is full of the given fluid in its fluid tank */
public record FluidFullSubPredicate(HolderSet<Fluid> fluid) implements ItemSubPredicate {
  public static final Codec<FluidFullSubPredicate> CODEC = MantleCodecs.FLUID_SET.xmap(FluidFullSubPredicate::new, FluidFullSubPredicate::fluid);

  public FluidFullSubPredicate(Fluid... fluids) {
    this(IngredientHelper.fluidSet(List.of(fluids)));
  }

  @Override
  public boolean matches(ItemStack stack) {
    IFluidHandlerItem fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
    if (fluidHandler != null && fluidHandler.getTanks() > 0) {
      FluidStack fluid = fluidHandler.getFluidInTank(0);
      return !fluid.isEmpty() && fluid.getAmount() == fluidHandler.getTankCapacity(0) && fluid.is(this.fluid);
    }
    return false;
  }
}
