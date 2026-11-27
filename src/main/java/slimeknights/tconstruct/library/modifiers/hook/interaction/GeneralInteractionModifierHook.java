package slimeknights.tconstruct.library.modifiers.hook.interaction;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.hook.build.ConditionalStatModifierHook;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.Collection;

/**
 * Hooks for standard interaction logic post block/entity interaction, notably including using a modifier by holding right click.
 * See {@link BlockInteractionModifierHook} for block interaction and {@link EntityInteractionModifierHook} for entities.
 * See {@link UsingToolModifierHook} for a hook that triggers when another modifier stops using.
 * TODO 1.21: split using hooks out to a {@code UsingActiveModifierHook}.
 */
public interface GeneralInteractionModifierHook {
  /**
   * Hook called after block/entity interaction passes, or when interacting with empty air.
   * @param tool       Tool performing interaction
   * @param modifier   Modifier instance
   * @param player     Interacting player
   * @param hand       Hand used for interaction
   * @param source     Source of the interaction
   * @return  Return PASS or FAIL to allow vanilla handling, any other to stop vanilla and later modifiers from running.
   */
  InteractionResult onToolUse(ToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source);


  /* Charged usage */

  /**
   * Called every tick when the player is using this modifier. Only supported for {@link InteractionSource#RIGHT_CLICK}.
   * To setup, use {@link #startUsing(ToolStackView, ModifierId, LivingEntity, InteractionHand)} in {@link #onToolUse(ToolStackView, ModifierEntry, Player, InteractionHand, InteractionSource)}.
   * @param tool       Tool performing interaction
   * @param modifier   Modifier instance
   * @param entity     Interacting entity
   * @param timeLeft   How many ticks of use duration was left.
   * @see UsingToolModifierHook#onUsingTick(ToolStackView, ModifierEntry, LivingEntity, int, int, ModifierEntry)
   */
  default void onUsingTick(ToolStackView tool, ModifierEntry modifier, LivingEntity entity, int timeLeft) {}

  /**
   * Called on the active modifier when the player stops using the modifier without finishing. See {@link #onFinishUsing(ToolStackView, ModifierEntry, LivingEntity)} for finishing interaction.
   * Only supported for {@link InteractionSource#RIGHT_CLICK}.
   * To setup, use {@link #startUsing(ToolStackView, ModifierId, LivingEntity, InteractionHand)} in {@link #onToolUse(ToolStackView, ModifierEntry, Player, InteractionHand, InteractionSource)}.
   * @param tool       Tool performing interaction
   * @param modifier   Modifier instance
   * @param entity     Interacting entity
   * @param timeLeft   How many ticks of use duration was left
   * @see UsingToolModifierHook#beforeReleaseUsing(ToolStackView, ModifierEntry, LivingEntity, int, int, ModifierEntry)
   */
  default void onStoppedUsing(ToolStackView tool, ModifierEntry modifier, LivingEntity entity, int timeLeft) {}

  /**
   * Called on the active modifier when the use duration on this modifier reaches the end. See {@link #onStoppedUsing(ToolStackView, ModifierEntry, LivingEntity, int)} for unfinished interaction.
   * To setup, use {@link LivingEntity#startUsingItem(InteractionHand)} in {@link #onToolUse(ToolStackView, ModifierEntry, Player, InteractionHand, InteractionSource)} and set the duration in {@link #getUseDuration(ToolStackView, ModifierEntry)}
   * @param tool       Tool performing interaction
   * @param modifier   Modifier instance
   * @param entity     Interacting entity
   * @see UsingToolModifierHook#beforeReleaseUsing(ToolStackView, ModifierEntry, LivingEntity, int, int, ModifierEntry)
   */
  default void onFinishUsing(ToolStackView tool, ModifierEntry modifier, LivingEntity entity) {}

  /**
   * Called on the active modifier to determines how long usage lasts on a tool.
   * @param tool       Tool performing interaction
   * @param modifier   Modifier instance
   * @return  For how many ticks the modifier should run its use action
   */
  default int getUseDuration(ToolStackView tool, ModifierEntry modifier) {
    return 0;
  }

  /**
   * Called on the active modifier to determines the animation for using this modifier.
   * @param tool       Tool performing interaction
   * @param modifier   Modifier instance
   * @return  Use action to be performed
   */
  default UseAnim getUseAction(ToolStackView tool, ModifierEntry modifier) {
    return UseAnim.NONE;
  }


  /* Helpers */

  /**
   * Use in {@link #onToolUse(ToolStackView, ModifierEntry, Player, InteractionHand, InteractionSource)} to start using an item, ensuring later hooks are properly called.
   * @param tool      Tool being used
   * @param modifier  Modifier to call for later hooks, must be on the tool
   * @param living    Entity using the tool, used for the vanilla hook
   * @param hand      Hand using the tool
   */
  static void startUsing(ToolStackView tool, ModifierId modifier, LivingEntity living, InteractionHand hand) {
    tool.set(TinkerData.ACTIVE_MODIFIER, modifier);
    living.startUsingItem(hand);
  }

  /** Gets the drawtime for the passed tool. */
  static int getDrawtime(ToolStackView tool, LivingEntity living, float speedFactor) {
    return (int)Math.ceil(20f * speedFactor / ConditionalStatModifierHook.getModifiedStat(tool, living, ToolStats.DRAW_SPEED));
  }

  /** Causes cooldown on the given tool based on its draw speed stat. */
  static void addCooldown(ToolStackView tool, Player player, float speedFactor) {
    player.getCooldowns().addCooldown(tool.getItem(), getDrawtime(tool, player, speedFactor));
  }

  /**
   * Use in {@link net.minecraft.world.item.Item#use(Level, Player, InteractionHand)} or {@link #onToolUse(ToolStackView, ModifierEntry, Player, InteractionHand, InteractionSource)} to setup draw time for {@link slimeknights.tconstruct.library.client.model.TinkerItemProperties}.
   * @param tool      Tool being used
   * @param living    Entity using the tool, used for the vanilla hook
   * @param speedFactor  Additional factor to multiply drawtime by, after considering {@link ToolStats#DRAW_SPEED}
   */
  static int startDrawing(ToolStackView tool, LivingEntity living, float speedFactor) {
    int drawtime = getDrawtime(tool, living, speedFactor);
    tool.set(TinkerData.DRAWTIME, drawtime);
    return drawtime;
  }

  /**
   * Combination of {@link #startUsing(ToolStackView, ModifierId, LivingEntity, InteractionHand)} and {@link #startDrawing(ToolStackView, LivingEntity, float)} ensuring they are added in the proper order.
   * @param tool         Tool being used
   * @param modifier     Modifier to call for later hooks, must be on the tool
   * @param living       Entity using the tool, used for the vanilla hook
   * @param hand         Hand using the tool
   * @param speedFactor  Additional factor to multiply drawtime by, after considering {@link ToolStats#DRAW_SPEED}
   */
  static void startUsingWithDrawtime(ToolStackView tool, ModifierId modifier, LivingEntity living, InteractionHand hand, float speedFactor) {
    startDrawing(tool, living, speedFactor);
    startUsing(tool, modifier, living, hand);
  }

  /**
   * Gets the current charge percentage based on the given tool
   * @param tool        Tool being used
   * @param chargeTime  Ticks the item has been used so far, typically from {@link #onStoppedUsing(ToolStackView, ModifierEntry, LivingEntity, int)}.
   */
  static float getToolCharge(ToolStackView tool, float chargeTime) {
    if (chargeTime < 0) {
      return 0;
    }
    float charge = chargeTime / tool.getOrDefault(TinkerData.DRAWTIME, 20);
    charge = (charge * charge + charge * 2) / 3;
    if (charge > 1) {
      charge = 1;
    }
    return charge;
  }

  /** Gets the currently active modifier, or {@link ModifierEntry#EMPTY} if none is active. Generally does not need to be called in modifiers as we call it in internal logic. */
  static ModifierEntry getActiveModifier(ToolStackView tool) {
    ModifierId modifier = tool.get(TinkerData.ACTIVE_MODIFIER);
    if (modifier != null) {
      return tool.getModifiers().getEntry(modifier);
    }
    return ModifierEntry.EMPTY;
  }

  /** Called to clear any data modifiers set when usage starts. Generally does not need to be called in modifiers as we call it in internal logic. */
  static void finishUsing(ToolStackView tool) {
    tool.remove(TinkerData.ACTIVE_MODIFIER);
    tool.remove(TinkerData.DRAWTIME);
  }


  /** Logic to merge multiple interaction hooks into one */
  record FirstMerger(Collection<GeneralInteractionModifierHook> modules) implements GeneralInteractionModifierHook {
    @Override
    public InteractionResult onToolUse(ToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
      InteractionResult result = InteractionResult.PASS;
      for (GeneralInteractionModifierHook module : modules) {
        result = module.onToolUse(tool, modifier, player, hand, source);
        if (result.consumesAction()) {
          return result;
        }
      }
      return result;
    }

    @Override
    public void onUsingTick(ToolStackView tool, ModifierEntry modifier, LivingEntity entity, int timeLeft) {
      for (GeneralInteractionModifierHook module : modules) {
        module.onUsingTick(tool, modifier, entity, timeLeft);
      }
    }

    @Override
    public void onStoppedUsing(ToolStackView tool, ModifierEntry modifier, LivingEntity entity, int timeLeft) {
      for (GeneralInteractionModifierHook module : modules) {
        module.onStoppedUsing(tool, modifier, entity, timeLeft);
      }
    }

    @Override
    public void onFinishUsing(ToolStackView tool, ModifierEntry modifier, LivingEntity entity) {
      for (GeneralInteractionModifierHook module : modules) {
        module.onFinishUsing(tool, modifier, entity);
      }
    }

    @Override
    public int getUseDuration(ToolStackView tool, ModifierEntry modifier) {
      for (GeneralInteractionModifierHook module : modules) {
        int duration = module.getUseDuration(tool, modifier);
        if (duration > 0) {
          return duration;
        }
      }
      return 0;
    }

    @Override
    public UseAnim getUseAction(ToolStackView tool, ModifierEntry modifier) {
      for (GeneralInteractionModifierHook module : modules) {
        UseAnim anim = module.getUseAction(tool, modifier);
        if (anim != UseAnim.NONE) {
          return anim;
        }
      }
      return UseAnim.NONE;
    }
  }
}
