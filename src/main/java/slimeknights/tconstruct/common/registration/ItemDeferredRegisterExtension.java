package slimeknights.tconstruct.common.registration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import slimeknights.mantle.registration.deferred.EnumDeferredRegister;
import slimeknights.mantle.registration.deferred.ItemDeferredRegister;
import slimeknights.tconstruct.common.registration.object.CastItemObject;
import slimeknights.tconstruct.common.registration.object.EnumItem;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.library.tools.part.PartCastItem;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/** Extension of {@link ItemDeferredRegister} with additional helpers for casts and enum items. */
public class ItemDeferredRegisterExtension extends ItemDeferredRegister {
  public ItemDeferredRegisterExtension(String modID) {
    super(modID);
  }

  /**
   * Registers a set of three cast items at once
   * @param name         Base name of cast
   * @param constructor  Item constructor
   * @return  Object containing casts
   */
  public CastItemObject registerCast(String name, Supplier<? extends Item> constructor) {
    DeferredItem<Item> cast = register(name + "_cast", constructor);
    DeferredItem<Item> sandCast = register(name + "_sand_cast", constructor);
    DeferredItem<Item> redSandCast = register(name + "_red_sand_cast", constructor);
    return new CastItemObject(resource(name), cast, sandCast, redSandCast);
  }

  /**
   * Registers a set of three cast items at once
   * @param name   Base name of cast
   * @param props  Item properties
   * @return  Object containing casts
   */
  public CastItemObject registerCast(String name, Item.Properties props) {
    return registerCast(name, () -> new Item(props));
  }

  /**
   * Registers a set of three cast items at once using the part item cast
   * @param item   Part item base for the cast
   * @param props  Item properties
   * @return  Object containing casts
   */
  public CastItemObject registerCast(DeferredHolder<Item,? extends IMaterialItem> item, Item.Properties props) {
    return registerCast(item.getId().getPath(), () -> new PartCastItem(props, item));
  }


  /* ItemObject */

  /** Same as {@link EnumDeferredRegister#registerEnum(String, Enum[], BiFunction)} except the result is a typed {@link EnumItem} */
  public <E extends Enum<E>, T extends Item> EnumItem<E,T> registerEnumItem(String name, E[] values, BiFunction<ResourceLocation, E, ? extends T> mapper) {
    return new EnumItem<>(registerEnum(name, values, mapper));
  }

  /** Same as {@link EnumDeferredRegister#registerEnum(Enum[], String, BiFunction)} except the result is a typed {@link EnumItem} */
  public <E extends Enum<E>, T extends Item> EnumItem<E,T> registerEnumItem(E[] values, String name, BiFunction<ResourceLocation, E, ? extends T> mapper) {
    return new EnumItem<>(registerEnum(values, name, mapper));
  }

  /** Same as {@link EnumDeferredRegister#registerEnum(String, Enum[], Function)} except the result is a typed {@link EnumItem} */
  public <E extends Enum<E>, T extends Item> EnumItem<E,T> registerEnumItem(String name, E[] values, Function<E, ? extends T> mapper) {
    return new EnumItem<>(registerEnum(name, values, mapper));
  }

  /** Same as {@link EnumDeferredRegister#registerEnum(Enum[], String, Function)} except the result is a typed {@link EnumItem} */
  public  <E extends Enum<E>, T extends Item> EnumItem<E,T> registerEnumItem(E[] values, String name, Function<E, ? extends T> mapper) {
    return new EnumItem<>(registerEnum(values, name, mapper));
  }
}
