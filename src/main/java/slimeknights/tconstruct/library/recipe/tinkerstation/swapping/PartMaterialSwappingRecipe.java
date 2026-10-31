package slimeknights.tconstruct.library.recipe.tinkerstation.swapping;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.MultiNamedRecipe;
import slimeknights.mantle.recipe.NamedRecipe;
import slimeknights.tconstruct.library.materials.IMaterialRegistry;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.RecipeResult;
import slimeknights.tconstruct.library.recipe.TinkerRecipes;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialCastingLookup;
import slimeknights.tconstruct.library.recipe.modifiers.adding.ModifierRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.IDisplayToolTinkering;
import slimeknights.tconstruct.library.recipe.tinkerstation.TinkerStationRecipeInput;
import slimeknights.tconstruct.library.tools.definition.module.material.ToolPartsHook;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.LazyToolStack;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.IToolPart;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Recipe that replaces a tool part with another.
 * TODO consider merging with {@link PartSwappingOverrideRecipe} or {@link slimeknights.tconstruct.library.recipe.tinkerstation.building.ToolBuildingRecipe}
 * That or merge it into tool building as previously planned.
 * @see ToolMaterialSwappingRecipeBuilder
 */
public class PartMaterialSwappingRecipe extends MaterialSwappingRecipe implements MultiNamedRecipe<IDisplayToolTinkering> {
  public static final RecordLoadable<PartMaterialSwappingRecipe> LOADER = RecordLoadable.create(TOOLS_FIELD, STACK_SIZE_FIELD, EXTRA_REQUIREMENTS_FIELD, PartMaterialSwappingRecipe::new);

  protected PartMaterialSwappingRecipe(Ingredient tools, int maxStackSize, List<SizedIngredient> extraRequirements) {
    super(tools, maxStackSize, extraRequirements);
  }

  @Override
  public boolean matches(TinkerStationRecipeInput inv, Level world) {
    ItemStack tinkerable = inv.getTinkerableStack();
    if (tinkerable.isEmpty() || !tools.test(tinkerable) || !(tinkerable.getItem() instanceof IModifiable modifiable)) {
      return false;
    }
    // get the list of parts, empty means its not multipart
    List<IToolPart> parts = ToolPartsHook.parts(modifiable.getToolDefinition());
    if (parts.isEmpty()) {
      return false;
    }

    BitSet used = ModifierRecipe.makeBitset(inv);
    // we have two concerns on part swapping:
    // part must be valid in the tool
    boolean foundItem = false;
    for (int i = 0; i < inv.getInputCount(); i++) {
      ItemStack stack = inv.getInput(i);
      if (!stack.isEmpty() && stack.getItem() instanceof IToolPart part) {
        // if the part is not in the list, don't bother with this recipe even if another part mathces later
        if (!parts.contains(part)) {
          return false;
        }
        foundItem = true;
        used.set(i);
        break;
      }
    }
    // extra requiremenets ensures we do not try to swap multiple parts
    return foundItem && ModifierRecipe.checkMatch(inv, extraRequirements, used);
  }

  @Override
  public RecipeResult<LazyToolStack> getValidatedResult(TinkerStationRecipeInput inv, HolderLookup.Provider registries) {
    // copy the tool NBT to ensure the original tool is intact
    List<IToolPart> parts = ToolPartsHook.parts(inv.getTinkerable().getDefinition());

    // prevent part swapping on large tools in small tables
    if (parts.size() > inv.getInputCount()) {
      return TOO_MANY_PARTS;
    }

    // actual part swap logic
    for (int i = 0; i < inv.getInputCount(); i++) {
      ItemStack stack = inv.getInput(i);
      if (!stack.isEmpty() && stack.getItem() instanceof IToolPart part) {
        // ensure the part is valid
        MaterialVariantId partVariant = part.getMaterial(stack);
        if (partVariant.equals(MaterialId.UNKNOWN)) {
          return RecipeResult.pass();
        }

        // we have a part and its not at this index, find the first copy of this part
        // means slot only matters if a tool uses a part twice
        int index = i;
        if (i >= parts.size() || parts.get(i) != part) {
          index = IntStream.range(0, parts.size())
                           .filter(pi -> parts.get(pi) == part)
                           .findFirst().orElse(-1);
          if (index == -1) {
            return RecipeResult.pass();
          }
        }
        return swapMaterial(inv, partVariant, index, MaterialCastingLookup.getItemCost(part));
      }
    }
    // no item found, should never happen
    return RecipeResult.pass();
  }

  @Override
  protected boolean shrinkPart(TinkerStationRecipeInput.Mutable inv, int index, ItemStack stack) {
    if (stack.getItem() instanceof IToolPart) {
      inv.shrinkInput(index, 1);
      return true;
    }
    return false;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerRecipes.partSwapping.get();
  }


  /* JEI */
  private List<NamedRecipe<IDisplayToolTinkering>> multiRecipes;

  @Override
  public List<NamedRecipe<IDisplayToolTinkering>> getRecipes(ResourceLocation id, HolderLookup.Provider registries) {
    if (multiRecipes == null) {
      IMaterialRegistry registry = MaterialRegistry.getInstance();
      Collection<IMaterial> materials = registry.getVisibleMaterials();
      List<ResourceLocation> parent = List.of(id);
      multiRecipes = Arrays.stream(tools.getItems()).flatMap(stack -> {
        ToolStack tool = ToolStack.from(stack);
        List<IToolPart> parts = ToolPartsHook.parts(tool.getDefinition());
        // JEI only shows up to 5 slots
        if (parts.size() > MAX_SLOTS) {
          return Stream.empty();
        }
        ResourceLocation toolId = id.withSuffix('/' + Loadables.ITEM.getKey(stack.getItem()).toDebugFileName());
        return IntStream.range(0, parts.size()).mapToObj(i -> {
          IToolPart part = parts.get(i);
          ToolStack copy = tool.copy();
          List<IMaterial> filtered = materials.stream().filter(mat -> registry.getMaterialStats(mat.getIdentifier(), part.getStatType()).isPresent()).toList();
          return NamedRecipe.<IDisplayToolTinkering>of(toolId.withSuffix("/part_" + i), parent, new PartDisplayRecipe(i,
            // one part per material
            filtered.stream().map(mat -> part.withMaterialForDisplay(mat.getIdentifier())).toList(),
            // single tool with the material to swap left blank
            List.of(withMaterial(copy, i, MaterialVariant.of(ToolBuildHandler.getRenderMaterial(0))).copy()),
            // one output per material
            filtered.stream().map(mat -> withMaterial(copy, i, MaterialVariant.of(mat)).copy()).toList(),
            // part info
            filtered.stream().map(MaterialVariant::of).toList(), part
          ));
        });
      }).toList();
    }
    return multiRecipes;
  }
}
