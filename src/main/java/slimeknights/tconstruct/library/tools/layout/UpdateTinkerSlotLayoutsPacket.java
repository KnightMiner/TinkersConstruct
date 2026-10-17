package slimeknights.tconstruct.library.tools.layout;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.TConstruct;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Packet to update the slot layouts for the tinker station
 */
@Internal
public record UpdateTinkerSlotLayoutsPacket(Collection<StationSlotLayout> layouts) implements ISimplePacket {
  public static final Type<UpdateTinkerSlotLayoutsPacket> TYPE = new Type<>(TConstruct.getResource("update_tinker_slot_layouts"));
  private static final StreamCodec<FriendlyByteBuf,StationSlotLayout> LAYOUT_CODEC = StreamCodec.of((buffer, layout) -> layout.write(buffer), StationSlotLayout::read);
  private static final StreamCodec<FriendlyByteBuf,Collection<StationSlotLayout>> LAYOUT_LIST_CODEC = ByteBufCodecs.collection(ArrayList::new, LAYOUT_CODEC);
  public static final StreamCodec<FriendlyByteBuf,UpdateTinkerSlotLayoutsPacket> CODEC = LAYOUT_LIST_CODEC.map(UpdateTinkerSlotLayoutsPacket::new, UpdateTinkerSlotLayoutsPacket::layouts);

  @Override
  public Type<UpdateTinkerSlotLayoutsPacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    StationSlotLayoutLoader.getInstance().setSlots(layouts);
  }
}
