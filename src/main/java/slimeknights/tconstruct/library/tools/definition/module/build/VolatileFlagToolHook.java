package slimeknights.tconstruct.library.tools.definition.module.build;

import com.google.common.collect.ImmutableSet;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.tools.stack.ToolContext;

import java.util.Collection;

/**
 * Hook to add flags to the tool which are reset on tool data rebuild.
 * @see slimeknights.tconstruct.library.tools.component.VolatileFlags
 */
public interface VolatileFlagToolHook {
  /**
   * Adds any relevant volatile flags to {@link slimeknights.tconstruct.library.TinkerData#VOLATILE_FLAGS}. This data is rebuilt every time modifiers rebuild.
   * @param context  Context about the tool being built. Will not yet contain
   * @param builder  Builder for the flag map.
   */
  void addVolatileFlags(ToolContext context, ImmutableSet.Builder<ResourceLocation> builder);

  /** Merger that runs all hooks */
  record AllMerger(Collection<VolatileFlagToolHook> modules) implements VolatileFlagToolHook {
    @Override
    public void addVolatileFlags(ToolContext context, ImmutableSet.Builder<ResourceLocation> builder) {
      for (VolatileFlagToolHook module : modules) {
        module.addVolatileFlags(context, builder);
      }
    }
  }
}
