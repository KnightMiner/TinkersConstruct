package slimeknights.tconstruct.library.tools.definition.module.display;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.helper.TooltipUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.util.Collection;

/** Hook for fetching the display name of a tool */
public interface ToolNameHook {
  /**
   * Gets the display name for the given tool
   * @param definition  Tool definition instance
   * @param stack       Item stack instance for calling the legacy hook.
   * @param tool        Tool instance, for fetching either stack or tool.
   * @param name        Name from previous hooks, allowing composing.
   * @return  Display name component
   */
  Component getDisplayName(ToolDefinition definition, ItemStack stack, @Nullable IToolStackView tool, Component name);

  /** Gets the tool for the parameters */
  static IToolStackView getTool(ItemStack stack, @Nullable IToolStackView tool) {
    if (tool != null) {
      return tool;
    }
    return ToolStack.from(stack);
  }

  /**
   * Gets the display name for a tool including as defined by the hook.
   * @param definition  Tool definition
   * @param stack       Stack instance
   * @param tool        Tool instance, may be null if one does not exist
   * @return  Display name including the head material
   */
  static Component getName(ToolDefinition definition, ItemStack stack, @Nullable IToolStackView tool) {
    // support override name
    String name = TooltipUtil.getDisplayName(stack);
    if (!name.isEmpty()) {
      return Component.literal(name);
    }
    return definition.getHook(ToolHooks.DISPLAY_NAME).getDisplayName(definition, stack, tool, Component.translatable(stack.getItem().getDescriptionId()));
  }

  /**
   * Gets the display name for a tool including as defined by the hook.
   * @param definition  Tool definition
   * @param stack       Stack instance
   * @return  Display name including the head material
   */
  static Component getName(ToolDefinition definition, ItemStack stack) {
    return getName(definition, stack, null);
  }

  /** Merger running each hook in order */
  record ComposeMerger(Collection<ToolNameHook> modules) implements ToolNameHook {
    @Override
    public Component getDisplayName(ToolDefinition definition, ItemStack stack, @Nullable IToolStackView tool, Component name) {
      // create the tool instance so we can share it between multiple implementations
      tool = getTool(stack, tool);
      for (ToolNameHook hook : modules) {
        name = hook.getDisplayName(definition, stack, tool, name);
      }
      return name;
    }
  }
}
