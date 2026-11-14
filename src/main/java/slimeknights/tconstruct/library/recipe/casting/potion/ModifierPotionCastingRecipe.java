package slimeknights.tconstruct.library.recipe.casting.potion;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

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
    return super.matches(inv, level) && ModifierUtil.getModifierLevel(inv.getItem(), modifier) > 0;
  }


  /* JEI */

  /** Adds the modifier to the given stack */
  public ItemStack addModifier(ItemStack stack) {
    return IDisplayModifierRecipe.withModifiers(IModifiableDisplay.getDisplayStack(stack), List.of(new ModifierEntry(modifier, 1)));
  }

  /** Gets a list of all potion IDs ready for usage in building NBT */
  public static List<String> getPotionIds() {
    return getPotions().map(holder -> holder.key().location().toString()).toList();
  }

  /**
   * Adds the potion to the given stacks.
   * @param stack    Stack with the modifier.
   * @param potions  List of potions from {@link #getPotionIds()}
   * @return  Stacks with potions set.
   */
  public List<ItemStack> addPotion(ItemStack stack, List<String> potions) {
    return potions.stream().map(id -> {
      ToolStack tool = ToolStack.copyFrom(stack);
      tool.getPersistentData().putString(modifier, id);
      return tool.copyStack(stack);
    }).toList();
  }
}
