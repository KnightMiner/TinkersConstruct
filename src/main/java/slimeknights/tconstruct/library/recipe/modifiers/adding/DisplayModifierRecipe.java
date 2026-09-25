package slimeknights.tconstruct.library.recipe.modifiers.adding;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.recipe.ingredient.SizedIngredient;
import slimeknights.tconstruct.library.json.IntRange;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.tools.SlotType.SlotCount;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/** Recipe instance to return in JEI from recipes that contain multiple display recipes */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DisplayModifierRecipe implements IDisplayModifierRecipe {
  @Nullable
  private final ResourceLocation recipeId;
  private final List<List<ItemStack>> inputs;
  private final List<ItemStack> toolWithoutModifier;
  private final List<ItemStack> toolWithModifier;
  private final int maxToolSize;
  private final Predicate<ItemStack> isTool;
  private final ModifierEntry displayResult;
  private final IntRange level;
  @Nullable
  private final SlotCount slots;
  private final List<SlotCount> resultSlots;
  private final boolean incremental;
  @Accessors(fluent = true)
  private final boolean checkTraitLevel;

  @Override
  public int getInputCount() {
    return inputs.size();
  }

  @Override
  public List<ItemStack> getDisplayItems(int slot) {
    if (slot >= 0 && slot < inputs.size()) {
      return inputs.get(slot);
    }
    return List.of();
  }

  @Override
  public boolean isTool(ItemStack check) {
    return isTool != null ? isTool.test(check) : IDisplayModifierRecipe.super.isTool(check);
  }


  /* Builder */

  /** Creates a new builder instance */
  public static Builder builder() {
    return new Builder();
  }

  /** Resolves a list of sized ingredients into a list of item stack lists */
  private static List<List<ItemStack>> resolve(List<SizedIngredient> ingredients) {
    return ingredients.stream().map(SizedIngredient::getMatchingStacks).toList();
  }

  /** Builder for creating a display recipe */
  @Setter
  @Accessors(fluent = true)
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private ModifierEntry result = ModifierEntry.EMPTY;
    @Nullable
    private ResourceLocation id = null;
    private List<List<ItemStack>> inputs = List.of();
    /** Predicate to check if the focus is a valid tool for this recipe. If unset, uses {@link #toolWithoutModifier} */
    private Predicate<ItemStack> isTool;
    private List<ItemStack> toolWithoutModifier = List.of();
    private List<ItemStack> toolWithModifier = List.of();
    private IntRange level = ModifierEntry.VALID_LEVEL;
    @Nullable
    private SlotCount slots = null;
    private List<SlotCount> resultSlots = List.of();
    private boolean incremental = false;
    /** If true, this recipe uses the trait level for its level range instead of just crafted levels */
    private boolean checkTraitLevel = false;
    /** Maximum stack size for dynamically computed tools. The minimum between this and the stack's max stack size will be used. */
    private int maxToolSize = ITinkerStationRecipe.DEFAULT_TOOL_STACK_SIZE;

    /** Creates a copy of this builder with the same properties */
    public Builder copy() {
      Builder copy = new Builder();
      copy.id = this.id;
      copy.inputs = this.inputs;
      copy.toolWithoutModifier = this.toolWithoutModifier;
      copy.toolWithModifier = this.toolWithModifier;
      copy.isTool = this.isTool;
      copy.level = this.level;
      copy.slots = this.slots;
      copy.result = this.result;
      copy.resultSlots = this.resultSlots;
      copy.incremental = this.incremental;
      copy.checkTraitLevel = this.checkTraitLevel;
      return copy;
    }

    /** Sets the inputs to a list of ingredients */
    public Builder ingredients(List<SizedIngredient> inputs) {
      return inputs(resolve(inputs));
    }

    /** Sets this display recipe to be incremental */
    public Builder incremental() {
      return incremental(true);
    }

    /** Builds the final recipe */
    public IDisplayModifierRecipe build() {
      if (result == ModifierEntry.EMPTY) {
        throw new IllegalStateException("Must set result");
      }
      if (inputs.isEmpty()) {
        throw new IllegalStateException("Must set inputs");
      }
      if (toolWithoutModifier.isEmpty()) {
        throw new IllegalStateException("Must set tools without modifier");
      }
      if (toolWithModifier.isEmpty()) {
        throw new IllegalStateException("Must set tools with modifier");
      }
      return new DisplayModifierRecipe(id, inputs, toolWithoutModifier, toolWithModifier, maxToolSize, isTool, result, level, slots, resultSlots, incremental, checkTraitLevel);
    }
  }
}
