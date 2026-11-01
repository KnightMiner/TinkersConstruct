package slimeknights.tconstruct.library.recipe.tinkerstation.building;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.recipe.crafting.ShapelessExtensionBuilder;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for {@link ShapelessMaterialsRecipe}
 * @see ShapedMaterialsRecipeBuilder
 */
public class ShapelessMaterialsRecipeBuilder extends ShapelessExtensionBuilder<ShapelessMaterialsRecipeBuilder> {
  @Accessors(fluent = true)
  @Setter
  private int parts;
  private final List<MaterialVariantId> extraMaterials = new ArrayList<>();
  protected ShapelessMaterialsRecipeBuilder(ItemStack result) {
    super(result);
  }

  /** Creates a builder for the given stack */
  public static ShapelessMaterialsRecipeBuilder shapeless(ItemStack result) {
    return new ShapelessMaterialsRecipeBuilder(result);
  }

  /** Creates a builder for the given item and count */
  public static ShapelessMaterialsRecipeBuilder shapeless(ItemLike result, int count) {
    return shapeless(new ItemStack(result, count));
  }

  /** Creates a builder for the given item */
  public static ShapelessMaterialsRecipeBuilder shapeless(ItemLike result) {
    return shapeless(result, 1);
  }

  /** Adds a fixed material included after the part materials */
  public ShapelessMaterialsRecipeBuilder extraMaterial(MaterialVariantId material) {
    extraMaterials.add(material);
    return this;
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    if (parts <= 0) {
      throw new IllegalArgumentException("Parts must be greater than 0");
    }
    output.accept(id, new ShapelessMaterialsRecipe(group, getBookCategory(), result, ingredients, parts, extraMaterials), buildAdvancement(output, id, category.getFolderName()));
  }
}
