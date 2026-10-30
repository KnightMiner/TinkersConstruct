package slimeknights.tconstruct.library.recipe.ingredient;

import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicateField;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;

import java.util.stream.Stream;

/**
 * Extension of the vanilla ingredient to display materials on items and support matching by materials
 */
public record MaterialIngredient(HolderSet<Item> item, IJsonPredicate<MaterialVariantId> material) implements ICustomIngredient {
  public static final RecordLoadable<MaterialIngredient> LOADABLE = RecordLoadable.create(
    Loadables.ITEM_SET.requiredField("item", MaterialIngredient::item),
    new MaterialPredicateField<>("material", MaterialIngredient::material),
    MaterialIngredient::new);

  /** Creates an ingredient matching the given materials */
  public static Ingredient of(HolderSet<Item> item, IJsonPredicate<MaterialVariantId> material) {
    return new MaterialIngredient(item, material).toVanilla();
  }

  /** Creates an ingredient matching the given materials */
  @SuppressWarnings("deprecation")
  public static Ingredient of(ItemLike item, IJsonPredicate<MaterialVariantId> material) {
    return of(HolderSet.direct(item.asItem().builtInRegistryHolder()), material);
  }

  /** Creates an ingredient matching any material */
  public static Ingredient of(HolderSet<Item> item) {
    return of(item, MaterialPredicate.ANY);
  }

  /**
   * Creates a new instance from an item with a fixed material
   * @param item      Material item
   * @param material  Material ID
   * @return  Material ingredient instance
   */
  public static Ingredient of(ItemLike item, MaterialVariantId material) {
    return of(item, MaterialPredicate.variant(material));
  }

  /**
   * Creates a new ingredient matching any material from items
   * @param item  Material item
   * @return  Material ingredient instance
   */
  public static Ingredient of(ItemLike item) {
    return of(item, MaterialPredicate.ANY);
  }


  @Override
  public IngredientType<MaterialIngredient> getType() {
    return TinkerRecipes.materialIngredient.get();
  }

  @Override
  public boolean isSimple() {
    // while technically any would be simple, we get faster matching outside shapeless with false
    return false;
  }

  @Override
  public boolean test(ItemStack stack) {
    // item first, should be faster
    if (stack.isEmpty() || !item.contains(stack.getItemHolder())) {
      return false;
    }
    // no need to read material NBT if the material is the any predicate
    return material == MaterialPredicate.ANY || material.matches(IMaterialItem.getMaterialFromStack(stack));
  }

  @Override
  public Stream<ItemStack> getItems() {
    // no material? apply all materials for variants
    return item.stream().flatMap(item -> MaterialRecipeCache.getAllVariants().stream()
      .filter(material::matches)
      .map(mat -> IMaterialItem.withMaterial(item.value(), mat))
      .filter(stack -> !stack.isEmpty()));
  }
}
