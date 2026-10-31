package slimeknights.tconstruct.library.recipe.modifiers.severing;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.ingredient.EntityIngredient;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;

public class AgeableSeveringRecipe extends SeveringRecipe {
  /** Loader instance */
  public static final RecordLoadable<AgeableSeveringRecipe> LOADER = RecordLoadable.create(
    ENTITY_FIELD,
    ItemOutput.REQUIRED_STACK_LOADABLE.requiredField("adult_result", r -> r.output),
    ItemOutput.OPTIONAL_STACK_LOADABLE.defaultField("child_result", ItemOutput.EMPTY, false, r -> r.childOutput),
    BASE_CHANCE_FIELD, LOOTING_BONUS_FIELD,
    AgeableSeveringRecipe::new);

  private final ItemOutput childOutput;
  public AgeableSeveringRecipe(EntityIngredient ingredient, ItemOutput adultOutput, ItemOutput childOutput, float baseChance, float lootingBonus) {
    super(ingredient, adultOutput, baseChance, lootingBonus);
    this.childOutput = childOutput;
  }

  @Override
  public ItemStack getOutput(Entity entity) {
    if (entity instanceof LivingEntity && ((LivingEntity) entity).isBaby()) {
      return childOutput.get().copy();
    }
    return getOutput().copy();
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerRecipes.ageableSevering.get();
  }
}
