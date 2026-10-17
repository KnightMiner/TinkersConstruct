package slimeknights.tconstruct.smeltery.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.BlockEntityPacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.smeltery.block.entity.FaucetBlockEntity;

/**
 * Sent to clients to activate the faucet animation clientside.
 */
@Internal
public record FaucetActivationPacket(BlockPos pos, FluidStack fluid, boolean isPouring) implements BlockEntityPacket<FaucetBlockEntity> {
  public static final Type<FaucetActivationPacket> TYPE = new Type<>(TConstruct.getResource("faucet_activation"));
  public static final StreamCodec<RegistryFriendlyByteBuf,FaucetActivationPacket> CODEC = StreamCodec.composite(
    BlockPos.STREAM_CODEC, FaucetActivationPacket::pos,
    FluidStack.OPTIONAL_STREAM_CODEC, FaucetActivationPacket::fluid,
    ByteBufCodecs.BOOL, FaucetActivationPacket::isPouring,
    FaucetActivationPacket::new);

  @Override
  public Type<FaucetActivationPacket> type() {
    return TYPE;
  }

  @Override
  public Class<FaucetBlockEntity> blockEntityType() {
    return FaucetBlockEntity.class;
  }

  @Override
  public void handleBlockEntity(IPayloadContext context, FaucetBlockEntity be) {
    be.onActivationPacket(fluid, isPouring);
  }
}
