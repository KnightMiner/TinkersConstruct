package slimeknights.tconstruct.common.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.library.materials.definition.UpdateMaterialsPacket;
import slimeknights.tconstruct.library.materials.stats.UpdateMaterialStatsPacket;
import slimeknights.tconstruct.library.materials.traits.UpdateMaterialTraitsPacket;
import slimeknights.tconstruct.library.modifiers.UpdateModifiersPacket;
import slimeknights.tconstruct.library.modifiers.fluid.UpdateFluidEffectsPacket;
import slimeknights.tconstruct.library.tools.definition.UpdateToolDefinitionDataPacket;
import slimeknights.tconstruct.library.tools.layout.UpdateTinkerSlotLayoutsPacket;
import slimeknights.tconstruct.shared.network.GeneratePartTexturesPacket;
import slimeknights.tconstruct.smeltery.network.ChannelFlowPacket;
import slimeknights.tconstruct.smeltery.network.FaucetActivationPacket;
import slimeknights.tconstruct.smeltery.network.FluidUpdatePacket;
import slimeknights.tconstruct.smeltery.network.SmelteryFluidClickedPacket;
import slimeknights.tconstruct.smeltery.network.SmelteryTankUpdatePacket;
import slimeknights.tconstruct.smeltery.network.StructureErrorPositionPacket;
import slimeknights.tconstruct.smeltery.network.StructureUpdatePacket;
import slimeknights.tconstruct.tables.network.StationTabPacket;
import slimeknights.tconstruct.tables.network.TinkerStationRenamePacket;
import slimeknights.tconstruct.tables.network.TinkerStationSelectionPacket;
import slimeknights.tconstruct.tables.network.UpdateCraftingRecipePacket;
import slimeknights.tconstruct.tables.network.UpdateStationScreenPacket;
import slimeknights.tconstruct.tables.network.UpdateTinkerStationRecipePacket;
import slimeknights.tconstruct.tools.network.EntityMovementChangePacket;
import slimeknights.tconstruct.tools.network.InteractWithAirPacket;
import slimeknights.tconstruct.tools.network.PushBlockRowPacket;
import slimeknights.tconstruct.tools.network.SyncProjectileModifiersPacket;
import slimeknights.tconstruct.tools.network.TinkerControlPacket;
import slimeknights.tconstruct.tools.network.ToolContainerFluidUpdatePacket;

/**
 * Base network class for all tinkers logic
 * <p>
 * In general, if you need to send packets you should use your own network class
 */
public class TinkerNetwork {
  /*
   * Network versions:
   * 21.0: initial 1.21.1 release
   */
  private static final String VERSION = "21.0";

  /** Registers packets into this network */
  @Internal
  public static void registerPackets(RegisterPayloadHandlersEvent event) {
    PayloadRegistrar registrar = event.registrar(VERSION);

    // shared
    registrar.playToClient(InventorySlotSyncPacket.TYPE, InventorySlotSyncPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateNeighborsPacket.TYPE, UpdateNeighborsPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(GeneratePartTexturesPacket.TYPE, GeneratePartTexturesPacket.CODEC, ISimplePacket.handle());

    // gadgets
    registrar.playToClient(EntityMovementChangePacket.TYPE, EntityMovementChangePacket.CODEC, ISimplePacket.handle());

    // tables
    registrar.playToServer(StationTabPacket.TYPE, StationTabPacket.CODEC, ISimplePacket.handle());
    registrar.playToServer(TinkerStationRenamePacket.TYPE, TinkerStationRenamePacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateCraftingRecipePacket.TYPE, UpdateCraftingRecipePacket.CODEC, ISimplePacket.handle());
    registrar.playToServer(TinkerStationSelectionPacket.TYPE, TinkerStationSelectionPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateTinkerSlotLayoutsPacket.TYPE, UpdateTinkerSlotLayoutsPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateStationScreenPacket.TYPE, UpdateStationScreenPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateTinkerStationRecipePacket.TYPE, UpdateTinkerStationRecipePacket.CODEC, ISimplePacket.handle());

    // tools
    registrar.playToClient(UpdateMaterialsPacket.TYPE, UpdateMaterialsPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateMaterialStatsPacket.TYPE, UpdateMaterialStatsPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateMaterialTraitsPacket.TYPE, UpdateMaterialTraitsPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateToolDefinitionDataPacket.TYPE, UpdateToolDefinitionDataPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(ToolContainerFluidUpdatePacket.TYPE, ToolContainerFluidUpdatePacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(SyncProjectileModifiersPacket.TYPE, SyncProjectileModifiersPacket.CODEC, ISimplePacket.handle());

    // modifiers
    registrar.playToServer(TinkerControlPacket.TYPE, TinkerControlPacket.CODEC, ISimplePacket.handle());
    registrar.playToServer(InteractWithAirPacket.TYPE, InteractWithAirPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateModifiersPacket.TYPE, UpdateModifiersPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(UpdateFluidEffectsPacket.TYPE, UpdateFluidEffectsPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(PushBlockRowPacket.TYPE, PushBlockRowPacket.CODEC, ISimplePacket.handle());

    // smeltery
    registrar.playToClient(FluidUpdatePacket.TYPE, FluidUpdatePacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(FaucetActivationPacket.TYPE, FaucetActivationPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(ChannelFlowPacket.TYPE, ChannelFlowPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(SmelteryTankUpdatePacket.TYPE, SmelteryTankUpdatePacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(StructureUpdatePacket.TYPE, StructureUpdatePacket.CODEC, ISimplePacket.handle());
    registrar.playToServer(SmelteryFluidClickedPacket.TYPE, SmelteryFluidClickedPacket.CODEC, ISimplePacket.handle());
    registrar.playToClient(StructureErrorPositionPacket.TYPE, StructureErrorPositionPacket.CODEC, ISimplePacket.handle());
  }
}
