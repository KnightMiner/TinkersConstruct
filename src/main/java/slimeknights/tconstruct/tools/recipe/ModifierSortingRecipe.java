package slimeknights.tconstruct.tools.recipe;

import lombok.RequiredArgsConstructor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.RecipeResult;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.library.recipe.TinkerableRecipeInput;
import slimeknights.tconstruct.library.recipe.modifiers.ModifierRecipeLookup;
import slimeknights.tconstruct.library.recipe.worktable.IModifierWorktableRecipe;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;
import slimeknights.tconstruct.library.tools.nbt.LazyToolStack;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Reorders modifiers ion a tool.
 * @see ModifierSortingRecipeBuilder
 */
@RequiredArgsConstructor
public class ModifierSortingRecipe implements IModifierWorktableRecipe {
  private static final Component TITLE = TConstruct.makeTranslation("recipe", "modifier_sorting.title");
  /** JEI description */
  private static final Component DESCRIPTION = TConstruct.makeTranslation("recipe", "modifier_sorting.description");
  /** Forward sorting description */
  private static final Component DESCRIPTION_FORWARD = TConstruct.makeTranslation("recipe", "modifier_sorting.description.forward");
  /** Reverse sorting description */
  private static final Component DESCRIPTION_REVERSE = TConstruct.makeTranslation("recipe", "modifier_sorting.description.reversed");
  /** Error for too few modifiers */
  private static final Component NOT_ENOUGH_MODIFIERS = TConstruct.makeTranslation("recipe", "modifier_sorting.not_enough_modifiers").withStyle(ChatFormatting.RED);
  public static final RecordLoadable<ModifierSortingRecipe> LOADER = RecordLoadable.create(Loadables.SIZED_ITEM_INGREDIENT.requiredField("input", r -> r.input), ModifierSortingRecipe::new);

  private final SizedIngredient input;

  @Override
  public boolean matches(TinkerableRecipeInput inv, Level world) {
    if (!inv.getTinkerableStack().is(TinkerTags.Items.MODIFIABLE)) {
      return false;
    }
    boolean found = false;
    for (int i = 0; i < inv.getInputCount(); i++) {
      ItemStack stack = inv.getInput(i);
      if (!stack.isEmpty()) {
        // at most one copy, and no other inputs
        if (found || !input.test(stack)) {
          return false;
        }
        found = true;
      }
    }
    return found;
  }

  @Override
  public Component getTitle() {
    return TITLE;
  }

  @Override
  public Component getDescription(@Nullable TinkerableRecipeInput inv) {
    if (inv != null) {
      if (inv.getTinkerable().getUpgrades().getModifiers().size() < 2) {
        return NOT_ENOUGH_MODIFIERS;
      }
      if (inv.getInput(0).isEmpty()) {
        return DESCRIPTION_REVERSE;
      } else {
        return DESCRIPTION_FORWARD;
      }
    }
    return DESCRIPTION;
  }

  @Override
  public List<ModifierEntry> getModifierOptions(@Nullable TinkerableRecipeInput inv) {
    if (inv == null) {
      return ModifierRecipeLookup.getRecipeModifierList();
    }
    List<ModifierEntry> modifiers = inv.getTinkerable().getUpgrades().getModifiers();
    if (modifiers.size() < 2) {
      return List.of();
    }
    return modifiers;
  }

  @Override
  public RecipeResult<LazyToolStack> getResult(TinkerableRecipeInput inv, ModifierEntry modifier) {
    ToolStack tool = inv.getTinkerable();

    // find the modifier to remove
    List<ModifierEntry> upgrades = tool.getUpgrades().getModifiers();
    int toMove = IntStream.range(0, upgrades.size())
                          .filter(i -> upgrades.get(i).matches(modifier.id()))
                          .findFirst().orElse(-1);
    // if no change, no need to do anything
    if (toMove == -1) {
      return RecipeResult.pass();
    }

    // reorder
    tool = tool.copy();
    List<ModifierEntry> newUpgrades = new ArrayList<>(upgrades);
    // first slot empty? reverse direction
    int target;
    if (inv.getInput(0).isEmpty()) {
      target = toMove == 0 ? upgrades.size() - 1 : toMove - 1;
    } else {
      target = toMove == upgrades.size() - 1 ? 0 : toMove + 1;
    }
    Collections.swap(newUpgrades, toMove, target);
    tool.setUpgrades(new ModifierNBT(newUpgrades));

    // no need to validate, its the same modifiers
    return LazyToolStack.successCopy(tool, inv.getTinkerableStack());
  }

  @Override
  public void updateInputs(LazyToolStack result, TinkerableRecipeInput.Mutable inv, ModifierEntry selected, boolean isServer) {
    // input is not consumed
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerRecipes.modifierSorting.get();
  }


  /* JEI */
  @Nullable
  protected List<ItemStack> tools;

  @Override
  public List<ItemStack> getInputTools() {
    if (tools == null) {
      tools = RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, TinkerTags.Items.MODIFIABLE).map(IModifiableDisplay::getDisplayStack).toList();
    }
    return tools;
  }

  @Override
  public List<ItemStack> getDisplayItems(int slot) {
    if (slot == 0) {
      return Arrays.asList(input.getItems());
    }
    return List.of();
  }

  @Override
  public int getInputCount() {
    return 1;
  }
}
