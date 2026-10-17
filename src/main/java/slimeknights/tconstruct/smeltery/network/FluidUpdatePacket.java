package slimeknights.tconstruct.smeltery.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.BlockEntityPacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.smeltery.network.FluidUpdatePacket.IFluidPacketReceiver;

/**
 * Packet for when the fluid changes in a block entity.
 */
public record FluidUpdatePacket(BlockPos pos, FluidStack fluid) implements BlockEntityPacket<IFluidPacketReceiver> {
  public static final Type<FluidUpdatePacket> TYPE = new Type<>(TConstruct.getResource("fluid_update"));
  public static final StreamCodec<RegistryFriendlyByteBuf,FluidUpdatePacket> CODEC = StreamCodec.composite(
    BlockPos.STREAM_CODEC, FluidUpdatePacket::pos,
    FluidStack.OPTIONAL_STREAM_CODEC, FluidUpdatePacket::fluid,
    FluidUpdatePacket::new);

  @Override
  public Type<FluidUpdatePacket> type() {
    return TYPE;
  }

  @Override
  public Class<IFluidPacketReceiver> blockEntityType() {
    return IFluidPacketReceiver.class;
  }

  @Override
  public void handleBlockEntity(IPayloadContext context, IFluidPacketReceiver be) {
    be.updateFluidTo(fluid);
  }

  /** Interface to implement for anything wishing to receive fluid updates */
  public interface IFluidPacketReceiver {
    /**
     * Updates the current fluid to the specified value
     * @param fluid New fluidstack
     */
    void updateFluidTo(FluidStack fluid);
  }
}
