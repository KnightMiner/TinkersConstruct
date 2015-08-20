package tconstruct.tools.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import java.util.List;

import tconstruct.library.TinkerRegistry;
import tconstruct.library.materials.Material;
import tconstruct.library.tinkering.MaterialItem;

public class Shard extends MaterialItem {
  @Override
  public void getSubItems(Item itemIn, CreativeTabs tab, List subItems) {
    // this adds a variant of each material to the creative menu
    for(Material mat : TinkerRegistry.getAllMaterials()) {
      if(mat.craftable || mat.castable)
        subItems.add(getItemstackWithMaterial(mat));
    }
  }
}