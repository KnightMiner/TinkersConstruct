package slimeknights.tconstruct.common.registration;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.registration.holder.DeferredRecipeSerializer;

/** Holds a pair of table and basin recipe serializers for casting recipes */
public record CastingRecipeSerializers<T extends Recipe<?>>(DeferredRecipeSerializer<T> table, DeferredRecipeSerializer<T> basin) {
  /** Gets the table serializer */
  public RecipeSerializer<T> getTable() {
    return table.get();
  }

  /** Gets the basin serializer */
  public RecipeSerializer<T> getBasin() {
    return basin.get();
  }
}
