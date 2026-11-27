package slimeknights.tconstruct.library.recipe.casting.potion;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.casting.CastingRecipeInput;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.tools.component.ModifierEntries;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;

import java.util.List;

/** Common logic for {@link TippingCastingRecipe} and {@link TipClearingCastingRecipe} */
public abstract class ModifierPotionCastingRecipe extends PotionCastingRecipe {
  protected static final LoadableField<Ingredient, ModifierPotionCastingRecipe> TOOL_FIELD = Loadables.ITEM_INGREDIENT_NONEMPTY.requiredField("tools", r -> r.bottle);
  protected static final LoadableField<ModifierId, ModifierPotionCastingRecipe> MODIFIER_FIELD = ModifierId.LOADABLE.requiredField("modifier", r -> r.modifier);

  protected final ModifierId modifier;
  public ModifierPotionCastingRecipe(RecipeType<?> type, RecipeSerializer<?> serializer, String group, Ingredient bottle, SizedFluidIngredient fluid, Item result, int coolingTime, ModifierId modifier) {
    super(type, serializer, group, bottle, fluid, result, coolingTime);
    this.modifier = modifier;
  }

  @Override
  public boolean matches(CastingRecipeInput inv, Level level) {
    return super.matches(inv, level) && ModifierEntries.getLevel(inv.getItem(), modifier) > 0;
  }


  /* JEI */

  /** Adds the modifier to the given stack */
  public ItemStack addModifier(ItemStack stack) {
    return IDisplayModifierRecipe.withModifiers(IModifiableDisplay.getDisplayStack(stack), ModifierEntries.create(new ModifierEntry(modifier, 1)));
  }

  /**
   * Adds the potion contents to the given stacks.
   * @param stack    Stack with the modifier.
   * @param contents  List of potions from {@link #getPotions()}
   * @return  Stacks with potions set.
   */
  public List<ItemStack> addPotions(ItemStack stack, List<PotionContents> contents) {
    return contents.stream().map(content -> {
      ItemStack copy = stack.copy();
      copy.set(DataComponents.POTION_CONTENTS, content);
      return copy;
    }).toList();
  }
}
