package slimeknights.tconstruct.library.recipe.tinkerstation.building;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.recipe.crafting.ShapedExtensionBuilder;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for {@link ShapedMaterialsRecipe}.
 * @see ShapelessMaterialsRecipeBuilder
 */
public class ShapedMaterialsRecipeBuilder extends ShapedExtensionBuilder<ShapedMaterialsRecipeBuilder> {
  @Accessors(fluent = true)
  @Setter
  private String parts = "";
  private final List<MaterialVariantId> extraMaterials = new ArrayList<>();
  protected ShapedMaterialsRecipeBuilder(ItemStack result) {
    super(result);
  }

  /** Creates a builder for the given stack */
  public static ShapedMaterialsRecipeBuilder shaped(ItemStack result) {
    return new ShapedMaterialsRecipeBuilder(result);
  }

  /** Creates a builder for the given item and count */
  public static ShapedMaterialsRecipeBuilder shaped(ItemLike result, int count) {
    return shaped(new ItemStack(result, count));
  }

  /** Creates a builder for the given item */
  public static ShapedMaterialsRecipeBuilder shaped(ItemLike result) {
    return shaped(result, 1);
  }

  /** Adds a fixed material included after the part materials */
  public ShapedMaterialsRecipeBuilder extraMaterial(MaterialVariantId material) {
    extraMaterials.add(material);
    return this;
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    if (parts.isEmpty()) {
      throw new IllegalArgumentException("Parts may not be empty");
    }
    for (char part : parts.toCharArray()) {
      if (!this.key.containsKey(part)) {
        throw new IllegalStateException("Parts references symbol '" + parts + "' but it's not defined in the key");
      }
    }
    output.accept(id, new ShapedMaterialsRecipe(group, getBookCategory(), new ShapedMaterialsRecipe.Pattern(getPattern(), List.of(), parts), result, showNotification, extraMaterials), buildAdvancement(output, id, category.getFolderName()));
  }
}
