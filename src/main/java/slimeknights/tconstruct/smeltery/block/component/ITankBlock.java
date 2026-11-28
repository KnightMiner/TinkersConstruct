package slimeknights.tconstruct.smeltery.block.component;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidType;

/** Interface for tank blocks to return their capacity */
public interface ITankBlock {
  /** Default max capacity for the tank */
  int DEFAULT_CAPACITY = FluidType.BUCKET_VOLUME * 4;

  /** Gets the capacity for this tank */
  int getCapacity();


  /* Helpers */

  /**
   * Gets the capacity for the given block
   * @param block  block
   * @return  Capacity
   */
  static int getCapacity(Block block) {
    if (block instanceof ITankBlock tankBlock) {
      return tankBlock.getCapacity();
    }
    return DEFAULT_CAPACITY;
  }

  /**
   * Gets the capacity for the given item
   * @param item  item
   * @return  Capacity
   */
  static int getCapacity(Item item) {
    if (item instanceof BlockItem block) {
      return getCapacity(block.getBlock());
    }
    return DEFAULT_CAPACITY;
  }
}
