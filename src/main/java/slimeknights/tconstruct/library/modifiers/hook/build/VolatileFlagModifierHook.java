package slimeknights.tconstruct.library.modifiers.hook.build;

import com.google.common.collect.ImmutableSet;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.stack.ToolContext;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;

import java.util.Collection;

/**
 * Hook to add flags to the tool which are reset on tool data rebuild.
 * @see slimeknights.tconstruct.library.tools.component.VolatileFlags
 */
public interface VolatileFlagModifierHook {
  /**
   * Adds any relevant volatile flags to {@link slimeknights.tconstruct.library.TinkerData#VOLATILE_FLAGS}. This data is rebuilt every time modifiers rebuild.
   * @param context   Context about the tool being built. Partial view of {@link ToolStackView} as the tool is not fully built
   * @param modifier  Modifier level
   * @param builder   Builder for the flag map.
   */
  void addVolatileFlags(ToolContext context, ModifierEntry modifier, ImmutableSet.Builder<ResourceLocation> builder);

  /** Merger that runs all hooks */
  record AllMerger(Collection<VolatileFlagModifierHook> modules) implements VolatileFlagModifierHook {
    @Override
    public void addVolatileFlags(ToolContext context, ModifierEntry modifier, ImmutableSet.Builder<ResourceLocation> builder) {
      for (VolatileFlagModifierHook module : modules) {
        module.addVolatileFlags(context, modifier, builder);
      }
    }
  }
}
