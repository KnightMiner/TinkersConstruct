package slimeknights.tconstruct.library.recipe.partbuilder.recycle;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.MultiNamedRecipe;
import slimeknights.mantle.recipe.NamedRecipe;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.library.recipe.partbuilder.DisplayPartRecipe;
import slimeknights.tconstruct.library.recipe.partbuilder.IDisplayPartBuilderRecipe;
import slimeknights.tconstruct.library.recipe.partbuilder.IPartBuilderRecipe;
import slimeknights.tconstruct.library.recipe.partbuilder.PartBuilderInput;
import slimeknights.tconstruct.library.recipe.partbuilder.Pattern;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static slimeknights.tconstruct.library.recipe.partbuilder.recycle.PartBuilderToolRecycle.NO_MODIFIERS;

/**
 * Recipe for recycling a vanilla tool or a tinkers tool with no materials in the part builder.
 * @see PartBuilderToolRecycle
 * @see PartBuilderRecycleBuilder
 */
public class PartBuilderRecycle implements IPartBuilderRecipe, MultiNamedRecipe<IDisplayPartBuilderRecipe> {
  /** Title for the screen */
  private static final Component RECYCLING = TConstruct.makeTranslation("recipe", "recycling");
  /** General instructions for recycling */
  private static final List<Component> INSTRUCTIONS = List.of(TConstruct.makeTranslation("recipe", "recycling.info"));
  /** Error when durability is too low */
  private static final List<Component> NOT_ENOUGH_DURABILITY = List.of(TConstruct.makeTranslation("recipe", "recycling.not_enough_durability").withStyle(ChatFormatting.RED));
  /** Error for trying to recycle a tool with enchantments */
  private static final List<Component> NO_ENCHANTMENTS = List.of(TConstruct.makeTranslation("recipe", "recycling.no_enchantments").withStyle(ChatFormatting.RED));

  /** Loader instance */
  public static final RecordLoadable<PartBuilderRecycle> LOADER = RecordLoadable.create(
    Loadables.ITEM_INGREDIENT_NONEMPTY.requiredField("tool", r -> r.tool),
    Loadables.ITEM_INGREDIENT_NONEMPTY.requiredField("pattern", r -> r.pattern),
    Pattern.LOADABLE.mapWithValues(ItemOutput.REQUIRED_STACK_LOADABLE).requiredField("results", r -> r.results),
    PartBuilderRecycle::new);

  private final Ingredient tool;
  private final Ingredient pattern;
  private final Map<Pattern,ItemOutput> results;
  private final int resultCount;

  protected PartBuilderRecycle(Ingredient tool, Ingredient pattern, Map<Pattern,ItemOutput> results) {
    this.tool = tool;
    this.pattern = pattern;
    this.results = results;
    this.resultCount = results.values().stream().mapToInt(ItemOutput::getCount).sum();
  }

  @Override
  public int getItemsUsed(PartBuilderInput inv) {
    return 1;
  }

  @Override
  public Pattern getPattern() {
    return MISSING;
  }

  @Override
  public Stream<Pattern> getPatterns(PartBuilderInput inv) {
    return results.keySet().stream();
  }

  @Override
  public boolean partialMatch(PartBuilderInput inv) {
    return pattern.test(inv.getPatternStack()) && tool.test(inv.getItem());
  }

  /** Scales the amount based on the tack damage */
  private static int getAmount(ItemStack stack, int max) {
    int maxDamage = stack.getMaxDamage();
    if (maxDamage == 0) {
      return max;
    }
    return max * (maxDamage - stack.getDamageValue()) / maxDamage;
  }

  @Override
  public boolean matches(PartBuilderInput inv, Level level) {
    ItemStack stack = inv.getItem();
    return partialMatch(inv) && getAmount(stack, resultCount) > 0 && stack.is(TinkerTags.Items.MODIFIABLE) ? !ModifierUtil.hasUpgrades(stack) : !stack.isEnchanted();
  }

  @Override
  public ItemStack assemble(PartBuilderInput inv, HolderLookup.Provider access, Pattern pattern) {
    int maxCount = getAmount(inv.getItem(), resultCount);
    ItemOutput result = results.get(pattern);
    // should never happen
    if (result == null) {
      return ItemStack.EMPTY;
    }
    return result.get().copyWithCount(Math.min(maxCount, result.getCount()));
  }

  @Override
  public ItemStack getLeftover(PartBuilderInput inv, Pattern pattern) {
    // maximum items to return
    int maxCount = getAmount(inv.getItem(), resultCount);
    ItemOutput result = results.get(pattern);
    if (result != null) {
      maxCount -= result.getCount();
    }
    // if we have remaining items after removing the main choice, randomly choose a second
    if (maxCount > 0 && results.size() > 1) {
      List<ItemOutput> alternatives = results.entrySet().stream().filter(p -> p.getKey() != pattern).map(Entry::getValue).toList();
      if (!alternatives.isEmpty()) {
        ItemOutput chosen = alternatives.get(TConstruct.RANDOM.nextInt(alternatives.size()));
        return chosen.get().copyWithCount(Math.min(maxCount, chosen.getCount()));
      }
    }
    return ItemStack.EMPTY;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerRecipes.partBuilderRecycle.get();
  }


  /* Unused */

  @Override
  public int getCost() {
    return 0;
  }

  /** @deprecated use {@link #assemble(PartBuilderInput, HolderLookup.Provider, Pattern)}  */
  @Deprecated
  @Override
  public ItemStack getResultItem(HolderLookup.Provider registries) {
    return ItemStack.EMPTY;
  }


  /* Display */

  @Nullable
  @Override
  public Component getTitle() {
    return RECYCLING;
  }

  @Override
  public List<Component> getText(PartBuilderInput inv) {
    ItemStack stack = inv.getItem();
    if (getAmount(stack, resultCount) <= 0) {
      return NOT_ENOUGH_DURABILITY;
    }
    if (stack.is(TinkerTags.Items.MODIFIABLE)) {
      if (ModifierUtil.hasUpgrades(stack)) {
        return NO_MODIFIERS;
      }
    } else if (stack.isEnchanted()) {
      return NO_ENCHANTMENTS;
    }
    return INSTRUCTIONS;
  }


  /* JEI */
  private List<NamedRecipe<IDisplayPartBuilderRecipe>> displayRecipes;

  @Override
  public List<NamedRecipe<IDisplayPartBuilderRecipe>> getRecipes(ResourceLocation id, HolderLookup.Provider registries) {
    if (displayRecipes == null) {
      List<ItemStack> patternItems = List.of(pattern.getItems());
      displayRecipes = Arrays.stream(tool.getItems()).map(tool -> DisplayPartRecipe.id(id)
        .title(RECYCLING).tooltip(INSTRUCTIONS)
        .patterns(results.keySet().stream().toList())
        .patternItems(patternItems)
        .materialItem(tool)
        .results(results.values().stream().map(ItemOutput::get).toList())
        .build()).toList();
    }
    return displayRecipes;
  }
}
