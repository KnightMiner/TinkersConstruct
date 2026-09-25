package slimeknights.tconstruct.library.tools.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;

/** Base interface for all tools that can receive modifiers */
public interface IModifiable extends ItemLike {
  /** Volatile boolean key to make a tool spawn an indestructable entity */
  ResourceLocation SHINY = TConstruct.getResource("shiny");
  /** Volatile int key to increase a tool's range */
  ResourceLocation EXPANDED = TConstruct.getResource("expanded");
  /** Modifier key to defer tool interaction to the offhand if present */
  ResourceLocation DEFER_OFFHAND = TConstruct.getResource("defer_offhand");
  /** Modifier key to entirely disable tool interaction */
  ResourceLocation NO_INTERACTION = TConstruct.getResource("no_interaction");

  /** Gets the definition of this tool for building and applying modifiers */
  ToolDefinition getToolDefinition();

  /** Gets the tool definition for the given item, or {@link ToolDefinition#EMPTY} if its not modifiable. */
  static ToolDefinition getToolDefinition(Item item) {
    if (item instanceof IModifiable modifiable) {
      return modifiable.getToolDefinition();
    }
    return ToolDefinition.EMPTY;
  }
}
