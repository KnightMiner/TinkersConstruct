package slimeknights.tconstruct.smeltery.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.inventory.BaseContainerMenu;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.smeltery.block.entity.tank.ISmelteryTankHandler;

/**
 * Packet sent when a fluid is clicked in the smeltery UI
 */
public record SmelteryFluidClickedPacket(int index) implements ISimplePacket {
  public static final Type<SmelteryFluidClickedPacket> TYPE = new Type<>(TConstruct.getResource("smeltery_fluid_clicked"));
  public static final StreamCodec<ByteBuf,SmelteryFluidClickedPacket> CODEC = ByteBufCodecs.VAR_INT.map(SmelteryFluidClickedPacket::new, SmelteryFluidClickedPacket::index);

  @Override
  public void handle(IPayloadContext context) {
    Player player = context.player();
    if (!player.isSpectator() &&player.containerMenu instanceof BaseContainerMenu<?> base && base.getTile() instanceof ISmelteryTankHandler tank) {
      tank.getTank().moveFluidToBottom(index);
    }
  }
}
