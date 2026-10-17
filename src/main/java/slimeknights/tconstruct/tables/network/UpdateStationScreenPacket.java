package slimeknights.tconstruct.tables.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.tables.client.inventory.BaseTabbedScreen;

/** Packet sent to update the screen on a table. */
public enum UpdateStationScreenPacket implements ISimplePacket {
  INSTANCE;

  public static final Type<UpdateStationScreenPacket> TYPE = new Type<>(TConstruct.getResource("update_station_screen"));
  public static final StreamCodec<ByteBuf,UpdateStationScreenPacket> CODEC = StreamCodec.unit(INSTANCE);

  @Override
  public Type<UpdateStationScreenPacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    HandleClient.handle();
  }

  /** Helper to update the screen client side. Only call if on the client side. */
  public static void updateScreen() {
    HandleClient.handle();
  }

  /** Safely runs client side only code in a method only called on client */
  private static class HandleClient {
    private static void handle() {
      Screen screen = Minecraft.getInstance().screen;
      if (screen != null) {
        if (screen instanceof BaseTabbedScreen) {
          ((BaseTabbedScreen<?,?>) screen).updateDisplay();
        }
      }
    }
  }
}
