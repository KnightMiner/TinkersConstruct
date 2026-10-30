package slimeknights.tconstruct.library.recipe.ingredient;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.item.IModifiable;

import java.util.stream.Stream;

/** Ingredient that only matches tools with a specific hook */
public record ToolHookIngredient(TagKey<Item> tag, ModuleHook<?> hook) implements ICustomIngredient {
  public static final RecordLoadable<ToolHookIngredient> LOADABLE = RecordLoadable.create(
    Loadables.ITEM_TAG.requiredField("tag", ToolHookIngredient::tag),
    ToolHooks.LOADER.requiredField("hook", ToolHookIngredient::hook),
    ToolHookIngredient::new);

  /** Creates a new ingredient instance */
  public static Ingredient of(TagKey<Item> tag, ModuleHook<?> hook) {
    return new ToolHookIngredient(tag, hook).toVanilla();
  }

  /** Creates a new ingredient instance for any modifiable tool */
  public static Ingredient of(ModuleHook<?> hook) {
    return of(TinkerTags.Items.MODIFIABLE, hook);
  }


  @Override
  public IngredientType<ToolHookIngredient> getType() {
    return TinkerRecipes.toolHookIngredient.get();
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public boolean test(ItemStack stack) {
    return stack.is(tag) && stack.getItem() instanceof IModifiable modifiable && modifiable.getToolDefinition().getData().getHooks().hasHook(hook);
  }

  @Override
  public Stream<ItemStack> getItems() {
    return BuiltInRegistries.ITEM.getTag(tag).stream().flatMap(HolderSet::stream).map(item -> {
      if (item.value() instanceof IModifiable modifiable && modifiable.getToolDefinition().getData().getHooks().hasHook(hook)) {
        return new ItemStack(item);
      }
      return ItemStack.EMPTY;
    }).filter(stack -> !stack.isEmpty());
  }
}
