package slimeknights.tconstruct.library.tools.definition.module.build;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import slimeknights.tconstruct.library.modifiers.hook.build.RawDataModifierHook;
import slimeknights.tconstruct.library.tools.stack.ToolContext;
import slimeknights.tconstruct.library.tools.stack.ToolStack;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;
import slimeknights.tconstruct.library.tools.stack.VolatileBuilder;

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
   *   <li>Persistent mod data (accessed via {@link ToolStackView#set(DataComponentType, Object)}): Can be written to freely, but will not automatically remove if the modifier is removed.</li>
   *   <li>{@link RawDataModifierHook}: Runs on tool rebuild to modify non-volatile components.</li>
   * </ul>
   * @param context      Context about the tool being built. Will not yet contain
   * @param builder      Volatile data builder. See {@link slimeknights.tconstruct.library.tools.stack.BuilderKeys} for builders.
   * @param volatileData Component holder that only accepts components from {@link ToolStack#isVolatile(DataComponentType)}.
   */
  void addVolatileData(ToolContext context, VolatileBuilder builder, MutableDataComponentHolder volatileData);

  /** Merger that runs all hooks */
  record AllMerger(Collection<VolatileDataToolHook> modules) implements VolatileDataToolHook {
    @Override
    public void addVolatileData(ToolContext context, VolatileBuilder builder, MutableDataComponentHolder volatileData) {
      for (VolatileDataToolHook module : modules) {
        module.addVolatileData(context, builder, volatileData);
      }
    }
  }
}
