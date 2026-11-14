package slimeknights.tconstruct.library.recipe.partbuilder;

import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.primitive.ResourceLocationLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.library.recipe.material.IMaterialValue;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * Recipe to craft an ordinary item using the part builder.
 * Can be used for crafting with a material, or leave the material blank to just require the pattern item.
 * @see MaterialPartRecipe
 * @see ItemPartRecipeBuilder
 */
public class ItemPartRecipe implements IPartBuilderRecipe, IDisplayPartBuilderRecipe {
  public static final RecordLoadable<ItemPartRecipe> LOADER = RecordLoadable.create(
    MaterialVariantId.LOADABLE.defaultField("material", MaterialId.UNKNOWN, r -> r.material.getVariant()),
    Pattern.LOADABLE.requiredField("pattern", ItemPartRecipe::getPattern),
    Loadables.ITEM_INGREDIENT_NONEMPTY.defaultField("pattern_item", DEFAULT_PATTERNS, r -> r.patternItem),
    IntLoadable.FROM_ZERO.defaultField("cost", 0, ItemPartRecipe::getCost),
    ItemOutput.REQUIRED_STACK_LOADABLE.requiredField("result", r -> r.result),
    ResourceLocationLoadable.DEFAULT.nullableField("title_key", r -> r.titleKey),
    ItemPartRecipe::new).validate((recipe, error) -> {
      if (recipe.cost == 0 && !recipe.material.isEmpty()) {
        throw error.create("Cost must be greater than zero if material is defined");
      }
      return recipe;
    });

  @Getter
  private final MaterialVariant material;
  @Getter
  private final Pattern pattern;
  private final Ingredient patternItem;
  @Getter
  private final int cost;
  private final ItemOutput result;
  private final ResourceLocation titleKey;
  @Nullable @Getter
  private final Component title;
  @Getter
  private final List<Component> information;

  protected ItemPartRecipe(MaterialVariantId material, Pattern pattern, Ingredient patternItem, int cost, ItemOutput result, @Nullable ResourceLocation titleKey) {
    this.material = MaterialVariant.of(material);
    this.pattern = pattern;
    this.patternItem = patternItem;
    this.cost = cost;
    this.result = result;
    this.titleKey = titleKey;
    if (titleKey != null) {
      String key = "recipe." + titleKey.toLanguageKey();
      title = Component.translatable(key);
      information = List.of(Component.translatable(key + ".info"));
    } else {
      title = null;
      information = List.of();
    }
  }

  @Override
  public boolean allowUncraftable() {
    // if we have a recipe, we craft it
    return true;
  }

  @Override
  public boolean partialMatch(PartBuilderInput inv) {
    // first, must have a pattern
    if (!patternItem.test(inv.getPatternStack())) {
      return false;
    }
    // if there is a material item, it must have a valid material and be craftable
    if (!inv.getItem().isEmpty()) {
      // no material means we expect no stack in the material slot
      if (material.isEmpty()) {
        return false;
      }
      IMaterialValue materialRecipe = inv.getMaterial();
      return materialRecipe != null && material.matchesVariant(materialRecipe.getMaterial());
    }
    // no material item? return match in case we get one later
    return true;
  }

  @Override
  public boolean matches(PartBuilderInput inv, Level level) {
    if (material.isEmpty()) {
      return inv.getItem().isEmpty();
    }
    IMaterialValue materialRecipe = inv.getMaterial();
    return materialRecipe != null && material.matchesVariant(materialRecipe.getMaterial())
           && inv.getItem().getCount() >= materialRecipe.getItemsUsed(cost);
  }

  @Override
  public int getItemsUsed(PartBuilderInput inv) {
    if (material.isEmpty()) {
      return 0;
    }
    return IPartBuilderRecipe.super.getItemsUsed(inv);
  }

  @Override
  public ItemStack getResultItem(HolderLookup.Provider registries) {
    return result.get();
  }

  @Override
  public ItemStack assemble(PartBuilderInput inv, HolderLookup.Provider registries) {
    ItemStack result = getResultItem(registries).copy();
    IMaterialValue materialRecipe = inv.getMaterial();
    if (materialRecipe != null) {
      // if no leftover, give them more parts provided we have the patterns for it
      int value = materialRecipe.getValue();
      if (!materialRecipe.hasLeftover() && value > cost) {
        result.setCount(result.getCount() * value / cost);
      }
    }
    return result;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerRecipes.itemPartBuilder.get();
  }


  /* Title */

  @Nullable
  @Override
  public Component getDisplayTitle() {
    return title;
  }

  @Override
  public List<Component> getText(PartBuilderInput inv) {
    return information;
  }

  @Override
  public List<Component> getTooltip() {
    return information;
  }


  /* JEI */
  private List<ItemStack> materialItems;

  @Override
  public List<Pattern> getPatterns() {
    return List.of(pattern);
  }

  @Override
  public List<ItemStack> getPatternItems() {
    return Arrays.asList(patternItem.getItems());
  }

  @Override
  public List<ItemStack> getMaterialItems() {
    if (materialItems == null) {
      // if unknown, nothing to display. Used for no material input
      if (material.isEmpty()) {
        materialItems = List.of();
      } else {
        materialItems = List.copyOf(MaterialPartRecipe.getItems(material.getVariant(), cost));
      }
    }
    return materialItems;
  }

  @Override
  public List<ItemStack> getResultItems() {
    return List.of(result.get());
  }
}
