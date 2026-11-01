package slimeknights.tconstruct.library.recipe.display;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Interface for vanilla recipes to allow them to expose filtered recipes. Used since JEI will not filter the list to remove filtered recipes from the recipe manager.
 * To effectively use this interface, you must return true for {@link Recipe#isSpecial()}, and then return an object or list of objects extending the proper vanilla class that is not the recipe itself.
 */
public interface VanillaFilteredRecipe<T extends Recipe<?> & FilteredRecipe> {
  /**
   * Gets a list of filtered recipes for adding to a recipe manager plugin.
   * @param id          Parent recipe ID. Used to generate nested IDs or simply for the ID for {@link RecipeHolder}
   * @param registries  Registry access instance
   * @return  List of recipes
   */
  List<RecipeHolder<T>> getFilteredRecipes(ResourceLocation id, HolderLookup.Provider registries);


  /** Gets a  modifiable list of filtered recipes matching the given recipe type. */
  @SuppressWarnings("unchecked")
  static <T extends R, C extends RecipeInput, R extends Recipe<C>> List<RecipeHolder<T>> getRecipes(HolderLookup.Provider registries, RecipeManager manager, RecipeType<R> type, Class<T> recipeClass) {
    return manager.byType(type).stream()
      .flatMap(recipe -> recipe.value() instanceof VanillaFilteredRecipe<?> filtered ? filtered.getFilteredRecipes(recipe.id(), registries).stream() : Stream.empty())
      .filter(recipe -> recipe.value().isFiltered() && recipeClass.isInstance(recipe.value()))
      .map(recipe -> (RecipeHolder<T>)recipe)
      .collect(Collectors.toList()); // intentionally a mutable list
  }
}
