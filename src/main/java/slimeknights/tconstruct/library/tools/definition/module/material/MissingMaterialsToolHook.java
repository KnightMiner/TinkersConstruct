package slimeknights.tconstruct.library.tools.definition.module.material;

import net.minecraft.util.RandomSource;
import slimeknights.tconstruct.library.materials.MaterialVariant;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.component.ToolMaterials;

import java.util.ArrayList;
import java.util.List;

/** Hook for filling a tool with materials */
public interface MissingMaterialsToolHook {
  /** Fills the tool with materials */
  ToolMaterials fillMaterials(ToolDefinition definition, RandomSource random);

  /**
   * Fills the given existing materials with any missing materials.
   * Will only be called if {@link #needsMaterials(ToolDefinition, ToolMaterials)} returns true.
   */
  default ToolMaterials fillMaterials(ToolDefinition definition, ToolMaterials existing, RandomSource random) {
    ToolMaterials newMaterials = fillMaterials(definition, random);
    int oldSize = existing.size();
    // no existing materials, return the full new ones
    if (oldSize == 0) {
      return newMaterials;
    }
    // new materials is larger, copy all the ones from the smaller to fill in the gaps
    int newSize = newMaterials.size();
    List<MaterialVariant> materials = new ArrayList<>(newSize);
    for (int i = 0; i < oldSize; i++) {
      materials.add(existing.get(i));
    }
    for (int i = oldSize; i < newSize; i++) {
      materials.add(newMaterials.get(i));
    }
    return ToolMaterials.of(materials);
  }

  /** Checks if we are missing materials */
  default boolean needsMaterials(ToolDefinition definition, ToolMaterials existing) {
    return existing.size() < ToolMaterialHook.stats(definition).size();
  }
}
