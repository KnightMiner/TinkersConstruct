package slimeknights.tconstruct.library.recipe.worktable;

import lombok.RequiredArgsConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.TinkerableRecipeInput;
import slimeknights.tconstruct.library.recipe.modifiers.ModifierRecipeLookup;
import slimeknights.tconstruct.library.recipe.modifiers.adding.ModifierRecipe;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;
import slimeknights.tconstruct.library.tools.nbt.LazyToolStack;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * Default implementation of modifier worktable recipes, taking a list of inputs.
 */
@RequiredArgsConstructor
public abstract class AbstractWorktableRecipe implements IModifierWorktableRecipe {
  // TODO: remove since cache was removed
  public static final Ingredient DEFAULT_TOOLS = Ingredient.of(TinkerTags.Items.MODIFIABLE);
  protected static final LoadableField<Ingredient,AbstractWorktableRecipe> TOOL_FIELD = Loadables.ITEM_INGREDIENT_NONEMPTY.defaultField("tools", DEFAULT_TOOLS, true, r -> r.toolRequirement);
  protected static final LoadableField<List<SizedIngredient>,AbstractWorktableRecipe> INPUTS_FIELD = Loadables.SIZED_ITEM_INGREDIENT.list(1).requiredField("inputs", r -> r.inputs);

  protected final Ingredient toolRequirement;
  protected final List<SizedIngredient> inputs;

  /* JEI */
  @Nullable
  protected List<ItemStack> tools;

  public AbstractWorktableRecipe(List<SizedIngredient> inputs) {
    this(Ingredient.of(TinkerTags.Items.MODIFIABLE), inputs);
  }

  @Override
  public boolean matches(TinkerableRecipeInput inv, Level world) {
    if (!toolRequirement.test(inv.getTinkerableStack())) {
      return false;
    }
    return ModifierRecipe.checkMatch(inv, inputs);
  }

  @Override
  public List<ModifierEntry> getModifierOptions(@Nullable TinkerableRecipeInput inv) {
    if (inv == null) {
      return ModifierRecipeLookup.getRecipeModifierList();
    }
    return inv.getTinkerable().getUpgrades().getModifiers();
  }

  @Override
  public void updateInputs(LazyToolStack result, TinkerableRecipeInput.Mutable inv, ModifierEntry selected, boolean isServer) {
    ModifierRecipe.updateInputs(inv, inputs);
  }


  /* JEI */

  @Override
  public List<ItemStack> getInputTools() {
    if (tools == null) {
      tools = Arrays.stream(toolRequirement.getItems()).map(stack -> IModifiableDisplay.getDisplayStack(stack.getItem())).toList();
    }
    return tools;
  }

  @Override
  public List<ItemStack> getDisplayItems(int slot) {
    if (slot < 0 || slot >= inputs.size()) {
      return List.of();
    }
    return Arrays.asList(inputs.get(slot).getItems());
  }

  @Override
  public int getInputCount() {
    return inputs.size();
  }
}
