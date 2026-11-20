package slimeknights.tconstruct.plugin.jei.util;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.materials.MaterialVariant;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;

/**
 * Subtype interpreter for material items, considers variants in ingredient context but ignores them in recipe context.
 */
public enum ToolPartSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
  INSTANCE;

  @Override
  public MaterialVariant getSubtypeData(ItemStack stack, UidContext context) {
    MaterialVariant material = IMaterialItem.getMaterialFromStack(stack);
    if (context == UidContext.Ingredient) {
      return material.holder();
    }
    return material;
  }

  @Override
  public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
    return getSubtypeData(ingredient, context).toString();
  }
}
