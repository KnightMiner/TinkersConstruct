package slimeknights.tconstruct.library.materials.stats;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.tconstruct.library.materials.IMaterialUser;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.resource.ResourceId;
import slimeknights.tconstruct.library.resource.ResourceIdLoadable;
import slimeknights.tconstruct.library.utils.SimpleCache;

import javax.annotation.Nullable;

/**
 * {@link ResourceId} for {@link IMaterialStats}
 */
public record MaterialStatsId(ResourceLocation id) implements ResourceId<IMaterialStats>, IMaterialUser {
  private static final SimpleCache<ResourceLocation,MaterialStatsId> CACHE = new SimpleCache<>(MaterialStatsId::new);
  /** {@link slimeknights.mantle.data.loadable.Loadable} instance. */
  public static final ResourceIdLoadable<MaterialStatsId> LOADABLE = new ResourceIdLoadable<>(CACHE);

  /** @apiNote use {@link #create(ResourceLocation)} */
  @Internal
  public MaterialStatsId {}

  /** Checks if the given material can be used */
  @Override
  public boolean canUseMaterial(MaterialId material) {
    return MaterialRegistry.getInstance().getMaterialStats(material.material(), this).isPresent();
  }

  @Override
  public String toString() {
    return id.toString();
  }


  /* Constructing */

  /** Parses a pattern from the given string */
  public static MaterialStatsId create(ResourceLocation id) {
    return CACHE.apply(id);
  }

  /** {@return Material Stats ID} */
  public static MaterialStatsId create(String domain, String name) {
    return create(ResourceLocation.fromNamespaceAndPath(domain, name));
  }

  /** {@return Material Stats ID, or throws if invalid} */
  public static MaterialStatsId create(String string) {
    return create(ResourceLocation.parse(string));
  }

  /** {@return Material Stats ID, or null if invalid} */
  @Nullable
  public static MaterialStatsId tryParse(String string) {
    return LOADABLE.tryParse(string);
  }
}
