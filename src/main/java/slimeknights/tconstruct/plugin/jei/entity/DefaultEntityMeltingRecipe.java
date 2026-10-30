package slimeknights.tconstruct.plugin.jei.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.common.util.Lazy;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.ingredient.EntityIngredient;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.recipe.entitymelting.EntityMeltingRecipe;
import slimeknights.tconstruct.smeltery.block.entity.module.EntityMeltingModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension of entity melting recipe for the sake of displaying entities in the default "recipe"
 */
public class DefaultEntityMeltingRecipe extends EntityMeltingRecipe {
  /**
   * Gets a list of entity types, filtered by the recipe list
   * @param recipes  Recipe list
   * @return List of entity types
   */
  private static EntityIngredient getEntityList(List<EntityMeltingRecipe> recipes) {
    List<EntityType<?>> unusedTypes = new ArrayList<>();
    typeLoop:
    for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
      // use tag overrides for default recipe
      if (type.is(TinkerTags.EntityTypes.MELTING_HIDE)) continue;
      if (type.getCategory() == MobCategory.MISC && !type.is(TinkerTags.EntityTypes.MELTING_SHOW)) continue;
      for (EntityMeltingRecipe recipe : recipes) {
        if (recipe.matches(type)) {
          continue typeLoop;
        }
      }
      unusedTypes.add(type);
    }
    return EntityIngredient.of(unusedTypes);
  }

  private final Lazy<EntityIngredient> entities;
  public DefaultEntityMeltingRecipe(List<EntityMeltingRecipe> recipes) {
    super(EntityIngredient.EMPTY, FluidOutput.fromStack(EntityMeltingModule.getDefaultFluid()), 2);
    entities = Lazy.of(() -> getEntityList(recipes));
  }

  @Override
  public EntityIngredient getIngredient() {
    return entities.get();
  }
}
