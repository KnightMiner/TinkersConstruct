package slimeknights.tconstruct.library.modifiers.hook.build;

import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.SlotsNBT;

import java.util.Collection;

/**
 * Hook to add modifier slots to a tool as granted by modifiers.
 */
public interface AddSlotsModifierHook {
  /**
   * Adds modifier slots to the tool. This data is rebuilt every time modifiers rebuild.
   * @param context    Context about the tool being built. Partial view of {@link IToolStackView} as the tool is not fully built
   * @param modifier   level
   * @param slots      Slot builder
   */
  void addSlots(IToolContext context, ModifierEntry modifier, SlotsNBT.Builder slots);

  /** Merger that runs all hooks */
  record AllMerger(Collection<AddSlotsModifierHook> modules) implements AddSlotsModifierHook {
    @Override
    public void addSlots(IToolContext context, ModifierEntry modifier, SlotsNBT.Builder slots) {
      for (AddSlotsModifierHook module : modules) {
        module.addSlots(context, modifier, slots);
      }
    }
  }
}
