package slimeknights.tconstruct.common.registration.object;

import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.mantle.registration.object.MultiObject;

import java.util.Collection;
import java.util.List;

/**
 * Deferred wrapper holding gold, sand, and red sand casts
 */
@Getter
public class CastItemObject extends ItemObject<Item,Item> implements MultiObject.Holders<Item> {
  private final ResourceLocation name;
  private final Holder<Item> sandHolder;
  private final Holder<Item> redSandHolder;
  private final TagKey<Item> singleUseTag;
  private final TagKey<Item> multiUseTag;

  public CastItemObject(ResourceLocation name, Holder<Item> gold, Holder<Item> sand, Holder<Item> redSand) {
    super(gold);
    this.name = name;
    this.sandHolder = sand;
    this.redSandHolder = redSand;
    this.singleUseTag = makeTag("single_use");
    this.multiUseTag = makeTag("multi_use");
  }

  /**
   * Gets the single use tag for this object
   * @return  Single use tag
   */
  protected TagKey<Item> makeTag(String type) {
    return TagKey.create(Registries.ITEM, name.withPrefix("casts/" + type + '/'));
  }

  /**
   * Gets the yellow sand variant
   * @return  Yellow sand variant
   */
  public Item getSand() {
    return sandHolder.value();
  }

  /**
   * Gets the red sand variant
   * @return  Red sand variant
   */
  public Item getRedSand() {
    return redSandHolder.value();
  }

  @Override
  public Collection<Holder<Item>> holders() {
    return List.of(holder, sandHolder, redSandHolder);
  }
}
