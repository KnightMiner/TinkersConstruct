package slimeknights.tconstruct.library.recipe.modifiers.adding;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.modifiers.ModifierHolder;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.modifiers.adding.variant.SwappableVariant;

/**
 * Builder for a modifier with a swappable string key.
 * @see SwappableModifierRecipe
 * @see ModifierRecipeBuilder
 */
public class SwappableModifierRecipeBuilder extends ModifierRecipeBuilder {
  private final SwappableVariant value;
  protected SwappableModifierRecipeBuilder(ModifierId modifier, SwappableVariant value) {
    super(modifier);
    this.value = value;
    // most variants do not want this as that will conflict, explicitly set it on the default if desired
    this.allowCrystal = false;
  }

  /** Creates a new builder */
  public static SwappableModifierRecipeBuilder modifier(ModifierId modifier, SwappableVariant value) {
    return new SwappableModifierRecipeBuilder(modifier, value);
  }

  /** Creates a new builder */
  public static SwappableModifierRecipeBuilder modifier(ModifierHolder modifier, SwappableVariant value) {
    return modifier(modifier.id(), value);
  }

  /** @deprecated Min level is always 1 for swappable. */
  @Override
  @Deprecated
  public ModifierRecipeBuilder setMinLevel(int level) {
    return this;
  }

  /** @deprecated Max level is always 1 for swappable. */
  @Override
  @Deprecated
  public ModifierRecipeBuilder setMaxLevel(int level) {
    return this;
  }

  /* Building */

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    if (inputs.isEmpty()) {
      throw new IllegalStateException("Must have at least 1 input");
    }
    output.accept(id, new SwappableModifierRecipe(inputs, tools, maxToolSize, result, value, slots, allowCrystal), buildOptionalAdvancement(output, id, "modifiers"));
  }
}
