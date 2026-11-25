package slimeknights.tconstruct.library.modifiers.hook.interaction;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.component.SetComponent;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;

/**
 * Enum representing different sources of interaction
 */
public enum InteractionSource {
  /** Standard interaction hook from right-clicking with a tool */
  RIGHT_CLICK,
  /** Interaction from left-clicking a tool, used on bows and slimestaffs */
  LEFT_CLICK,
  /** Interaction from chestplates with an empty hand. See also {@link KeybindInteractModifierHook} */
  ARMOR;

  /** All interaction sources on held tools */
  public static InteractionSource[] HELD = { RIGHT_CLICK, LEFT_CLICK };
  /** Key for denoting toggled to attack in tooltip */
  private static final String ATTACK_FORMAT = TConstruct.makeTranslationKey("modifier", "attack_toggled");
  /** Key for denoting toggled to interaction in tooltip */
  private static final String INTERACT_FORMAT = TConstruct.makeTranslationKey("modifier", "interact_toggled");

  /** Translates the context to a slot for the sake of breaking animations */
  public EquipmentSlot getSlot(InteractionHand hand) {
    return switch (this) {
      case RIGHT_CLICK -> switch (hand) {
        case MAIN_HAND -> EquipmentSlot.MAINHAND;
        case OFF_HAND -> EquipmentSlot.OFFHAND;
      };
      case LEFT_CLICK -> EquipmentSlot.MAINHAND;
      case ARMOR -> EquipmentSlot.CHEST;
    };
  }

  /**
   * Translates the equipment slot to an interaction source. Will never return {@link #LEFT_CLICK}.
   * @param slot  Original slot
   * @return  Proper interaction source
   */
  public static InteractionSource fromEquipmentSlot(EquipmentSlot slot) {
    return switch (slot.getType()) {
      case HUMANOID_ARMOR, ANIMAL_ARMOR -> ARMOR;
      case HAND -> RIGHT_CLICK;
    };
  }

  /** Adds the format string to the modifier name */
  public static Component formatModifierName(ToolStackView tool, Modifier modifier, Component originalName) {
    ModifierId id = modifier.id();
    if (SetComponent.get(tool, TinkerData.ATTACK_MODIFIERS).contains(id)) {
      return modifier.applyStyle(Component.translatable(ATTACK_FORMAT, originalName));
    }
    if (SetComponent.get(tool, TinkerData.INTERACT_MODIFIERS).contains(id)) {
      return modifier.applyStyle(Component.translatable(INTERACT_FORMAT, originalName));
    }
    return originalName;
  }
}
