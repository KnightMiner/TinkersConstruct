package slimeknights.tconstruct.library.materials.definition;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.ApiStatus.Internal;

/** Base {@link IMaterial} implementation. */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Material implements IMaterial {
  /** Default white color */
  protected static final TextColor WHITE = TextColor.fromRgb(0xFFFFFF);

  /** This resource location uniquely identifies a material. */
  private final MaterialId id;
  /** Materials tier, mostly for sorting right now */
  private final int tier;
  /** Materials order within the tier, for sorting */
  private final int sortOrder;

  /** Rarity for this material's name */
  private final Rarity rarity;
  /** Material can be crafted into parts in the PartBuilder */
  private final boolean craftable;
  /** if true, this material is hidden */
  private final boolean hidden;

  /**
   * Materials should only be created by the MaterialManager, except when used for data gen
   * They're synced over the network and other classes might lead to unexpected behaviour.
   */
  @Internal
  public Material(ResourceLocation id, int tier, int order, Rarity rarity, boolean craftable, boolean hidden) {
    this(MaterialId.create(id), tier, order, rarity, craftable, hidden);
  }

  @Override
  public String toString() {
    return "Material{" + id + '}';
  }
}
