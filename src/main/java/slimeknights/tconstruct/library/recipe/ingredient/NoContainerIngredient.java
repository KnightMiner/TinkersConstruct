package slimeknights.tconstruct.library.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.tconstruct.library.codec.TinkerCodecs;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;

import java.util.Arrays;
import java.util.stream.Stream;

/** Ingredient matching an item with no container item, used to ensure NBT fluid items are empty */
public record NoContainerIngredient(Ingredient nested) implements ICustomIngredient {
  public static final MapCodec<NoContainerIngredient> CODEC = TinkerCodecs.NESTED.xmap(NoContainerIngredient::new, NoContainerIngredient::nested);
  public static final StreamCodec<RegistryFriendlyByteBuf, NoContainerIngredient> STREAM_CODEC = Ingredient.CONTENTS_STREAM_CODEC.map(NoContainerIngredient::new, NoContainerIngredient::nested);

  /** Creates an instance from the given nested ingredient */
  public static Ingredient of(Ingredient ingredient) {
    return new NoContainerIngredient(ingredient).toVanilla();
  }

  /** Creates an instance from the given items */
  public static Ingredient of(ItemLike... items) {
    return of(Ingredient.of(items));
  }

  /** Creates an instance from the given stacks */
  public static Ingredient of(ItemStack... stacks) {
    return of(Ingredient.of(stacks));
  }

  /** Creates an instance from the given tag */
  public static Ingredient of(TagKey<Item> tag) {
    return of(Ingredient.of(tag));
  }


  /* Implementation */

  @Override
  public IngredientType<NoContainerIngredient> getType() {
    return TinkerRecipes.noContainerIngredient.get();
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public boolean test(ItemStack stack) {
    return nested.test(stack) && !stack.hasCraftingRemainingItem();
  }

  @Override
  public Stream<ItemStack> getItems() {
    return Arrays.stream(nested.getItems()).filter(stack -> !stack.hasCraftingRemainingItem());
  }
}
