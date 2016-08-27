package slimeknights.tconstruct.smeltery.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.items.IItemHandler;
import slimeknights.tconstruct.smeltery.tileentity.TileSearedFurnace;
import slimeknights.tconstruct.tools.inventory.ContainerSideInventory;

public class ContainerSearedFurnaceSideInventory extends ContainerSideInventory<TileSearedFurnace> {

  private EntityPlayer player;

  public ContainerSearedFurnaceSideInventory(EntityPlayer player, TileSearedFurnace tile, int x, int y, int columns) {
    super(tile, null, x, y, columns);

    this.player = player;
  }

  @Override
  protected Slot createSlot(IItemHandler itemHandler, int index, int x, int y) {
    return new SlotSearedFurnace(player, tile, itemHandler, index, x, y);
  }
}
