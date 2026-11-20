package slimeknights.tconstruct.library.tools.part;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.materials.IMaterialUser;
import slimeknights.tconstruct.library.materials.MaterialHolder;
import slimeknights.tconstruct.library.materials.MaterialId;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.MaterialVariant;
import slimeknights.tconstruct.library.materials.MaterialVariantId;

import java.util.function.Consumer;

/**
 * Items implementing this interface contain a material
 */
public interface IMaterialItem extends ItemLike, IMaterialUser {
  /**
   * Returns the material ID of the part this itemstack holds.
   *
   * @return Material ID or {@link MaterialHolder#UNKNOWN} if invalid
   */
  default MaterialVariant getMaterial(ItemStack stack) {
    // TODO: do we need this as an overridable method, or can we make it static?
    return stack.getOrDefault(TinkerData.MATERIAL, MaterialHolder.UNKNOWN);
  }

  /** Sets the material on the existing stack. */
  default ItemStack setMaterial(ItemStack stack, MaterialVariant material) {
    if (canUseMaterial(material.holder())) {
      return setMaterialForced(stack, material);
    } else {
      stack.remove(TinkerData.MATERIAL);
    }
    return stack;
  }

  /** Sets the material on the existing stack, bypassing the valid material check. */
  default ItemStack setMaterialForced(ItemStack stack, MaterialVariant material) {
    if (material.isPresent()) {
      stack.set(TinkerData.MATERIAL, material);
    } else {
      stack.remove(TinkerData.MATERIAL);
    }
    return stack;
  }

  /** Returns the item with the given material, bypassing material validation */
  @NonExtendable
  default ItemStack withMaterialForDisplay(MaterialVariant material) {
    // TODO 1.21: ditch this in favor of setMaterialForDisplay?
    return setMaterialForced(new ItemStack(this), material);
  }

  /** Returns the item with the given material, bypassing material validation */
  @NonExtendable
  default ItemStack withMaterialForDisplay(MaterialVariantId material) {
    return withMaterialForDisplay(MaterialVariant.create(material));
  }

  /** Returns the item with the given material, validating it */
  @NonExtendable
  default ItemStack withMaterial(MaterialVariant material) {
    return setMaterial(new ItemStack(this), material);
  }

  @Deprecated
  @Override
  default boolean canUseMaterial(MaterialId material) {
    return true;
  }

  @Override
  default boolean canUseMaterial(MaterialHolder material) {
    return canUseMaterial(material.id());
  }

  /** Adds all variants of the material item to the given item stack list */
  default void addVariants(Consumer<ItemStack> items, String showOnlyMaterial) {
    if (MaterialRegistry.isFullyLoaded()) {
      // TODO: filter is not the best for the different material stat types
      // if a specific material is set in the config, try adding that as search tab only
      boolean added = false;
      if (!showOnlyMaterial.isEmpty()) {
        MaterialVariantId materialId = MaterialVariantId.tryParse(showOnlyMaterial);
        if (materialId != null) {
          MaterialVariant material = MaterialVariant.create(materialId);
          if (canUseMaterial(material.holder())) {
            items.accept(this.withMaterialForDisplay(material));
            added = true;
          }
        }
      }
      // add all applicable materials to the tab, and possibly to serach
      if (!added) {
        for (MaterialHolder material : MaterialRegistry.getVisibleMaterials()) {
          if (this.canUseMaterial(material)) {
            items.accept(this.withMaterial(material));
            // if filter is set we wanted just the 1 item
            if (!showOnlyMaterial.isEmpty()) {
              break;
            }
          }
        }
      }
    }
  }

  /**
   * Gets the material from a given item stack
   * @param stack  Item stack containing a material item
   * @return  Material, or unknown if none
   */
  static MaterialVariant getMaterialFromStack(ItemStack stack) {
    if (stack.getItem() instanceof IMaterialItem materialItem) {
      return materialItem.getMaterial(stack);
    }
    return MaterialHolder.UNKNOWN;
  }

  /**
   * Gets the given item stack with this material applied
   * @param item      Item instance
   * @param material  Material
   * @return  Stack with material, or original stack if not a material item
   */
  static ItemStack withMaterial(ItemLike item, MaterialVariant material) {
    if (item.asItem() instanceof IMaterialItem materialItem) {
      return materialItem.withMaterial(material);
    }
    return ItemStack.EMPTY;
  }

  /**
   * Gets the given item stack with this material applied
   * @param stack     Stack instance
   * @param material  Material
   * @return  Stack with material, or original stack if not a material item
   */
  static ItemStack withMaterial(ItemStack stack, MaterialVariant material) {
    Item item = stack.getItem();
    if (item instanceof IMaterialItem materialItem) {
      return materialItem.setMaterial(stack.copy(), material);
    }
    // TODO: return empty if not a material item?
    return stack;
  }


  /** Material item instance to use as a fallback to ensure a nonnull material item in some contexts */
  IMaterialItem EMPTY = new IMaterialItem() {
    @Override
    public Item asItem() {
      return Items.AIR;
    }

    @Override
    public MaterialVariant getMaterial(ItemStack stack) {
      return MaterialHolder.UNKNOWN;
    }

    @Deprecated
    @Override
    public boolean canUseMaterial(MaterialId mat) {
      return false;
    }

    @Override
    public boolean canUseMaterial(MaterialHolder material) {
      return false;
    }
  };
}
