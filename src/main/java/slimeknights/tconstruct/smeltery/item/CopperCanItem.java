package slimeknights.tconstruct.smeltery.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.fluid.item.FluidComponents;
import slimeknights.tconstruct.library.recipe.FluidValues;

import java.util.List;

/** Fluid container holding 1 ingot of fluid */
public class CopperCanItem extends Item {
  public CopperCanItem(Properties properties) {
    super(properties);
  }

  /** Gets the contents for the given stack */
  private FluidComponents getContents(ItemStack stack) {
    return stack.getOrDefault(TinkerData.FLUID, FluidComponents.EMPTY);
  }

  @Override
  public boolean hasCraftingRemainingItem(ItemStack stack) {
    return !getContents(stack).isEmpty();
  }

  @Override
  public ItemStack getCraftingRemainingItem(ItemStack stack) {
    if (hasCraftingRemainingItem(stack)) {
      return new ItemStack(this);
    }
    return ItemStack.EMPTY;
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    FluidComponents fluid = stack.getOrDefault(TinkerData.FLUID, FluidComponents.EMPTY);
    if (!fluid.isEmpty()) {
      MutableComponent text;
      if (fluid.hasComponents()) {
        text = fluid.toStack(FluidValues.INGOT).getHoverName().plainCopy();
      } else {
        text = Component.translatable(fluid.fluid().getFluidType().getDescriptionId());
      }
      tooltip.add(Component.translatable(this.getDescriptionId() + ".contents", text).withStyle(ChatFormatting.GRAY));
      if (flag.isAdvanced()) {
        tooltip.add(Component.translatable(TankItem.FLUID_ID, Loadables.FLUID.getKey(fluid.fluid())).withStyle(ChatFormatting.DARK_GRAY));
      }
    } else {
      tooltip.add(Component.translatable(this.getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
    }
  }
}
