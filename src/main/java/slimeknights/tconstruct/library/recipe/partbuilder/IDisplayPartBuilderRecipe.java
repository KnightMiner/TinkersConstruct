package slimeknights.tconstruct.library.recipe.partbuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;

import javax.annotation.Nullable;
import java.util.List;

/** Part builder recipes that can show in JEI. */
public interface IDisplayPartBuilderRecipe {
  /** Gets the title to display above the recipe. If null, will try dynamically making the title based on the displayed material. */
  @Nullable
  default Component getDisplayTitle() {
    return null;
  }

  /** Gets the tooltip for the title in JEI */
  default List<Component> getTooltip() {
    return List.of();
  }

  /** Gets the list of material inputs for this recipe. Will be focus linked to the output if the sizes match. May be empty for non-material recipes. */
  default List<MaterialVariant> getMaterials() {
    return List.of();
  }

  /** Gets a list of input items for display in the material slot, typically material items. */
  List<ItemStack> getMaterialItems();

  /** Gets the list of patterns for display on this recipe. */
  List<Pattern> getPatterns();

  /**
   * Gets a list of pattern items to display in the pattern slot
   * @return  Pattern items
   */
  @SuppressWarnings("deprecation")
  default List<ItemStack> getPatternItems() {
    return RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, TinkerTags.Items.DEFAULT_PATTERNS).map(ItemStack::new).toList();
  }

  /** Gets a list of results. Should either be size 1, or size matching {@link #getMaterialItems()} */
  List<ItemStack> getResultItems();

  /**
   * Gets the number of material needed for this recipe
   * @return  Material amount
   */
  int getCost();


  /* Dynamic focus */

  /** Gets a list of hidden input items that act as focuses to this recipe, allowing them to be responded to in the below methods. */
  default List<ItemStack> getHiddenInputs() {
    return List.of();
  }

  /** Gets a list of hidden output items that act as focuses to this recipe, allowing them to be responded to in the below methods. */
  default List<ItemStack> getHiddenOutputs() {
    return List.of();
  }

  /**
   * Gets the materials to display subject to the given focuses
   * @param focusMaterial  Currently focused material. Will only ever be input. Will be {@link MaterialVariant#isEmpty()} if not focused.
   * @param focusStack     Currently focused item stack. Will be {@link ItemStack#isEmpty()} if not focused.
   * @param focusOutput    If true, focus is an output. If false, focus is an input.
   * @return  List of materials subject to the current focus.
   * @see #getMaterials()
   */
  default List<MaterialVariant> getMaterials(MaterialVariant focusMaterial, ItemStack focusStack, boolean focusOutput) {
    return getMaterials();
  }

  /**
   * Gets the material items to display subject to the given focuses.
   * @param focusMaterial  Currently focused material. Will only ever be input. Will be {@link MaterialVariant#isEmpty()} if not focused.
   * @param focusStack     Currently focused item stack. Will be {@link ItemStack#isEmpty()} if not focused.
   * @param focusOutput    If true, focus is an output. If false, focus is an input.
   * @return  List of material items subject to the current focus.
   * @see #getMaterialItems()
   */
  default List<ItemStack> getMaterialItems(MaterialVariant focusMaterial, ItemStack focusStack, boolean focusOutput) {
    return getMaterialItems();
  }

  /**
   * Gets the result items to display subject to the given focuses
   * @param focusMaterial  Currently focused material. Will only ever be input. Will be {@link MaterialVariant#isEmpty()} if not focused.
   * @param focusStack     Currently focused item stack. Will be {@link ItemStack#isEmpty()} if not focused.
   * @param focusOutput    If true, focus is an output. If false, focus is an input.
   * @return  List of results subject to the current focus.
   * @see #getResultItems()
   */
  default List<ItemStack> getResultItems(MaterialVariant focusMaterial, ItemStack focusStack, boolean focusOutput) {
    return getResultItems();
  }
}
