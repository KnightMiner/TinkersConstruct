package slimeknights.tconstruct.library.client.modifiers.model;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.loadable.record.SingletonLoader;
import slimeknights.mantle.data.registry.GenericLoaderRegistry;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;
import slimeknights.mantle.util.ItemLayerPixels;
import slimeknights.tconstruct.library.client.modifiers.ModifierModelLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

/** Represents a model defined for the given tool */
public interface ModifierModel extends IHaveLoader {
  ModifierModel EMPTY = SingletonLoader.singleton(loader -> new ModifierModel() {
    @Override
    public void addQuads(IToolStackView tool, ModifierEntry modifier, Function<Material, TextureAtlasSprite> spriteGetter, Transformation transforms, boolean isLarge, int startTintIndex, Consumer<Collection<BakedQuad>> quadConsumer, @Nullable ItemLayerPixels pixels) {}

    @Nullable
    @Override
    public Object getCacheKey(IToolStackView tool, ModifierEntry modifier) {
      return null;
    }

    @Override
    public RecordLoadable<? extends ModifierModel> getLoader() {
      return loader;
    }

    @Override
    public void validate(Function<Material, TextureAtlasSprite> spriteGetter) {}
  });

  /**
   * Loader for registering modifier models.
   * @see ModifierModelLoadable
   */
  GenericLoaderRegistry<ModifierModel> LOADER = new GenericLoaderRegistry<>("Modifier Model", EMPTY, false);
  /** Loadable for reading materials, a common feature of modifier models */
  StringLoadable<Material> MATERIAL_LOADABLE = Loadables.RESOURCE_LOCATION.flatXmap(ModifierModel::blockAtlas, Material::texture);

  @Override
  RecordLoadable<? extends ModifierModel> getLoader();


  /**
   * Gets the key to use for caching results from this modifier. Should uniquely represent this tool state for the given modifier
   * For most models, this can be just the modifier itself
   * @param tool      Tool
   * @param modifier  Modifier instance
   * @return  Cache key for the given data, or null to not cache anything
   */
  @Nullable
  default Object getCacheKey(IToolStackView tool, ModifierEntry modifier) {
    return modifier == ModifierEntry.EMPTY ? null : modifier.getId();
  }

  /**
   * Gets quads for the given model. These quads should not be cached as that will make them inconsistent with {@link ItemLayerPixels}.
   * @param tool             Tool instance for modifier sensitive models
   * @param modifier         Modifier being rendered
   * @param spriteGetter     Function to fetch sprites
   * @param transforms       Transforms
   * @param isLarge          If true, use the large sprites and quads
   * @param startTintIndex   First tint index that can be used for this model. Use with {@link #getTintIndexes()} and {@link #getTint(IToolStackView, ModifierEntry, int)}, if neither is used this index will not work
   * @param quadConsumer     Consumer accepting modifier quads. Note you are expected to add quads in top down order, that is whatever you want visible should be added first.
   * @param pixels           Item layer pixels to reduce z-fighting. Pass into methods from {@link slimeknights.mantle.client.model.util.MantleItemLayerModel}
   */
  void addQuads(IToolStackView tool, ModifierEntry modifier, Function<Material,TextureAtlasSprite> spriteGetter, Transformation transforms, boolean isLarge, int startTintIndex, Consumer<Collection<BakedQuad>> quadConsumer, @Nullable ItemLayerPixels pixels);

  /** {@return  Number of tint indexes used by this model} */
  default int getTintIndexes() {
    return 0;
  }

  /**
   * Gets the color to tint this model. In general its better to use {@link slimeknights.mantle.client.model.util.MantleItemLayerModel} as a baked color will be faster. Only use if you need dynamic colors
   * @param tool   Tool stack instance
   * @param entry  Modifier entry representing the relevant modifier
   * @param index  Localized tint index for this modifier, starting from 0. Only considers tint indexes this model handles as per {@link #getTintIndexes()}
   * @return  Color for this quad
   */
  default int getTint(IToolStackView tool, ModifierEntry entry, int index) {
    return -1;
  }

  /** Validates that all textures in this model exist. */
  void validate(Function<Material, TextureAtlasSprite> spriteGetter);

  /** Fetches a path from the block atlas */
  static Material blockAtlas(ResourceLocation path) {
    return new Material(InventoryMenu.BLOCK_ATLAS, path);
  }
}
