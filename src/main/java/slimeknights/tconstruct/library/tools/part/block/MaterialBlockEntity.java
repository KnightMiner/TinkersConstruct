package slimeknights.tconstruct.library.tools.part.block;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import slimeknights.mantle.block.entity.MantleBlockEntity;
import slimeknights.mantle.util.RetexturedHelper;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.client.model.ModelProperties;
import slimeknights.tconstruct.library.materials.MaterialHolder;
import slimeknights.tconstruct.library.materials.MaterialId;
import slimeknights.tconstruct.library.materials.MaterialVariant;
import slimeknights.tconstruct.library.materials.MaterialVariantId;
import slimeknights.tconstruct.tools.TinkerToolParts;

import javax.annotation.Nonnull;
import java.util.Objects;

/** Block entity logic for {@link MaterialBlock} */
public class MaterialBlockEntity extends MantleBlockEntity {
  /** Tag used in NBT for the material ID */
  public static final String MATERIAL_TAG = "material";

  @Nonnull
  @Getter
  private MaterialVariant material = MaterialHolder.UNKNOWN;

  /** Constructor for addons to register a new block entity for their material blocks */
  public MaterialBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  /** Constructor for our material blocks. */
  public MaterialBlockEntity(BlockPos pos, BlockState state) {
    this(TinkerToolParts.materialBlock.get(), pos, state);
  }

  @Override
  public ModelData getModelData() {
    return ModelData.builder().with(ModelProperties.MATERIAL, material.id()).build();
  }

  /** Called to update the material on the block. */
  private void setMaterial(MaterialVariant material) {
    MaterialVariant oldMaterial = this.material;
    this.material = material;
    if (!oldMaterial.equals(material)) {
      setChangedFast();
      RetexturedHelper.onTextureUpdated(this);
    }
  }

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  protected void collectImplicitComponents(DataComponentMap.Builder components) {
    super.collectImplicitComponents(components);
    if (material.isPresent()) {
      components.set(TinkerData.MATERIAL, material);
    }
  }

  @Override
  protected void applyImplicitComponents(DataComponentInput components) {
    super.applyImplicitComponents(components);
    setMaterial(components.getOrDefault(TinkerData.MATERIAL, MaterialHolder.UNKNOWN));
  }

  @Override
  protected void saveSynced(CompoundTag tags, HolderLookup.Provider registries) {
    super.saveSynced(tags, registries);
    if (material.isPresent()) {
      tags.putString(MATERIAL_TAG, material.id().toString());
    }
  }

  @Override
  public void loadAdditional(CompoundTag tags, HolderLookup.Provider registries) {
    super.loadAdditional(tags, registries);
    if (tags.contains(MATERIAL_TAG, Tag.TAG_STRING)) {
      material = MaterialVariant.create(Objects.requireNonNullElse(MaterialVariantId.tryParse(tags.getString(MATERIAL_TAG)), MaterialId.UNKNOWN));
      RetexturedHelper.onTextureUpdated(this);
    }
  }
}
