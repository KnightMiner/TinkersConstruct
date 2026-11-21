package slimeknights.tconstruct.library.tools.definition.module.build;

import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.SlotsNBT;

import java.util.Collection;

/**
 * Hook to add modifier slots to a tool as granted by the tool.
 */
public interface AddSlotsToolHook {
  /**
   * Adds modifier slots to the tool. This data is rebuilt every time modifiers rebuild.
   * @param context    Context about the tool being built. Will not yet contain
   * @param slots      Slot builder
   */
  void addSlots(IToolContext context, SlotsNBT.Builder slots);

  /** Merger that runs all hooks */
  record AllMerger(Collection<AddSlotsToolHook> modules) implements AddSlotsToolHook {
    @Override
    public void addSlots(IToolContext context, SlotsNBT.Builder slots) {
      for (AddSlotsToolHook module : modules) {
        module.addSlots(context, slots);
      }
    }
  }
}
