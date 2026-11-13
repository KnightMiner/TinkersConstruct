package slimeknights.tconstruct.world.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.SkullBlock.Type;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.Map;

/** Generics do not match to use the vanilla armor layer, so this is a reimplementation of some of {@link HumanoidArmorLayer} */
public class SlimeArmorLayer<T extends Slime, M extends HierarchicalModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T,M> {
  private final A armorModel;
  private final TextureAtlas armorTrimAtlas;
  public final Map<Type,SkullModelBase> skullModels;
  private final boolean lavaSlime;

  public SlimeArmorLayer(RenderLayerParent<T,M> pRenderer, A armorModel, EntityModelSet modelSet, ModelManager modelManager, boolean lavaSlime) {
    super(pRenderer);
    this.armorModel = armorModel;
    this.skullModels = SkullBlockRenderer.createSkullRenderers(modelSet);
    this.armorTrimAtlas = modelManager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
    this.lavaSlime = lavaSlime;
  }

  /** Based on {@link HumanoidArmorLayer} and {@link CustomHeadLayer} */
  @Override
  public void render(PoseStack matrices, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float age, float headYaw, float headPitch) {
    ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
    if (!helmet.isEmpty()) {
      matrices.pushPose();
      if (lavaSlime) {
        float squish = Mth.lerp(partialTicks, entity.oSquish, entity.squish);
        if (squish < 0) {
          squish = 0;
        }
        matrices.translate(0, 1.5 - 0.425 * squish, 0);
      } else {
        matrices.translate(0, 1.5, 0);
      }
      matrices.scale(0.9f, 0.9f, 0.9f);

      Item item = helmet.getItem();
      // helmet renderer, based on humanoid armor layer
      if (item instanceof ArmorItem armor && armor.getType() == ArmorItem.Type.HELMET) {
        this.getParentModel().copyPropertiesTo(armorModel);
        armorModel.setAllVisible(false);
        armorModel.head.visible = true;
        armorModel.hat.visible = true;
        IClientItemExtensions extensions = IClientItemExtensions.of(helmet);
        Model model = extensions.getGenericArmorModel(entity, helmet, EquipmentSlot.HEAD, armorModel);
        extensions.setupModelAnimations(entity, helmet, EquipmentSlot.HEAD, model, limbSwing, limbSwingAmount, partialTicks, age, headYaw, headPitch);

        Holder<ArmorMaterial> materialHolder = armor.getMaterial();
        ArmorMaterial material = materialHolder.value();
        int fallbackColor = extensions.getDefaultDyeColor(helmet);

        // render each texture layer
        for (int layerIdx = 0; layerIdx < material.layers().size(); layerIdx++) {
          ArmorMaterial.Layer layer = material.layers().get(layerIdx);
          int tint = extensions.getArmorLayerTintColor(helmet, entity, layer, layerIdx, fallbackColor);
          if (tint != 0) {
            VertexConsumer consumer = buffer.getBuffer(RenderType.armorCutoutNoCull(ClientHooks.getArmorTexture(entity, helmet, layer, false, EquipmentSlot.HEAD)));
            model.renderToBuffer(matrices, consumer, packedLight, OverlayTexture.NO_OVERLAY, tint);
          }
        }

        // render trim
        ArmorTrim trim = helmet.get(DataComponents.TRIM);
        if (trim != null) {
          TextureAtlasSprite sprite = this.armorTrimAtlas.getSprite(trim.outerTexture(materialHolder));
          VertexConsumer vertexconsumer = sprite.wrap(buffer.getBuffer(Sheets.armorTrimsSheet(trim.pattern().value().decal())));
          model.renderToBuffer(matrices, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
        }

        // render enchanted glint
        if (helmet.hasFoil()) {
          model.renderToBuffer(matrices, buffer.getBuffer(RenderType.armorEntityGlint()), packedLight, OverlayTexture.NO_OVERLAY);
        }
      } else {
        // block model renderer, based on custom head layer

        // skull block rendering
        if (item instanceof BlockItem block && block.getBlock() instanceof AbstractSkullBlock skullBlock) {
          matrices.scale(1.1875F, -1.1875F, -1.1875F);
          ResolvableProfile profile = helmet.get(DataComponents.PROFILE);
          matrices.translate(-0.5, 0.0, -0.5);
          SkullBlock.Type type = skullBlock.getType();
          SkullModelBase skullModel = this.skullModels.get(type);
          RenderType renderType = SkullBlockRenderer.getRenderType(type, profile);
          SkullBlockRenderer.renderSkull(null, 180.0F, limbSwing, matrices, buffer, packedLight, skullModel, renderType);
        } else {
          // standard rendering
          CustomHeadLayer.translateToHead(matrices, false);
          Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().renderItem(entity, helmet, ItemDisplayContext.HEAD, false, matrices, buffer, packedLight);
        }
      }
      matrices.popPose();
    }
  }
}
