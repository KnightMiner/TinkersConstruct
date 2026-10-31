package slimeknights.tconstruct.library.recipe.casting.material;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.library.recipe.casting.material.ToolCastingRecipe.CastPurpose;
import slimeknights.tconstruct.library.recipe.ingredient.MaterialIngredient;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Builder for {@link MaterialCastingRecipe} and {@link ToolCastingRecipe} */
@SuppressWarnings({"unused", "WeakerAccess"})
@Accessors(chain = true)
@RequiredArgsConstructor(staticName = "castingRecipe")
public class MaterialCastingRecipeBuilder extends AbstractRecipeBuilder<MaterialCastingRecipeBuilder> {
  private final RecipeType<?> type;
  private final RecipeSerializer<? extends AbstractMaterialCastingRecipe> serializer;
  @Nullable
  private final IMaterialItem resultPart;
  @Nullable
  private final IModifiable resultTool;
  private Ingredient cast = Ingredient.EMPTY;
  @Setter
  private int itemCost = 0;
  private CastPurpose castPurpose = CastPurpose.CATALYST;
  private boolean switchSlots = false;
  /**
   * If true, tool swapping recipes (from {@link #basinRecipe(IModifiable)} or {@link #tableRecipe(IModifiable)}) will not generate a part swapping recipe.
   * Can use {@link PartSwapCastingRecipeBuilder} to manually create one as a separate JSON.
   */
  @Setter
  private boolean fluidSwapping = true;
  @Setter
  private IJsonPredicate<MaterialVariantId> allowedMaterials = MaterialPredicate.ANY;
  /** Extra materials for tool casting. Has no impact on part casting. */
  private final List<MaterialVariantId> extraMaterials = new ArrayList<>();

  /** Creates a new table recipe */
  public static MaterialCastingRecipeBuilder tableRecipe(RecipeSerializer<? extends AbstractMaterialCastingRecipe> serializer, @Nullable IMaterialItem part, @Nullable IModifiable tool) {
    return castingRecipe(TinkerRecipes.castingTableType.get(), serializer, part, tool);
  }

  /** Creates a new basin recipe */
  public static MaterialCastingRecipeBuilder basinRecipe(RecipeSerializer<? extends AbstractMaterialCastingRecipe> serializer, @Nullable IMaterialItem part, @Nullable IModifiable tool) {
    return castingRecipe(TinkerRecipes.castingBasinType.get(), serializer, part, tool);
  }

  /** Creates a new casting recipe for a tool part in a table */
  public static MaterialCastingRecipeBuilder tableRecipe(IMaterialItem result) {
    return tableRecipe(TinkerRecipes.materialCasting.getTable(), result, null);
  }

  /** Creates a new casting recipe for a tool part in a basin */
  public static MaterialCastingRecipeBuilder basinRecipe(IMaterialItem result) {
    return basinRecipe(TinkerRecipes.materialCasting.getBasin(), result, null);
  }

  /** Creates a new casting recipe for a tool in a table */
  public static MaterialCastingRecipeBuilder tableRecipe(IModifiable result) {
    return tableRecipe(TinkerRecipes.toolCasting.getTable(), null, result);
  }

  /** Creates a new casting recipe for a tool in a basin */
  public static MaterialCastingRecipeBuilder basinRecipe(IModifiable result) {
    return basinRecipe(TinkerRecipes.toolCasting.getBasin(), null, result);
  }

  /**
   * Set the cast to the given ingredient
   * @param cast      Ingredient
   * @param purpose   Function the cast performs. For tool casting, see {@link #setCast(Ingredient, boolean)}.
   * @return  Builder instance
   */
  public MaterialCastingRecipeBuilder setCast(Ingredient cast, CastPurpose purpose) {
    this.cast = cast;
    this.castPurpose = purpose;
    return this;
  }

  /**
   * Set the cast to the given ingredient
   * @param cast      Ingredient
   * @param consumed  If true, cast is consumed. For tool casting, see {@link #setCast(Ingredient, CastPurpose)}.
   * @return  Builder instance
   */
  public MaterialCastingRecipeBuilder setCast(Ingredient cast, boolean consumed) {
    return setCast(cast, consumed ? CastPurpose.CONSUMED : CastPurpose.CATALYST);
  }

  /**
   * Sets the cast to the given item.
   * @param item      Cast item
   * @param purpose   Function the cast performs. For part casting, see {@link #setCast(ItemLike, boolean)}.
   * @return  Builder instance
   */
  public MaterialCastingRecipeBuilder setCast(ItemLike item, CastPurpose purpose) {
    return this.setCast(Ingredient.of(item), purpose);
  }

  /**
   * Sets the cast to the given item
   * @param item      Cast item
   * @param consumed  If true, cast is consumed. For tool casting, see {@link #setCast(ItemLike, CastPurpose)}.
   * @return  Builder instance
   */
  public MaterialCastingRecipeBuilder setCast(ItemLike item, boolean consumed) {
    return this.setCast(Ingredient.of(item), consumed);
  }

  /**
   * Sets the cast to the given tag
   * @param tag       Cast tag
   * @param consumed  If true, cast is consumed. For tool casting, see {@link #setCast(Ingredient, CastPurpose)}.
   * @return  Builder instance
   */
  public MaterialCastingRecipeBuilder setCast(TagKey<Item> tag, boolean consumed) {
    return this.setCast(Ingredient.of(tag), consumed);
  }

  /**
   * Sets the cast to the given tool part with the proper ingredient type
   * @param part      Cast part item
   * @param first     If true, the part is the first material. If false it is the second
   * @return  Builder instance
   */
  public MaterialCastingRecipeBuilder setPart(ItemLike part, boolean first) {
    return this.setCast(MaterialIngredient.of(part), first ? CastPurpose.FIRST_MATERIAL : CastPurpose.SECOND_MATERIAL);
  }

  /**
   * Set output of recipe to be put into the input slot.
   * Mostly used for cast creation
   */
  public MaterialCastingRecipeBuilder setSwitchSlots() {
    this.switchSlots = true;
    return this;
  }

  /** Adds a material to set after the end of the parts list */
  public MaterialCastingRecipeBuilder addExtraMaterial(MaterialVariantId material) {
    extraMaterials.add(material);
    return this;
  }

  @Override
  public void save(RecipeOutput output) {
    this.save(output, Loadables.ITEM.getKey(Objects.requireNonNullElse(this.resultPart, this.resultTool).asItem()));
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    if (this.itemCost <= 0) {
      throw new IllegalStateException("Material casting recipes require a positive amount of fluid");
    }
    AdvancementHolder advancement = buildOptionalAdvancement(output, id, "casting");
    if (resultPart != null && resultTool != null) {
      throw new IllegalArgumentException("Cannot set both result part and result tool");
    } else if (resultPart != null) {
      output.accept(id, new MaterialCastingRecipe(type, serializer, group, cast, itemCost, resultPart, allowedMaterials, castPurpose != CastPurpose.CATALYST, switchSlots), advancement);
    } else if (resultTool != null) {
      output.accept(id, new ToolCastingRecipe(type, serializer, group, cast, itemCost, castPurpose, resultTool, allowedMaterials, extraMaterials, fluidSwapping), advancement);
    } else {
      throw new IllegalArgumentException("Must have either result part or result tool");
    }
  }
}
