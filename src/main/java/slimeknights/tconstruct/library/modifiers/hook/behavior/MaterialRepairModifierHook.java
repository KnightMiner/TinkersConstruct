package slimeknights.tconstruct.library.modifiers.hook.behavior;

import slimeknights.tconstruct.library.materials.MaterialId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;

import java.util.Collection;
import java.util.Set;

/**
 * Hook allowing a modifier to add an additional repair material to a tool.
 * @see slimeknights.tconstruct.library.tools.definition.module.material.MaterialRepairToolHook
 */
public interface MaterialRepairModifierHook {
  /**
   * Checks if the given material can be used to repair this tool
   * @param tool      Tool to check
   * @param material  Material to check
   * @return  True if it can be used to repair this tool
   */
  boolean isRepairMaterial(ToolStackView tool, ModifierEntry modifier, MaterialId material);

  /**
   * Gets the amount of durability restored by this material for repair.
   * Important: make sure to filter by your material else you will override how much is repaired by other materials.
   * @param tool      Tool instance
   * @param material  Material used for repair
   * @return  Repair amount
   */
  float getRepairAmount(ToolStackView tool, ModifierEntry modifier, MaterialId material);

  /**
   * Adds all repair materials from the given tool to the material list for display in recipe viewers.
   * @param tool       Tool instance
   * @param materials  List of materials being built;
   */
  default void addRepairMaterials(ToolStackView tool, ModifierEntry modifier, Set<MaterialId> materials) {}

  /** Merger that gets the max repair amount across all materials */
  record MaxMerger(Collection<MaterialRepairModifierHook> modules) implements MaterialRepairModifierHook {
    @Override
    public boolean isRepairMaterial(ToolStackView tool, ModifierEntry modifier, MaterialId material) {
      for (MaterialRepairModifierHook module : modules) {
        if (module.isRepairMaterial(tool, modifier, material)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public float getRepairAmount(ToolStackView tool, ModifierEntry modifier, MaterialId material) {
      float maxRepair = 0;
      for (MaterialRepairModifierHook module : modules) {
        float repair = module.getRepairAmount(tool, modifier, material);
        if (repair > maxRepair) {
          maxRepair = repair;
        }
      }
      return maxRepair;
    }

    @Override
    public void addRepairMaterials(ToolStackView tool, ModifierEntry modifier, Set<MaterialId> materials) {
      for (MaterialRepairModifierHook module : modules) {
        module.addRepairMaterials(tool, modifier, materials);
      }
    }
  }
}
