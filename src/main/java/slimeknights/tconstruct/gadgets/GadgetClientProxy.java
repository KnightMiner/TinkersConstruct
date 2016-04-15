package slimeknights.tconstruct.gadgets;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import slimeknights.tconstruct.common.ClientProxy;
import slimeknights.tconstruct.gadgets.client.RenderFancyItemFrame;
import slimeknights.tconstruct.gadgets.entity.EntityFancyItemFrame;
import slimeknights.tconstruct.library.Util;

public class GadgetClientProxy extends ClientProxy {

  @Override
  protected void registerModels() {
    super.registerModels();

    // Blocks
    registerItemModel(Item.getItemFromBlock(TinkerGadgets.stoneTorch));
    registerItemModel(Item.getItemFromBlock(TinkerGadgets.stoneLadder));
    registerItemModel(Item.getItemFromBlock(TinkerGadgets.woodRail));
    registerItemModel(Item.getItemFromBlock(TinkerGadgets.punji));
    registerItemBlockMeta(TinkerGadgets.driedClay);
    
    registerItemModel(new ItemStack(TinkerGadgets.rack, 1, 0), "item_rack");
    registerItemModel(new ItemStack(TinkerGadgets.rack, 1, 1), "drying_rack");

    // Items
    registerItemModel(TinkerGadgets.slimeSling);
    registerItemModel(TinkerGadgets.slimeBoots);
    registerItemModel(TinkerGadgets.stoneStick);
    
    // Entity
    RenderingRegistry.registerEntityRenderingHandler(EntityFancyItemFrame.class, RenderFancyItemFrame.FACTORY);
    
    for(EntityFancyItemFrame.FrameType type : EntityFancyItemFrame.FrameType.values()) {
      ModelResourceLocation loc = Util.getModelResource("fancy_frame", type.toString());
      ModelLoader.registerItemVariants(TinkerGadgets.fancyFrame, loc);
      ModelLoader.setCustomModelResourceLocation(TinkerGadgets.fancyFrame, type.ordinal(), loc);
    }
  }

  @Override
  public void postInit() {
    super.postInit();

    MinecraftForge.EVENT_BUS.register(new GadgetClientEvents());
  }
}
