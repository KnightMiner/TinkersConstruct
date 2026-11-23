package slimeknights.tconstruct.library.modifiers.hook.build;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.stack.ToolContext;
import slimeknights.tconstruct.library.tools.stack.ToolStack;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;

import java.util.Collection;

/**
 * Hook to add NBT to a tool that is reset every time modifiers or stats need a refresh. Ideal for storing data to communicate between modifiers or cache stat calculations.
 */
public interface VolatileDataModifierHook {
  /**
   * Adds any relevant volatile data to the tool data. This data is rebuilt every time modifiers rebuild.
   * <br>
   * Alternatives:
   * <ul>
   *   <li>Persistent mod data (accessed via {@link ToolStackView}): Can be written to freely, but will not automatically remove if the modifier is removed.</li>
   *   <li>{@link VolatileFlagModifierHook}: Allows adding flags to {@link slimeknights.tconstruct.library.TinkerData#VOLATILE_FLAGS}</li>
   *   <li>{@link RawDataModifierHook}: Allows modifying a restricted view of the tools main data, might help with other mod compat, but not modifier compat</li>
   * </ul>
   * @param context         Context about the tool being built. Partial view of {@link ToolStackView} as the tool is not fully built
   * @param modifier        Modifier level
   * @param volatileData    Component holder that only accepts components from {@link ToolStack#isVolatile(DataComponentType)}.
   */
  void addVolatileData(ToolContext context, ModifierEntry modifier, MutableDataComponentHolder volatileData);

  /** Merger that runs all hooks */
  record AllMerger(Collection<VolatileDataModifierHook> modules) implements VolatileDataModifierHook {
    @Override
    public void addVolatileData(ToolContext context, ModifierEntry modifier, MutableDataComponentHolder volatileData) {
      for (VolatileDataModifierHook module : modules) {
        module.addVolatileData(context, modifier, volatileData);
      }
    }
  }
}
