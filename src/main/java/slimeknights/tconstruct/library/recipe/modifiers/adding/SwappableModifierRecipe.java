package slimeknights.tconstruct.library.recipe.modifiers.adding;

import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.json.IntRange;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.RecipeResult;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.library.recipe.modifiers.adding.variant.SwappableVariant;
import slimeknights.tconstruct.library.recipe.tinkerstation.TinkerStationRecipeInput;
import slimeknights.tconstruct.library.tools.SlotType.SlotCount;
import slimeknights.tconstruct.library.tools.component.ModifierEntries;
import slimeknights.tconstruct.library.tools.component.ResourceMap;
import slimeknights.tconstruct.library.tools.stack.LazyToolStack;
import slimeknights.tconstruct.library.tools.stack.ToolStack;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;

import javax.annotation.Nullable;
import java.util.List;

import static slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe.modifiersForResult;
import static slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe.withModifiers;

/**
 * Recipe for a modifier with a swappable variant string.
 * @see SwappableModifierRecipeBuilder
 * @see ModifierRecipe
 */
public class SwappableModifierRecipe extends ModifierRecipe {
  private static final String ALREADY_PRESENT = TConstruct.makeTranslationKey("recipe", "swappable.already_present");
  public static final RecordLoadable<SwappableModifierRecipe> LOADER = RecordLoadable.create(
    INPUTS_FIELD, TOOLS_FIELD, MAX_TOOL_SIZE_FIELD, RESULT_FIELD,
    SwappableVariant.LOADER.requiredField("value", r -> r.value),
    SLOTS_FIELD, ALLOW_CRYSTAL_FIELD,
    SwappableModifierRecipe::new);

  /** Value of the modifier being swapped, distinguishing this recipe from others for the same modifier */
  private final SwappableVariant value;
  /** Display component for the variant string */
  @Getter
  private final Component variant;

  public SwappableModifierRecipe(List<SizedIngredient> inputs, Ingredient toolRequirement, int maxToolSize, ModifierId result, SwappableVariant value, @Nullable SlotCount slots, boolean allowCrystal) {
    super(inputs, toolRequirement, maxToolSize, result, new IntRange(1, 1), slots, allowCrystal, false);
    this.value = value;
    this.variant = value.formatVariant(this.result);
  }

  @Override
  public RecipeResult<LazyToolStack> getValidatedResult(TinkerStationRecipeInput inv, HolderLookup.Provider registries) {
    ToolStack tool = inv.getTinkerable();

    // if the tool has the modifier already, can skip most requirements
    ModifierId modifier = result.id();

    boolean needsModifier;
    int level = tool.getCraftedModifiers().getLevel(modifier);
    if (level == 0) {
      needsModifier = true;
      // no need to check level as we know the level is 0 and we are making it 1
      Component slotError = checkSlots(tool, getSlots());
      if (slotError != null) {
        return RecipeResult.failure(slotError);
      }
    } else {
      needsModifier = false;
    }


    // do not allow adding the modifier if this variant is already present
    ResourceMap<String> variants = ResourceMap.get(tool, TinkerData.MODIFIER_STRING);
    if (level > 0 && value.isPresent(tool, result)) {
      return RecipeResult.failure(ALREADY_PRESENT, result.get().getDisplayName(), variant);
    }

    // consume slots
    tool = tool.copyMaxCount(shrinkToolSlotBy());
    if (needsModifier) {
      SlotCount slots = getSlots();
      if (slots != null) {
        tool.removeSlots(slots);
      }
    }

    // set the new value to the modifier
    value.applyToTool(tool, result);

    // add modifier if needed
    if (needsModifier) {
      tool.addModifier(result.id(), 1);
    } else {
      tool.rebuildStats();
    }

    // ensure no modifier problems
    Component toolValidation = tool.tryValidate();
    if (toolValidation != null) {
      return RecipeResult.failure(toolValidation);
    }
    return LazyToolStack.success(tool);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerRecipes.swappableModifier.get();
  }


  /* Display */

  @Override
  public List<ItemStack> getToolWithModifier() {
    if (toolWithModifier == null) {
      ModifierEntry displayResult = getDisplayResult();
      ModifierEntries modifiers = modifiersForResult(displayResult, displayResult);
      toolWithModifier = getToolInputs().stream()
        .map(stack -> withModifiers(stack, maxToolSize, modifiers, data -> value.applyToTool(data, result)))
        .toList();
    }
    return toolWithModifier;
  }

  @Override
  public List<SlotCount> getResultSlots() {
    if (resultSlots == null) {
      ItemStack[] tools = toolRequirement.getItems();
      resultSlots = getResultSlots(getDisplayResult(), tools.length > 0 ? tools[0].getItem() : Items.AIR, value);
    }
    return resultSlots;
  }

  @Nullable
  @Override
  public Component canApply(ToolStackView tool) {
    // only check slots if we lack the modifier
    if (tool.getCraftedModifiers().getLevel(result.id()) == 0) {
      return checkSlots(tool, getSlots());
    }
    // if we already have it, ensure the variant is changing
    if (value.isPresent(tool, result)) {
      return Component.translatable(ALREADY_PRESENT, this.result.get().getDisplayName(), variant);
    }
    return null;
  }

  @Override
  public void applyModifier(ToolStack tool) {
    // only add the modifier if we lack it
    if (tool.getCraftedModifiers().getLevel(result.id()) == 0) {
      super.applyModifier(tool);
    }
    // apply the variant
    value.applyToTool(tool, result);
  }
}
