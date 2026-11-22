package slimeknights.tconstruct.plugin.jei.util;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.materials.MaterialId;
import slimeknights.tconstruct.library.materials.MaterialVariant;
import slimeknights.tconstruct.library.tools.component.ToolMaterials;

import java.util.List;

/**
 * Subtype interpreter for tools, treats the tool as unique in ingredient list, generic in recipes
 */
public enum ToolSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
  /** Materials are always considered */
  ALWAYS,
  /** Materials are only considered in the ingredients view */
  INGREDIENT,
  /** Materials are only considered in the ingredients view or the first material. */
  FIRST;

  @Nullable
  @Override
  public Object getSubtypeData(ItemStack stack, UidContext context) {
    if (context == UidContext.Ingredient || this != INGREDIENT) {
      ToolMaterials materials = ToolMaterials.get(stack);
      if (!materials.isEmpty()) {
        // if in first mode and looking at a recipe, only use the first material
        if (context == UidContext.Recipe && this == FIRST) {
          return materials.get(0);
        }
        return materials;
      }
    }
    return null;
  }

  @Override
  public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
    boolean isIngredient = context == UidContext.Ingredient;
    if (isIngredient || this != INGREDIENT) {
      List<MaterialVariant> materialList = ToolMaterials.get(stack).getList();
      if (!materialList.isEmpty()) {
        // if in first mode and looking at a recipe, only use the first material
        MaterialId first = materialList.get(0).material();
        if (!isIngredient && this == FIRST) {
          return first.toString();
        }
        // append first entry without a comma
        StringBuilder builder = new StringBuilder();
        builder.append(first);
        for (int i = 1; i < materialList.size(); i++) {
          builder.append(',');
          builder.append(materialList.get(i).material());
        }
        return builder.toString();
      }
    }
    return "";
  }
}
