package slimeknights.tconstruct.library.recipe.casting.material;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.tconstruct.library.json.FluidStackSet;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;

import javax.annotation.Nullable;

import static slimeknights.tconstruct.library.recipe.melting.IMeltingRecipe.getTemperature;

/**
 * Builder to make parts and composites castable
 */
@Accessors(chain = true)
@RequiredArgsConstructor(staticName = "material")
public class MaterialFluidRecipeBuilder extends AbstractRecipeBuilder<MaterialFluidRecipeBuilder> {
  /** Output material ID */
  private final MaterialVariantId result;
  /** Fluid used for casting */
  @Setter
  private FluidStackSet fluid = FluidStackSet.EMPTY;
  /** Temperature for cooling time calculations */
  @Setter
  private int temperature = -1;
  /** Material base for composite */
  @Setter @Nullable
  private MaterialVariantId inputId;
  /** If true, this recipe will not show in book pages. */
  @Setter
  private boolean hideInBook = false;

  /**
   * Sets the fluid for this recipe, and cooling time if unset.
   * @param fluid   Fluid object
   * @param amount  Fluid amount
   * @return  Builder instance
   */
  public MaterialFluidRecipeBuilder setFluidAndTemp(FluidObject<?> fluid, int amount) {
    this.fluid = FluidStackSet.of(fluid.holderSet(), amount);
    if (this.temperature == -1) {
      this.temperature = getTemperature(fluid);
    }
    return this;
  }

  /**
   * Sets the fluid for this recipe, and cooling time if unset.
   * Note {@link FluidStack#getComponentsPatch()} will be ignored.
   * @param fluid  Fluid input
   * @return  Builder instance
   */
  public MaterialFluidRecipeBuilder setFluidAndTemp(FluidStack fluid) {
    this.fluid = FluidStackSet.of(fluid.getFluid(), fluid.getAmount());
    if (this.temperature == -1) {
      this.temperature = getTemperature(fluid);
    }
    return this;
  }

  /**
   * Sets the fluid for this recipe, and cooling time
   * @param tagIn   Tag<Fluid> instance
   * @param amount  Fluid amount
   */
  public MaterialFluidRecipeBuilder setFluid(TagKey<Fluid> tagIn, int amount) {
    setFluid(FluidStackSet.of(tagIn, amount));
    return this;
  }

  @Override
  public void save(RecipeOutput output) {
    save(output, result.getId());
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    if (this.fluid == FluidStackSet.EMPTY) {
      throw new IllegalStateException("Material fluid recipes require a fluid input");
    }
    if (this.temperature < 0) {
      throw new IllegalStateException("Temperature is too low, must be at least 0");
    }
    output.accept(id, new MaterialFluidRecipe(fluid, temperature, inputId, result, hideInBook), buildOptionalAdvancement(output, id, "materials"));
  }
}
