package slimeknights.tconstruct.library.client.armor.texture;

import com.google.common.collect.ImmutableSet;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.loadable.array.ArrayLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfoLoader;
import slimeknights.tconstruct.library.materials.MaterialId;
import slimeknights.tconstruct.library.materials.MaterialVariantId;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/** Texture supplier that runs the nested supplier if the material at the given index has the given fallback in its render info. */
@RequiredArgsConstructor
public class MaterialHasFallbackTextureSupplier implements ArmorTextureSupplier, Function<MaterialVariantId, Boolean> {
  public static final RecordLoadable<MaterialHasFallbackTextureSupplier> LOADER = RecordLoadable.create(
    IntLoadable.FROM_ZERO.requiredField("index", m -> m.index),
    StringLoadable.DEFAULT.set(ArrayLoadable.COMPACT).requiredField("fallback", m -> m.fallback),
    ArmorTextureSupplier.LOADER.requiredField("apply", m -> m.apply),
    MaterialHasFallbackTextureSupplier::new);

  /** Material index on the tool */
  private final int index;
  /** Set of fallback options, if any exist in the render info than {@code apply} is used */
  private final Set<String> fallback;
  /** Texture to apply if conditions are met. */
  private final ArmorTextureSupplier apply;

  /** Cache of the predicate for each seen material */
  private final Map<MaterialVariantId,Boolean> cache = new HashMap<>();

  public MaterialHasFallbackTextureSupplier(int index, ArmorTextureSupplier apply, String... fallback) {
    this(index, ImmutableSet.copyOf(fallback), apply);
  }

  @Override
  public RecordLoadable<? extends MaterialHasFallbackTextureSupplier> getLoader() {
    return LOADER;
  }

  /** Logic to compute a given material if it's not present in the cache */
  @Override
  public Boolean apply(MaterialVariantId material) {
    if (!MaterialId.UNKNOWN.equals(material)) {
      return MaterialRenderInfoLoader.INSTANCE.hasFallback(material, fallback);
    }
    return false;
  }

  @Override
  public ArmorTexture getArmorTexture(ItemStack stack, TextureType type, RegistryAccess access) {
    if (cache.computeIfAbsent(MaterialNBT.get(stack, index).id(), this)) {
      return apply.getArmorTexture(stack, type, access);
    }
    return ArmorTexture.EMPTY;
  }
}
