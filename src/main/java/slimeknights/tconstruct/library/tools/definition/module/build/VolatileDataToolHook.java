package slimeknights.tconstruct.library.tools.definition.module.build;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import slimeknights.tconstruct.library.modifiers.hook.build.RawDataModifierHook;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.Collection;

/**
 * Hook to add NBT to a tool that is reset every time modifiers or stats need a refresh. Ideal for storing data to communicate between modifiers or cache stat calculations.
 */
public interface VolatileDataToolHook {
  /**
   * Adds any relevant volatile data to the tool data. This data is rebuilt every time modifiers rebuild.
   * <br>
   * Alternatives:
   * <ul>
   *   <li>Persistent mod data (accessed via {@link IToolStackView#set(DataComponentType, Object)}): Can be written to freely, but will not automatically remove if the modifier is removed.</li>
   *   <li>{@link RawDataModifierHook}: Runs on tool rebuild to modify non-volatile components.</li>
   * </ul>
   * @param context         Context about the tool being built. Will not yet contain
   * @param volatileData    Component holder that only accepts components from {@link slimeknights.tconstruct.library.tools.nbt.ToolStack#isVolatile(DataComponentType)}.
   */
  void addVolatileData(IToolContext context, MutableDataComponentHolder volatileData);

  /** Merger that runs all hooks */
  record AllMerger(Collection<VolatileDataToolHook> modules) implements VolatileDataToolHook {
    @Override
    public void addVolatileData(IToolContext context, MutableDataComponentHolder volatileData) {
      for (VolatileDataToolHook module : modules) {
        module.addVolatileData(context, volatileData);
      }
    }
  }
}
