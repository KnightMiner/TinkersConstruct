package slimeknights.tconstruct.library.tools.definition.module.build;

import slimeknights.tconstruct.library.tools.component.ModifierEntries;
import slimeknights.tconstruct.library.tools.component.ToolMaterials;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.helper.ModifierBuilder;

import java.util.Collection;

/** Hook for tools exposing tool traits */
public interface ToolTraitHook {
  /** Adds all traits to the given builder */
  void addTraits(ToolDefinition definition, ToolMaterials materials, ModifierBuilder builder);

  /** Gets the traits from the given definition */
  static ModifierEntries getTraits(ToolDefinition definition, ToolMaterials materials) {
    ModifierEntries.Builder builder = ModifierEntries.builder();
    definition.getHook(ToolHooks.TOOL_TRAITS).addTraits(definition, materials, builder);
    return builder.build();
  }

  /** Gets the traits for the given tool */
  record AllMerger(Collection<ToolTraitHook> hooks) implements ToolTraitHook {
    @Override
    public void addTraits(ToolDefinition definition, ToolMaterials materials, ModifierBuilder builder) {
      for (ToolTraitHook hook : hooks) {
        hook.addTraits(definition, materials, builder);
      }
    }
  }
}
