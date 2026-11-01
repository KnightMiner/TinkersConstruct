package slimeknights.tconstruct.fluids;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.mantle.fluid.InvertedFluid;
import slimeknights.mantle.fluid.MantleFluids;
import slimeknights.mantle.fluid.texture.ClientInvertedFluidType;
import slimeknights.mantle.fluid.texture.ClientTextureFluidType;
import slimeknights.mantle.registration.object.FlowingFluidObject;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.ClientEventBase;
import slimeknights.tconstruct.fluids.client.FluidEffectProjectileRenderer;

@EventBusSubscriber(modid = TConstruct.MOD_ID, value = Dist.CLIENT)
public class FluidClientEvents extends ClientEventBase {
  @SubscribeEvent
  static void clientSetup(final FMLClientSetupEvent event) {
    setTranslucent(MantleFluids.honey);
    // slime
    setTranslucent(TinkerFluids.earthSlime);
    setTranslucent(TinkerFluids.skySlime);
    setTranslucent(TinkerFluids.enderSlime);
    // molten
    setTranslucent(TinkerFluids.moltenDiamond);
    setTranslucent(TinkerFluids.moltenEmerald);
    setTranslucent(TinkerFluids.moltenGlass);
    setTranslucent(TinkerFluids.moltenGlass);
    setTranslucent(TinkerFluids.liquidSoul);
    setTranslucent(TinkerFluids.moltenAmethyst);
  }

  private static void setTranslucent(FlowingFluidObject<?> fluid) {
    ItemBlockRenderTypes.setRenderLayer(fluid.getStill(), RenderType.translucent());
    ItemBlockRenderTypes.setRenderLayer(fluid.getFlowing(), RenderType.translucent());
  }

  @SubscribeEvent
  static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerEntityRenderer(TinkerFluids.fluidProjectile.get(), FluidEffectProjectileRenderer::new);
  }

  /** Registers the texture extension for the given fluid */
  private static void textureExtension(RegisterClientExtensionsEvent event, FluidObject<?> fluid) {
    FluidType type = fluid.getType();
    event.registerFluidType(new ClientTextureFluidType(type), type);
  }

  /** Registers the texture extension for the given fluid */
  private static void invertedTexture(RegisterClientExtensionsEvent event, FluidObject<InvertedFluid> fluid) {
    FluidType type = fluid.getType();
    event.registerFluidType(new ClientInvertedFluidType(type), type);
  }

  @SubscribeEvent
  static void registerClientExtensions(RegisterClientExtensionsEvent event) {
    // slime
    textureExtension(event, TinkerFluids.earthSlime);
    textureExtension(event, TinkerFluids.skySlime);
    textureExtension(event, TinkerFluids.enderSlime);
    textureExtension(event, TinkerFluids.magma);
    invertedTexture(event, TinkerFluids.ichor);
    textureExtension(event, TinkerFluids.meatSoup);

    // base molten fluids
    textureExtension(event, TinkerFluids.searedStone);
    textureExtension(event, TinkerFluids.scorchedStone);
    textureExtension(event, TinkerFluids.moltenClay);
    textureExtension(event, TinkerFluids.moltenGlass);
    textureExtension(event, TinkerFluids.liquidSoul);
    // ceramics compat
    textureExtension(event, TinkerFluids.moltenPorcelain);
    // fancy molten fluids
    textureExtension(event, TinkerFluids.moltenObsidian);
    textureExtension(event, TinkerFluids.moltenEnder);
    textureExtension(event, TinkerFluids.blazingBlood);

    // ores
    textureExtension(event, TinkerFluids.moltenEmerald);
    textureExtension(event, TinkerFluids.moltenQuartz);
    textureExtension(event, TinkerFluids.moltenAmethyst);
    textureExtension(event, TinkerFluids.moltenDiamond);
    textureExtension(event, TinkerFluids.moltenDebris);
    // metal ores
    textureExtension(event, TinkerFluids.moltenIron);
    textureExtension(event, TinkerFluids.moltenGold);
    textureExtension(event, TinkerFluids.moltenCopper);
    textureExtension(event, TinkerFluids.moltenCobalt);
    textureExtension(event, TinkerFluids.moltenSteel);
    textureExtension(event, TinkerFluids.moltenKnightmetal);
    // alloys
    textureExtension(event, TinkerFluids.moltenSlimesteel);
    textureExtension(event, TinkerFluids.moltenAmethystBronze);
    textureExtension(event, TinkerFluids.moltenRoseGold);
    textureExtension(event, TinkerFluids.moltenPigIron);
    textureExtension(event, TinkerFluids.moltenManyullyn);
    textureExtension(event, TinkerFluids.moltenHepatizon);
    invertedTexture(event, TinkerFluids.moltenCinderslime);
    textureExtension(event, TinkerFluids.moltenQueensSlime);
    textureExtension(event, TinkerFluids.moltenNetherite);
    textureExtension(event, TinkerFluids.moltenKnightslime);

    // compat ores
    textureExtension(event, TinkerFluids.moltenTin);
    textureExtension(event, TinkerFluids.moltenAluminum);
    textureExtension(event, TinkerFluids.moltenLead);
    textureExtension(event, TinkerFluids.moltenSilver);
    textureExtension(event, TinkerFluids.moltenNickel);
    textureExtension(event, TinkerFluids.moltenZinc);
    textureExtension(event, TinkerFluids.moltenPlatinum);
    textureExtension(event, TinkerFluids.moltenTungsten);
    textureExtension(event, TinkerFluids.moltenOsmium);
    textureExtension(event, TinkerFluids.moltenUranium);
    textureExtension(event, TinkerFluids.moltenChromium);
    textureExtension(event, TinkerFluids.moltenCadmium);
    // compat alloys
    textureExtension(event, TinkerFluids.moltenBronze);
    textureExtension(event, TinkerFluids.moltenBrass);
    textureExtension(event, TinkerFluids.moltenElectrum);
    textureExtension(event, TinkerFluids.moltenInvar);
    textureExtension(event, TinkerFluids.moltenConstantan);
    textureExtension(event, TinkerFluids.moltenPewter);
    // mod specific compat
    // thermal
    textureExtension(event, TinkerFluids.moltenEnderium);
    textureExtension(event, TinkerFluids.moltenLumium);
    textureExtension(event, TinkerFluids.moltenSignalum);
    // mekanism
    textureExtension(event, TinkerFluids.moltenRefinedGlowstone);
    textureExtension(event, TinkerFluids.moltenRefinedObsidian);
    // cosmere metals
    textureExtension(event, TinkerFluids.moltenNicrosil);
    textureExtension(event, TinkerFluids.moltenDuralumin);
    textureExtension(event, TinkerFluids.moltenBendalloy);
    // twilight
    textureExtension(event, TinkerFluids.moltenSteeleaf);
    textureExtension(event, TinkerFluids.fieryLiquid);
  }
}
