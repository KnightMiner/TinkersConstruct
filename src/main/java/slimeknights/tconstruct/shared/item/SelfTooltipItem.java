package slimeknights.tconstruct.shared.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import slimeknights.mantle.item.tooltip.FixedTooltipItem;

import java.util.List;

/** Item using {@link ItemStack#getDescriptionId()} for the tooltip key. */
public class SelfTooltipItem extends Item {
  public SelfTooltipItem(Properties properties) {
    super(properties);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(FixedTooltipItem.makeComponent(stack.getDescriptionId() + ".tooltip"));
  }
}
