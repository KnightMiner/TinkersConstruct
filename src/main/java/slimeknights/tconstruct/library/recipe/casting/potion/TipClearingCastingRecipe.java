package slimeknights.tconstruct.library.recipe.casting.potion;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.NamedRecipe;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.casting.CastingRecipeInput;
import slimeknights.tconstruct.library.recipe.casting.DisplayCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.IDisplayableCastingRecipe;

import java.util.Arrays;
import java.util.List;

/** Casting recipe clearing the potion from a tool. */
public class TipClearingCastingRecipe extends ModifierPotionCastingRecipe {
  public static final RecordLoadable<TipClearingCastingRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.TYPE.requiredField(), LoadableRecipeSerializer.SERIALIZER.requiredField(), LoadableRecipeSerializer.RECIPE_GROUP,
    TOOL_FIELD, FLUID_FIELD, COOLING_TIME_FIELD, MODIFIER_FIELD, TipClearingCastingRecipe::new);

  public TipClearingCastingRecipe(RecipeType<?> type, RecipeSerializer<?> serializer, String group, Ingredient tool, SizedFluidIngredient fluid, int coolingTime, ModifierId modifier) {
    super(type, serializer, group, tool, fluid, Items.AIR, coolingTime, modifier);
  }

  @Override
  public boolean matches(CastingRecipeInput inv, Level level) {
    // must have the modifier, and the potion set
    return super.matches(inv, level) && inv.getItem().has(DataComponents.POTION_CONTENTS);
  }

  @Override
  public ItemStack assemble(CastingRecipeInput inv, HolderLookup.Provider registries) {
    ItemStack result = inv.getItem().copy();
    result.remove(DataComponents.POTION_CONTENTS);
    return result;
  }


  /* JEI */
  @Override
  public List<NamedRecipe<IDisplayableCastingRecipe>> getRecipes(ResourceLocation id, HolderLookup.Provider registries) {
    if (displayRecipes == null) {
      List<FluidStack> fluids = List.of(fluid.getFluids());
      List<PotionContents> potions = getPotions();

      // create a recipe per tool
      List<ResourceLocation> parent = List.of(id);
      displayRecipes = Arrays.stream(bottle.getItems()).map(stack -> {
        // start with just the tool with the modifier
        ItemStack withModifier = addModifier(stack);
        return DisplayCastingRecipe.id(id.withSuffix('/' + Loadables.ITEM.getKey(stack.getItem()).toDebugFileName()), parent)
          .casts(addPotions(withModifier, potions)).consumed()
          .result(withModifier).unlinkOutput()
          .fluids(fluids).coolingTime(coolingTime).build();
      }).toList();
    }
    return displayRecipes;
  }
}
