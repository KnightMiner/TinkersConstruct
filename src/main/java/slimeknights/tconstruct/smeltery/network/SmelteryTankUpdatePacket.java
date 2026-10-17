package slimeknights.tconstruct.smeltery.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.BlockEntityPacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.smeltery.block.entity.tank.ISmelteryTankHandler;

import java.util.List;

/**
 * Packet sent whenever the contents of the smeltery tank change.
 */
public record SmelteryTankUpdatePacket(BlockPos pos, List<FluidStack> fluids) implements BlockEntityPacket<ISmelteryTankHandler> {
  public static final Type<SmelteryTankUpdatePacket> TYPE = new Type<>(TConstruct.getResource("smeltery_tank_update"));
  public static final StreamCodec<RegistryFriendlyByteBuf,SmelteryTankUpdatePacket> CODEC = StreamCodec.composite(
    BlockPos.STREAM_CODEC, SmelteryTankUpdatePacket::pos,
    FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()), SmelteryTankUpdatePacket::fluids,
    SmelteryTankUpdatePacket::new);

  @Override
  public Type<SmelteryTankUpdatePacket> type() {
    return TYPE;
  }

  @Override
  public Class<ISmelteryTankHandler> blockEntityType() {
    return ISmelteryTankHandler.class;
  }

  @Override
  public void handleBlockEntity(IPayloadContext context, ISmelteryTankHandler be) {
    be.updateFluidsFromPacket(fluids);
  }
}
