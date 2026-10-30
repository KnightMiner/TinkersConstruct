package slimeknights.tconstruct.library.recipe.fuel;

import lombok.Getter;
import lombok.ToString;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.ICustomOutputRecipe;
import slimeknights.tconstruct.library.recipe.SingleFluidInput;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock.TankType;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Recipe for a fuel for the melter or smeltery
 */
@Getter
@ToString
public class MeltingFuel implements ICustomOutputRecipe<SingleFluidInput> {
  public static final RecordLoadable<MeltingFuel> LOADER = RecordLoadable.create(
    Loadables.SIZED_FLUID_INGREDIENT.nullableField("fluid", r -> r.input),
    IntLoadable.FROM_ONE.defaultField("duration", 0, MeltingFuel::getDuration),
    IntLoadable.FROM_ONE.requiredField("temperature", MeltingFuel::getTemperature),
    IntLoadable.FROM_ONE.requiredField("rate", MeltingFuel::getRate),
    MeltingFuel::new).validate((fuel, error) -> {
      // duration is optional (and ignored) for solid
      if (fuel.input != null && fuel.duration == 0) {
        throw error.create("Missing JSON field duration");
      }
      return fuel;
    });

  @Nullable
  private final SizedFluidIngredient input;
  private final int duration;
  private final int temperature;
  private final int rate;

  public MeltingFuel(@Nullable SizedFluidIngredient input, int duration, int temperature, int rate) {
    this.input = input;
    this.duration = duration;
    this.temperature = temperature;
    this.rate = rate;
    // register this recipe with the lookup
    MeltingFuelLookup.addFuel(this);
  }

  /* Recipe methods */

  @Override
  public boolean matches(SingleFluidInput inv, Level worldIn) {
    return matches(inv.getFluid());
  }

  /**
   * Checks if this fuel matches the given fluid
   * @param fluid  Fluid
   * @return  True if matches
   */
  public boolean matches(FluidStack fluid) {
    return input != null && input.test(fluid);
  }

  /**
   * Gets the amount of fluid consumed for the given fluid
   * @param inv  Inventory instance
   * @return  Amount of fluid consumed
   */
  public int getAmount(SingleFluidInput inv) {
    return getAmount(inv.getFluid());
  }

  /**
   * Gets the amount of fluid consumed for the given fluid
   * @param fluid  Fluid
   * @return  Amount of fluid consumed
   */
  public int getAmount(FluidStack fluid) {
    return input == null ? 0 : input.amount();
  }

  /**
   * Gets a list of all valid input fluids for this recipe
   * @return  Input fluids
   */
  public List<FluidStack> getInputs() {
    return input == null ? List.of() : List.of(input.getFluids());
  }

  /* Recipe type methods */

  @Override
  public RecipeType<?> getType() {
    return TinkerRecipes.fuelType.get();
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerRecipes.fuel.get();
  }

  @Override
  public ItemStack getToastSymbol() {
    return new ItemStack(TinkerSmeltery.searedTank.get(TankType.FUEL_TANK));
  }
}
