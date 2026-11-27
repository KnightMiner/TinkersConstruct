package slimeknights.tconstruct.library.recipe.modifiers.adding;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.BooleanLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.tconstruct.library.json.IntRange;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHolder;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.RecipeResult;
import slimeknights.tconstruct.library.recipe.modifiers.ModifierRecipeLookup;
import slimeknights.tconstruct.library.recipe.modifiers.adding.variant.SwappableVariant;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.TinkerStationRecipeInput;
import slimeknights.tconstruct.library.tools.SlotType.SlotCount;
import slimeknights.tconstruct.library.tools.component.ModifierEntries;
import slimeknights.tconstruct.library.tools.component.ToolMaterials;
import slimeknights.tconstruct.library.tools.definition.module.build.ToolTraitHook;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.stack.BuilderKeys;
import slimeknights.tconstruct.library.tools.stack.DummyToolStack;
import slimeknights.tconstruct.library.tools.stack.LazyToolStack;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;
import slimeknights.tconstruct.library.tools.stack.VolatileBuilder;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.item.ModifierCrystalItem;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe.modifiersForResult;
import static slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe.withModifiers;

/** Shared logic between modifier and incremental modifier recipes */
public abstract class AbstractModifierRecipe implements ITinkerStationRecipe, IDisplayModifierRecipe {
  /* Fields */
  protected static final LoadableField<Ingredient,AbstractModifierRecipe> TOOLS_FIELD = Loadables.ITEM_INGREDIENT_NONEMPTY.requiredField("tools", r -> r.toolRequirement);
  protected static final LoadableField<Integer,AbstractModifierRecipe> MAX_TOOL_SIZE_FIELD = IntLoadable.FROM_ONE.defaultField("max_tool_size", ITinkerStationRecipe.DEFAULT_TOOL_STACK_SIZE, r -> r.maxToolSize);
  protected static final LoadableField<ModifierId,AbstractModifierRecipe> RESULT_FIELD = ModifierId.LOADABLE.requiredField("result", r -> r.result.id());
  protected static final LoadableField<IntRange,AbstractModifierRecipe> LEVEL_FIELD = ModifierEntry.VALID_LEVEL.defaultField("level", r -> r.level);
  protected static final LoadableField<SlotCount,AbstractModifierRecipe> SLOTS_FIELD = SlotCount.LOADABLE.nullableField("slots", r -> r.slots);
  protected static final LoadableField<Boolean,AbstractModifierRecipe> ALLOW_CRYSTAL_FIELD = BooleanLoadable.INSTANCE.defaultField("allow_crystal", true, r -> r.allowCrystal);
  protected static final LoadableField<Boolean,AbstractModifierRecipe> CHECK_TRAIT_LEVEL_FIELD = BooleanLoadable.INSTANCE.defaultField("check_trait_level", false, false, r -> r.checkTraitLevel);


  /** Ingredient representing the required tool, typically a tag */
  protected final Ingredient toolRequirement;
  /** Max size of the tool for this modifier. If the tool size is smaller, the stack will reduce by less */
  @Getter
  protected final int maxToolSize;
  /** Modifier this recipe is adding */
  protected final ModifierHolder result;
  /** Range of result levels that is valid on the tool */
  @Getter
  private final IntRange level;
  /** Gets the slots required by this recipe. If null, no slots required */
  @Getter
  @Nullable
  private final SlotCount slots;
  /** If true, this recipe can be applied using modifier crystals */
  protected final boolean allowCrystal;
  /** If true, validates the level against the trait level. False validates against recipe modifiers only. */
  @Getter @Accessors(fluent = true)
  protected final boolean checkTraitLevel;

  protected AbstractModifierRecipe(Ingredient toolRequirement, int maxToolSize,
                                   ModifierId result, IntRange level, @Nullable SlotCount slots, boolean allowCrystal, boolean checkTraitLevel) {
    this.toolRequirement = toolRequirement;
    this.maxToolSize = maxToolSize;
    this.result = ModifierHolder.create(result);
    this.level = level;
    this.slots = slots;
    this.allowCrystal = allowCrystal;
    this.checkTraitLevel = checkTraitLevel;
    ModifierRecipeLookup.addRecipeModifier(SlotCount.type(slots), this.result);
  }

  @Override
  public abstract RecipeResult<LazyToolStack> getValidatedResult(TinkerStationRecipeInput inv, HolderLookup.Provider registries);

  @Override
  public int shrinkToolSlotBy() {
    return maxToolSize;
  }


  /* JEI display */
  /** Cache of input items shared between result and input */
  @Nullable
  private List<ItemStack> toolInputs = null;
  /** Cache of modifier slots produced by this recipe for JEI display */
  @Nullable
  protected List<SlotCount> resultSlots = null;

  /** Gets or builds the list of tool inputs */
  protected List<ItemStack> getToolInputs() {
    if (toolInputs == null) {
      Stream<ItemStack> stream = Arrays.stream(this.toolRequirement.getItems());
      // if we check the trait level, then filter out any tools that would never be able to use this recipe
      if (checkTraitLevel) {
        ModifierId result = this.result.id();
        int max = this.level.max();
        stream = stream.filter(stack -> {
          if (stack.getItem() instanceof IModifiable modifiable) {
            return ToolTraitHook.getTraits(modifiable.getToolDefinition(), ToolMaterials.EMPTY).getLevel(result) < max;
          }
          return false;
        });
      }
      toolInputs = stream.map(MAP_TOOL_STACK_FOR_RENDERING).collect(Collectors.toList());
    }
    return toolInputs;
  }

  /** Cache of display tool inputs */
  private List<ItemStack> displayInputs = null;

  /** Cache of display output */
  List<ItemStack> toolWithModifier = null;

  /** Display result, may be a higher level than real result */
  private ModifierEntry displayResult;

  @Override
  public ModifierEntry getDisplayResult() {
    if (displayResult == null) {
      // display result is just the min level result, means when a recipe is for Luck II, it displays as Luck II
      displayResult = new ModifierEntry(result, this.level.min());
    }
    return displayResult;
  }

  @Override
  public boolean isTool(ItemStack check) {
    return toolRequirement.test(check);
  }

  @Override
  public List<ItemStack> getToolWithoutModifier() {
    if (displayInputs == null) {
      int min = level.min() - 1;
      ModifierEntry existing = min > 0 ? new ModifierEntry(result, min) : null;
      ModifierEntries modifiers = modifiersForResult(getDisplayResult(), existing);
      displayInputs = getToolInputs().stream().map(stack -> withModifiers(stack, maxToolSize, modifiers)).collect(Collectors.toList());
    }
    return displayInputs;
  }

  @Override
  public List<ItemStack> getToolWithModifier() {
    if (toolWithModifier == null) {
      ModifierEntry result = getDisplayResult();
      ModifierEntries modifiers = modifiersForResult(result, result);
      toolWithModifier = getToolInputs().stream().map(stack -> withModifiers(stack, maxToolSize, modifiers)).collect(Collectors.toList());
    }
    return toolWithModifier;
  }

  /**
   * Helper to get the result slots for a given modifier recipe by querying volatile data.
   * Does not handle modifier traits for simplicity.
   * @param result  Modifier crafted by this recipe.
   * @param item    Representative tool for this recipe
   * @param variant Swappable variant, for recipes like rebalanced.
   */
  public static List<SlotCount> getResultSlots(ModifierEntry result, Item item, @Nullable SwappableVariant variant) {
    // add variant info for the sake of rebalanced
    ToolStackView tool = new DummyToolStack(item, ModifierEntries.EMPTY, new PatchedDataComponentMap(DataComponentMap.EMPTY));
    if (variant != null) {
      variant.applyToTool(tool, result.getHolder());
    }
    // build slot data
    VolatileBuilder builder = new VolatileBuilder();
    result.getHook(ModifierHooks.VOLATILE_DATA).addVolatileData(tool, result, builder, tool);
    return builder.get(BuilderKeys.SLOTS).buildCounts();
  }

  @Override
  public List<SlotCount> getResultSlots() {
    if (resultSlots == null) {
      // we need to decide a tool for the dummy stack. Could just use air, but might as well use a tool that shows up
      // on the odd chance the behavior differs per tool this might be wrong, but practically that just affects ancient tools on input right now
      ItemStack[] tools = toolRequirement.getItems();
      resultSlots = getResultSlots(getDisplayResult(), tools.length > 0 ? tools[0].getItem() : Items.AIR, null);
    }
    return resultSlots;
  }


  /* Helpers */

  /** Checks if the inventory contains a crystal */
  public static boolean matchesCrystal(TinkerStationRecipeInput container, ModifierId match) {
    boolean found = false;
    for (int i = 0; i < container.getInputCount(); i++) {
      ItemStack stack = container.getInput(i);
      if (!stack.isEmpty()) {
        // cannot have two stacks
        // must be a crystal
        if (found || !stack.is(TinkerModifiers.modifierCrystal.asItem())) {
          return false;
        }
        // found a crystal, make sure we have enough and the ID matches
        if (ModifierCrystalItem.getModifier(stack).is(match)) {
          return false;
        }
        found = true;
      }
    }
    return found;
  }

  /** Checks if the inventory contains a crystal */
  protected boolean matchesCrystal(TinkerStationRecipeInput container) {
    return allowCrystal && matchesCrystal(container, result.id());
  }

  /** Validates that the given level is a valid result */
  @Nullable
  protected Component validateLevel(int resultLevel) {
    return IDisplayModifierRecipe.checkLevel(result, level, resultLevel, checkTraitLevel);
  }

  /**
   * Validate tool has the right number of slots, called internally by {@link #validatePrerequisites(ToolStackView, int)}
   * @param tool   Tool instance
   * @param slots  Required slots
   * @return  Error message, or null if no error
   */
  @Nullable
  protected static Component checkSlots(ToolStackView tool, @Nullable SlotCount slots) {
    return IDisplayModifierRecipe.checkSlots(tool, slots);
  }

  /**
   * Validates that this tool has a resulting level in the range and has enough modifier slots
   * @param tool    Tool stack instance
   * @param resultLevel  Level after adding this modifier
   * @return  Error message, or null if no error
   */
  @Nullable
  protected Component validatePrerequisites(ToolStackView tool, int resultLevel) {
    Component error = validateLevel(resultLevel);
    if (error != null) {
      return error;
    }
    return checkSlots(tool, slots);
  }

  /** Gets the new level after applying this recipe */
  protected int getNewLevel(ToolStackView tool) {
    return (checkTraitLevel ? tool.getModifiers() : tool.getCraftedModifiers()).getLevel(result.id()) + 1;
  }

  /**
   * Validates that this tool has a resulting level in the range and has enough modifier slots
   * @param tool    Tool stack instance
   * @return  Error message, or null if no error
   */
  @Nullable
  protected Component validatePrerequisites(ToolStackView tool) {
    return validatePrerequisites(tool, getNewLevel(tool));
  }
}
