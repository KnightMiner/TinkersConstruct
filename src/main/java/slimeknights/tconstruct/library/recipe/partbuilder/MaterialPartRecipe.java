package slimeknights.tconstruct.library.recipe.partbuilder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.BooleanLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.MultiNamedRecipe;
import slimeknights.mantle.recipe.NamedRecipe;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.tconstruct.library.json.TinkerLoadables;
import slimeknights.tconstruct.library.json.field.MergingField;
import slimeknights.tconstruct.library.json.field.MergingField.MissingMode;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.library.recipe.material.IMaterialValue;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic to make a tool part from material items in the part builder.
 * @see ItemPartRecipe
 * @see slimeknights.tconstruct.library.recipe.casting.material.MaterialCastingRecipe
 * @see MaterialPartRecipeBuilder
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class MaterialPartRecipe implements IPartBuilderRecipe, MultiNamedRecipe<IDisplayPartBuilderRecipe> {
  public static final RecordLoadable<MaterialPartRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.RECIPE_GROUP,
    Pattern.PARSER.requiredField("pattern", MaterialPartRecipe::getPattern),
    Loadables.ITEM_INGREDIENT_NONEMPTY.defaultField("pattern_item", DEFAULT_PATTERNS, r -> r.patternItem),
    IntLoadable.FROM_ONE.requiredField("cost", MaterialPartRecipe::getCost),
    BooleanLoadable.INSTANCE.defaultField("allow_uncraftable", false, false, r -> r.allowUncraftable),
    new MergingField<>(TinkerLoadables.MATERIAL_ITEM.requiredField("item", r -> r.output), "result", MissingMode.DISALLOWED),
    new MergingField<>(IntLoadable.FROM_ONE.defaultField("count", 1, r -> r.outputCount), "result", MissingMode.CREATE),
    MaterialPartRecipe::new);

  @Getter
  protected final String group;
  @Getter
  protected final Pattern pattern;
  protected final Ingredient patternItem;
  /** Recipe material cost */
  @Getter
  protected final int cost;
  /** If true, this recipe can craft items normally not craftable in the part builder */
  @Getter
  @Accessors(fluent = true)
  protected final boolean allowUncraftable;
  /** Recipe result, used to fetch a material */
  protected final IMaterialItem output;
  /** Count for the recipe output */
  protected final int outputCount;

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerRecipes.materialPartBuilder.get();
  }

  @Override
  public boolean partialMatch(PartBuilderInput inv) {
    // first, must have a pattern
    if (!patternItem.test(inv.getPatternStack())) {
      return false;
    }
    // if there is a material item, it must have a valid material and be craftable
    ItemStack stack = inv.getItem();
    if (!stack.isEmpty()) {
      // no sense allowing if there is no change
      if (stack.getItem() == output) {
        return false;
      }
      IMaterialValue materialRecipe = inv.getMaterial();
      if (materialRecipe == null) {
        return false;
      }
      MaterialVariant material = materialRecipe.getMaterial();
      return (allowUncraftable || material.get().isCraftable()) && output.canUseMaterial(material.getId());
    }
    // no material item? return match in case we get one later
    return true;
  }

  /**
   * Checks if the recipe is valid for the given input. Assumes {@link #partialMatch(PartBuilderInput)} is true
   * @param inv    Inventory instance
   * @param world  World instance
   * @return  True if this recipe matches
   */
  @Override
  public boolean matches(PartBuilderInput inv, Level world) {
    // must have a material
    IMaterialValue materialRecipe = inv.getMaterial();
    if (materialRecipe != null) {
      // material must be craftable, usable in the item, and have a cost we can afford
      MaterialVariant material = materialRecipe.getMaterial();
      return (allowUncraftable || material.get().isCraftable()) && output.canUseMaterial(material.getId())
             && inv.getItem().getCount() >= materialRecipe.getItemsUsed(cost);
    }
    return false;
  }

  /** @deprecated use {@link #getRecipeOutput(MaterialVariantId, int)} */
  @Deprecated
  @Override
  public ItemStack getResultItem(HolderLookup.Provider registries) {
    return new ItemStack(output);
  }

  /**
   * Gets the output of the recipe for display
   * @param material  Material to use
   * @return  Output of the recipe
   */
  @SuppressWarnings("WeakerAccess")
  public ItemStack getRecipeOutput(MaterialVariantId material, int count) {
    ItemStack stack = output.withMaterial(material);
    stack.setCount(count);
    return stack;
  }

  @Override
  public ItemStack assemble(PartBuilderInput inv, HolderLookup.Provider registries) {
    MaterialVariant material = MaterialVariant.UNKNOWN;
    int count = outputCount;
    IMaterialValue materialRecipe = inv.getMaterial();
    if (materialRecipe != null) {
      material = materialRecipe.getMaterial();
      // if no leftover, give them more parts provided we have the patterns for it
      int value = materialRecipe.getValue();
      if (!materialRecipe.hasLeftover() && value > cost) {
        count = outputCount * value / cost;
      }
    }
    return this.getRecipeOutput(material.getVariant(), count);
  }

  /** Cache of recipes for display in JEI */
  @Nullable
  private List<NamedRecipe<IDisplayPartBuilderRecipe>> multiRecipes;

  @Override
  public List<NamedRecipe<IDisplayPartBuilderRecipe>> getRecipes(ResourceLocation id, HolderLookup.Provider registries) {
    if (multiRecipes == null) {
      // start building the recipe
      List<MaterialVariant> materials = new ArrayList<>();
      List<ItemStack> materialItems = new ArrayList<>();
      List<ItemStack> hiddenInputs = new ArrayList<>();
      List<ItemStack> resultItems = new ArrayList<>();

      // iterate materials to generate recipes
      for (IMaterial material : MaterialRegistry.getMaterials()) {
        // require the material to be craftable and valid for this part
        if ((allowUncraftable || material.isCraftable()) && output.canUseMaterial(material)) {
          List<MaterialVariantId> variants = MaterialRecipeCache.getVariants(material.getIdentifier())
            .stream().filter(variant -> !MaterialRecipeCache.getRecipes(variant).isEmpty()).toList();
          if (variants.isEmpty()) continue;

          // if the size is 1, add the variant itself as display variant
          MaterialVariantId displayId;
          MaterialVariant displayVariant;
          List<ItemStack> newItems = new ArrayList<>();
          if (variants.size() == 1) {
            displayId = variants.get(0);
            displayVariant = MaterialVariant.of(displayId);

            // process items
            MaterialRecipeCache.addItems(displayId, cost, newItems);
          } else {
            // if we have multiple variants, use the base as display variant
            displayId = material.getIdentifier();
            displayVariant = MaterialVariant.of(material);

            // process items
            for (MaterialVariantId variant : variants) {
              MaterialRecipeCache.addItems(variant, cost, newItems);
            }
          }

          // if no items, skip this material - shouldn't happen
          if (newItems.isEmpty()) continue;

          // first item becomes our display item
          materialItems.add(newItems.get(0));
          // all other items become focusable
          hiddenInputs.addAll(newItems.subList(1, newItems.size()));

          // add the material info
          materials.add(displayVariant);
          resultItems.add(output.withMaterialForDisplay(displayId));
        }
      }

      // safety: make sure we got results
      if (materials.isEmpty()) {
        multiRecipes = List.of();
      } else {
        multiRecipes = List.of(NamedRecipe.of(id, new DisplayRecipe(
          List.copyOf(materialItems), List.copyOf(materials), List.copyOf(resultItems),
          List.of(patternItem.getItems()), List.copyOf(hiddenInputs))
        ));
      }
    }
    return multiRecipes;
  }

  /** Gets the display items for the given material */
  public static List<ItemStack> getItems(MaterialVariantId id, int cost) {
    List<ItemStack> stacks = new ArrayList<>();
    if (id.hasVariant()) {
      MaterialRecipeCache.addItems(id, cost, stacks);
    } else {
      for (MaterialVariantId variant : MaterialRecipeCache.getVariants(id.getId())) {
        MaterialRecipeCache.addItems(variant, cost, stacks);
      }
    }
    return stacks;
  }

  /** Display recipe handling dynamic focus */
  @Getter
  @RequiredArgsConstructor
  private class DisplayRecipe implements IDisplayPartBuilderRecipe {
    private final List<ItemStack> materialItems;
    private final List<MaterialVariant> materials;
    private final List<ItemStack> resultItems;
    private final List<ItemStack> patternItems;
    private final List<ItemStack> hiddenInputs;

    @Override
    public int getCost() {
      return cost;
    }

    @Override
    public List<Pattern> getPatterns() {
      return List.of(pattern);
    }

    @Override
    public List<MaterialVariant> getMaterials(MaterialVariant focusMaterial, ItemStack focusStack, boolean focusOutput) {
      // if focusing on a material, just display that
      if (!focusMaterial.isEmpty()) {
        // if it's a base ID, animate its variant recipes
        return List.of(focusMaterial);
      }
      // if focusing on an item, use it to determine our material
      if (!focusStack.isEmpty()) {
        if (focusOutput) {
          // outputs are tool parts, so ask the material item
          MaterialVariantId variant = output.getMaterial(focusStack);
          if (!MaterialId.UNKNOWN.equals(variant)) {
            // if it's a base ID, animate its variant recipes
            return List.of(MaterialVariant.of(variant));
          }
        } else {
          // if the item has a material, return that directly, no need to animate
          MaterialRecipe recipe = MaterialRecipeCache.findRecipe(focusStack);
          if (recipe != MaterialRecipe.EMPTY) {
            return List.of(recipe.getMaterial());
          }
        }
      }
      return getMaterials();
    }

    @Override
    public List<ItemStack> getMaterialItems(MaterialVariant focusMaterial, ItemStack focusStack, boolean focusOutput) {
      if (!focusMaterial.isEmpty()) {
        return MaterialPartRecipe.getItems(focusMaterial.getId(), cost);
      }
      if (!focusStack.isEmpty()) {
        if (focusOutput) {
          // outputs are tool parts, so ask the material item
          MaterialVariantId variant = output.getMaterial(focusStack);
          if (!MaterialId.UNKNOWN.equals(variant)) {
            return MaterialPartRecipe.getItems(variant, cost);
          }
        } else {
          // if the item has a material, return it directly, no need to animate
          MaterialRecipe recipe = MaterialRecipeCache.findRecipe(focusStack);
          if (recipe != MaterialRecipe.EMPTY) {
            return List.of(focusStack.copyWithCount(recipe.getItemsUsed(cost)));
          }
        }
      }
      return materialItems;
    }

    @Override
    public List<ItemStack> getResultItems(MaterialVariant focusMaterial, ItemStack focusStack, boolean focusOutput) {
      if (!focusMaterial.isEmpty()) {
        return List.of(output.withMaterial(focusMaterial.getVariant()));
      }
      if (!focusStack.isEmpty()) {
        if (focusOutput) {
          MaterialVariantId variant = output.getMaterial(focusStack);
          if (!MaterialId.UNKNOWN.equals(variant)) {
            return List.of(output.withMaterial(variant));
          }
        } else {
          MaterialRecipe recipe = MaterialRecipeCache.findRecipe(focusStack);
          if (recipe != MaterialRecipe.EMPTY) {
            return List.of(output.withMaterial(recipe.getMaterial().getVariant()));
          }
        }
      }
      return resultItems;
    }
  }
}
