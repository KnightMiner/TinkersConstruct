package slimeknights.tconstruct.plugin.jei;

import java.awt.Color;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import slimeknights.tconstruct.library.DryingRecipe;

public class DryingRecipeWrapper extends BlankRecipeWrapper {
	
	  protected final List<ItemStack> input;
	  protected final List<ItemStack> output;
	  protected final int time;

	  public DryingRecipeWrapper(DryingRecipe recipe) {
	    this.input = recipe.input.getInputs();
	    this.output = ImmutableList.of(recipe.getResult());
	    this.time = recipe.getTime();
	  }
	  
	  @Override
	  public List<ItemStack> getInputs() {
	    return input;
	  }

	  @Override
	  public List<ItemStack> getOutputs() {
	    return output;
	  }

	  @Override
	  public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		String minStr = "", secStr = "";
		
		int minutes = time / 20 / 60;
		if ( minutes > 0 )
			minStr = String.valueOf(minutes) + "m";
		
		int seconds = ( time / 20 ) % 60;
		if ( seconds > 0 )
		{
			// if using both minutes and seconds, add a space between
			if ( minutes > 0 )
				secStr += " ";
			secStr += String.valueOf(seconds) + "s";
		}
		
	    String timeStr = minStr + secStr;
	    int x = 80 - minecraft.fontRendererObj.getStringWidth(timeStr) / 2;
	    minecraft.fontRendererObj.drawString(timeStr, x, 5, Color.gray.getRGB());
	  }
}
