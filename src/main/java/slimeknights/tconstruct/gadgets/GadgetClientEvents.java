package slimeknights.tconstruct.gadgets;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.ClientEventBase;
import slimeknights.tconstruct.gadgets.client.FancyArmorStandRenderer;
import slimeknights.tconstruct.gadgets.client.FancyItemFrameRenderer;

@SuppressWarnings("unused")
@EventBusSubscriber(modid=TConstruct.MOD_ID, value= Dist.CLIENT)
public class GadgetClientEvents extends ClientEventBase {
  @SubscribeEvent
  static void registerModels(RegisterAdditional event) {
    FancyItemFrameRenderer.LOCATIONS_MODEL.values().forEach(event::register);
    FancyItemFrameRenderer.LOCATIONS_MODEL_MAP.values().forEach(event::register);
  }

  @SubscribeEvent
  static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerEntityRenderer(TinkerGadgets.itemFrameEntity.get(), FancyItemFrameRenderer::new);
    event.registerEntityRenderer(TinkerGadgets.armorStandEntity.get(), FancyArmorStandRenderer::new);
  }
}
