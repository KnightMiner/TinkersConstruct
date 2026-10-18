package slimeknights.tconstruct.gadgets.item;

import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import slimeknights.tconstruct.library.TinkerData;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Holds a list of passengers on the entity for handling syncing of players.
 * <p>
 * Does not serialize as the world saves the entities already, they just dismounted on logout
 */
public class PiggybackHandler {

  /** Player holding this capability */
  @Nullable
  private final Player riddenPlayer;
  /** Last found list of passengers, used in serialization and syncing */
  private List<Entity> lastPassengers;

  /** Creates a new handler */
  public PiggybackHandler(IAttachmentHolder entity) {
    riddenPlayer = entity instanceof Player p ? p : null;
  }

  /**
   * Updates the passengers on the back
   */
  public void updatePassengers() {
    if (this.riddenPlayer != null && !this.riddenPlayer.level().isClientSide) {
      // tell the player itself if his riders changed serverside
      if (!this.riddenPlayer.getPassengers().equals(this.lastPassengers)) {
        if (this.riddenPlayer instanceof ServerPlayer sPlayer) {
          sPlayer.connection.send(new ClientboundSetPassengersPacket(this.riddenPlayer));
        }
      }
      this.lastPassengers = this.riddenPlayer.getPassengers();
    }
  }

  /** Gets the handler for the given entity. */
  public static PiggybackHandler get(LivingEntity entity) {
    return entity.getData(TinkerData.PIGGYBACK_HANDLER);
  }
}
