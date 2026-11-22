package slimeknights.tconstruct.library.recipe.modifiers.adding.variant;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.client.materials.MaterialTooltipCache;
import slimeknights.tconstruct.library.materials.MaterialVariant;
import slimeknights.tconstruct.library.materials.MaterialVariantId;
import slimeknights.tconstruct.library.modifiers.ModifierHolder;
import slimeknights.tconstruct.library.tools.component.ResourceMap;

/** {@link SwappableVariant} using {@link TinkerData#MODIFIER_MATERIAL} to set {@link MaterialVariant} */
public record SwappableMaterial(MaterialVariant value) implements SwappableModifierMap<MaterialVariant> {
  public static final RecordLoadable<SwappableMaterial> LOADER = RecordLoadable.create(MaterialVariant.LOADABLE.requiredField("material", SwappableMaterial::value), SwappableMaterial::new);

  public SwappableMaterial(MaterialVariantId material) {
    this(MaterialVariant.create(material));
  }

  @Override
  public RecordLoadable<? extends GenericLoaderRegistry.IHaveLoader> getLoader() {
    return LOADER;
  }

  @Override
  public DataComponentType<ResourceMap<MaterialVariant>> component() {
    return TinkerData.MODIFIER_MATERIAL.get();
  }

  @Override
  public Component formatVariant(ModifierHolder modifier) {
    return MaterialTooltipCache.getDisplayName(value.id());
  }
}
