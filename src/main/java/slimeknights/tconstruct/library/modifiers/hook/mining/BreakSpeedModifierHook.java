package slimeknights.tconstruct.library.modifiers.hook.mining;

import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.Collection;

/**
 * Hook used to increase mining speed of a block conditioned on the environment or the player.
 * <br>
 * Alternatives:
 * <ul>
 *   <li>{@link slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook}: Limited context, but effect shows in the tooltip automatically.</li>
 *   <li>{@link TooltipModifierHook}: Allows adding bonus effects to the tooltip.</li>
 * </ul>
 */
public interface BreakSpeedModifierHook {
  /**
   * Called when break speed is being calculated to affect mining speed conditionally.
   * @param tool                 Current tool instance
   * @param modifier             Modifier level
   * @param context   Context about the block being broken
   * @param speed     Speed as modified by other modifiers.
   * @return new speed as modified by this hook.
   */
  float modifyBreakSpeed(IToolStackView tool, ModifierEntry modifier, BreakSpeedContext context, float speed);


  /** Merger that runs each hook in succession */
  record AllMerger(Collection<BreakSpeedModifierHook> modules) implements BreakSpeedModifierHook {
    @Override
    public float modifyBreakSpeed(IToolStackView tool, ModifierEntry modifier, BreakSpeedContext context, float speed) {
      for (BreakSpeedModifierHook module : modules) {
        speed = module.modifyBreakSpeed(tool, modifier, context, speed);
      }
      return speed;
    }
  }
}
