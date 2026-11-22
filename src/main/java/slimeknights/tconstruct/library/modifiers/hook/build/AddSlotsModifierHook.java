package slimeknights.tconstruct.library.modifiers.hook.build;

import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.component.ModifierSlots;
import slimeknights.tconstruct.library.tools.stack.ToolContext;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;

import java.util.Collection;

/**
 * Hook to add modifier slots to a tool as granted by modifiers.
 */
public interface AddSlotsModifierHook {
  /**
   * Adds modifier slots to the tool. This data is rebuilt every time modifiers rebuild.
   * @param context    Context about the tool being built. Partial view of {@link ToolStackView} as the tool is not fully built
   * @param modifier   level
   * @param slots      Slot builder
   */
  void addSlots(ToolContext context, ModifierEntry modifier, ModifierSlots.Builder slots);

  /** Merger that runs all hooks */
  record AllMerger(Collection<AddSlotsModifierHook> modules) implements AddSlotsModifierHook {
    @Override
    public void addSlots(ToolContext context, ModifierEntry modifier, ModifierSlots.Builder slots) {
      for (AddSlotsModifierHook module : modules) {
        module.addSlots(context, modifier, slots);
      }
    }
  }
}
