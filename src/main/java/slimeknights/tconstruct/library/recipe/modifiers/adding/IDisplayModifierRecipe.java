package slimeknights.tconstruct.library.recipe.modifiers.adding;

import com.google.common.collect.Streams;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.json.IntRange;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHolder;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.RecipeResult;
import slimeknights.tconstruct.library.recipe.tinkerstation.IDisplayTinkerStationRecipe;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.SlotType.SlotCount;
import slimeknights.tconstruct.library.tools.component.ModifierEntries;
import slimeknights.tconstruct.library.tools.component.ToolMaterials;
import slimeknights.tconstruct.library.tools.context.ToolRebuildContext;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.stack.RestrictedComponentMap;
import slimeknights.tconstruct.library.tools.stack.ToolStack;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/** Common interface for modifier recipes that can show in JEI */
public interface IDisplayModifierRecipe extends IModifierRecipe, IDisplayTinkerStationRecipe {
  /** Gets the modifier output of this recipe */
  ModifierEntry getDisplayResult();

  @Override
  default Modifier getModifier() {
    return getDisplayResult().getModifier();
  }

  /**
   * Gets the range this modifier is valid for
   * @return  level range, defaults to {@link ModifierEntry#VALID_LEVEL}
   */
  default IntRange getLevel() {
    return ModifierEntry.VALID_LEVEL;
  }

  /** If true, this recipe checks the level from {@link ToolStackView#getModifiers()}. If false, it uses the level from {@link ToolStackView#getCraftedModifiers()} */
  default boolean checkTraitLevel() {
    return false;
  }

  /** Gets the slot type used by this modifier */
  @Nullable
  default SlotCount getSlots() {
    return null;
  }

  @Nullable
  @Override
  default SlotType getSlotType() {
    SlotCount count = getSlots();
    if (count == null) {
      return null;
    }
    return count.type();
  }

  /** If true, this recipe can be applied incrementally */
  default boolean isIncremental() {
    return false;
  }

  /** Gets a list of modifier slots obtained from this recipe. Generally just redirects to modifier behavior. */
  default List<SlotCount> getResultSlots() {
    return List.of();
  }


  /* Focus updates */

  /** Gets the matching entry on the tool */
  private ModifierEntry getEntry(ToolStackView tool) {
    return (checkTraitLevel() ? tool.getModifiers() : tool.getCraftedModifiers()).getEntry(getDisplayResult().id());
  }

  @Override
  default RecipeResult<ItemStack> onFocused(ItemStack focus) {
    if (Config.CLIENT.showToolInModifiers.get()) {
      ToolStack tool = ToolStack.from(focus);
      Component error = canApply(tool);
      if (error != null) {
        return RecipeResult.failure(error);
      }

      // if adding this modifier leaves you at a valid level, show the focus
      tool = tool.copy();
      // allow easy overriding of applying
      applyModifier(tool);

      // run validation unless the recipe opts out
      if (shouldDisplayValidate()) {
        error = tool.tryValidate();
        if (error != null) {
          return RecipeResult.failure(error);
        }
      }

      return RecipeResult.success(tool.getStack());
    }
    return RecipeResult.pass();
  }

  /** If true, skips running display modifier validation. Used on modifier recipes that don't have validation failures. */
  default boolean shouldDisplayValidate() {
    return true;
  }

  /** Checks if the given tool can receive this modifier. Called when a tool is focused on the modifier recipe. */
  @Nullable
  default Component canApply(ToolStackView tool) {
    // ensure this recipe applies at the given level range
    ModifierEntry entry = getEntry(tool);
    int checkLevel = entry.getLevel();
    boolean checkSlots = false;
    if (!isIncremental() || entry.getAmount(0) <= 0) {
      checkLevel += 1;
      checkSlots = true;
    }
    Component error = checkLevel(getDisplayResult().getHolder(), getLevel(), checkLevel, checkTraitLevel());
    if (error != null) {
      return error;
    }
    // validate the slots if adding a new level
    return checkSlots ? checkSlots(tool, getSlots()) : null;
  }

  /**
   * Adds the modifier to the given tool.
   */
  default void applyModifier(ToolStack tool) {
    ModifierId result = getDisplayResult().id();
    if (isIncremental()) {
      ModifierEntry entry = getEntry(tool);
      // if we have a partial level, just make it a full level
      if (entry.intEffectiveLevel() < entry.getLevel()) {
        int needed = entry.getNeeded();
        tool.addModifierAmount(result, needed - entry.getAmount(0), needed);
        return;
      }
    }
    // apply the modifier
    tool.addModifier(result, 1);
    // spend the slots
    SlotCount slots = getSlots();
    if (slots != null) {
      tool.removeSlots(slots);
    }
  }


  /* Helpers */

  // TODO 1.21: move next two helpers to IDisplayTinkerStationRecipe
  /** Maps the stream from tool items to applicable tool stacks */
  Function<Item,ItemStack> MAP_TOOL_FOR_RENDERING = IModifiableDisplay::getDisplayStack;
  /** Maps the stream from tool items to applicable tool stacks */
  Function<ItemStack,ItemStack> MAP_TOOL_STACK_FOR_RENDERING = stack -> stack.getItem() instanceof IModifiableDisplay display ? display.getRenderTool() : stack;

  /**
   * Gets the list of modifiers to display for the given result
   * @param result  Resulting modifier
   * @param self    Current modifier to display, will typically be result or a lower level of result
   * @return  List of modifiers
   */
  static ModifierEntries modifiersForResult(ModifierEntry result, @Nullable ModifierEntry self) {
    List<ModifierEntry> requirements = result.getHook(ModifierHooks.REQUIREMENTS).displayModifiers(result);
    if (self != null) {
      return ModifierEntries.create(Streams.concat(requirements.stream(), Stream.of(self)).toList());
    }
    return ModifierEntries.create(requirements);
  }

  /* Gets a copy of the stack with the given modifiers */
  static ItemStack withModifiers(ItemStack stack, ModifierEntries modifiers) {
    return withModifiers(stack, 1, modifiers);
  }

  /* Gets a copy of the stack with the given modifiers */
  static ItemStack withModifiers(ItemStack stack, int maxSize, ModifierEntries modifiers) {
    return withModifiers(stack, maxSize, modifiers, data -> {});
  }

  /* Gets a copy of the stack with the given modifiers */
  static ItemStack withModifiers(ItemStack stack, ModifierEntries modifiers, Consumer<ItemStack> dataConsumer) {
    return withModifiers(stack, 1, modifiers, dataConsumer);
  }

  /* Gets a copy of the stack with the given modifiers */
  static ItemStack withModifiers(ItemStack stack, int maxSize, ModifierEntries modifiers, Consumer<ItemStack> dataConsumer) {
    ItemStack output = stack.copyWithCount(Math.min(stack.getMaxStackSize(), maxSize));

    // build modifiers list
    // go through the builder to ensure they are merged properly
    stack.set(TinkerData.CRAFTED_MODIFIERS, modifiers);
    stack.set(TinkerData.ALL_MODIFIERS, modifiers);

    // build persistent and volatile NBT
    MutableDataComponentHolder volatileData = RestrictedComponentMap.volatileData(stack);
    dataConsumer.accept(output);
    ToolRebuildContext context = new ToolRebuildContext(stack, ToolDefinition.EMPTY, ToolMaterials.EMPTY, modifiers, modifiers);
    for (ModifierEntry entry : modifiers.modifiers()) {
      entry.getHook(ModifierHooks.VOLATILE_DATA).addVolatileData(context, entry, volatileData);
    }

    return output;
  }

  /* Gets a copy of the stack with the given modifiers */
  @Deprecated(forRemoval = true)
  static ItemStack withModifiers(ItemStack stack, List<ModifierEntry> modifierList, Consumer<ModDataNBT> persistentDataConsumer) {
    return withModifiers(stack, 1, modifierList, persistentDataConsumer);
  }

  /* Gets a copy of the stack with the given modifiers */
  @Deprecated(forRemoval = true)
  static ItemStack withModifiers(ItemStack stack, int maxSize, List<ModifierEntry> modifierList, Consumer<ModDataNBT> persistentDataConsumer) {
    return withModifiers(stack, maxSize, ModifierEntries.create(modifierList), s -> persistentDataConsumer.accept(new ModDataNBT()));
  }


  /* Recipe errors */
  /** Error for when the tool has does not have enough existing levels of this modifier, has a single parameter, modifier with level */
  String KEY_MIN_LEVEL = TConstruct.makeTranslationKey("recipe", "modifier.min_level");
  /** Same as {@link #KEY_MIN_LEVEL} but for {@link #checkTraitLevel()} */
  String KEY_MIN_LEVEL_TRAITS = KEY_MIN_LEVEL + ".traits";
  /** Error for when the tool is at the max modifier level */
  String KEY_MAX_LEVEL = TConstruct.makeTranslationKey("recipe", "modifier.max_level");
  /** Same as {@link #KEY_MAX_LEVEL} but for {@link #checkTraitLevel()} */
  String KEY_MAX_LEVEL_TRAITS = KEY_MAX_LEVEL + ".traits";
  /** Error for when the tool has too few upgrade slots */
  String KEY_NOT_ENOUGH_SLOTS = TConstruct.makeTranslationKey("recipe", "modifier.not_enough_slots");
  /** Error for when the tool has too few upgrade slots from a single slot */
  String KEY_NOT_ENOUGH_SLOT = TConstruct.makeTranslationKey("recipe", "modifier.not_enough_slot");

  /**
   * Validates the level, returning an error if invalid.
   *
   * @param modifier        Modifier for errors.
   * @param levelRange      Range of valid levels after applying.
   * @param resultLevel     Level after applying this modifier.
   * @param checkTraitLevel If true, uses the trait error keys. If false, uses the upgrade error keys.
   * @return Error if levels are invalid, or {@code null} otherwise.
   */
  @Nullable
  static Component checkLevel(ModifierHolder modifier, IntRange levelRange, int resultLevel, boolean checkTraitLevel) {
    if (resultLevel < levelRange.min()) {
      return Component.translatable(checkTraitLevel ? KEY_MIN_LEVEL_TRAITS : KEY_MIN_LEVEL, modifier.get().getDisplayName(levelRange.min() - 1));
    }
    // max level of modifier
    if (resultLevel > levelRange.max()) {
      return Component.translatable(checkTraitLevel ? KEY_MAX_LEVEL_TRAITS : KEY_MAX_LEVEL, modifier.get().getDisplayName(), levelRange.max());
    }
    return null;
  }

  /**
   * Validate tool has the right number of slots to apply the given slot count.
   * @param tool   Tool instance.
   * @param slots  Required slots.
   * @return  Error message, or null if no error.
   */
  @Nullable
  static Component checkSlots(ToolStackView tool, @Nullable SlotCount slots) {
    if (slots != null) {
      int count = slots.count();
      if (tool.getFreeSlots(slots.type()) < count) {
        if (count == 1) {
          return Component.translatable(KEY_NOT_ENOUGH_SLOT, slots.type().getLowercaseName());
        } else {
          return Component.translatable(KEY_NOT_ENOUGH_SLOTS, count, slots.type().getLowercaseName());
        }
      }
    }
    return null;
  }
}
