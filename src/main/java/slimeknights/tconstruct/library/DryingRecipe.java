package slimeknights.tconstruct.library;

import net.minecraft.item.ItemStack;

public class DryingRecipe {
    public final int time;
    public final ItemStack input;
    public final ItemStack result;

    DryingRecipe(ItemStack input, int time, ItemStack result)
    {
        this.time = time;
        this.input = input;
        this.result = result;
    }

    public boolean matches (ItemStack input)
    {
        // makes all drying rack recipes compatible with stuff killed by a frying pan
    	// TODO: is this tag still used for the achievement?
        if(input.hasTagCompound()) {
            input = input.copy();
            input.getTagCompound().removeTag("frypanKill");
            if(input.getTagCompound().hasNoTags())
                input.setTagCompound(null);
        }
        return ItemStack.areItemStacksEqual(this.input, input);
    }

    public ItemStack getResult ()
    {
        return result.copy();
    }
}
