package slimeknights.tconstruct.library.events.teleport;

import lombok.Getter;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.eventbus.api.Cancelable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;

/** Event fired when an entity teleports using the ender sling modifier */
@Cancelable
@Getter
public class SlingModifierTeleportEvent extends ModifierTeleportEvent {
  private final ToolStackView tool;
  public SlingModifierTeleportEvent(Entity entity, double targetX, double targetY, double targetZ, ToolStackView tool, ModifierEntry entry) {
    super(entity, targetX, targetY, targetZ, entry);
    this.tool = tool;
  }
}
