package slimeknights.tconstruct.library.tools.part;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.client.materials.MaterialTooltipCache;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.MaterialVariant;
import slimeknights.tconstruct.library.materials.MaterialVariantId;
import slimeknights.tconstruct.library.tools.helper.TooltipUtil;
import slimeknights.tconstruct.library.utils.Util;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents an item that has a Material associated with it. The NBT of the itemstack identifies which material the
 * itemstack of this item has.
 */
public class MaterialItem extends Item implements IMaterialItem {
  private static final String ADDED_BY = TConstruct.makeTranslationKey("tooltip", "part.added_by");

  public MaterialItem(Properties properties) {
    super(properties);
  }

  @Nullable
  private static Component getName(String baseKey, MaterialVariantId material) {
    // if there is a specific name, use that
    ResourceLocation location = material.getLocation('.');
    String fullKey = String.format("%s.%s.%s", baseKey, location.getNamespace(), location.getPath());
    if (Util.canTranslate(fullKey)) {
      return Component.translatable(fullKey);
    }
    // try material name prefix next
    String materialKey = MaterialTooltipCache.getKey(material);
    String materialPrefix = materialKey + ".format";
    if (Util.canTranslate(materialPrefix)) {
      return Component.translatable(materialPrefix, Component.translatable(baseKey));
    }
    // format as "<material> <item name>"
    if (Util.canTranslate(materialKey)) {
      return Component.translatable(TooltipUtil.KEY_FORMAT, Component.translatable(materialKey), Component.translatable(baseKey));
    }
    return null;
  }

  /** Static helper to get part name, used also by {@link slimeknights.tconstruct.library.tools.part.block.MaterialBlockItem} */
  public static Component getName(IMaterialItem self, ItemStack stack) {
    // if no material, return part name directly
    MaterialVariant material = self.getMaterial(stack);
    String key = self.asItem().getDescriptionId(stack);
    if (!material.isPresent()) {
      return Component.translatable(key);
    }
    // try variant first
    if (material.hasVariant()) {
      Component component = getName(key, material.id());
      if (component != null) {
        return component;
      }
    }
    // if variant did not work, do base material
    Component component = getName(key, material.material());
    if (component != null) {
      return component;
    }
    // if neither worked, format directly
    return Component.translatable(key);
  }

  @Override
  public Component getName(ItemStack stack) {
    return getName(this, stack);
  }

  @Override
  public Rarity getRarity(ItemStack stack) {
    return getMaterial(stack).get().getRarity();
  }

  @Override
  public boolean isFoil(ItemStack stack) {
    return MaterialRegistry.getInstance().isInTag(getMaterial(stack).material(), TinkerTags.Materials.SHINY);
  }

  /** Helper to append the material ID to the tool part. */
  public static void appendHoverText(IMaterialItem self, ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
    if (flag.isAdvanced() && !TooltipUtil.isDisplay(stack)) {
      MaterialVariant materialVariant = self.getMaterial(stack);
      if (materialVariant.isPresent()) {
        tooltip.add((Component.translatable(ToolPartItem.MATERIAL_KEY, materialVariant.toString())).withStyle(ChatFormatting.DARK_GRAY));
      }
    }
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    appendHoverText(this, stack, tooltip, flag);
  }

  /** Gets the creator mod ID based on the material. */
  public static String getCreatorModId(IMaterialItem self, ItemStack stack) {
    MaterialVariant material = self.getMaterial(stack);
    if (material.isPresent()) {
      String namespace = material.material().getNamespace();
      // skip if it's a tinkers material; we want addon tool parts to prefer showing their mod ID as end users mistake those for us
      if (!TConstruct.MOD_ID.equals(namespace)) {
        return namespace;
      }
    }
    return BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
  }

  @Nullable
  @Override
  public String getCreatorModId(ItemStack stack) {
    return getCreatorModId(this, stack);
  }
}
