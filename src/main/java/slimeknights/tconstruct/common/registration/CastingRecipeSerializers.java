package slimeknights.tconstruct.common.registration;

import net.minecraft.world.item.crafting.Recipe;
import slimeknights.mantle.registration.holder.DeferredRecipeSerializer;

/** Holds a pair of table and basin recipe serializers for casting recipes */
public record CastingRecipeSerializers<T extends Recipe<?>>(DeferredRecipeSerializer<T> table, DeferredRecipeSerializer<T> basin) {}
