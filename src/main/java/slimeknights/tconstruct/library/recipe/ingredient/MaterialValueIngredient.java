package slimeknights.tconstruct.library.recipe.ingredient;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicateField;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Stream;

/** Ingredient matching material items with the given value based on {@link MaterialRecipe}. Typically, matches ingots or blocks */
public record MaterialValueIngredient(IJsonPredicate<MaterialVariantId> material, Range range) implements ICustomIngredient {
  public static final RecordLoadable<MaterialValueIngredient> LOADABLE = RecordLoadable.create(
    new MaterialPredicateField<>("material", MaterialValueIngredient::material),
    Range.LOADABLE.requiredField("value", MaterialValueIngredient::range),
    MaterialValueIngredient::new);

  /** Creates an ingredient matching a range of values */
  public static Ingredient of(IJsonPredicate<MaterialVariantId> materials, float minValue, float maxValue) {
    return new MaterialValueIngredient(materials, new Range(minValue, maxValue)).toVanilla();
  }

  /** Creates an ingredient matching an exact value */
  public static Ingredient of(IJsonPredicate<MaterialVariantId> materials, float value) {
    return of(materials, value, value);
  }

  @Override
  public IngredientType<MaterialValueIngredient> getType() {
    return TinkerRecipes.materialValueIngredient.get();
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  /** Checks the given material recipe against our filters */
  public boolean test(MaterialRecipe material) {
    return range.test(material.getValue() / (float) material.getNeeded()) && this.material.matches(material.getMaterial().getVariant());
  }

  @Override
  public boolean test(ItemStack stack) {
    MaterialRecipe recipe = MaterialRecipeCache.findRecipe(stack);
    return recipe != MaterialRecipe.EMPTY && test(recipe);
  }

  @Override
  public Stream<ItemStack> getItems() {
    return MaterialRecipeCache.getSortedRecipes().stream()
        .filter(this::test)
        .flatMap(material -> Arrays.stream(material.getIngredient().getItems()));
  }


  /* Helpers for ShapedMaterialRecipe */

  /** Checks if this ingredient fully contains the range of the other */
  private boolean contains(MaterialValueIngredient other) {
    return this.range.contains(other.range);
  }

  /** Creates an ingredient that matches anything either of the two ingredients matches */
  public MaterialValueIngredient merge(MaterialValueIngredient other) {
    if (this == other) return this;

    // if we have the same predicate, we can possibly skip creating a new instance
    IJsonPredicate<MaterialVariantId> predicate = this.material;
    if (this.material.equals(other.material)) {
      if (this.contains(other)) {
        return this;
      }
      if (other.contains(this)) {
        return other;
      }
    } else {
      predicate = MaterialPredicate.or(this.material, other.material);
    }
    return new MaterialValueIngredient(predicate, Range.merge(this.range, other.range));
  }

  /** Gets the material matching this recipe */
  @Nullable
  public MaterialVariantId material(ItemStack stack) {
    MaterialRecipe recipe = MaterialRecipeCache.findRecipe(stack);
    return recipe != MaterialRecipe.EMPTY && test(recipe) ? recipe.getMaterial().getVariant() : null;
  }


  /** Handles the float range part of the ingredient */
  private record Range(float min, float max) {
    public static final RecordLoadable<Range> LOADABLE = RecordLoadable.create(
      FloatLoadable.FROM_ZERO.defaultField("min", 0f, false, Range::min),
      FloatLoadable.FROM_ZERO.defaultField("max", Float.POSITIVE_INFINITY, false, Range::max),
      Range::new
    ).compact(FloatLoadable.FROM_ZERO.map(value -> new Range(value, value), Range::min), range -> range.min == range.max);

    /** Tests this range against the given value */
    public boolean test(float value) {
      return min <= value && value <= max;
    }

    /** Checks if this range contains the other range */
    public boolean contains(Range other) {
      return this.min <= other.min && other.max <= this.max;
    }

    /** Gets the minimum of the two ranges */
    public static Range merge(Range a, Range b) {
      if (a.contains(b)) return a;
      if (b.contains(a)) return b;
      return new Range(Math.min(a.min, b.min), Math.max(a.max, b.max));
    }
  }
}
