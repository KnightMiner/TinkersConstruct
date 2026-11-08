package slimeknights.tconstruct.shared;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.joml.Matrix4f;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.data.model.ModelSpriteProvider;
import slimeknights.tconstruct.common.data.model.SimpleBlockStateProvider;
import slimeknights.tconstruct.common.data.model.TinkerBlockStateProvider;
import slimeknights.tconstruct.common.data.model.TinkerItemModelProvider;
import slimeknights.tconstruct.common.data.model.TinkerSpriteSourceProvider;
import slimeknights.tconstruct.common.data.render.RenderFluidProvider;
import slimeknights.tconstruct.common.data.render.RenderItemProvider;
import slimeknights.tconstruct.common.recipe.RecipeCacheInvalidator;
import slimeknights.tconstruct.library.client.armor.texture.ArmorTextureSupplier;
import slimeknights.tconstruct.library.client.armor.texture.DyedArmorTextureSupplier;
import slimeknights.tconstruct.library.client.armor.texture.FirstArmorTextureSupplier;
import slimeknights.tconstruct.library.client.armor.texture.FixedArmorTextureSupplier;
import slimeknights.tconstruct.library.client.armor.texture.MaterialArmorTextureSupplier;
import slimeknights.tconstruct.library.client.armor.texture.MaterialHasFallbackTextureSupplier;
import slimeknights.tconstruct.library.client.armor.texture.TrimArmorTextureSupplier;
import slimeknights.tconstruct.library.client.book.TinkerBook;
import slimeknights.tconstruct.library.client.data.spritetransformer.FramesSpriteTransformer;
import slimeknights.tconstruct.library.client.data.spritetransformer.GreyToColorMapping;
import slimeknights.tconstruct.library.client.data.spritetransformer.GreyToSpriteTransformer;
import slimeknights.tconstruct.library.client.data.spritetransformer.IColorMapping;
import slimeknights.tconstruct.library.client.data.spritetransformer.ISpriteTransformer;
import slimeknights.tconstruct.library.client.data.spritetransformer.OffsettingSpriteTransformer;
import slimeknights.tconstruct.library.client.data.spritetransformer.RecolorSpriteTransformer;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfoLoader;
import slimeknights.tconstruct.library.client.modifiers.ModifierIconManager;
import slimeknights.tconstruct.library.client.modifiers.model.DyedModifierModel;
import slimeknights.tconstruct.library.client.modifiers.model.ModifierModel;
import slimeknights.tconstruct.library.client.modifiers.model.NormalModifierModel;
import slimeknights.tconstruct.library.client.modifiers.model.fluid.FluidModifierModel;
import slimeknights.tconstruct.library.client.modifiers.model.fluid.TankModifierModel;
import slimeknights.tconstruct.library.client.modifiers.model.material.MaterialHasFallbackModifierModel;
import slimeknights.tconstruct.library.client.modifiers.model.material.MaterialModifierModel;
import slimeknights.tconstruct.library.client.modifiers.model.nested.CompoundModifierModel;
import slimeknights.tconstruct.library.client.modifiers.model.nested.ConditionalModifierModel;
import slimeknights.tconstruct.library.client.modifiers.model.nested.NestedModifierModel;
import slimeknights.tconstruct.library.client.modifiers.model.specialized.BannerModifierModel;
import slimeknights.tconstruct.library.client.modifiers.model.specialized.PotionModifierModel;
import slimeknights.tconstruct.library.client.modifiers.model.specialized.TrimModifierModel;
import slimeknights.tconstruct.tools.client.SlimeskullModifierModel;

import static slimeknights.tconstruct.TConstruct.getResource;

/**
 * This class should only be referenced on the client side
 */
@Mod(value = TConstruct.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = TConstruct.MOD_ID, value = Dist.CLIENT)
public class TinkerClient {
  /** Called during mod construct to handle any client side logic that needs to run during the constructor */
  public TinkerClient(IEventBus modEventBus) {
    TinkerBook.initBook();
    // needs to register listeners early enough for minecraft to load
    ModifierIconManager.init(modEventBus);
    MaterialRenderInfoLoader.init(modEventBus);

    // add the recipe cache invalidator to the client
    NeoForge.EVENT_BUS.addListener(RecipesUpdatedEvent.class, event -> RecipeCacheInvalidator.reload(true));

    // register datagen serializers
    ISpriteTransformer.SERIALIZER.registerDeserializer(RecolorSpriteTransformer.NAME, RecolorSpriteTransformer.DESERIALIZER);
    GreyToSpriteTransformer.init();
    ISpriteTransformer.SERIALIZER.registerDeserializer(OffsettingSpriteTransformer.NAME, OffsettingSpriteTransformer.DESERIALIZER);
    ISpriteTransformer.SERIALIZER.registerDeserializer(FramesSpriteTransformer.NAME, FramesSpriteTransformer.DESERIALIZER);
    IColorMapping.SERIALIZER.registerDeserializer(GreyToColorMapping.NAME, GreyToColorMapping.DESERIALIZER);

    // armor textures
    ArmorTextureSupplier.LOADER.register(getResource("fixed"), FixedArmorTextureSupplier.LOADER);
    ArmorTextureSupplier.LOADER.register(getResource("dyed"), DyedArmorTextureSupplier.LOADER);
    ArmorTextureSupplier.LOADER.register(getResource("first_present"), FirstArmorTextureSupplier.LOADER);
    ArmorTextureSupplier.LOADER.register(getResource("material"), MaterialArmorTextureSupplier.Material.LOADER);
    ArmorTextureSupplier.LOADER.register(getResource("persistent_data"), MaterialArmorTextureSupplier.PersistentData.LOADER);
    ArmorTextureSupplier.LOADER.register(getResource("trim"), TrimArmorTextureSupplier.LOADER);
    ArmorTextureSupplier.LOADER.register(getResource("material_has_fallback"), MaterialHasFallbackTextureSupplier.LOADER);

    // modifier models
    ModifierModel.LOADER.register(getResource("empty"), ModifierModel.EMPTY.getLoader());
    ModifierModel.LOADER.register(getResource("compound"), CompoundModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("conditional"), ConditionalModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("trait"), NestedModifierModel.Trait.LOADER);
    ModifierModel.LOADER.register(getResource("crafted"), NestedModifierModel.Crafted.LOADER);
    ModifierModel.LOADER.register(getResource("basic"), NormalModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("dyed"), DyedModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("material_index"), MaterialModifierModel.Index.LOADER);
    ModifierModel.LOADER.register(getResource("persistent_material"), MaterialModifierModel.PersistentData.LOADER);
    ModifierModel.LOADER.register(getResource("dyed_material"), MaterialModifierModel.Dyed.LOADER);
    ModifierModel.LOADER.register(getResource("potion"), PotionModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("armor_trim"), TrimModifierModel.Armor.LOADER);
    ModifierModel.LOADER.register(getResource("custom_trim"), TrimModifierModel.Custom.LOADER);
    ModifierModel.LOADER.register(getResource("banner"), BannerModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("fluid"), FluidModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("tank"), TankModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("material_has_fallback"), MaterialHasFallbackModifierModel.LOADER);
    // specialized
    ModifierModel.LOADER.register(getResource("slimeskull"), SlimeskullModifierModel.LOADER);
  }


  @SubscribeEvent
  static void gatherData(final GatherDataEvent event) {
    DataGenerator generator = event.getGenerator();
    PackOutput output = generator.getPackOutput();
    ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
    boolean client = event.includeClient();
    generator.addProvider(client, new ModelSpriteProvider(output, existingFileHelper));
    generator.addProvider(client, new TinkerSpriteSourceProvider(output, event.getLookupProvider(), existingFileHelper));
    generator.addProvider(client, new TinkerItemModelProvider(output, existingFileHelper));
    generator.addProvider(client, new TinkerBlockStateProvider(output, existingFileHelper));
    generator.addProvider(client, new RenderFluidProvider(output));
    generator.addProvider(client, new RenderItemProvider(output));
    generator.addProvider(client, new SimpleBlockStateProvider(output));
  }

  @SubscribeEvent
  static void renderBlockOverlay(RenderBlockScreenEffectEvent event) {
    BlockState state = event.getBlockState();
    if (state.is(TinkerTags.Blocks.TRANSPARENT_OVERLAY)) {
      Minecraft minecraft = Minecraft.getInstance();
      assert minecraft.level != null;
      assert minecraft.player != null;
      BlockPos pos = event.getBlockPos();
      float width = minecraft.player.getBbWidth() * 0.8F;
      // check collision of the block again, for non-full blocks
      if (Shapes.joinIsNotEmpty(state.getShape(minecraft.level, pos).move(pos.getX(), pos.getY(), pos.getZ()), Shapes.create(AABB.ofSize(minecraft.player.getEyePosition(), width, 1.0E-6D, width)), BooleanOp.AND)) {
        // this is for the most part a clone of the vanilla logic from ScreenEffectRenderer with some changes mentioned below

        TextureAtlasSprite texture = minecraft.getBlockRenderer().getBlockModelShaper().getTexture(state, minecraft.level, pos);
        RenderSystem.setShaderTexture(0, texture.atlasLocation());
        // changed: shader using pos tex
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        // change: handle brightness based on renderWater, and enable blend
        Player player = minecraft.player;
        BlockPos blockpos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        float brightness = LightTexture.getBrightness(player.level().dimensionType(), player.level().getMaxLocalRawBrightness(blockpos));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(brightness, brightness, brightness, 1.0f);

        // draw the quad
        float u0 = texture.getU0();
        float u1 = texture.getU1();
        float v0 = texture.getV0();
        float v1 = texture.getV1();
        Matrix4f matrix4f = event.getPoseStack().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        // change: dropped color, see above
        bufferbuilder.addVertex(matrix4f, -1, -1, -0.5f).setUv(u1, v1);
        bufferbuilder.addVertex(matrix4f, 1, -1, -0.5f).setUv(u0, v1);
        bufferbuilder.addVertex(matrix4f, 1, 1, -0.5f).setUv(u0, v0);
        bufferbuilder.addVertex(matrix4f, -1, 1, -0.5f).setUv(u1, v0);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        // changed: disable blend
        RenderSystem.disableBlend();
      }
      event.setCanceled(true);
    }
  }
}
