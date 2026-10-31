package slimeknights.tconstruct.library.recipe.casting.potion;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.NamedRecipe;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.casting.CastingRecipeInput;
import slimeknights.tconstruct.library.recipe.casting.DisplayCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.IDisplayableCastingRecipe;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Casting recipe applying a potion to a tool. */
public class TippingCastingRecipe extends ModifierPotionCastingRecipe {
  protected static final LoadableField<Ingredient, ModifierPotionCastingRecipe> TOOL_FIELD = ModifierPotionCastingRecipe.TOOL_FIELD;
  public static final RecordLoadable<TippingCastingRecipe> LOADER = RecordLoadable.create(
    LoadableRecipeSerializer.TYPE.requiredField(), LoadableRecipeSerializer.SERIALIZER.requiredField(), LoadableRecipeSerializer.RECIPE_GROUP,
    TOOL_FIELD, FLUID_FIELD, COOLING_TIME_FIELD, MODIFIER_FIELD, TippingCastingRecipe::new);

  public TippingCastingRecipe(RecipeType<?> type, RecipeSerializer<?> serializer, String group, Ingredient tool, SizedFluidIngredient fluid, int coolingTime, ModifierId modifier) {
    super(type, serializer, group, tool, fluid, Items.AIR, coolingTime, modifier);
  }

  @Nullable
  private Holder<Potion> getPotion(CastingRecipeInput input) {
    return input.getFluid().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().orElse(null);
  }

  @Override
  public boolean matches(CastingRecipeInput input, Level level) {
    // must have the modifier to cast
    if (super.matches(input, level)) {
      // must also have a specific potion, it's what we are going to copy
      // but it can't match what is already on the stack
      Holder<Potion> potion = getPotion(input);
      return potion != null
        && !ModifierUtil.getPersistentString(input.getItem(), modifier).equals(Objects.requireNonNull(potion.getKey()).location().toString());
    }
    return false;
  }

  @Override
  public ItemStack assemble(CastingRecipeInput input, HolderLookup.Provider registries) {
    ItemStack result = input.getItem().copy();
    Holder<Potion> potion = getPotion(input);
    if (potion != null) {
      ToolStack.from(result).getPersistentData().putString(modifier, Objects.requireNonNull(potion.getKey()).location().toString());
    }
    return result;
  }


  /* JEI */

  @Override
  public List<NamedRecipe<IDisplayableCastingRecipe>> getRecipes(ResourceLocation id, HolderLookup.Provider registries) {
    if (displayRecipes == null) {
      // first, get a list of potion IDs
      List<PotionContents> potionContents = getPotions().map(PotionContents::new).toList();

      // next, make 1 copy of the potion fluid with NBT list per potion fluid
      List<FluidStack> basePotions = List.of(this.fluid.getFluids());
      List<FluidStack> fluids = basePotions.stream()
        .flatMap(fluid -> potionContents.stream().map(contents -> {
          FluidStack copy = fluid.copy();
          copy.set(DataComponents.POTION_CONTENTS, contents);
          return copy;
        })).toList();

      // finally, create 1 recipe per input tool
      int basePotionCount = basePotions.size();
      List<ResourceLocation> parent = List.of(id);
      List<String> potions = getPotionIds();
      displayRecipes = Arrays.stream(bottle.getItems()).map(stack -> {
        // start with just the tool with the modifier
        ItemStack withModifier = addModifier(stack);
        // next, add the potion to the tools
        List<ItemStack> withPotion = addPotion(withModifier, potions);
        // duplicate the list if we have multiple potion fluids
        if (basePotionCount > 1) {
          List<ItemStack> list = new ArrayList<>(withPotion.size() * basePotionCount);
          for (int i = 0; i < basePotionCount; i++) {
            list.addAll(withPotion);
          }
          withPotion = List.copyOf(list);
        }
        // and finally create the recipe
        return DisplayCastingRecipe.id(id.withSuffix('/' + Loadables.ITEM.getKey(stack.getItem()).toDebugFileName()), parent)
          .cast(withModifier).consumed()
          .results(withPotion).fluids(fluids).linkFluidsToOutput()
          .coolingTime(coolingTime).build();
      }).toList();
    }
    return displayRecipes;
  }
}
