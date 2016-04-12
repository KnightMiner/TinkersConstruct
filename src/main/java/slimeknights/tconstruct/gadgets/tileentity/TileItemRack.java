package slimeknights.tconstruct.gadgets.tileentity;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.world.WorldServer;
import slimeknights.mantle.tileentity.TileInventory;
import slimeknights.tconstruct.common.PlayerHelper;
import slimeknights.tconstruct.common.TinkerNetwork;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.tools.network.InventorySlotSyncPacket;

public class TileItemRack extends TileInventory {

	// for the sake of the drying rack
	protected TileItemRack(String name, int slots) {
		super(name, slots, 1);
	}
	
	public TileItemRack() {
		this("itemrack", 1);
	}
    
    public void readCustomNBT (NBTTagCompound tags)
    {
        super.readFromNBT(tags);
    }

    public void writeCustomNBT (NBTTagCompound tags)
    {
        super.writeToNBT(tags);
    }
    
    @Override
    public void setInventorySlotContents(int slot, ItemStack itemstack) {
      // we sync slot changes to all clients around
      if(this.worldObj != null  && this.worldObj instanceof WorldServer && !this.worldObj.isRemote && !ItemStack.areItemStacksEqual(itemstack, getStackInSlot(slot))) {
        TinkerNetwork.sendToClients((WorldServer) this.worldObj, this.pos, new InventorySlotSyncPacket(itemstack, slot, pos));
      }
      super.setInventorySlotContents(slot, itemstack);

      if(getWorld() != null && getWorld().isRemote && Config.renderTableItems) {
        Minecraft.getMinecraft().renderGlobal.notifyBlockUpdate(null, pos, null, null, 0);
      }
    }
    
    // note that if there is no slot 1, it will always return false for having an item
    public void interact(EntityPlayer player) {
      // completely empty -> insert current item into input
      if(!isStackInSlot(0) && !isStackInSlot(1)) {
        ItemStack stack = player.inventory.decrStackSize(player.inventory.currentItem, stackSizeLimit);
        setInventorySlotContents(0, stack);
      }
      // take item out
      else {
        // take out of stack 1 if something is in there, 0 otherwise
        int slot = isStackInSlot(1) ? 1 : 0;

        PlayerHelper.spawnItemAtPlayer(player, getStackInSlot(slot));
        setInventorySlotContents(slot, null);
      }
    }
    
    /* Packets */
	@Override
    public SPacketUpdateTileEntity getDescriptionPacket ()
    {
        NBTTagCompound tag = new NBTTagCompound();
        writeCustomNBT(tag);
        return new SPacketUpdateTileEntity(pos, 1, tag);
    }

    @Override
    public void onDataPacket (NetworkManager net, SPacketUpdateTileEntity packet)
    {
        readCustomNBT(packet.getNbtCompound());
        //worldObj.scheduleUpdate(pos, blockType, 0);
    }
}
