package slimeknights.tconstruct.gadgets.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.TinkerRegistry;

public class TileDryingRack extends TileItemRack implements ITickable {
    int currentTime;
    int maxTime;
    
	public TileDryingRack() {
		super("dryingrack");
	}
	
	@Override
	public void update() {
      if (!worldObj.isRemote && maxTime > 0 && currentTime < maxTime) {
      	currentTime++;
      	if (currentTime >= maxTime) {
      	  setInventorySlotContents(0, TinkerRegistry.getDryingResult(getStackInSlot(0)));
          updateDryingTime();
        }
      }
	}
	
    @Override
    public void setInventorySlotContents (int slot, ItemStack itemstack)
    {
        super.setInventorySlotContents(slot, itemstack);
        updateDryingTime();
    }
    
    @Override
    public ItemStack decrStackSize (int slot, int quantity)
    {
        ItemStack stack = super.decrStackSize(slot, quantity);
        maxTime = 0;
        currentTime = 0;
        return stack;
    }
    
    public void updateDryingTime ()
    {
        currentTime = 0;
        ItemStack stack = getStackInSlot(0);
        if (stack != null)
            maxTime = TinkerRegistry.getDryingTime(stack);
        else
            maxTime = 0;
        //worldObj.scheduleUpdate(pos, blockType, 0);
    }
    
    @Override
    public void readFromNBT (NBTTagCompound tags)
    {
        currentTime = tags.getInteger("Time");
        maxTime = tags.getInteger("MaxTime");
        readCustomNBT(tags);
    }

    @Override
    public void writeToNBT (NBTTagCompound tags)
    {
        tags.setInteger("Time", currentTime);
        tags.setInteger("MaxTime", maxTime);
        writeCustomNBT(tags);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox ()
    {
        AxisAlignedBB cbb = new AxisAlignedBB(pos.getX(), pos.getY() - 1, pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        return cbb;
    }
}
