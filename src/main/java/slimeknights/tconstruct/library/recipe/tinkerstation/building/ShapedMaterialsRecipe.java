package slimeknights.tconstruct.library.recipe.tinkerstation.building;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Shaped recipe with a number of {@link slimeknights.tconstruct.library.recipe.ingredient.MaterialIngredient} and
 * {@link slimeknights.tconstruct.library.recipe.ingredient.MaterialValueIngredient} to set the materials of the result.
 * @see ShapelessMaterialsRecipe
 * @see ShapedMaterialsRecipeBuilder
 */
public class ShapedMaterialsRecipe extends ShapedRecipe implements MaterialsCraftingTableRecipe {
  /** Codec for JSON */
  public static final MapCodec<ShapedMaterialsRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
    Codec.STRING.optionalFieldOf("group", "").forGetter(Recipe::getGroup),
    CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category),
    Pattern.MAP_CODEC.forGetter(r -> r.pattern),
    ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.result),
    Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(Recipe::showNotification),
    MaterialVariantId.CODEC.listOf().optionalFieldOf("extra_materials", List.of()).forGetter(r -> r.extraMaterials)
  ).apply(builder, ShapedMaterialsRecipe::new));
  /** Codec for network */
  public static final StreamCodec<RegistryFriendlyByteBuf,ShapedMaterialsRecipe> STREAM_CODEC = StreamCodec.composite(
    ByteBufCodecs.STRING_UTF8, Recipe::getGroup,
    CraftingBookCategory.STREAM_CODEC, CraftingRecipe::category,
    Pattern.STREAM_CODEC, r -> r.pattern,
    ItemStack.STREAM_CODEC, r -> r.result,
    ByteBufCodecs.BOOL, Recipe::showNotification,
    MaterialVariantId.STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.extraMaterials,
    ShapedMaterialsRecipe::new);

  /** Object containing information on the pattern including the part list */
  private final Pattern pattern;
  /**
   * If true, a part may show up multiple times in the inputs, and all copies should match.
   * If false, only the first instance of a part is checked for each input, allowing a tool with the same part multiple times.
   */
  private final boolean checkRepeats;
  /** List of additional materials to add beyond the parts */
  @Getter
  private final List<MaterialVariantId> extraMaterials;
  public ShapedMaterialsRecipe(String group, CraftingBookCategory category, Pattern pattern, ItemStack result, boolean showNotification, List<MaterialVariantId> extraMaterials) {
    super(group, category, pattern.pattern, result, showNotification);
    this.pattern = pattern;
    this.checkRepeats = pattern.parts.stream().unordered().distinct().count() == pattern.parts.size();
    this.extraMaterials = extraMaterials;
  }

  @Override
  public List<Ingredient> getParts() {
    return pattern.parts;
  }

  @Override
  public int getPartCount() {
    return pattern.parts.size();
  }

  /**
   * Finds materials for each of the parts
   * @return Array of all matched materials. Array will have no null entries, though the array may be null if no match was found.
   */
  @Nullable
  static MaterialVariantId[] findMaterials(CraftingInput inventory, List<Ingredient> parts, int partCount, boolean checkRepeats) {
    // want one material for each
    MaterialVariantId[] materials = new MaterialVariantId[partCount];
    for (int i = 0; i < inventory.size(); i++) {
      ItemStack stack = inventory.getItem(i);
      if (!stack.isEmpty()) {
        for (int p = 0; p < partCount; p++) {
          MaterialVariantId current = materials[p];
          // if we have not found the material yet, or repeats are considered the same material, test the ingredient
          if ((current == null || checkRepeats) && parts.get(p).test(stack)) {
            MaterialVariantId matched = MaterialRecipeCache.getMaterial(stack);
            // first occurrence? thats our material
            if (current == null) {
              materials[p] = matched;
              break;
            } else if (!current.matchesVariant(matched)) {
              // if same material but different variants, just discard the variant
              if (current.material().equals(matched.material())) {
                materials[p] = current.material();
                break;
              } else {
                // if different materials, no match
                return null;
              }
            }
          }
        }
      }
    }
    // ensure we found all materials needed
    for (int p = 0; p < partCount; p++) {
      if (materials[p] == null) {
        return null;
      }
    }
    return materials;
  }

  @Override
  public boolean matches(CraftingInput inventory, Level level) {
    if (!super.matches(inventory, level)) {
      return false;
    }
    // ensure all part materials matched and we found all parts
    return findMaterials(inventory, pattern.parts, pattern.parts.size(), checkRepeats) != null;
  }

  /** Common logic to this and {@link ShapedMaterialsRecipe} */
  public static void setMaterial(ItemStack stack, MaterialVariantId material, List<MaterialVariantId> extraMaterials) {
    if (extraMaterials.isEmpty() && stack.getItem() instanceof IMaterialItem materialItem) {
      materialItem.setMaterial(stack, material);
    } else {
      MaterialNBT.Builder builder = MaterialNBT.builder();
      builder.add(material);
      for (MaterialVariantId extraMaterial : extraMaterials) {
        builder.add(extraMaterial);
      }
      ToolStack.from(stack).setMaterials(builder.build());
    }
  }

  /** Sets the material for the given stack */
  @Override
  public void setMaterial(ItemStack stack, MaterialVariantId material) {
    setMaterial(stack, material, extraMaterials);
  }

  /** Assembles the item with material information */
  static ItemStack assemble(ItemStack stack, CraftingInput inventory, List<Ingredient> parts, int partCount, boolean checkRepeats, List<MaterialVariantId> extraMaterials) {
    MaterialVariantId[] materials = findMaterials(inventory, parts, partCount, checkRepeats);
    if (materials != null) {
      // if the result is a tool part, and we only have the one material, set its material
      if (materials.length == 1 && extraMaterials.isEmpty() && stack.getItem() instanceof IMaterialItem materialItem) {
        return materialItem.setMaterial(stack, materials[0]);
      }
      MaterialNBT.Builder builder = MaterialNBT.builder();
      // add each material
      for (MaterialVariantId material : materials) {
        builder.add(material);
      }
      // add extra materials
      builder.add(extraMaterials);
      ToolStack.from(stack).setMaterials(builder.build());
    }
    return stack;
  }

  @Override
  public ItemStack assemble(CraftingInput inventory, HolderLookup.Provider registries) {
    return assemble(super.assemble(inventory, registries), inventory, pattern.parts, pattern.parts.size(), checkRepeats, extraMaterials);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerRecipes.shapedMaterials.get();
  }


  /** Data object holding information on the shaped pattern plus the part ingredients */
  public record Pattern(ShapedRecipePattern pattern, List<Ingredient> parts, String partSymbols) {
    /** Codec for JSON parsing */
    public static final MapCodec<Pattern> MAP_CODEC = Data.MAP_CODEC.flatXmap(Pattern::unpack, pattern -> {
      if (pattern.partSymbols.isEmpty()) {
        return DataResult.error(() -> "Cannot encode unpacked recipe");
      }
      return pattern.pattern.data.map(data -> DataResult.success(new Data(data, pattern.partSymbols)))
        .orElseGet(() -> DataResult.error(() -> "Cannot encode unpacked recipe"));
    });
    /** Codec for byte buffer */
    public static final StreamCodec<RegistryFriendlyByteBuf, Pattern> STREAM_CODEC = StreamCodec.composite(
      ShapedRecipePattern.STREAM_CODEC, Pattern::pattern,
      ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), Pattern::partIndices,
      (pattern, parts) -> {
        // select the ingredient from the list by index, using modulo if invalid
        List<Ingredient> ingredients = pattern.ingredients();
        return new Pattern(pattern, ingredients.isEmpty() ? List.of() : parts.stream().map(i -> ingredients.get(i % ingredients.size())).toList(), "");
      }
    );


    /** Gets the indices of all part ingredients for network serialization */
    private IntList partIndices() {
      List<Ingredient> ingredients = pattern.ingredients();
      IntList indices = new IntArrayList(parts.size());
      for (Ingredient part : parts) {
        int index = ingredients.indexOf(part);
        // part should be in the list, but just in case make it a valid index
        indices.add(index == -1 ? 0 : index);
      }
      return indices;
    }

    /** Unpacks the texture from the given data */
    private static DataResult<Pattern> unpack(Data data) {
      List<Ingredient> parts = new ArrayList<>(data.parts.length());
      for (int i = 0; i < data.parts.length(); i++) {
        char symbol = data.parts.charAt(i);
        Ingredient part = data.data.key().get(symbol);
        if (part == null) {
          int index = i;
          return DataResult.error(() -> "Part list references symbol '" + symbol + "' at index " + index + " but its not defined in the key");
        }
        parts.add(part);
      }
      return ShapedRecipePattern.unpack(data.data).map(pattern -> new Pattern(pattern, List.copyOf(parts), data.parts));
    }

    /** Raw data from the recipe instance */
    private record Data(ShapedRecipePattern.Data data, String parts) {
      public static final MapCodec<Data> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ShapedRecipePattern.Data.MAP_CODEC.forGetter(Data::data),
        ExtraCodecs.NON_EMPTY_STRING.fieldOf("parts").forGetter(Data::parts)
      ).apply(instance, Data::new));
    }
  }
}
