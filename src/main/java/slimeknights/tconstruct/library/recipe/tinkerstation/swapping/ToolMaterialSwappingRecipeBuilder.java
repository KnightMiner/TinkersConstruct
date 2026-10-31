package slimeknights.tconstruct.library.recipe.tinkerstation.swapping;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;

import java.util.ArrayList;
import java.util.List;

/** Builder for {@link PartMaterialSwappingRecipe} and {@link ToolMaterialSwappingRecipe} */
@RequiredArgsConstructor(staticName = "tools")
public class ToolMaterialSwappingRecipeBuilder extends AbstractRecipeBuilder<ToolMaterialSwappingRecipeBuilder> {
  private final Ingredient tools;
  private boolean fromTool = false;
  @Setter
  @Accessors(fluent = true)
  private int maxStackSize = 16;
  /** Additional requirements beyond the "part" */
  private final List<SizedIngredient> extraRequirements = new ArrayList<>();

  /** Sets the swapping to be from a tool instead of from a part */
  public ToolMaterialSwappingRecipeBuilder fromTool() {
    this.fromTool = true;
    return this;
  }

  /** Adds an extra ingredient requirement */
  public ToolMaterialSwappingRecipeBuilder addExtraRequirement(SizedIngredient ingredient) {
    extraRequirements.add(ingredient);
    return this;
  }

  /** Adds an extra ingredient requirement */
  public ToolMaterialSwappingRecipeBuilder addExtraRequirement(Ingredient ingredient) {
    return addExtraRequirement(new SizedIngredient(ingredient, 1));
  }

  /** Adds an extra ingredient requirement */
  public ToolMaterialSwappingRecipeBuilder addExtraRequirement(ItemLike... items) {
    return addExtraRequirement(Ingredient.of(items));
  }

  @Override
  public void save(RecipeOutput output) {
    save(output, Loadables.ITEM.getKey(tools.getItems()[0].getItem()));
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    if (fromTool) {
      output.accept(id, new ToolMaterialSwappingRecipe(tools, extraRequirements), null);
    } else {
      output.accept(id, new PartMaterialSwappingRecipe(tools, maxStackSize, extraRequirements), null);
    }
  }
}
