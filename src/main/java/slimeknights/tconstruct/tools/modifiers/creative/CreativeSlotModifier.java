package slimeknights.tconstruct.tools.modifiers.creative;

import net.minecraft.network.chat.Component;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.AddSlotsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.ModifierRemovalHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap.Builder;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.component.ModifierSlots;
import slimeknights.tconstruct.library.tools.stack.ToolContext;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** Modifier that adds a variable number of slots to a tool. Could easily be done via Tag editing, but this makes it easier */
public class CreativeSlotModifier extends NoLevelsModifier implements AddSlotsModifierHook, ModifierRemovalHook {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addHook(this, ModifierHooks.ADD_SLOTS, ModifierHooks.REMOVE);
  }

  @Nullable
  @Override
  public Component onRemoved(ToolStackView tool, Modifier modifier) {
    tool.remove(TinkerData.CREATIVE_SLOTS);
    return null;
  }

  @Override
  public void addSlots(ToolContext context, ModifierEntry modifier, ModifierSlots.Builder builder) {
    builder.add(context.getOrDefault(TinkerData.CREATIVE_SLOTS, ModifierSlots.EMPTY));
  }

  /** Formats the given slot type as a count */
  private static Component formatCount(SlotType slotType, int count) {
    return Component.literal((count > 0 ? "+" : "") + count + " ")
      .append(slotType.getDisplayName())
      .withStyle(style -> style.withColor(slotType.getColor()));
  }

  @Override
  public List<Component> getDescriptionList(ToolStackView tool, ModifierEntry entry) {
    List<Component> tooltip = getDescriptionList(entry.getLevel());
    ModifierSlots slots = tool.getOrDefault(TinkerData.CREATIVE_SLOTS, ModifierSlots.EMPTY);
    if (!slots.isEmpty() ) {
      // first one found has special behavior
      boolean first = true;
      for (SlotType slotType : SlotType.getAllSlotTypes()) {
        int count = slots.get(slotType);
        if (count > 0) {
          if (first) {
            // found a valid slot? copy the list once then add the rest
            tooltip = new ArrayList<>(tooltip);
            first = false;
          }
          tooltip.add(formatCount(slotType, count));
        }
      }
    }
    return tooltip;
  }
}
