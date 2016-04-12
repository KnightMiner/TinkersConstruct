package slimeknights.tconstruct.gadgets.client;

import net.minecraft.block.BlockLever.EnumOrientation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import slimeknights.tconstruct.gadgets.block.BlockRack;
import slimeknights.tconstruct.gadgets.tileentity.TileItemRack;
import slimeknights.tconstruct.library.client.RenderUtil;

public class DryingRackRenderer extends TileEntitySpecialRenderer<TileItemRack> {

	@Override
	public void renderTileEntityAt(TileItemRack te, double x, double y, double z, float partialTicks, int destroyStage) {	
		if ( te.isStackInSlot(0) || te.isStackInSlot(1) ) {
			IBlockState state = te.getWorld().getBlockState(te.getPos());
	
			if ( !( state.getBlock() instanceof BlockRack ) )
				return;

		    Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		    RenderUtil.pre(x,y,z);

		    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		    RenderHelper.enableStandardItemLighting();
		    
		    int brightness = te.getWorld().getCombinedLight(te.getPos(), 0);
		    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)(brightness % 0x10000) / 1f,
		                                            (float)(brightness / 0x10000) / 1f);
			
			EnumOrientation facing = state.getValue(BlockRack.FACING);
			// Translation is based on height and depth of the rack
		    switch( facing ) {
		    case DOWN_X: case DOWN_Z:
			    GlStateManager.translate(0.5F, -0.4375F, 0.5F);
			    break;
		    default:
			    GlStateManager.translate(0.5F, 0.3125F, 0.5F);
			    break;
		    }
		    // Rotation based on facing
		    switch( facing ) {
			    case SOUTH:
				    GlStateManager.translate(0F, 0F, 0.375F);
					GlStateManager.rotate(180F, 0F, 1F, 0F);
				    break;
				case NORTH:
				    GlStateManager.translate(0F, 0F, -0.375F);
					break;
				case WEST:
				    GlStateManager.translate(-0.375F, 0F, 0F);
				    // keep going
			    case UP_X: case DOWN_X:
			    	GlStateManager.rotate(90F, 0F, 1F, 0F);
				    break;
			    case EAST: 
				    GlStateManager.translate(0.375F, 0F, 0F);
					GlStateManager.rotate(270F, 0F, 1F, 0F);
					break;
				default: break;
		    }
		    
		    // drying racks have two slots
		    for(int i = 0; i < te.getSizeInventory(); i++)
		    {
		    	if ( !te.isStackInSlot(i) )
		    		continue;
		    	
		    	ItemStack stack = te.getStackInSlot(i);
		    	
			    // move the model slightly if it is a block
			    if (stack.getItem() instanceof ItemBlock)
			    	GlStateManager.translate(0F, 0.1875F, 0F);
			      
			    IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, te.getWorld(), null);
			    model = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.FIXED, false);
			    Minecraft.getMinecraft().getRenderItem().renderItem(stack, model);
		    }
			    
		    RenderUtil.post();    
		}
	}
}
