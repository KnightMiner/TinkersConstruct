package slimeknights.tconstruct.library.client.modifiers.model;

import com.mojang.math.Transformation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.ItemLayerPixels;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.component.ResourceMap;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

/** Modifier model that copies dye from a key. */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class DyedModifierModel implements SimpleModifierModel {
  public static final RecordLoadable<DyedModifierModel> LOADER = SimpleModifierModel.loader(DyedModifierModel::new);

  /** Textures to show */
  @Nullable
  private final Material small;
  @Nullable
  private final Material large;

  @Override
  public RecordLoadable<? extends DyedModifierModel> getLoader() {
    return LOADER;
  }

  @Nullable
  @Override
  public Object getCacheKey(ToolStackView tool, ModifierEntry entry) {
    ModifierId modifier = entry.id();
    int color = ResourceMap.get(tool, TinkerData.MODIFIER_INTEGER).getOrDefault(entry.id(), -1);
    return new CacheKey(modifier, color);
  }

  @Override
  public void addQuads(ToolStackView tool, ModifierEntry modifier, Function<Material,TextureAtlasSprite> spriteGetter, Transformation transforms, boolean isLarge, int startTintIndex, Consumer<Collection<BakedQuad>> quadConsumer, @Nullable ItemLayerPixels pixels) {
    Material texture = isLarge ? large : small;
    if (texture != null) {
      Integer color = ResourceMap.get(tool, TinkerData.MODIFIER_INTEGER).get(modifier.id());
      if (color != null) {
        quadConsumer.accept(MantleItemLayerModel.getQuadsForSprite(0xFF000000 | color, -1, spriteGetter.apply(texture), transforms, 0, pixels));
      }
    }
  }

  /** Data class to cache a colored texture */
  private record CacheKey(ModifierId modifier, int color) {}
}
