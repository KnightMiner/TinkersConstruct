package slimeknights.tconstruct.plugin.jei;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.awt.*;
import java.util.List;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.BlankRecipeWrapper;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.smeltery.MeltingRecipe;

public class SmelteryRecipeWrapper extends BlankRecipeWrapper {

  protected final List<ItemStack> inputs;
  protected final List<FluidStack> outputs;
  protected final int temperature;
  protected final List<FluidStack> fuels;

  public SmelteryRecipeWrapper(MeltingRecipe recipe) {
    this.inputs = recipe.input.getInputs();
    this.outputs = ImmutableList.of(recipe.getResult());
    this.temperature = recipe.getTemperature();

    ImmutableList.Builder<FluidStack> builder = ImmutableList.builder();
    for(FluidStack fs : TinkerRegistry.getSmelteryFuels()) {
      if(fs.getFluid().getTemperature(fs) >= temperature) {
        fs = fs.copy();
        fs.amount = 1000;
        builder.add(fs);
      }
    }
    fuels = builder.build();
  }

  @Override
  public List getInputs() {
    return inputs;
  }

  @Override
  public List<FluidStack> getFluidOutputs() {
    return outputs;
  }

  @Override
  public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight) {
    String tmpStr = String.valueOf(temperature) + "°";
    int x = 80 - minecraft.fontRendererObj.getStringWidth(tmpStr)/2;
    minecraft.fontRendererObj.drawString(tmpStr, x, 10, Color.gray.getRGB());
  }

  @Override
  public void drawAnimations(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight) {

  }
}
