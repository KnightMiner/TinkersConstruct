package slimeknights.tconstruct.tools.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.tools.menu.ToolContainerMenu;

/** Packet used when a fluid is changed inside a tool container menu */
@Internal
public record ToolContainerFluidUpdatePacket(FluidStack fluid) implements ISimplePacket {
  public static final Type<ToolContainerFluidUpdatePacket> TYPE = new Type<>(TConstruct.getResource("tool_container_fluid_update"));
  public static final StreamCodec<RegistryFriendlyByteBuf,ToolContainerFluidUpdatePacket> CODEC = FluidStack.OPTIONAL_STREAM_CODEC.map(ToolContainerFluidUpdatePacket::new, ToolContainerFluidUpdatePacket::fluid);

  @Override
  public Type<ToolContainerFluidUpdatePacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    if (context.player().containerMenu instanceof ToolContainerMenu toolMenu) {
      toolMenu.getTank().setFluid(fluid);
    }
  }
}
