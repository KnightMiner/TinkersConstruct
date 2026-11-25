package slimeknights.tconstruct.tools.recipe;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.recipe.RecipeResult;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.library.recipe.TinkerableRecipeInput;
import slimeknights.tconstruct.library.recipe.modifiers.ModifierRecipeLookup;
import slimeknights.tconstruct.library.recipe.worktable.AbstractWorktableRecipe;
import slimeknights.tconstruct.library.recipe.worktable.ModifierSetWorktableRecipe;
import slimeknights.tconstruct.library.tools.component.CollectionComponent;
import slimeknights.tconstruct.library.tools.component.SetComponent;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.stack.LazyToolStack;
import slimeknights.tconstruct.library.tools.stack.ToolStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * Recipe handling toggling for modifiers that work on left and right click.
 * @see slimeknights.tconstruct.library.tools.definition.module.interaction.DualOptionInteraction
 * @see slimeknights.tconstruct.library.tools.definition.module.interaction.ToggleableSetInteraction
 * @see ToggleInteractionWorktableRecipeBuilder
 */
public class ToggleInteractionWorktableRecipe extends AbstractWorktableRecipe {
  private static final Component TITLE = TConstruct.makeTranslation("recipe", "toggle_interaction.title");
  private static final Component DESCRIPTION = TConstruct.makeTranslation("recipe", "toggle_interaction.description");
  private static final Predicate<ModifierEntry> FILTER = entry -> ModifierManager.isInTag(entry.id(), TinkerTags.Modifiers.DUAL_INTERACTION);
  /** Loader instance */
  public static final RecordLoadable<ToggleInteractionWorktableRecipe> LOADER = RecordLoadable.create(TOOL_FIELD, INPUTS_FIELD, ToggleInteractionWorktableRecipe::new);

  /** Cached list of modifiers shown in JEI */
  private List<ModifierEntry> filteredModifiers = null;

  public ToggleInteractionWorktableRecipe(Ingredient toolRequirement, List<SizedIngredient> inputs) {
    super(toolRequirement, inputs);
  }

  @Override
  public Component getTitle() {
    return TITLE;
  }

  @Override
  public Component getDescription(@Nullable TinkerableRecipeInput inv) {
    if (inv != null && inv.getTinkerable().getModifiers().modifiers().stream().noneMatch(FILTER)) {
      return ModifierSetWorktableRecipe.NO_MATCHES;
    }
    return DESCRIPTION;
  }

  @Override
  public List<ModifierEntry> getModifierOptions(@Nullable TinkerableRecipeInput inv) {
    if (inv == null) {
      if (filteredModifiers == null) {
        filteredModifiers = ModifierRecipeLookup.getRecipeModifierList().stream().filter(FILTER).toList();
      }
      return filteredModifiers;
    }
    return inv.getTinkerable().getModifiers().modifiers().stream().filter(FILTER).toList();
  }

  @Override
  public RecipeResult<LazyToolStack> getResult(TinkerableRecipeInput inv, ModifierEntry modifier) {
    ToolStack tool = inv.getTinkerable().copy();
    // start by obtaining the two set
    SetComponent<ModifierId> leftSet = SetComponent.get(tool, TinkerData.ATTACK_MODIFIERS);
    SetComponent<ModifierId> rightSet = SetComponent.get(tool, TinkerData.INTERACT_MODIFIERS);

    // if either set contains it, remove from both
    ModifierId id = modifier.id();
    boolean canRemove = leftSet.contains(id) || rightSet.contains(id);
    if (canRemove) {
      CollectionComponent.set(tool, TinkerData.ATTACK_MODIFIERS, leftSet.without(id));
      CollectionComponent.set(tool, TinkerData.INTERACT_MODIFIERS, rightSet.without(id));
    } else {
      // neither had it? add to the proper set. if it can interact on right, means we want it in the left list
      if (tool.getHook(ToolHooks.INTERACTION).canInteract(tool, id, InteractionSource.RIGHT_CLICK)) {
        tool.set(TinkerData.ATTACK_MODIFIERS, leftSet.with(id));
      } else {
        tool.set(TinkerData.INTERACT_MODIFIERS, rightSet.with(id));
      }
    }
    return LazyToolStack.success(tool);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerRecipes.toggleInteraction.get();
  }
}
