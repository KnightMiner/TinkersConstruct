package slimeknights.tconstruct.tables.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;

/** Packet to send to the server to update the name in the UI */
@Internal
public record TinkerStationRenamePacket(String name) implements ISimplePacket {
  public static final Type<TinkerStationRenamePacket> TYPE = new Type<>(TConstruct.getResource("tinker_station_rename"));
  public static final StreamCodec<ByteBuf,TinkerStationRenamePacket> CODEC = ByteBufCodecs.STRING_UTF8.map(TinkerStationRenamePacket::new, TinkerStationRenamePacket::name);

  @Override
  public Type<TinkerStationRenamePacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    if (context.player().containerMenu instanceof TinkerStationContainerMenu station) {
      TinkerStationBlockEntity tile = station.getTile();
      if (tile != null) {
        tile.setItemName(name);
      }
    }
  }
}
