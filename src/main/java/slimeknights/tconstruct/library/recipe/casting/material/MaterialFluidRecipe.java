package slimeknights.tconstruct.library.recipe.casting.material;

import lombok.Getter;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.data.loadable.primitive.BooleanLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.ICustomOutputRecipe;
import slimeknights.tconstruct.library.json.FluidStackSet;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.library.recipe.casting.CastingRecipeInput;
import slimeknights.tconstruct.library.recipe.material.IDisplayMaterialRecipe;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Recipe defining casting and composite fluids for a given material.
 * @see slimeknights.tconstruct.library.recipe.material.MaterialRecipe
 */
public class MaterialFluidRecipe implements ICustomOutputRecipe<CastingRecipeInput>, IDisplayMaterialRecipe {
  public static final RecordLoadable<MaterialFluidRecipe> LOADER = RecordLoadable.create(
    FluidStackSet.LOADABLE.requiredField("fluid", r -> r.fluid),
    IntLoadable.FROM_ZERO.requiredField("temperature", r -> r.temperature),
    MaterialVariantId.LOADABLE.nullableField("input", r -> r.input != null ? r.input.getVariant() : null),
    MaterialVariantId.LOADABLE.nullableField("output", r -> r.output.getVariant()),
    BooleanLoadable.INSTANCE.defaultField("hide_in_book", false, false, r -> r.hideInBook),
    MaterialFluidRecipe::new);
  /** Empty recipe instance, used as a fallback */
  public static final MaterialFluidRecipe EMPTY = new MaterialFluidRecipe(FluidStackSet.EMPTY, 0, null, MaterialId.UNKNOWN, true);

  private final FluidStackSet fluid;
  @Getter
  private final int temperature;
  /** Material base for composite */
  @Nullable @Getter
  private final MaterialVariant input;
  /** Output material ID */
  @Getter
  private final MaterialVariant output;
  @Getter
  private final boolean hideInBook;

  protected MaterialFluidRecipe(FluidStackSet fluid, int temperature, @Nullable MaterialVariantId inputId, MaterialVariantId outputId, boolean hideInBook) {
    this.fluid = fluid;
    this.temperature = temperature;
    this.input = inputId == null ? null : MaterialVariant.of(inputId);
    this.output = MaterialVariant.of(outputId);
    this.hideInBook = hideInBook;
    MaterialCastingLookup.registerFluid(this);
  }

  /** Checks if this recipe is valid for the given fluid and material */
  public boolean matches(Fluid fluid) {
    return !output.isUnknown() && this.fluid.contains(fluid);
  }

  /** Checks if this recipe is valid for the given fluid and material */
  public boolean matches(Fluid fluid, MaterialVariantId material) {
    // disallow casting if the input material matches the output (including variant) to prevent wasted resources
    return matches(fluid) && (input == null || input.matchesVariant(material)) && !output.sameVariant(material);
  }

  /** Gets the amount of fluid to cast this recipe */
  @SuppressWarnings("unused")  // API
  public int getFluidAmount(Fluid fluid) {
    return this.fluid.amount();
  }

  /** Gets a list of fluids for display */
  @Override
  public List<FluidStack> getFluids() {
    return fluid.display();
  }

  @Override
  public final boolean matches(CastingRecipeInput inv, Level level) {
    // if the input ID is null, can skip fetching the input stack material
    return matches(inv.getFluid().getFluid()) && (input == null || input.matchesVariant(inv.getItem()));
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerRecipes.materialFluidRecipe.get();
  }

  @Override
  public RecipeType<?> getType() {
    return TinkerRecipes.dataType.get();
  }

  @Override
  public boolean isVisible() {
    return !output.isUnknown() && !output.get().isHidden()
      && (input == null || !input.isUnknown() && !input.get().isHidden());
  }


  /* JEI */

  /** Alias for {@link #getOutput()} for the sake of implementing {@link IDisplayMaterialRecipe} */
  @Override
  public MaterialVariant getMaterial() {
    return output;
  }
}
