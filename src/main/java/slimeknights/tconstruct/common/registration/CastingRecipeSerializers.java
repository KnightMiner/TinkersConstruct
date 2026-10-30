package slimeknights.tconstruct.common.registration;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.registration.holder.DeferredRecipeSerializer;
import slimeknights.tconstruct.library.recipe.casting.CastingType;

/** Holds a pair of table and basin recipe serializers for casting recipes */
public record CastingRecipeSerializers<T extends Recipe<?>>(DeferredRecipeSerializer<T> table, DeferredRecipeSerializer<T> basin) {
  /** Gets the serializer for the given casting type */
  public RecipeSerializer<T> get(CastingType type) {
    return (type == CastingType.TABLE ? table : basin).get();
  }
}
