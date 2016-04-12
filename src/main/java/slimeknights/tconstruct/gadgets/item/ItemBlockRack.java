package slimeknights.tconstruct.gadgets.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import slimeknights.tconstruct.library.Util;

public class ItemBlockRack extends ItemMultiTexture {

	public ItemBlockRack(Block block) {
		super(block, block, new String[] {"item", "drying"});
	}    

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
    	if ( stack.getMetadata() == 0 )
    		tooltip.add(Util.translate("tile.tconstruct.rack.item.tooltip"));
    	else if ( stack.getMetadata() == 1 )
        	tooltip.add(Util.translate("tile.tconstruct.rack.drying.tooltip"));
    }
}
