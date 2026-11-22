package slimeknights.tconstruct.library.tools.capability.inventory;

import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.capability.CompoundIndexHookIterator;
import slimeknights.tconstruct.library.tools.capability.fluid.ToolFluidCapability;
import slimeknights.tconstruct.library.tools.capability.inventory.ToolInventoryCapability.InventoryModifierHook;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;

/**
 * Shared logic to iterate fluid capabilities for {@link ToolFluidCapability}
 */
abstract class InventoryModifierHookIterator<I> extends CompoundIndexHookIterator<InventoryModifierHook,I> {
  /** Entry from {@link #findHook(ToolStackView, int)}, will be set during or before iteration */
  protected ModifierEntry indexEntry = null;

  @Override
  protected int getSize(ToolStackView tool, InventoryModifierHook hook) {
    return hook.getSlots(tool, indexEntry);
  }
}
