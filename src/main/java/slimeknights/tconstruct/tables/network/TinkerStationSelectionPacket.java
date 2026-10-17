package slimeknights.tconstruct.tables.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayoutLoader;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;

/** Packet to update the tinker station slot layout on the server, which affects slot filters. */
@Internal
public record TinkerStationSelectionPacket(ResourceLocation layoutName) implements ISimplePacket {
  public static final Type<TinkerStationSelectionPacket> TYPE = new Type<>(TConstruct.getResource("tinker_station_selection"));
  public static final StreamCodec<ByteBuf,TinkerStationSelectionPacket> CODEC = ResourceLocation.STREAM_CODEC.map(TinkerStationSelectionPacket::new, TinkerStationSelectionPacket::layoutName);

  @Override
  public Type<TinkerStationSelectionPacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    AbstractContainerMenu container = context.player().containerMenu;
    if (container instanceof TinkerStationContainerMenu tinker) {
      tinker.setToolSelection(StationSlotLayoutLoader.getInstance().get(layoutName));
    }
  }
}
