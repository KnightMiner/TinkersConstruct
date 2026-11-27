package slimeknights.tconstruct.tools.client;

import net.minecraft.client.resources.model.Material;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfo.TintedSprite;
import slimeknights.tconstruct.library.client.modifiers.model.ModifierModel;
import slimeknights.tconstruct.library.client.modifiers.model.SimpleModifierModel;
import slimeknights.tconstruct.library.client.modifiers.model.material.MaterialModifierModel;
import slimeknights.tconstruct.library.materials.MaterialVariantId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.component.ResourceMap;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;
import slimeknights.tconstruct.tools.TinkerModifiers;

import javax.annotation.Nullable;

/**
 * Modifier model that tints the skull material. Will tint it using the dye color, or the material color if not dyed.
 */
public record SlimeskullModifierModel(Material small, int skullIndex, int slimeIndex) implements MaterialModifierModel {
  public static final RecordLoadable<SlimeskullModifierModel> LOADER = RecordLoadable.create(
    ModifierModel.MATERIAL_LOADABLE.requiredField("texture", SimpleModifierModel::small),
    IntLoadable.FROM_ZERO.requiredField("skull_index", SlimeskullModifierModel::skullIndex),
    IntLoadable.FROM_ZERO.requiredField("slime_index", SlimeskullModifierModel::slimeIndex),
    SlimeskullModifierModel::new);

  @Override
  public RecordLoadable<SlimeskullModifierModel> getLoader() {
    return LOADER;
  }

  @Nullable
  @Override
  public Material large() {
    return null;
  }

  @Override
  public MaterialVariantId getMaterial(ToolStackView tool, ModifierEntry entry) {
    return tool.getMaterial(skullIndex).id();
  }

  @Override
  public int getColor(ToolStackView tool, ModifierEntry entry, TintedSprite sprite) {
    Integer dyed = ResourceMap.get(tool, TinkerData.MODIFIER_INTEGER).get(TinkerModifiers.dyed.id());
    if (dyed != null) {
      return 0xFF000000 | dyed;
    } else {
      return SlimeskullArmorModel.MATERIAL_COLOR_CACHE.apply(tool.getMaterial(slimeIndex).id());
    }
  }

  @Nullable
  @Override
  public Object getCacheKey(ToolStackView tool, ModifierEntry modifier) {
    // only need to cache colors from dyed, if its from the slime we will get it from the material in cache
    return ResourceMap.get(tool, TinkerData.MODIFIER_INTEGER).get(TinkerModifiers.dyed.id());
  }
}
