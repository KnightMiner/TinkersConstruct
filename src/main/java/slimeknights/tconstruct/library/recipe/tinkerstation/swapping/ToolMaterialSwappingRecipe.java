package slimeknights.tconstruct.library.recipe.tinkerstation.swapping;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.MultiNamedRecipe;
import slimeknights.mantle.recipe.NamedRecipe;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.MaterialHolder;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.MaterialVariant;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.recipe.RecipeResult;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.library.recipe.modifiers.adding.ModifierRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.IDisplayToolTinkering;
import slimeknights.tconstruct.library.recipe.tinkerstation.TinkerStationRecipeInput;
import slimeknights.tconstruct.library.tools.component.ModifierEntries;
import slimeknights.tconstruct.library.tools.component.ToolMaterials;
import slimeknights.tconstruct.library.tools.definition.module.material.ToolMaterialHook;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;
import slimeknights.tconstruct.library.tools.stack.LazyToolStack;
import slimeknights.tconstruct.library.tools.stack.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

/** Recipe swapping a tool material using another tool as input */
public class ToolMaterialSwappingRecipe extends MaterialSwappingRecipe implements MultiNamedRecipe<IDisplayToolTinkering> {
  protected static final RecipeResult<LazyToolStack> NO_MODIFIERS = RecipeResult.failure(TConstruct.makeTranslationKey("recipe", "part_swapping.no_modifiers"));
  public static final RecordLoadable<ToolMaterialSwappingRecipe> LOADER = RecordLoadable.create(TOOLS_FIELD, EXTRA_REQUIREMENTS_FIELD, ToolMaterialSwappingRecipe::new);

  /** @apiNote Internal usage. To create see {@link ToolMaterialSwappingRecipeBuilder} */
  @Internal
  public ToolMaterialSwappingRecipe(Ingredient tools, List<SizedIngredient> extraRequirements) {
    super(tools, 1, extraRequirements);
  }

  @Override
  public boolean matches(TinkerStationRecipeInput inv, Level world) {
    ItemStack tinkerable = inv.getTinkerableStack();
    if (tinkerable.isEmpty() || !tools.test(tinkerable)) {
      return false;
    }
    // get the list of materials, ensuring its valid
    List<MaterialStatsId> materials = ToolMaterialHook.stats(IModifiable.getToolDefinition(tinkerable.getItem()));
    if (materials.isEmpty()) {
      return false;
    }

    // find the tool we are using to replace materials
    BitSet used = ModifierRecipe.makeBitset(inv);
    boolean found = false;
    // don't bother checking higher than the max material index
    for (int i = 0; i < inv.getInputCount(); i++) {
      ItemStack input = inv.getInput(i);
      if (!input.isEmpty() && input.getItem() == tinkerable.getItem()) {
        found = true;
        used.set(i);
        break;
      }
    }
    // if we found the tool and all extra requirements, we match
    return found && ModifierRecipe.checkMatch(inv, extraRequirements, used);
  }

  @Override
  public RecipeResult<LazyToolStack> getValidatedResult(TinkerStationRecipeInput inv, HolderLookup.Provider registries) {
    // copy the tool NBT to ensure the original tool is intact
    ToolStackView original = inv.getTinkerable();
    List<MaterialStatsId> materials = ToolMaterialHook.stats(original.getDefinition());
    // prevent part swapping on large tools in small tables
    if (materials.size() > inv.getInputCount()) {
      return TOO_MANY_PARTS;
    }

    // find the index to swap
    // actual part swap logic
    for (int i = 0; i < inv.getInputCount(); i++) {
      ItemStack stack = inv.getInput(i);
      if (!stack.isEmpty() && stack.getItem() == original.getItem()) {
        ToolStackView sacrifice = ToolStack.from(stack);
        // ensure the sacrifice does not have any modifiers
        if (!sacrifice.getCraftedModifiers().isEmpty()) {
          return NO_MODIFIERS;
        }
        int index = i;
        if (index >= materials.size()) {
          index = 0;
        }

        // swap the material
        boolean mayRepair = MaterialRegistry.getInstance().canRepair(materials.get(index));
        RecipeResult<LazyToolStack> result = swapMaterial(inv, sacrifice.getMaterial(index), index, 0, mayRepair);
        // if it's a repairable part, repair the tool based on the remaining durability of the sacrifice, plus a 5% bonus
        if (mayRepair && result.isSuccess()) {
          ToolStack tool = result.getResult().getTool();
          if (tool.getDamage() > 0) {
            ToolDamageUtil.repair(tool, sacrifice.getCurrentDurability() + tool.getStats().getInt(ToolStats.DURABILITY) / 20);
          }
        }
        return result;
      }
    }

    // no item found, should never happen
    return RecipeResult.pass();
  }

  @Override
  protected boolean shrinkPart(TinkerStationRecipeInput.Mutable inv, int index, ItemStack stack) {
    if (stack.getItem() == inv.getTinkerableStack().getItem()) {
      inv.shrinkInput(index, 1);
      return true;
    }
    return false;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerRecipes.toolMaterialSwapping.get();
  }


  /* JEI */
  private List<NamedRecipe<IDisplayToolTinkering>> multiRecipes;

  @Override
  public List<NamedRecipe<IDisplayToolTinkering>> getRecipes(ResourceLocation id, HolderLookup.Provider registries) {
    if (multiRecipes == null) {
      Collection<MaterialHolder> materials = MaterialRegistry.getVisibleMaterials();
      List<NamedRecipe<IDisplayToolTinkering>> newRecipes = new ArrayList<>();
      List<ResourceLocation> parent = List.of(id);
      for (ItemStack stack : tools.getItems()) {
        ToolStack tool = ToolStack.from(stack);
        List<MaterialStatsId> stats = ToolMaterialHook.stats(tool.getDefinition());
        if (stats.size() > MAX_SLOTS) {
          continue;
        }
        List<MaterialVariant> renderMaterials = IntStream.range(0, stats.size()).mapToObj(ToolBuildHandler::getRenderMaterial).toList();
        ToolStack displayTool = tool.copy();
        displayTool.setMaterials(ToolMaterials.of(renderMaterials));
        ResourceLocation toolId = id.withSuffix('/' + Loadables.ITEM.getKey(stack.getItem()).toDebugFileName());
        // start making recipes
        for (int i = 0; i < stats.size(); i++) {
          MaterialStatsId stat = stats.get(i);
          List<MaterialHolder> filtered = materials.stream().filter(material -> material.hasStats(stat)).toList();
          ToolStack copy = tool.copy();
          setMaterials(copy, i, renderMaterials.get(i));
          List<ItemStack> withoutMaterial = List.of(copy.getStack().copy());
          // standard recipe - focus is the tool being changed
          ResourceLocation statId = toolId.withSuffix("/material_" + i);
          int index = i;
          newRecipes.add(NamedRecipe.of(statId, parent, new DisplayRecipe(i,
            // one part per material
            filtered.stream().map(mat -> {
              ToolStack displayCopy = displayTool.copy();
              displayCopy.replaceMaterial(index, mat);
              return displayCopy.getStack();
            }).toList(),
            // single tool with the material to swap left blank
            withoutMaterial,
            // one output per material
            filtered.stream().map(mat -> {
              copy.replaceMaterial(index, mat);
              return copy.getStack().copy();
            }).toList(),
            // material list
            filtered, stat, displayTool
          )));
          // recipe for sacrificing the focus to swap materials - just needs the tool on one list so it is detected by the cache
          newRecipes.add(NamedRecipe.of(statId.withSuffix("/sacrifice"), parent, new SacrificeDisplayRecipe(i, List.of(), withoutMaterial, List.of())));
        }
      }
      multiRecipes = List.copyOf(newRecipes);
    }
    return multiRecipes;
  }

  /** Recipe handling tool swapping including tool focuses */
  private class DisplayRecipe extends MaterialSwappingRecipe.LinkedDisplayRecipe {
    private static final Component TITLE = TConstruct.makeTranslation("recipe", "tool_material_swapping");
    private static final Component TOOLTIP = TConstruct.makeTranslation("recipe", "tool_material_swapping.tooltip");

    private final MaterialStatsId statType;
    private final ToolStack displayTool;
    public DisplayRecipe(int index, List<ItemStack> input, List<ItemStack> toolWithoutModifier, List<ItemStack> toolWithModifier, List<? extends MaterialVariant> materials, MaterialStatsId statType, ToolStack displayTool) {
      super(index, input, toolWithoutModifier, toolWithModifier, materials);
      this.statType = statType;
      this.displayTool = displayTool;
    }

    @Override
    public Component getTitle() {
      return TITLE;
    }

    @Override
    public Component getTooltip() {
      return TOOLTIP;
    }


    /* Dynamic focus */

    @Override
    public List<ItemStack> getDisplayItems(int slot, ItemStack focus, boolean focusOutput) {
      if (slot == index && !focus.isEmpty() && (focusOutput || isTool(focus))) {
        MaterialVariant material = ToolMaterials.get(focus, index);
        // if focusing on the output, display a sacrifice that would create this tool
        if (focusOutput) {
          if (material.hasStats(statType)) {
            ToolStack tool = displayTool.copy();
            tool.replaceMaterial(index, material);
            return List.of(tool.getStack());
          }
        } else {
          // sacrifice generic tools, will sacrifice the focus itself in the other display recipe
          List<ItemStack> tools = indicesWithout(material).mapToObj(input::get).toList();
          if (!tools.isEmpty()) return tools;
        }
      }
      return getDisplayItems(slot);
    }

    @Override
    public List<ItemStack> getToolWithModifier(ItemStack focus, boolean focusOutput) {
      if (!focus.isEmpty()) {
        if (focusOutput) {
          return getOutputFocusWithModifier(statType, focus);
        } else if (isTool(focus)) {
          return getInputFocusWithModifier(focus);
        }
      }
      return toolWithModifier;
    }
  }

  /** Display recipe for using the tool itself as a sacrifice for part swapping. */
  private class SacrificeDisplayRecipe extends MaterialSwappingRecipe.DisplayRecipe {
    public SacrificeDisplayRecipe(int index, List<ItemStack> input, List<ItemStack> toolWithoutModifier, List<ItemStack> toolWithModifier) {
      super(index, input, toolWithoutModifier, toolWithModifier);
    }

    @Override
    public Component getTitle() {
      return DisplayRecipe.TITLE;
    }

    @Override
    public Component getTooltip() {
      return DisplayRecipe.TOOLTIP;
    }

    /* Focus */

    @Override
    public List<ItemStack> getDisplayItems(int slot, ItemStack focus, boolean focusOutput) {
      if (slot == index) {
        return List.of(focus.copyWithCount(1));
      }
      return getDisplayItems(slot);
    }

    @Override
    public List<ItemStack> getToolWithModifier(ItemStack focus, boolean focusOutput) {
      // copy materials from the display output, but replace the swapped material to that from the focus
      return List.of(replaceMaterial(ToolMaterials.get(toolWithoutModifier.get(0)), ToolMaterials.get(focus, index), focus));
    }


    /* Filtering */

    @Override
    public boolean isFiltered() {
      return true;
    }

    @Override
    public boolean showUnfocused() {
      return false;
    }

    @Override
    public boolean isVisibleFromItem(ItemStack focus, boolean output) {
      // only show on inputs without modifiers
      return !output && !ModifierEntries.hasCrafted(focus);
    }
  }
}
