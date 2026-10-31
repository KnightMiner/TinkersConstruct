package slimeknights.tconstruct.library.recipe.partbuilder;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;

/** Builder for a material item part crafting recipe in {@link MaterialPartRecipe}. */
@Setter
@Accessors(chain = true)
@RequiredArgsConstructor(staticName = "part")
public class MaterialPartRecipeBuilder extends AbstractRecipeBuilder<MaterialPartRecipeBuilder> {
  private final IMaterialItem result;
  private final int resultCount;
  private int cost = 1;
  private Pattern pattern = null;
  private Ingredient patternItem = IPartBuilderRecipe.DEFAULT_PATTERNS;
  private boolean allowUncraftable = false;

  /**
   * Creates a new part recipe that outputs a single item
   * @param output  Output item
   * @return  Builder instance
   */
  public static MaterialPartRecipeBuilder part(IMaterialItem output) {
    return part(output, 1);
  }

  /** Sets the pattern in the builder */
  public MaterialPartRecipeBuilder setPattern(Pattern pattern) {
    this.pattern = pattern;
    return this;
  }

  /** Sets the pattern in the builder */
  public MaterialPartRecipeBuilder setPattern(ResourceLocation pattern) {
    return setPattern(new Pattern(pattern));
  }

  /** Sets the pattern to match the given item */
  public MaterialPartRecipeBuilder setPattern(ItemLike pattern) {
    return setPattern(Pattern.fromItem(pattern));
  }

  @Override
  public void save(RecipeOutput output) {
    this.save(output, Loadables.ITEM.getKey(this.result.asItem()));
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    if (this.resultCount <= 0) {
      throw new IllegalStateException("recipe " + id + " must output at least 1");
    }
    if (this.cost <= 0) {
      throw new IllegalStateException("recipe " + id + " has no cost associated with it");
    }
    if (this.pattern == null) {
      throw new IllegalStateException("recipe " + id + " has no pattern associated with it");
    }
    ResourceLocation advancementId = this.buildOptionalAdvancement(id, "parts");
    output(id, new MaterialPartRecipe(group, pattern, patternItem, cost, allowUncraftable, result, resultCount), buildOptionalAdvancement(output, id, "parts"));
  }
}
