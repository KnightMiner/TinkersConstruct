package slimeknights.tconstruct.library.tools.context;

import lombok.Data;
import lombok.With;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tools.component.ModifierEntries;
import slimeknights.tconstruct.library.tools.component.ToolMaterials;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.stack.ToolContext;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;

/**
 * Implementation of the limited view of {@link ToolStackView} for use in tool rebuild hooks
 */
@SuppressWarnings("ClassCanBeRecord")
@Data
public class ToolRebuildContext implements ToolContext {
  /** Item being rebuilt */
  private final ItemStack stack;
  /** Tool definition of the item being rebuilt */
  private final ToolDefinition definition;
  /** Materials on the tool being rebuilt */
  private final ToolMaterials materials;
  /** List of recipe modifiers on the tool being rebuilt */
  private final ModifierEntries craftedModifiers;
  /** List of all modifiers on the tool being rebuilt, from recipes and traits */
  @With
  private final ModifierEntries modifiers;

  @Override
  public Item getItem() {
    return stack.getItem();
  }

  @Override
  public DataComponentMap getComponents() {
    return stack.getComponents();
  }
}
