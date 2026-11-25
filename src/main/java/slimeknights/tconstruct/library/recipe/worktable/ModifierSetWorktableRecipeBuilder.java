package slimeknights.tconstruct.library.recipe.worktable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.tconstruct.library.json.predicate.modifier.ModifierPredicate;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.component.SetComponent;

import java.util.function.Supplier;

/** Builder for recipes to add or remove a modifier from a set in persistent data */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ModifierSetWorktableRecipeBuilder extends AbstractSizedIngredientRecipeBuilder<ModifierSetWorktableRecipeBuilder> {
  private final DataComponentType<SetComponent<ModifierId>> component;
  @Setter @Accessors(fluent = true)
  private IJsonPredicate<ModifierId> modifierPredicate = ModifierPredicate.ANY;
  private final boolean addToSet;
  private Ingredient tools = AbstractWorktableRecipe.DEFAULT_TOOLS;
  private boolean allowTraits = false;

  /** Creates a new recipe for adding to a set */
  public static ModifierSetWorktableRecipeBuilder setAdding(DataComponentType<SetComponent<ModifierId>> component) {
    return new ModifierSetWorktableRecipeBuilder(component, true);
  }

  /** Creates a new recipe for removing from a set */
  public static ModifierSetWorktableRecipeBuilder setRemoving(DataComponentType<SetComponent<ModifierId>> component) {
    return new ModifierSetWorktableRecipeBuilder(component, false);
  }

  /** Creates a new recipe for adding to a set */
  public static ModifierSetWorktableRecipeBuilder setAdding(Supplier<DataComponentType<SetComponent<ModifierId>>> component) {
    return setAdding(component.get());
  }

  /** Creates a new recipe for removing from a set */
  public static ModifierSetWorktableRecipeBuilder setRemoving(Supplier<DataComponentType<SetComponent<ModifierId>>> component) {
    return setRemoving(component.get());
  }

  /** Sets the tool requirement for this recipe */
  public ModifierSetWorktableRecipeBuilder setTools(Ingredient ingredient) {
    this.tools = ingredient;
    return this;
  }

  /** Sets the tool requirement for this recipe */
  public ModifierSetWorktableRecipeBuilder setTools(TagKey<Item> tag) {
    return this.setTools(Ingredient.of(tag));
  }

  /** Sets the recipe to allow traits */
  public ModifierSetWorktableRecipeBuilder allowTraits() {
    allowTraits = true;
    return this;
  }

  @Override
  public void save(RecipeOutput output) {
    save(output, Loadables.DATA_COMPONENT_TYPE.getKey(component));
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    if (inputs.isEmpty()) {
      throw new IllegalStateException("Must have at least one ingredient");
    }
    if (tools == Ingredient.EMPTY) {
      throw new IllegalStateException("Tools cannot be empty");
    }
    output.accept(id, new ModifierSetWorktableRecipe(component, inputs, tools, modifierPredicate, addToSet, allowTraits), buildOptionalAdvancement(output, id, "modifiers"));
  }
}
