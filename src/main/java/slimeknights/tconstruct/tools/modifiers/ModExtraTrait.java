package slimeknights.tconstruct.tools.modifiers;

import com.google.common.collect.ImmutableList;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import slimeknights.mantle.util.RecipeMatch;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.modifiers.ModifierAspect;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.modifiers.TinkerGuiException;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.IToolPart;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.ToolBuilder;
import slimeknights.tconstruct.tools.TinkerModifiers;

public class ModExtraTrait extends ToolModifier {

  public static final String EXTRA_TRAIT_IDENTIFIER = "extratrait";
  private final Material material;
  private final Set<ToolCore> toolCores;
  private final Collection<ITrait> traits;

  public  ModExtraTrait(Material material, Collection<ITrait> traits) {
    super(EXTRA_TRAIT_IDENTIFIER + generateIdentifier(material, traits), material.materialTextColor);

    this.material = material;
    this.toolCores = new HashSet<>();
    this.traits = traits;
    addAspects(new ExtraTraitAspect(material), new ModifierAspect.SingleAspect(this), new ModifierAspect.DataAspect(this));
  }

  public <T extends Item & IToolPart> void addCombination(ToolCore toolCore, T toolPart) {
    toolCores.add(toolCore);
    ItemStack kit = toolPart.getItemstackWithMaterial(material);
    ItemStack diamond = new ItemStack(Blocks.DIAMOND_BLOCK);
    addRecipeMatch(new RecipeMatch.ItemCombination(1, kit, diamond));
  }

  private static String generateIdentifier(Material material, Collection<ITrait> traits) {
    String traitString = traits.stream().map(ITrait::getIdentifier).sorted().collect(Collectors.joining());
    return material.getIdentifier() + traitString;
  }

  @Override
  protected boolean canApplyCustom(ItemStack stack) throws TinkerGuiException {
    return stack.getItem() instanceof ToolCore && toolCores.contains(stack.getItem());
  }

  @Override
  public String getLocalizedName() {
    return Util.translate(LOC_Name, EXTRA_TRAIT_IDENTIFIER) + " (" + material.getLocalizedName() + ")";
  }

  @Override
  public String getLocalizedDesc() {
    return Util.translateFormatted(String.format(LOC_Desc, EXTRA_TRAIT_IDENTIFIER), material.getLocalizedName());
  }

  @Override
  public void applyEffect(NBTTagCompound rootCompound, NBTTagCompound modifierTag) {
    NBTTagCompound tag = TagUtil.getToolTag(rootCompound);
    traits.forEach(trait -> ToolBuilder.addTrait(rootCompound, trait, color));
  }

  @Override
  public boolean hasTexturePerMaterial() {
    return true;
  }

  public static List<ITrait> getTraitsForPart(Material material, ToolCore toolCore, IToolPart toolPart) {
    List<PartMaterialType> pmts = toolCore.getRequiredComponents().stream()
                                                                .filter(pmt -> pmt.isValid(toolPart, material))
                                                                .collect(Collectors.toList());

    List<Collection<ITrait>> traitLists = pmts.stream().map(pmt -> pmt.getApplicableTraitsForMaterial(material)).distinct().collect(Collectors.toList());

    TinkerModifiers.log.info("test {}", "successful");
    if(traitLists.size() > 1) {
      TinkerModifiers.log.error("Found an extra-trait combination of material/part that is not uniquely identifiable, probably a bug?");
    }
    if(traitLists.isEmpty()) {
      return Collections.emptyList();
    }
    return ImmutableList.copyOf(traitLists.get(0));
  }

  private static class ExtraTraitAspect extends ModifierAspect {

    private final Material material;

    public ExtraTraitAspect(Material material) {
      this.material = material;
    }

    @Override
    public boolean canApply(ItemStack stack, ItemStack original) throws TinkerGuiException {
      NBTTagList modifierList = TagUtil.getModifiersTagList(original);
      for(int i = 0; i < modifierList.tagCount(); i++) {
        NBTTagCompound tag = modifierList.getCompoundTagAt(i);
        ModifierNBT data = ModifierNBT.readTag(tag);
        if(data.identifier.startsWith(EXTRA_TRAIT_IDENTIFIER)) {
          throw new TinkerGuiException(Util.translate("gui.error.already_has_extratrait"));
        }
      }
      return true;
    }

    @Override
    public void updateNBT(NBTTagCompound root, NBTTagCompound modifierTag) {
      // nothing to do
    }


  }
}