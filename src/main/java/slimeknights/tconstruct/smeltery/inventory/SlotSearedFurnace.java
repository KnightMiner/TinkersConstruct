package slimeknights.tconstruct.smeltery.inventory;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import slimeknights.tconstruct.smeltery.tileentity.TileSearedFurnace;

public class SlotSearedFurnace extends SlotItemHandler {

  private TileSearedFurnace furnace;
  private EntityPlayer player;
  private int removeCount, index;

  public SlotSearedFurnace(EntityPlayer player, TileSearedFurnace furnace, IItemHandler itemHandler, int index,
      int xPosition, int yPosition) {
    super(itemHandler, index, xPosition, yPosition);
    this.index = index;
    this.furnace = furnace;
    this.player = player;
  }

  @Override
  public ItemStack decrStackSize(int amount) {
    if (this.getHasStack()) {
      this.removeCount += Math.min(amount, this.getStack().stackSize);
    }

    return super.decrStackSize(amount);
  }

  /**
   * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not
   * ore and wood.
   */
  @Override
  protected void onCrafting(ItemStack stack) {
    if (furnace.getHeatingProgress(index) != Float.POSITIVE_INFINITY) {
      return;
    }

    stack.onCrafting(this.player.worldObj, this.player, this.removeCount);

    if (!this.player.worldObj.isRemote) {
      int count = this.removeCount;
      float xp = FurnaceRecipes.instance().getSmeltingExperience(stack);

      if (xp == 0.0F) {
        count = 0;
      } else if (xp < 1.0F) {
        int i = MathHelper.floor_float((float) count * xp);

        if (i < MathHelper.ceiling_float_int((float) count * xp)
            && Math.random() < (double) ((float) count * xp - (float) i)) {
          ++i;
        }

        count = i;
      }

      while (count > 0) {
        int k = EntityXPOrb.getXPSplit(count);
        count -= k;
        this.player.worldObj.spawnEntityInWorld(new EntityXPOrb(this.player.worldObj, this.player.posX,
            this.player.posY + 0.5D, this.player.posZ + 0.5D, k));
      }
    }

    this.removeCount = 0;

    net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerSmeltedEvent(player, stack);

    if (stack.getItem() == Items.IRON_INGOT) {
      this.player.addStat(AchievementList.ACQUIRE_IRON);
    }

    if (stack.getItem() == Items.COOKED_FISH) {
      this.player.addStat(AchievementList.COOK_FISH);
    }
  }
}
