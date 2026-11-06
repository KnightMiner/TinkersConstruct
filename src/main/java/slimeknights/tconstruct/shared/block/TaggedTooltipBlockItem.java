package slimeknights.tconstruct.shared.block;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

/** Block item that adds a tooltip if the block is in the given block tag. */
public class TaggedTooltipBlockItem extends BlockItem {
  private final TagKey<Block> tag;
  private final Component component;
  public TaggedTooltipBlockItem(Block block, Properties properties, TagKey<Block> tag, Component component) {
    super(block, properties);
    this.component = component;
    this.tag = tag;
  }

  /** Sets the tag to the block being a beacon base */
  public static TaggedTooltipBlockItem beaconBase(Block block, Properties properties, Component component) {
    return new TaggedTooltipBlockItem(block, properties, BlockTags.BEACON_BASE_BLOCKS, component);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
    super.appendHoverText(stack, context, tooltip, tooltipFlag);
    if (getBlock().builtInRegistryHolder().is(tag)) {
      tooltip.add(component);
    }
  }
}
