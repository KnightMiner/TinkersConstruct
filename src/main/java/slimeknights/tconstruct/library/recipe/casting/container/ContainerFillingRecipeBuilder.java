package slimeknights.tconstruct.library.recipe.casting.container;

import lombok.AllArgsConstructor;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.library.recipe.casting.ICastingRecipe;

/**
 * Builder for a container filling recipe. Takes an arbitrary fluid for a specific amount to fill a Forge {@link net.neoforged.neoforge.fluids.capability.IFluidHandlerItem}
 */
@AllArgsConstructor(staticName = "castingRecipe")
public class ContainerFillingRecipeBuilder extends AbstractRecipeBuilder<ContainerFillingRecipeBuilder> {
  private final RecipeType<? extends ICastingRecipe> type;
  private final RecipeSerializer<? extends ContainerFillingRecipe> serializer;
  private final Item result;
  private final int fluidAmount;

  /** Creates a new builder for the casting basin with the given result name */
  public static ContainerFillingRecipeBuilder tableRecipe(ItemLike result, int fluidAmount) {
    return castingRecipe(TinkerRecipes.castingTableType.get(), TinkerRecipes.containerFilling.getTable(), result.asItem(), fluidAmount);
  }

  /** Creates a new builder for the casting basin with the given result name */
  public static ContainerFillingRecipeBuilder basinRecipe(ItemLike result, int fluidAmount) {
    return castingRecipe(TinkerRecipes.castingBasinType.get(), TinkerRecipes.containerFilling.getBasin(), result.asItem(), fluidAmount);
  }

  @Override
  public void save(RecipeOutput output) {
    this.save(output, Loadables.ITEM.getKey(result));
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    output.accept(id, new ContainerFillingRecipe(type, serializer, group, fluidAmount, result), buildOptionalAdvancement(output, id, "casting"));
  }
}
