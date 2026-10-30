package slimeknights.tconstruct.library.recipe.melting;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.MultiNamedRecipe;
import slimeknights.mantle.recipe.NamedRecipe;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialCastingLookup;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;

import java.util.Comparator;
import java.util.List;

/**
 * Recipe to melt all castable tool parts of a given material
 */
public class MaterialMeltingRecipe implements IMeltingRecipe, MultiNamedRecipe<IDisplayableMeltingRecipe> {
  public static final RecordLoadable<MaterialMeltingRecipe> LOADER = RecordLoadable.create(
    MaterialVariantId.LOADABLE.requiredField("input", r -> r.input.getVariant()),
    IntLoadable.FROM_ONE.requiredField("temperature", r -> r.temperature),
    FluidOutput.REQUIRED_LOADABLE.requiredField("result", r -> r.result),
    FluidOutput.REQUIRED_LOADABLE.list(0).defaultField("byproducts", List.of(), false, r -> r.byproducts),
    MaterialMeltingRecipe::new);

  private final MaterialVariant input;
  private final int temperature;
  private final FluidOutput result;
  private final List<FluidOutput> byproducts;

  public MaterialMeltingRecipe(MaterialVariantId input, int temperature, FluidOutput result, List<FluidOutput> byproducts) {
    this.input = MaterialVariant.of(input);
    this.temperature = temperature;
    this.result = result;
    this.byproducts = byproducts;
  }

  @Override
  public boolean matches(MeltingRecipeInput inv, Level worldIn) {
    if (input.isUnknown()) {
      return false;
    }
    ItemStack stack = inv.getItem();
    if (stack.isEmpty() || MaterialCastingLookup.getItemCost(stack.getItem()) == 0) {
      return false;
    }
    return input.matchesVariant(stack);
  }

  @Override
  public int getTemperature(MeltingRecipeInput inv) {
    return temperature;
  }

  @Override
  public int getTime(MeltingRecipeInput inv) {
    int cost = MaterialCastingLookup.getItemCost(inv.getItem().getItem());
    return IMeltingRecipe.calcTimeForAmount(temperature, result.getAmount() * cost);
  }

  @Override
  public FluidStack getOutput(MeltingRecipeInput inv) {
    int cost = MaterialCastingLookup.getItemCost(inv.getItem().getItem());
    return result.get().copyWithAmount(result.getAmount() * cost);
  }

  @Override
  public void handleByproducts(MeltingRecipeInput inv, IFluidHandler handler) {
    if (!byproducts.isEmpty()) {
      int cost = MaterialCastingLookup.getItemCost(inv.getItem().getItem());
      for (FluidOutput byproduct : byproducts) {
        handler.fill(byproduct.get().copyWithAmount(byproduct.getAmount() * cost), FluidAction.EXECUTE);
      }
    }
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerRecipes.materialMeltingSerializer.get();
  }


  /* JEI display */
  private List<NamedRecipe<IDisplayableMeltingRecipe>> multiRecipes = null;

  @Override
  public List<NamedRecipe<IDisplayableMeltingRecipe>> getRecipes(ResourceLocation id, HolderLookup.Provider provider) {
    if (multiRecipes == null) {
      if (input.get().isHidden()) {
        multiRecipes = List.of();
      } else {
        // grab and sort all parts that work
        MaterialVariantId inputId = input.getVariant();
        List<Entry<IMaterialItem>> entries = MaterialCastingLookup
          .getAllItemCosts().stream()
          .filter(entry -> entry.getKey().canUseMaterial(inputId.getId()))
          .sorted(Comparator.<Entry<IMaterialItem>,Integer>comparing(Entry::getIntValue).thenComparing(entry -> Loadables.ITEM.getKey(entry.getKey().asItem())))
          .toList();
        // if we found nothing, do nothing. Should never happen so error
        if (entries.isEmpty()) {
          TConstruct.LOG.warn("Failed to create display recipe for {}: found no tool parts that support {}", id, inputId);
          multiRecipes = List.of();
        } else {
          // start building the recipe
          DisplayMeltingRecipe.Builder recipe = DisplayMeltingRecipe.id(id).temperature(temperature).timeDynamic();
          // input items just use the material
          recipe.inputs(entries.stream().map(entry -> entry.getKey().withMaterialForDisplay(inputId)).toList());
          // fluids
          FluidStack output = this.result.get();
          recipe.outputs(entries.stream().map(entry -> output.copyWithAmount(output.getAmount() * entry.getIntValue())).toList());
          // if we have byproducts, scale those too
          for (FluidOutput byproduct : this.byproducts) {
            FluidStack fluid = byproduct.get();
            recipe.byproduct(entries.stream().map(entry -> fluid.copyWithAmount(fluid.getAmount() * entry.getIntValue())).toList());
          }
          this.multiRecipes = List.of(recipe.build());
        }
      }
    }
    return multiRecipes;
  }
}
