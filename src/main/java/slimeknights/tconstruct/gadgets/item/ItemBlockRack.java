package slimeknights.tconstruct.gadgets.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

public class ItemBlockRack extends ItemMultiTexture {

	public ItemBlockRack(Block block) {
		super(block, block, new String[] {"item", "drying"});
	}

    /**
     * Converts the given ItemStack damage value into a metadata value to be placed in the world when this Item is
     * placed as a Block (mostly used with ItemBlocks).
     */
    public int getMetadata(int meta)
    {
        if ( meta == 1 )
        	return 8;
        else
        	return 0;
    }
    

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
    	if ( stack.getMetadata() == 0 )
    		tooltip.add(I18n.translateToLocal("tile.tconstruct.rack.item.tooltip"));
    	else if ( stack.getMetadata() == 1 )
        	tooltip.add(I18n.translateToLocal("tile.tconstruct.rack.drying.tooltip"));
    }
}
