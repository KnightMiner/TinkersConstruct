package slimeknights.tconstruct.tools.data;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.Tags.Fluids;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.fluid.MantleFluids;
import slimeknights.mantle.recipe.condition.TagFilledCondition;
import slimeknights.mantle.recipe.data.ItemNameIngredient;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.SimpleFinishedRecipe;
import slimeknights.mantle.recipe.ingredient.EntityIngredient;
import slimeknights.mantle.recipe.ingredient.FluidContainerIngredient;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.mantle.recipe.ingredient.SizedIngredient;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.data.BaseRecipeProvider;
import slimeknights.tconstruct.common.recipe.LoadableFinishedRecipe;
import slimeknights.tconstruct.common.registration.GeodeItemObject.BudSize;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.gadgets.TinkerGadgets;
import slimeknights.tconstruct.gadgets.entity.FrameType;
import slimeknights.tconstruct.library.json.predicate.modifier.ModifierPredicate;
import slimeknights.tconstruct.library.json.predicate.modifier.SlotTypeModifierPredicate;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.util.LazyModifier;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.library.recipe.casting.item.ItemCastingRecipeBuilder;
import slimeknights.tconstruct.library.recipe.casting.potion.PotionCastingRecipeBuilder;
import slimeknights.tconstruct.library.recipe.ingredient.MaterialIngredient;
import slimeknights.tconstruct.library.recipe.ingredient.NoContainerIngredient;
import slimeknights.tconstruct.library.recipe.ingredient.ToolHookIngredient;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IncrementalModifierRecipeBuilder;
import slimeknights.tconstruct.library.recipe.modifiers.adding.ModifierRecipeBuilder;
import slimeknights.tconstruct.library.recipe.modifiers.adding.MultilevelIncrementalModifierRecipeBuilder;
import slimeknights.tconstruct.library.recipe.modifiers.adding.MultilevelModifierRecipeBuilder;
import slimeknights.tconstruct.library.recipe.modifiers.adding.OverslimeModifierRecipeBuilder;
import slimeknights.tconstruct.library.recipe.modifiers.adding.SwappableModifierRecipe.VariantFormatter;
import slimeknights.tconstruct.library.recipe.modifiers.adding.SwappableModifierRecipeBuilder;
import slimeknights.tconstruct.library.recipe.modifiers.severing.SeveringRecipeBuilder;
import slimeknights.tconstruct.library.recipe.tinkerstation.repairing.ModifierRepairRecipeBuilder;
import slimeknights.tconstruct.library.recipe.worktable.ModifierSetWorktableRecipeBuilder;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.TinkerMaterials;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.TinkerToolParts;
import slimeknights.tconstruct.tools.TinkerTools;
import slimeknights.tconstruct.tools.data.material.MaterialIds;
import slimeknights.tconstruct.tools.recipe.BannerModifierRecipe;
import slimeknights.tconstruct.tools.recipe.EnchantmentConvertingRecipeBuilder;
import slimeknights.tconstruct.tools.recipe.ModifierRemovalRecipeBuilder;
import slimeknights.tconstruct.tools.recipe.ModifierSortingRecipeBuilder;
import slimeknights.tconstruct.tools.recipe.ToggleInteractionWorktableRecipeBuilder;
import slimeknights.tconstruct.tools.recipe.severing.MooshroomDemushroomingRecipe;
import slimeknights.tconstruct.tools.recipe.severing.PlayerBeheadingRecipe;
import slimeknights.tconstruct.tools.recipe.severing.SheepShearingRecipe;
import slimeknights.tconstruct.tools.recipe.severing.SnowGolemBeheadingRecipe;
import slimeknights.tconstruct.tools.recipe.severing.SpecialSeveringRecipeBuilder;
import slimeknights.tconstruct.world.TinkerHeadType;
import slimeknights.tconstruct.world.TinkerWorld;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static slimeknights.mantle.Mantle.COMMON;
import static slimeknights.tconstruct.library.recipe.melting.IMeltingRecipe.getTemperature;

public class ModifierRecipeProvider extends BaseRecipeProvider {
  public ModifierRecipeProvider(PackOutput packOutput) {
    super(packOutput);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Modifier Recipes";
  }

  @Override
  protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
    addItemRecipes(consumer);
    addModifierRecipes(consumer);
    addTextureRecipes(consumer);
    addHeadRecipes(consumer);
  }

  private void addItemRecipes(Consumer<FinishedRecipe> consumer) {
    String folder = "tools/modifiers/";

    // durability reinforcements, use obsidian
    ItemCastingRecipeBuilder.tableRecipe(TinkerModifiers.emeraldReinforcement)
                            .setFluidAndTime(TinkerFluids.moltenEmerald, FluidValues.GEM_SHARD)
                            .setCast(TinkerCommons.obsidianPane, true)
                            .save(consumer, prefix(TinkerModifiers.emeraldReinforcement, folder));
    ItemCastingRecipeBuilder.tableRecipe(TinkerModifiers.slimesteelReinforcement)
                            .setFluidAndTime(TinkerFluids.moltenSlimesteel, FluidValues.NUGGET * 3)
                            .setCast(TinkerCommons.obsidianPane, true)
                            .save(consumer, prefix(TinkerModifiers.slimesteelReinforcement, folder));
    // protection reinforcements, use patterns
    ItemCastingRecipeBuilder.tableRecipe(TinkerModifiers.ironReinforcement)
                            .setFluidAndTime(TinkerFluids.moltenIron, FluidValues.INGOT)
                            .setCast(TinkerTables.pattern, true)
                            .save(consumer, prefix(TinkerModifiers.ironReinforcement, folder));
    ItemCastingRecipeBuilder.tableRecipe(TinkerModifiers.searedReinforcement)
                            .setFluid(FluidIngredient.of(TinkerFluids.searedStone.ingredient(FluidValues.BRICK), TinkerFluids.scorchedStone.ingredient(FluidValues.BRICK)))
                            .setCoolingTime(getTemperature(TinkerFluids.searedStone), FluidValues.BRICK)
                            .setCast(TinkerTables.pattern, true)
                            .save(consumer, prefix(TinkerModifiers.searedReinforcement, folder));
    ItemCastingRecipeBuilder.tableRecipe(TinkerModifiers.goldReinforcement)
                            .setFluidAndTime(TinkerFluids.moltenGold, FluidValues.INGOT)
                            .setCast(TinkerTables.pattern, true)
                            .save(consumer, prefix(TinkerModifiers.goldReinforcement, folder));
    ItemCastingRecipeBuilder.tableRecipe(TinkerModifiers.obsidianReinforcement)
                            .setFluidAndTime(TinkerFluids.moltenObsidian, FluidValues.GLASS_BLOCK)
                            .setCast(TinkerTables.pattern, true)
                            .save(consumer, prefix(TinkerModifiers.obsidianReinforcement, folder));
    ItemCastingRecipeBuilder.tableRecipe(TinkerModifiers.cobaltReinforcement)
                            .setFluidAndTime(TinkerFluids.moltenCobalt, FluidValues.INGOT)
                            .setCast(TinkerTables.pattern, true)
                            .save(consumer, prefix(TinkerModifiers.cobaltReinforcement, folder));

    // jeweled apple
    ItemCastingRecipeBuilder.tableRecipe(TinkerCommons.jeweledApple)
                            .setFluidAndTime(TinkerFluids.moltenDiamond, FluidValues.GEM * 2)
                            .setCast(Items.APPLE, true)
                            .save(consumer, prefix(TinkerCommons.jeweledApple, folder));

    // silky cloth
    ItemCastingRecipeBuilder.tableRecipe(TinkerModifiers.silkyCloth)
                            .setCast(Items.COBWEB, true)
                            .setFluidAndTime(TinkerFluids.moltenRoseGold, FluidValues.INGOT)
                            .save(consumer, prefix(TinkerModifiers.silkyCloth, folder));

    // modifier repair
    // pig iron - from bacon
    ModifierRepairRecipeBuilder.repair(ModifierIds.tasty, Ingredient.of(TinkerCommons.bacon), 25)
      .buildCraftingTable(consumer, wrap(ModifierIds.tasty, folder, "_crafting_table"))
      .save(consumer, prefix(ModifierIds.tasty, folder));
  }

  @SuppressWarnings("removal")
  private void addModifierRecipes(Consumer<FinishedRecipe> consumer) {
    // modifiers
    String upgradeFolder = "tools/modifiers/upgrade/";
    String abilityFolder = "tools/modifiers/ability/";
    String slotlessFolder = "tools/modifiers/slotless/";
    String defenseFolder = "tools/modifiers/defense/";
    String compatFolder = "tools/modifiers/compat/";
    String worktableFolder = "tools/modifiers/worktable/";
    // salvage
    String salvageFolder = "tools/modifiers/salvage/";
    String upgradeSalvage = salvageFolder + "upgrade/";
    String abilitySalvage = salvageFolder + "ability/";
    String defenseSalvage = salvageFolder + "defense/";
    String compatSalvage = salvageFolder + "compat/";

    /*
     * durability
     */
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.reinforced)
                         .input(TinkerModifiers.emeraldReinforcement, 1, 4)
                         .setMaxLevel(5) // max 75% resistant to damage
                         .setSlots(SlotType.UPGRADE, 1)
                         .setTools(TinkerTags.Items.DURABILITY)
                         .saveSalvage(consumer, prefix(ModifierIds.reinforced, upgradeSalvage))
                         .save(consumer, prefix(ModifierIds.reinforced, upgradeFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.overforced)
                                    .input(TinkerModifiers.slimesteelReinforcement, 1, 4)
                                    .setMaxLevel(5) // +250 capacity
                                    .setSlots(SlotType.UPGRADE, 1)
                                    .setTools(TinkerTags.Items.DURABILITY)
                                    .saveSalvage(consumer, prefix(ModifierIds.overforced, upgradeSalvage))
                                    .save(consumer, prefix(ModifierIds.overforced, upgradeFolder));
    // gems are special, I'd like them to be useful on all types of tools
    ModifierRecipeBuilder.modifier(ModifierIds.emerald)
                         .setTools(TinkerTags.Items.DURABILITY)
                         .input(Tags.Items.GEMS_EMERALD)
                         .setMaxLevel(1)
                         .setSlots(SlotType.UPGRADE, 1)
                         .saveSalvage(consumer, prefix(ModifierIds.emerald, upgradeSalvage))
                         .save(consumer, prefix(ModifierIds.emerald, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.diamond)
                         .setTools(TinkerTags.Items.DURABILITY)
                         .input(Tags.Items.GEMS_DIAMOND)
                         .setMaxLevel(1)
                         .setSlots(SlotType.UPGRADE, 1)
                         .saveSalvage(consumer, prefix(ModifierIds.diamond, upgradeSalvage))
                         .save(consumer, prefix(ModifierIds.diamond, upgradeFolder));
    Ingredient multiuse = DifferenceIngredient.of(Ingredient.of(TinkerTags.Items.MODIFIABLE), Ingredient.of(TinkerTags.Items.SINGLE_USE));
    ModifierRecipeBuilder.modifier(ModifierIds.worldbound)
      .setTools(multiuse)
      .input(TinkerTags.Items.INGOTS_NETHERITE_SCRAP)
      .setMaxLevel(1)
      .save(consumer, prefix(ModifierIds.worldbound, slotlessFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.soulbound)
      .setTools(multiuse)
      .input(Items.ECHO_SHARD)
      .setSlots(SlotType.UPGRADE, 1)
      .setMaxLevel(1)
      .saveSalvage(consumer, prefix(ModifierIds.soulbound, upgradeSalvage))
      .save(consumer, prefix(ModifierIds.soulbound, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.soulbound)
      .setTools(TinkerTags.Items.SINGLE_USE)
      .input(Items.SCULK_VEIN)
      .setMaxLevel(1)
      .save(consumer, wrap(ModifierIds.soulbound, slotlessFolder, "_ammo"));
    ModifierRecipeBuilder.modifier(ModifierIds.netherite)
                         .setTools(TinkerTags.Items.DURABILITY)
                         .input(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
                         .input(Tags.Items.INGOTS_NETHERITE)
                         .setMaxLevel(1)
                         .setSlots(SlotType.UPGRADE, 1)
                         .saveSalvage(consumer, prefix(ModifierIds.netherite, upgradeSalvage))
                         .save(consumer, prefix(ModifierIds.netherite, upgradeFolder));

    // overslime
    Ingredient overslimeTools = Ingredient.of(TinkerTags.Items.DURABILITY);
    for (SlimeType type : SlimeType.values()) {
      int amount;
      switch (type) {
        // earth is common and easy to get
        case EARTH -> amount = 20;
        // sky is tinkers specialty
        case SKY -> amount = 50;
        // ichor is hard to farm but easier to find, plus you'd probably rather it for fuel
        case ICHOR -> amount = 60;
        // ender is late game
        case ENDER -> amount = 80;
        // unhandled -> update
        default -> {
          continue;
        }
      }
      String name = type.getSerializedName();
      // ball and bottle - base amount
      OverslimeModifierRecipeBuilder.modifier(TinkerCommons.slimeball.get(type), amount)
        .setTools(overslimeTools)
        .save(consumer, location(slotlessFolder + "overslime/" + name + "_ball"));
      OverslimeModifierRecipeBuilder.modifier(TinkerFluids.slimeBottle.get(type), amount)
        .saveCrafting(consumer, location(slotlessFolder + "overslime/" + name + "_bottle_crafting_table"))
        .save(consumer, location(slotlessFolder + "overslime/" + name + "_bottle"));
      // congealed: 4x
      OverslimeModifierRecipeBuilder.modifier(TinkerWorld.congealedSlime.get(type), amount * 4)
        .setTools(overslimeTools)
        .save(consumer, location(slotlessFolder + "overslime/" + name + "_congealed"));
      // block: 9x
      OverslimeModifierRecipeBuilder.modifier(TinkerWorld.slime.get(type), amount * 9)
        .setTools(overslimeTools)
        .save(consumer, location(slotlessFolder + "overslime/" + name + "_block"));
    }

    /*
     * general effects
     */
    ModifierRecipeBuilder.modifier(ModifierIds.experienced)
                         .input(Items.EXPERIENCE_BOTTLE)
                         .input(Items.EXPERIENCE_BOTTLE)
                         .input(Items.EXPERIENCE_BOTTLE)
                         .setMaxLevel(5) // max +250%
                         .setSlots(SlotType.UPGRADE, 1)
                         .setTools(ingredientFromTags(TinkerTags.Items.MELEE, TinkerTags.Items.HARVEST, TinkerTags.Items.LAUNCHERS, TinkerTags.Items.LEGGINGS))
                         .saveSalvage(consumer, prefix(ModifierIds.experienced, upgradeSalvage))
                         .save(consumer, prefix(ModifierIds.experienced, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.magnetic)
                         .input(Items.COMPASS)
                         .setMaxLevel(5)
                         .setSlots(SlotType.UPGRADE, 1)
                         .setTools(ingredientFromTags(TinkerTags.Items.MELEE_WEAPON, TinkerTags.Items.HARVEST))
                         .save(consumer, prefix(ModifierIds.magnetic, upgradeFolder));
    // armor has a max level of 1 per piece, so 4 total
    ModifierRecipeBuilder.modifier(ModifierIds.magnetic)
                         .input(Items.COMPASS)
                         .setMaxLevel(1)
                         .setSlots(SlotType.UPGRADE, 1)
                         .setTools(TinkerTags.Items.WORN_ARMOR) // TODO: reconsider for shields
                         .save(consumer, wrap(ModifierIds.magnetic, upgradeFolder, "_armor"));
    // salvage supports either
    ModifierRecipeBuilder.modifier(ModifierIds.magnetic)
                         .setSlots(SlotType.UPGRADE, 1)
                         .setTools(ingredientFromTags(TinkerTags.Items.MELEE_WEAPON, TinkerTags.Items.HARVEST, TinkerTags.Items.WORN_ARMOR))
                         .saveSalvage(consumer, prefix(ModifierIds.magnetic, upgradeSalvage));
    ModifierRecipeBuilder.modifier(ModifierIds.shiny)
                         .input(Ingredient.of(Items.ENCHANTED_GOLDEN_APPLE, Items.NETHER_STAR))
                         .setMaxLevel(1)
                         .save(consumer, prefix(ModifierIds.shiny, slotlessFolder));
    Ingredient sighted = ingredientFromTags(TinkerTags.Items.HELD, TinkerTags.Items.ARMOR);
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.farsighted)
                                    .setTools(sighted)
                                    .input(Tags.Items.CROPS_CARROT, 1, 45)
                                    .save(consumer, prefix(ModifierIds.farsighted, slotlessFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.nearsighted)
                                    .setTools(sighted)
                                    .input(Items.INK_SAC, 1, 45)
                                    .save(consumer, prefix(ModifierIds.nearsighted, slotlessFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.offhanded)
                         .setTools(TinkerTags.Items.INTERACTABLE_CHARGE_MODIFIER)
                         .input(Items.LEATHER)
                         .input(Items.FIRE_CHARGE)
                         .input(SlimeType.ICHOR.getSlimeballTag())
                         .setMaxLevel(2)
                         .save(consumer, prefix(ModifierIds.offhanded, slotlessFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.blunted)
      .setTools(TinkerTags.Items.INTERACTABLE_LEFT)
      .input(Items.HONEYCOMB)
      .input(Items.FEATHER)
      .input(Items.HONEYCOMB)
      .setMaxLevel(1).checkTraitLevel()
      .save(consumer, prefix(ModifierIds.blunted, slotlessFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.smelting)
      .setTools(ingredientFromTags(TinkerTags.Items.INTERACTABLE, TinkerTags.Items.WORN_ARMOR))
      .input(Blocks.CAMPFIRE)
      .setSlots(SlotType.UPGRADE, 1)
      .setMaxLevel(4)
      .saveSalvage(consumer, prefix(ModifierIds.smelting, upgradeSalvage))
      .save(consumer, prefix(ModifierIds.smelting, upgradeFolder));

    /*
     * Speed
     */

    // haste can use redstone or blocks
    hasteRecipes(consumer, ModifierIds.haste, ingredientFromTags(TinkerTags.Items.HARVEST, TinkerTags.Items.CHESTPLATES), 5, upgradeFolder, upgradeSalvage);
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.blasting)
                                    .setTools(TinkerTags.Items.STONE_HARVEST)
                                    .setInput(Tags.Items.GUNPOWDER, 1, 20)
                                    .setMaxLevel(5) // +50 mining speed at max, conditionally
                                    .setSlots(SlotType.UPGRADE, 1)
                                    .saveSalvage(consumer, prefix(ModifierIds.blasting, upgradeSalvage))
                                    .save(consumer, prefix(ModifierIds.blasting, upgradeFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.hydraulic)
                                    .setTools(TinkerTags.Items.HARVEST)
                                    .setInput(Tags.Items.DUSTS_PRISMARINE, 1, 36) // stupid forge name
                                    .setMaxLevel(5)
                                    .setSlots(SlotType.UPGRADE, 1)
                                    .saveSalvage(consumer, prefix(ModifierIds.hydraulic, upgradeSalvage))
                                    .save(consumer, wrap(ModifierIds.hydraulic, upgradeFolder, "_from_shard"));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.hydraulic)
                                    .setTools(TinkerTags.Items.HARVEST)
                                    .input(Blocks.PRISMARINE, 4, 36)
                                    .leftover(Items.PRISMARINE_SHARD)
                                    .setMaxLevel(5)
                                    .disallowCrystal()
                                    .setSlots(SlotType.UPGRADE, 1)
                                    .save(consumer, wrap(ModifierIds.hydraulic, upgradeFolder, "_from_block"));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.hydraulic)
                                    .setTools(TinkerTags.Items.HARVEST)
                                    .input(Blocks.PRISMARINE_BRICKS, 9, 36)
                                    .leftover(Items.PRISMARINE_SHARD)
                                    .setMaxLevel(5)
                                    .disallowCrystal()
                                    .setSlots(SlotType.UPGRADE, 1)
                                    .save(consumer, wrap(ModifierIds.hydraulic, upgradeFolder, "_from_bricks"));
    IncrementalModifierRecipeBuilder lightspeed = IncrementalModifierRecipeBuilder.modifier(ModifierIds.lightspeed)
      .input(Tags.Items.DUSTS_GLOWSTONE, 1, 64)
      .setMaxLevel(5) // +45 mining speed at max, conditionally
      .setSlots(SlotType.UPGRADE, 1)
      .setTools(ingredientFromTags(TinkerTags.Items.HARVEST, TinkerTags.Items.BOOTS))
      .saveSalvage(consumer, prefix(ModifierIds.lightspeed, upgradeSalvage));
    lightspeed
      .setTools(TinkerTags.Items.HARVEST)
      .save(consumer, wrap(ModifierIds.lightspeed, upgradeFolder, "_harvest_from_dust"));
    lightspeed
      .setMaxLevel(3) // 27% running speed at max, conditionally
      .setTools(TinkerTags.Items.BOOTS)
      .save(consumer, wrap(ModifierIds.lightspeed, upgradeFolder, "_boots_from_dust"));
    lightspeed = IncrementalModifierRecipeBuilder.modifier(ModifierIds.lightspeed)
      .input(Blocks.GLOWSTONE, 4, 64)
      .leftover(Items.GLOWSTONE_DUST)
      .disallowCrystal()
      .setSlots(SlotType.UPGRADE, 1);
    lightspeed
      .setMaxLevel(5)
      .setTools(TinkerTags.Items.HARVEST)
      .save(consumer, wrap(ModifierIds.lightspeed, upgradeFolder, "_harvest_from_block"));
    lightspeed
      .setMaxLevel(3)
      .setTools(TinkerTags.Items.BOOTS)
      .save(consumer, wrap(ModifierIds.lightspeed, upgradeFolder, "_boots_from_block"));

    /*
     * weapon
     */
    ModifierRecipeBuilder.modifier(ModifierIds.knockback)
                         .input(Items.PISTON)
                         .input(TinkerWorld.slime.get(SlimeType.EARTH))
                         .setMaxLevel(3) // max +2.5 knockback points (knockback 5) (whatever that number means in vanilla)
                         .setSlots(SlotType.UPGRADE, 1)
                         .setTools(ingredientFromTags(TinkerTags.Items.MELEE, TinkerTags.Items.CHESTPLATES))
                         .saveSalvage(consumer, prefix(ModifierIds.knockback, upgradeSalvage))
                         .save(consumer, prefix(ModifierIds.knockback, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.padded)
                         .input(Items.LEATHER)
                         .input(ItemTags.WOOL)
                         .input(Items.LEATHER)
                         .setMaxLevel(3) // max 12.5% knockback, or 6.25% on the dagger
                         .setSlots(SlotType.UPGRADE, 1)
                         .setTools(TinkerTags.Items.MELEE)
                         .saveSalvage(consumer, prefix(ModifierIds.padded, upgradeSalvage))
                         .save(consumer, prefix(ModifierIds.padded, upgradeFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.severing)
                         .input(TinkerTags.Items.WITHER_BONES)
                         .input(Items.LIGHTNING_ROD)
                         .input(TinkerTags.Items.WITHER_BONES)
                         .input(Items.TNT)
                         .setMaxLevel(3) // max +25% head drop chance, combine with +15% chance from luck
                         .setSlots(SlotType.UPGRADE, 1)
                         .setTools(ingredientFromTags(TinkerTags.Items.MELEE, TinkerTags.Items.LAUNCHERS))
                         .saveSalvage(consumer, prefix(TinkerModifiers.severing, upgradeSalvage))
                         .save(consumer, prefix(TinkerModifiers.severing, upgradeFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.fiery)
                                    .setTools(ingredientFromTags(TinkerTags.Items.MELEE, TinkerTags.Items.BOWS, TinkerTags.Items.FISHING_RODS, TinkerTags.Items.WORN_ARMOR, TinkerTags.Items.SHIELDS))
                                    .input(Items.BLAZE_POWDER, 1, 25)
                                    .setMaxLevel(5) // +25 seconds fire damage
                                    .setSlots(SlotType.UPGRADE, 1)
                                    .saveSalvage(consumer, prefix(ModifierIds.fiery, upgradeSalvage))
                                    .save(consumer, prefix(ModifierIds.fiery, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.necrotic)
                         .input(TinkerTags.Items.WITHER_BONES)
                         .input(TinkerWorld.congealedSlime.get(SlimeType.ICHOR))
                         .input(Items.GHAST_TEAR)
                         .setMaxLevel(5) // +50% chance of heal, combine with +40% from traits for +90% total
                         .setSlots(SlotType.UPGRADE, 1)
                         .setTools(ingredientFromTags(TinkerTags.Items.MELEE, TinkerTags.Items.BOWS, TinkerTags.Items.FISHING_RODS))
                         .saveSalvage(consumer, prefix(ModifierIds.necrotic, upgradeSalvage))
                         .save(consumer, prefix(ModifierIds.necrotic, upgradeFolder));

    /*
     * damage boost
     */
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.pierce)
                                    .setTools(ingredientFromTags(TinkerTags.Items.MELEE, TinkerTags.Items.LAUNCHERS))
                                    .input(TinkerGadgets.punji, 1, 10)
                                    .setMaxLevel(3) // +3 pierce, +1.5 damage
                                    .setSlots(SlotType.UPGRADE, 1)
                                    .saveSalvage(consumer, prefix(ModifierIds.pierce, upgradeSalvage))
                                    .save(consumer, prefix(ModifierIds.pierce, upgradeFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.smite)
                                    .setTools(TinkerTags.Items.MELEE)
                                    .input(Items.GLISTERING_MELON_SLICE, 1, 5)
                                    .setMaxLevel(5) // +12.5 undead damage
                                    .setSlots(SlotType.UPGRADE, 1)
                                    .saveSalvage(consumer, prefix(ModifierIds.smite, upgradeSalvage))
                                    .save(consumer, prefix(ModifierIds.smite, upgradeFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.baneOfSssss)
                                    .setTools(TinkerTags.Items.MELEE)
                                    .input(Items.FERMENTED_SPIDER_EYE, 1, 15)
                                    .setMaxLevel(5) // +12.5 spider damage
                                    .setSlots(SlotType.UPGRADE, 1)
                                    .saveSalvage(consumer, prefix(ModifierIds.baneOfSssss, upgradeSalvage))
                                    .save(consumer, prefix(ModifierIds.baneOfSssss, upgradeFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.antiaquatic)
                                    .setTools(TinkerTags.Items.MELEE)
                                    .input(Items.PUFFERFISH, 1, 5)
                                    .setMaxLevel(5) // +12.5 fish damage
                                    .setSlots(SlotType.UPGRADE, 1)
                                    .saveSalvage(consumer, prefix(ModifierIds.antiaquatic, upgradeSalvage))
                                    .save(consumer, prefix(ModifierIds.antiaquatic, upgradeFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.cooling)
                                    .setTools(TinkerTags.Items.MELEE)
                                    .input(Items.PRISMARINE_CRYSTALS, 1, 25)
                                    .setMaxLevel(5) // +10 fire mob damage
                                    .setSlots(SlotType.UPGRADE, 1)
                                    .saveSalvage(consumer, prefix(ModifierIds.cooling, upgradeSalvage))
                                    .save(consumer, prefix(ModifierIds.cooling, upgradeFolder));
    // killager uses both types of lapis
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.killager)
                                    .setTools(TinkerTags.Items.MELEE)
                                    .input(Tags.Items.GEMS_LAPIS, 1, 45)
                                    .setMaxLevel(5) // +12.5 illager damage
                                    .setSlots(SlotType.UPGRADE, 1)
                                    .saveSalvage(consumer, prefix(ModifierIds.killager, upgradeSalvage))
                                    .save(consumer, wrap(ModifierIds.killager, upgradeFolder, "_from_dust"));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.killager)
                                    .setTools(TinkerTags.Items.MELEE)
                                    .input(Tags.Items.STORAGE_BLOCKS_LAPIS, 9, 45)
                                    .setMaxLevel(5) // +12.5 illager damage
                                    .disallowCrystal()
                                    .setSlots(SlotType.UPGRADE, 1)
                                    .save(consumer, wrap(ModifierIds.killager, upgradeFolder, "_from_block"));
    // sharpness can use shards or blocks
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.sharpness)
                                    .setTools(TinkerTags.Items.MELEE)
                                    .input(Tags.Items.GEMS_QUARTZ, 1, 36)
                                    .setMaxLevel(5) // +5 damage
                                    .setSlots(SlotType.UPGRADE, 1)
                                    .saveSalvage(consumer, prefix(ModifierIds.sharpness, upgradeSalvage))
                                    .save(consumer, wrap(ModifierIds.sharpness, upgradeFolder, "_from_shard"));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.sharpness)
                                    .setTools(TinkerTags.Items.MELEE)
                                    .setInput(Tags.Items.STORAGE_BLOCKS_QUARTZ, 4, 36)
                                    .setLeftover(new ItemStack(Items.QUARTZ))
                                    .setMaxLevel(5)
                                    .disallowCrystal()
                                    .setSlots(SlotType.UPGRADE, 1)
                                    .save(consumer, wrap(ModifierIds.sharpness, upgradeFolder, "_from_block"));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.sweeping)
                                    .setTools(TinkerTags.Items.SWORD)
                                    .input(Blocks.CHAIN, 1, 5) // 5% per chain, costing 55 nuggets, or just above 6 ingots
                                    .setMaxLevel(3) // goes 25%, 50%, 75%
                                    .setSlots(SlotType.UPGRADE, 1)
                                    .saveSalvage(consumer, prefix(ModifierIds.sweeping, upgradeSalvage))
                                    .save(consumer, prefix(ModifierIds.sweeping, upgradeFolder));
    // swiftstrike works on blocks too, we are nice
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.swiftstrike)
                                    .setTools(TinkerTags.Items.MELEE_WEAPON)
                                    .input(Items.AMETHYST_SHARD, 1, 72)
                                    .setMaxLevel(5)
                                    .setSlots(SlotType.UPGRADE, 1)
                                    .saveSalvage(consumer, prefix(ModifierIds.swiftstrike, upgradeSalvage))
                                    .save(consumer, wrap(ModifierIds.swiftstrike, upgradeFolder, "_from_shard"));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.swiftstrike)
                                    .setTools(TinkerTags.Items.MELEE_WEAPON)
                                    .input(Blocks.AMETHYST_BLOCK, 4, 72)
                                    .leftover(new ItemStack(Items.AMETHYST_SHARD))
                                    .setMaxLevel(5)
                                    .disallowCrystal()
                                    .setSlots(SlotType.UPGRADE, 1)
                                    .save(consumer, wrap(ModifierIds.swiftstrike, upgradeFolder, "_from_block"));

    /*
     * ranged
     */
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.power)
      .setTools(TinkerTags.Items.RANGED_POWER)
      .input(TinkerWorld.ichorGeode.asItem(), 1, 72)
      .setSlots(SlotType.UPGRADE, 1)
      .setMaxLevel(5)
      .saveSalvage(consumer, prefix(ModifierIds.power, upgradeSalvage))
      .save(consumer, prefix(ModifierIds.power, upgradeFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.quickCharge)
      .setTools(TinkerTags.Items.RANGED_QUICK_CHARGE)
      .input(Items.MAGMA_CREAM, 1, 5)
      .setMaxLevel(4)
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, prefix(ModifierIds.quickCharge, upgradeSalvage))
      .save(consumer, prefix(ModifierIds.quickCharge, upgradeFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.trueshot)
                                    .input(Items.TARGET, 1, 10)
                                    .setSlots(SlotType.UPGRADE, 1)
                                    .setMaxLevel(3)
                                    .setTools(TinkerTags.Items.RANGED)
                                    .saveSalvage(consumer, prefix(ModifierIds.trueshot, upgradeSalvage))
                                    .save(consumer, prefix(ModifierIds.trueshot, upgradeFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.blindshot)
                                    .input(Items.DIRT, 1, 10)
                                    .setTools(TinkerTags.Items.RANGED)
                                    .save(consumer, prefix(ModifierIds.blindshot, slotlessFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.punch)
                         .input(Items.PISTON)
                         .input(TinkerWorld.slime.get(SlimeType.SKY))
                         .setMaxLevel(3) // vanilla caps at 2, we want to go a bit beyond that, but it becomes broken too high on fishing rods
                         .setSlots(SlotType.UPGRADE, 1)
                         .setTools(TinkerTags.Items.LAUNCHERS)
                         .saveSalvage(consumer, prefix(ModifierIds.punch, upgradeSalvage))
                         .save(consumer, prefix(ModifierIds.punch, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.arrowPierce)
                         .input(Items.POINTED_DRIPSTONE)
                         .input(Items.POINTED_DRIPSTONE)
                         .input(Items.POINTED_DRIPSTONE)
                         .setMaxLevel(4) // same max as vanilla
                         .setSlots(SlotType.UPGRADE, 1)
                         .setTools(TinkerTags.Items.BOWS) // salvage for old recipe
                         .saveSalvage(consumer, prefix(ModifierIds.arrowPierce, upgradeSalvage))
                         .setTools(TinkerTags.Items.CROSSBOWS)
                         .save(consumer, prefix(ModifierIds.arrowPierce, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.bounce)
      .input(Items.PISTON)
      .input(TinkerWorld.slime.get(SlimeType.ICHOR))
      .setMaxLevel(3) // 7 bounces is more than you will ever need
      .setSlots(SlotType.UPGRADE, 1)
      .setTools(TinkerTags.Items.RANGED_BOUNCE)
      .saveSalvage(consumer, prefix(ModifierIds.bounce, upgradeSalvage))
      .save(consumer, prefix(ModifierIds.bounce, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.freezing)
                         .input(Items.POWDER_SNOW_BUCKET)
                         .setMaxLevel(3)
                         .setSlots(SlotType.UPGRADE, 1)
                         .setTools(ingredientFromTags(TinkerTags.Items.MELEE, TinkerTags.Items.BOWS, TinkerTags.Items.FISHING_RODS, TinkerTags.Items.WORN_ARMOR, TinkerTags.Items.SHIELDS))
                         .saveSalvage(consumer, prefix(ModifierIds.freezing, upgradeSalvage))
                         .save(consumer, prefix(ModifierIds.freezing, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.bulkQuiver)
                         .input(Items.LEATHER)
                         .input(TinkerWorld.skySlimeVine)
                         .input(Items.LEATHER)
                         .input(TinkerWorld.skySlimeVine)
                         .input(TinkerWorld.skySlimeVine)
                         .setSlots(SlotType.ABILITY, 1)
                         .setTools(TinkerTags.Items.BOWS)
                         .saveSalvage(consumer, prefix(ModifierIds.bulkQuiver, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.bulkQuiver, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.trickQuiver)
                         .input(TinkerModifiers.silkyCloth)
                         .input(TinkerWorld.skySlimeVine)
                         .input(TinkerModifiers.silkyCloth)
                         .input(TinkerWorld.skySlimeVine)
                         .input(TinkerWorld.skySlimeVine)
                         .setSlots(SlotType.ABILITY, 1)
                         .setTools(TinkerTags.Items.BOWS)
                         .saveSalvage(consumer, prefix(ModifierIds.trickQuiver, abilitySalvage))
                         .setTools(IntersectionIngredient.of(Ingredient.of(TinkerTags.Items.BOWS), Ingredient.of(TinkerTags.Items.INTERACTABLE)))
                         .save(consumer, prefix(ModifierIds.trickQuiver, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.sliver)
      .input(TinkerModifiers.silkyCloth)
      .input(TinkerWorld.skySlimeVine)
      .input(TinkerModifiers.silkyCloth)
      .input(TinkerWorld.skySlimeVine)
      .input(TinkerWorld.skySlimeVine)
      .setSlots(SlotType.ABILITY, 1)
      .setTools(Ingredient.of(TinkerTags.Items.STAFFS))
      .saveSalvage(consumer, prefix(ModifierIds.sliver, abilitySalvage))
      .save(consumer, prefix(ModifierIds.sliver, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.ballista)
      .input(TinkerMaterials.hepatizon.getIngotTag())
      .input(Items.CHAIN)
      .input(TinkerMaterials.hepatizon.getIngotTag())
      .setSlots(SlotType.ABILITY, 1)
      .setMaxLevel(1).checkTraitLevel()
      .setTools(TinkerTags.Items.BALLISTAS)
      .saveSalvage(consumer, prefix(ModifierIds.ballista, abilitySalvage))
      .save(consumer, prefix(ModifierIds.ballista, abilityFolder));
    BiConsumer<ItemLike,String> crystalshotRecipe = (item, variant) ->
      SwappableModifierRecipeBuilder.modifier(ModifierIds.crystalshot, variant)
                                    .input(item)
                                    .input(Items.BLAZE_ROD)
                                    .input(item)
                                    .input(TinkerMaterials.manyullyn.getIngotTag())
                                    .input(TinkerMaterials.manyullyn.getIngotTag())
                                    .setTools(TinkerTags.Items.BOWS)
                                    .setSlots(SlotType.ABILITY, 1)
                                    .save(consumer, wrap(ModifierIds.crystalshot, abilityFolder, "_" + variant));
    crystalshotRecipe.accept(Items.AMETHYST_CLUSTER, "amethyst");
    crystalshotRecipe.accept(TinkerWorld.earthGeode.getBud(BudSize.CLUSTER), "earthslime");
    crystalshotRecipe.accept(TinkerWorld.skyGeode.getBud(BudSize.CLUSTER), "skyslime");
    crystalshotRecipe.accept(TinkerWorld.ichorGeode.getBud(BudSize.CLUSTER), "ichor");
    crystalshotRecipe.accept(TinkerWorld.enderGeode.getBud(BudSize.CLUSTER), "enderslime");
    crystalshotRecipe.accept(Items.NETHER_QUARTZ_ORE, "quartz");
    SwappableModifierRecipeBuilder.modifier(ModifierIds.crystalshot, "random")
                                  .input(Ingredient.of(TinkerWorld.earthGeode.getBud(BudSize.CLUSTER), TinkerWorld.skyGeode.getBud(BudSize.CLUSTER)))
                                  .input(Ingredient.of(Items.AMETHYST_CLUSTER, Items.NETHER_QUARTZ_ORE))
                                  .input(Ingredient.of(TinkerWorld.ichorGeode.getBud(BudSize.CLUSTER), TinkerWorld.enderGeode.getBud(BudSize.CLUSTER)))
                                  .input(TinkerMaterials.manyullyn.getIngotTag())
                                  .input(TinkerMaterials.manyullyn.getIngotTag())
                                  .setTools(TinkerTags.Items.BOWS)
                                  .setSlots(SlotType.ABILITY, 1)
                                  .allowCrystal() // random is the coolest, and happens to be the easiest to enable
                                  .save(consumer, wrap(ModifierIds.crystalshot, abilityFolder, "_random"));
    ModifierRecipeBuilder.modifier(ModifierIds.crystalshot)
                         .setSlots(SlotType.ABILITY, 1)
                         .setTools(TinkerTags.Items.BOWS)
                         .saveSalvage(consumer, prefix(ModifierIds.crystalshot, abilitySalvage));
    ModifierRecipeBuilder.modifier(ModifierIds.barebow)
      .setTools(TinkerTags.Items.BOWS)
      .addInput(Tags.Items.STRING)
      .input(Tags.Items.RODS_WOODEN)
      .addInput(Tags.Items.STRING)
      .setMaxLevel(1)
      .save(consumer, prefix(ModifierIds.barebow, slotlessFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.multishot)
                         .input(Items.PISTON)
                         .input(TinkerMaterials.steel.getIngotTag())
                         .input(Items.PISTON)
                         .input(SlimeType.ICHOR.getSlimeballTag())
                         .input(SlimeType.ICHOR.getSlimeballTag())
                         .setSlots(SlotType.ABILITY, 1)
                         .setTools(TinkerTags.Items.BOWS) // allow salvaging multishot from an older bow
                         .saveSalvage(consumer, prefix(ModifierIds.multishot, abilitySalvage))
                         .setTools(TinkerTags.Items.CROSSBOWS) // crossbow exclusive now
                         .save(consumer, prefix(ModifierIds.multishot, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.sinistral)
                         .input(TinkerMaterials.slimesteel.getIngotTag())
                         .input(Items.NAUTILUS_SHELL)
                         .input(TinkerMaterials.slimesteel.getIngotTag())
                         .setMaxLevel(1).checkTraitLevel()
                         .setSlots(SlotType.UPGRADE, 1)
                         .setTools(IntersectionIngredient.of(Ingredient.of(TinkerTags.Items.CROSSBOWS), Ingredient.of(TinkerTags.Items.INTERACTABLE_LEFT))) // this is the same recipes as dual wielding, but crossbows do not interact on left
                         .saveSalvage(consumer, prefix(TinkerModifiers.sinistral, upgradeSalvage))
                         .save(consumer, prefix(TinkerModifiers.sinistral, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.scope)
      .setTools(TinkerTags.Items.INTERACTABLE_CHARGE)
      .input(Items.SUGAR)
      .input(Items.SPYGLASS)
      .input(Items.SUGAR)
      .setSlots(SlotType.UPGRADE, 1)
      .setMaxLevel(1).checkTraitLevel()
      .saveSalvage(consumer, prefix(ModifierIds.scope, upgradeSalvage))
      .save(consumer, prefix(ModifierIds.scope, upgradeFolder));

    // fishing
    ModifierRecipeBuilder.modifier(ModifierIds.lure)
      .setTools(TinkerTags.Items.FISHING_RODS)
      .input(TinkerCommons.cheeseIngot)
      .input(TinkerCommons.cheeseIngot)
      .input(TinkerCommons.cheeseIngot)
      .setSlots(SlotType.UPGRADE, 1)
      .setMaxLevel(3)
      .saveSalvage(consumer, prefix(ModifierIds.lure, upgradeSalvage))
      .save(consumer, prefix(ModifierIds.lure, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.grapple)
      .setTools(TinkerTags.Items.FISHING_RODS)
      .input(Items.CHAIN)
      .input(Items.CHAIN)
      .input(TinkerMaterials.slimesteel.getIngotTag())
      .setSlots(SlotType.ABILITY, 1)
      .setMaxLevel(1).checkTraitLevel()
      .saveSalvage(consumer, prefix(ModifierIds.grapple, abilitySalvage))
      .save(consumer, prefix(ModifierIds.grapple, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.drillAttack)
      // allow on anything that might get springing, flinging, or grapple
      .setTools(ingredientFromTags(TinkerTags.Items.INTERACTABLE_CHARGE, TinkerTags.Items.FISHING_RODS))
      .input(TinkerMaterials.blazingBone)
      .input(Items.POINTED_DRIPSTONE)
      .input(TinkerMaterials.blazingBone)
      .setSlots(SlotType.ABILITY, 1)
      .setMaxLevel(1).checkTraitLevel()
      .saveSalvage(consumer, prefix(ModifierIds.drillAttack, abilitySalvage))
      .save(consumer, prefix(ModifierIds.drillAttack, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.collecting)
      .setTools(TinkerTags.Items.FISHING_RODS)
      .input(Blocks.HOPPER)
      .setSlots(SlotType.UPGRADE, 1)
      .setMaxLevel(1).checkTraitLevel()
      .saveSalvage(consumer, prefix(ModifierIds.collecting, upgradeSalvage))
      .save(consumer, prefix(ModifierIds.collecting, upgradeFolder));

    // throwing
    Ingredient bowLimb = MaterialIngredient.of(TinkerToolParts.bowLimb.get());
    ModifierRecipeBuilder.modifier(ModifierIds.throwing)
      .setTools(IntersectionIngredient.of(
        Ingredient.of(TinkerTags.Items.DURABILITY),
        Ingredient.of(TinkerTags.Items.INTERACTABLE_CHARGE),
        ingredientFromTags(TinkerTags.Items.MELEE_WEAPON, TinkerTags.Items.HARVEST)
      ))
      .input(bowLimb)
      .input(TinkerMaterials.cinderslime.getIngotTag())
      .input(MaterialIngredient.of(TinkerToolParts.bowGrip.get()))
      .setSlots(SlotType.ABILITY, 1)
      .setMaxLevel(1).checkTraitLevel()
      .saveSalvage(consumer, prefix(ModifierIds.throwing, abilitySalvage))
      .save(consumer, prefix(ModifierIds.throwing, abilityFolder));
    MultilevelModifierRecipeBuilder.modifier(ModifierIds.returning)
      .setTools(ingredientFromTags(TinkerTags.Items.MELEE_WEAPON, TinkerTags.Items.HARVEST))
      .input(Items.ENDER_PEARL)
      .input(Items.CLOCK)
      .input(Items.ENDER_PEARL)
      .addLevel(SlotType.ABILITY, 1, 1)
      .addLevelRange(SlotType.UPGRADE, 1, 2, 4)
      .checkTraitLevel()
      .saveSalvage(consumer, prefix(ModifierIds.returning, abilitySalvage))
      .save(consumer, prefix(ModifierIds.returning, abilityFolder));

    /*
     * armor
     */
    // protection
    // all held tools can receive defense slots, so give them something to use it for
    Ingredient protectableTools = ingredientFromTags(TinkerTags.Items.ARMOR, TinkerTags.Items.HELD);
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.meleeProtection)
                                    .input(TinkerModifiers.cobaltReinforcement, 1, 5)
                                    .setSlots(SlotType.DEFENSE, 1)
                                    .setTools(protectableTools)
                                    .saveSalvage(consumer, prefix(ModifierIds.meleeProtection, defenseSalvage))
                                    .save(consumer, prefix(ModifierIds.meleeProtection, defenseFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.projectileProtection)
                                    .input(TinkerModifiers.ironReinforcement, 1, 5)
                                    .setSlots(SlotType.DEFENSE, 1)
                                    .setTools(protectableTools)
                                    .saveSalvage(consumer, prefix(ModifierIds.projectileProtection, defenseSalvage))
                                    .save(consumer, prefix(ModifierIds.projectileProtection, defenseFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.blastProtection)
                                    .input(TinkerModifiers.obsidianReinforcement, 1, 5)
                                    .setSlots(SlotType.DEFENSE, 1)
                                    .setTools(protectableTools)
                                    .saveSalvage(consumer, prefix(ModifierIds.blastProtection, defenseSalvage))
                                    .save(consumer, prefix(ModifierIds.blastProtection, defenseFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.magicProtection)
                                    .input(TinkerModifiers.goldReinforcement, 1, 5)
                                    .setSlots(SlotType.DEFENSE, 1)
                                    .setTools(protectableTools)
                                    .saveSalvage(consumer, prefix(ModifierIds.magicProtection, defenseSalvage))
                                    .save(consumer, prefix(ModifierIds.magicProtection, defenseFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.fireProtection)
                                    .input(TinkerModifiers.searedReinforcement, 1, 5)
                                    .setSlots(SlotType.DEFENSE, 1)
                                    .setTools(protectableTools)
                                    .saveSalvage(consumer, prefix(ModifierIds.fireProtection, defenseSalvage))
                                    .save(consumer, prefix(ModifierIds.fireProtection, defenseFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.protection)
                         .input(TinkerModifiers.goldReinforcement)
                         .input(TinkerModifiers.searedReinforcement)
                         .input(TinkerModifiers.obsidianReinforcement)
                         .input(TinkerModifiers.ironReinforcement)
                         .input(TinkerModifiers.cobaltReinforcement)
                         .setSlots(SlotType.ABILITY, 1)
                         .setTools(TinkerTags.Items.ARMOR)
                         .setMaxLevel(1)
                         .saveSalvage(consumer, prefix(ModifierIds.protection, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.protection, abilityFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.blockade)
      .input(TinkerCommons.obsidianPane, 1, 10)
      .setSlots(SlotType.UPGRADE, 1)
      .setTools(IntersectionIngredient.of(Ingredient.of(TinkerTags.Items.INTERACTABLE_CHARGE), Ingredient.of(TinkerTags.Items.DURABILITY)))
      .setMaxLevel(3)
      .saveSalvage(consumer, prefix(ModifierIds.blockade, upgradeSalvage))
      .save(consumer, prefix(ModifierIds.blockade, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.boundless)
      .input(TinkerCommons.obsidianPane, 4)
      .input(Items.WRITABLE_BOOK)
      .input(TinkerCommons.obsidianPane, 4)
      .input(TinkerWorld.ichorGeode, 2)
      .input(TinkerWorld.ichorGeode, 2)
      .setSlots(SlotType.ABILITY, 1)
      .setTools(TinkerTags.Items.SHIELDS)
      .setMaxLevel(1)
      .saveSalvage(consumer, prefix(ModifierIds.boundless, abilitySalvage))
      .save(consumer, prefix(ModifierIds.boundless, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.knockbackResistance)
                         .setTools(TinkerTags.Items.ARMOR)
                         .addInput(SizedIngredient.fromItems(Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL))
                         .setSlots(SlotType.DEFENSE, 1)
                         .setMaxLevel(1)
                         .saveSalvage(consumer, prefix(ModifierIds.knockbackResistance, defenseSalvage))
                         .save(consumer, prefix(ModifierIds.knockbackResistance, defenseFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.turtleShell)
                                    .setInput(Items.SCUTE, 1, 5)
                                    .setSlots(SlotType.DEFENSE, 1)
                                    .setTools(TinkerTags.Items.ARMOR)
                                    .saveSalvage(consumer, prefix(ModifierIds.turtleShell, defenseSalvage))
                                    .save(consumer, prefix(ModifierIds.turtleShell, defenseFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.shulking)
                                    .input(Items.SHULKER_SHELL, 1, 3)
                                    .setSlots(SlotType.DEFENSE, 1)
                                    .setTools(TinkerTags.Items.ARMOR)
                                    .saveSalvage(consumer, prefix(ModifierIds.shulking, defenseSalvage))
                                    .save(consumer, prefix(ModifierIds.shulking, defenseFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.dragonborn)
                                    .input(TinkerModifiers.dragonScale, 1, 5)
                                    .setSlots(SlotType.DEFENSE, 1)
                                    .setTools(TinkerTags.Items.ARMOR)
                                    .saveSalvage(consumer, prefix(ModifierIds.dragonborn, defenseSalvage))
                                    .save(consumer, prefix(ModifierIds.dragonborn, defenseFolder));
    // 3 each for chest and legs, 2 each for boots and helmet, leads to 10 total
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.revitalizing)
                                    .setTools(ingredientFromTags(TinkerTags.Items.WORN_ARMOR)) // revitalizing would suck on an item you constantly change
                                    .input(TinkerCommons.jeweledApple, 1, 2)
                                    .setSlots(SlotType.DEFENSE, 1)
                                    .saveSalvage(consumer, prefix(ModifierIds.revitalizing, defenseSalvage))
                                    .save(consumer, prefix(ModifierIds.revitalizing, defenseFolder));

    // upgrade - counterattack
    Ingredient wornOrShield = ingredientFromTags(TinkerTags.Items.WORN_ARMOR, TinkerTags.Items.SHIELDS); // held armor may include things that cannot block
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.thorns)
                                    .setTools(wornOrShield)
                                    .input(Blocks.CACTUS, 1, 25)
                                    .setMaxLevel(3)
                                    .setSlots(SlotType.UPGRADE, 1)
                                    .saveSalvage(consumer, prefix(ModifierIds.thorns, upgradeSalvage))
                                    .save(consumer, prefix(ModifierIds.thorns, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.springy)
                         .setTools(wornOrShield)
                         .input(Items.PISTON)
                         .input(TinkerWorld.slime.get(SlimeType.ICHOR))
                         .setSlots(SlotType.UPGRADE, 1)
                         .setMaxLevel(3)
                         .saveSalvage(consumer, prefix(ModifierIds.springy, upgradeSalvage))
                         .save(consumer, prefix(ModifierIds.springy, upgradeFolder));
    // upgrade - helmet
    ModifierRecipeBuilder.modifier(ModifierIds.respiration)
                         .setTools(TinkerTags.Items.HELMETS)
                         .input(ItemTags.FISHES)
                         .addInput(Tags.Items.GLASS_COLORLESS)
                         .input(ItemTags.FISHES)
                         .input(Items.KELP)
                         .input(Items.KELP)
                         .setMaxLevel(3)
                         .setSlots(SlotType.UPGRADE, 1)
                         .saveSalvage(consumer, prefix(ModifierIds.respiration, upgradeSalvage))
                         .save(consumer, prefix(ModifierIds.respiration, upgradeFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.itemFrame)
                         .setTools(TinkerTags.Items.HELMETS)
                         .input(Ingredient.of(Arrays.stream(FrameType.values())
                                                               .filter(type -> type != FrameType.CLEAR)
                                                               .map(type -> new ItemStack(TinkerGadgets.itemFrame.get(type)))))
                         .setSlots(SlotType.UPGRADE, 1)
                         .saveSalvage(consumer, prefix(TinkerModifiers.itemFrame, upgradeSalvage))
                         .save(consumer, prefix(TinkerModifiers.itemFrame, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.minimap)
      .setTools(TinkerTags.Items.HELMETS)
      .input(Items.COMPASS)
      .input(Tags.Items.SLIMEBALLS)
      .input(Items.PAPER)
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, prefix(ModifierIds.minimap, upgradeSalvage))
      .save(consumer, prefix(ModifierIds.minimap, upgradeFolder));
    // upgrade - leggings
    hasteRecipes(consumer, ModifierIds.speedy, Ingredient.of(TinkerTags.Items.LEGGINGS), 3, upgradeFolder, upgradeSalvage);
    // leaping changes slot type on level 2
    MultilevelIncrementalModifierRecipeBuilder.modifier(ModifierIds.leaping)
      .setTools(TinkerTags.Items.LEGGINGS)
      .input(TinkerWorld.skyGeode, 1, 36)
      .addLevelRange(SlotType.UPGRADE, 1, 1, 1)
      .addLevelRange(SlotType.ABILITY, 1, 2, 2)
      .saveSalvage(consumer, prefix(ModifierIds.leaping, salvageFolder))
      .save(consumer, wrap(ModifierIds.leaping, upgradeFolder, "_from_crystal"));
    MultilevelIncrementalModifierRecipeBuilder.modifier(ModifierIds.leaping)
      .setTools(TinkerTags.Items.LEGGINGS)
      .input(TinkerWorld.skyGeode.getBlock(), 4, 36)
      .leftover(TinkerWorld.skyGeode)
      .addLevelRange(SlotType.UPGRADE, 1, 1, 1)
      .addLevelRange(SlotType.ABILITY, 1, 2, 2)
      .disallowCrystal()
      .save(consumer, wrap(ModifierIds.leaping, upgradeFolder, "_from_block"));
    ModifierRecipeBuilder.modifier(ModifierIds.stepUp)
                         .setTools(TinkerTags.Items.LEGGINGS)
                         .input(Items.LEATHER)
                         .input(Items.GOLDEN_CARROT)
                         .input(Items.LEATHER)
                         .input(Items.SCAFFOLDING)
                         .input(Items.SCAFFOLDING)
                         .setSlots(SlotType.UPGRADE, 1)
                         .setMaxLevel(2)
                         .saveSalvage(consumer, prefix(ModifierIds.stepUp, upgradeSalvage))
                         .save(consumer, prefix(ModifierIds.stepUp, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.swiftSneak)
                         .setTools(TinkerTags.Items.LEGGINGS)
                         .input(Blocks.SCULK_SENSOR)
                         .setMaxLevel(5)
                         .setSlots(SlotType.UPGRADE, 1)
                         .saveSalvage(consumer, prefix(ModifierIds.swiftSneak, upgradeSalvage))
                         .save(consumer, prefix(ModifierIds.swiftSneak, upgradeFolder));

    // upgrade - boots
    ModifierRecipeBuilder.modifier(ModifierIds.featherFalling)
      .setTools(TinkerTags.Items.BOOTS)
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, prefix(ModifierIds.featherFalling, upgradeSalvage));
    MultilevelIncrementalModifierRecipeBuilder.modifier(ModifierIds.featherFalling)
      .setTools(TinkerTags.Items.BOOTS)
      .input(Items.FEATHER, 1, 12) // 1% per feather
      .addLevel(SlotType.UPGRADE, 1, 1)
      .addLevel(2)
      .addLevel(SlotType.UPGRADE, 1, 3)
      .addLevel(4)
      .checkTraitLevel()
      .saveSalvage(consumer, prefix(ModifierIds.featherFalling, upgradeSalvage))
      .save(consumer, prefix(ModifierIds.featherFalling, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.longFall)
      .setTools(TinkerTags.Items.BOOTS)
      .input(Items.PISTON)
      .input(Items.PHANTOM_MEMBRANE)
      .input(Items.PISTON)
      .input(TinkerWorld.slime.get(SlimeType.ICHOR))
      .input(TinkerWorld.slime.get(SlimeType.ICHOR))
      .setSlots(SlotType.UPGRADE, 1)
      .setMaxLevel(1).checkTraitLevel()
      .saveSalvage(consumer, prefix(ModifierIds.longFall, upgradeSalvage))
      .save(consumer, prefix(ModifierIds.longFall, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.soulspeed)
                         .setTools(TinkerTags.Items.BOOTS)
                         .input(Items.MAGMA_BLOCK)
                         .input(Items.CRYING_OBSIDIAN)
                         .input(Items.MAGMA_BLOCK)
                         .setSlots(SlotType.UPGRADE, 1)
                         .setMaxLevel(3)
                         .saveSalvage(consumer, prefix(ModifierIds.soulspeed, upgradeSalvage))
                         .save(consumer, prefix(ModifierIds.soulspeed, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.depthStrider)
                         .setTools(TinkerTags.Items.BOOTS)
                         .input(ItemTags.FISHES)
                         .input(Blocks.PRISMARINE_BRICKS)
                         .input(ItemTags.FISHES)
                         .setSlots(SlotType.UPGRADE, 1)
                         .setMaxLevel(3)
                         .saveSalvage(consumer, prefix(ModifierIds.depthStrider, upgradeSalvage))
                         .save(consumer, prefix(ModifierIds.depthStrider, upgradeFolder));

    // upgrade - all
    ModifierRecipeBuilder.modifier(ModifierIds.ricochet)
                         .setTools(wornOrShield)
                         .input(Items.PISTON)
                         .input(TinkerWorld.slime.get(SlimeType.SKY))
                         .setSlots(SlotType.UPGRADE, 1)
                         .setMaxLevel(2) // 2 per piece gives +160% total
                         .saveSalvage(consumer, prefix(ModifierIds.ricochet, upgradeSalvage))
                         .save(consumer, prefix(ModifierIds.ricochet, upgradeFolder));

    // armor ability
    // helmet
    ModifierRecipeBuilder.modifier(ModifierIds.zoom)
                         .setTools(ingredientFromTags(TinkerTags.Items.HELMETS, TinkerTags.Items.INTERACTABLE_CHARGE))
                         .addInput(Tags.Items.STRING)
                         .input(Items.SPYGLASS)
                         .addInput(Tags.Items.STRING)
                         .setSlots(SlotType.UPGRADE, 1)
                         .setMaxLevel(1).checkTraitLevel()
                         .saveSalvage(consumer, prefix(ModifierIds.zoom, upgradeSalvage))
                         .save(consumer, prefix(ModifierIds.zoom, upgradeFolder));
    Ingredient tanks = NoContainerIngredient.of(TinkerTags.Items.TANKS);
    ModifierRecipeBuilder.modifier(ModifierIds.slurping)
                         .input(Items.GLASS_BOTTLE)
                         .input(tanks)
                         .input(Items.GLASS_BOTTLE)
                         .input(Tags.Items.INGOTS_COPPER)
                         .input(Tags.Items.INGOTS_COPPER)
                         .setSlots(SlotType.ABILITY, 1)
                         .setTools(ingredientFromTags(TinkerTags.Items.HELMETS, TinkerTags.Items.INTERACTABLE_CHARGE))
                         .saveSalvage(consumer, prefix(ModifierIds.slurping, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.slurping, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.aquaAffinity)
                         .input(Blocks.PRISMARINE_BRICKS)
                         .input(Items.HEART_OF_THE_SEA)
                         .input(Blocks.PRISMARINE_BRICKS)
                         .input(Blocks.DARK_PRISMARINE)
                         .input(Blocks.DARK_PRISMARINE)
                         .setSlots(SlotType.ABILITY, 1)
                         .setTools(TinkerTags.Items.HELMETS)
                         .setMaxLevel(1).checkTraitLevel()
                         .saveSalvage(consumer, prefix(ModifierIds.aquaAffinity, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.aquaAffinity, abilityFolder));
    // chestplate
    ModifierRecipeBuilder.modifier(TinkerModifiers.ambidextrous)
                         .setTools(TinkerTags.Items.UNARMED)
                         .input(Items.LEATHER)
                         .input(Tags.Items.GEMS_DIAMOND)
                         .input(Items.LEATHER)
                         .addInput(Tags.Items.STRING)
                         .addInput(Tags.Items.STRING)
                         .setMaxLevel(1).checkTraitLevel()
                         .setSlots(SlotType.ABILITY, 1)
                         .saveSalvage(consumer, prefix(TinkerModifiers.ambidextrous, abilitySalvage))
                         .save(consumer, prefix(TinkerModifiers.ambidextrous, abilityFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.strength)
                                    .setTools(TinkerTags.Items.CHESTPLATES)
                                    .input(TinkerWorld.ichorGeode.asItem(), 1, 72)
                                    .setSlots(SlotType.ABILITY, 1)
                                    .setMaxLevel(2)
                                    .saveSalvage(consumer, prefix(ModifierIds.strength, abilitySalvage))
                                    .save(consumer, prefix(ModifierIds.strength, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.wings)
                         .setTools(TinkerTags.Items.CHESTPLATES)
                         .setMaxLevel(1).checkTraitLevel()
                         .input(Items.ELYTRA)
                         .setSlots(SlotType.ABILITY, 2)
                         .saveSalvage(consumer, prefix(ModifierIds.wings, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.wings, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.sleeves)
      .setTools(TinkerTags.Items.CHESTPLATES)
      .input(TinkerModifiers.silkyCloth)
      .input(TinkerMaterials.cinderslime.getIngotTag())
      .input(TinkerModifiers.silkyCloth)
      .setSlots(SlotType.UPGRADE, 1)
      .setMaxLevel(3)
      .saveSalvage(consumer, prefix(TinkerModifiers.sleeves, upgradeSalvage))
      .save(consumer, prefix(TinkerModifiers.sleeves, upgradeFolder));

    // leggings
    ModifierRecipeBuilder.modifier(ModifierIds.pockets)
                         .setTools(TinkerTags.Items.LEGGINGS)
                         .input(Items.SHULKER_SHELL)
                         .input(Tags.Items.INGOTS_IRON)
                         .input(Items.SHULKER_SHELL)
                         .input(Items.LEATHER)
                         .input(Items.LEATHER)
                         .setSlots(SlotType.ABILITY, 1)
                         .setMaxLevel(2)
                         .saveSalvage(consumer, prefix(ModifierIds.pockets, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.pockets, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.shieldStrap)
                         .input(TinkerWorld.skySlimeVine)
                         .input(TinkerMaterials.slimesteel.getIngotTag())
                         .input(TinkerWorld.skySlimeVine)
                         .setSlots(SlotType.UPGRADE, 1)
                         .setTools(TinkerTags.Items.LEGGINGS)
                         .saveSalvage(consumer, prefix(TinkerModifiers.shieldStrap, upgradeSalvage))
                         .save(consumer, prefix(TinkerModifiers.shieldStrap, upgradeFolder));
    BiConsumer<Integer,TagKey<Item>> toolBeltRecipe = (level, ingot) -> {
      ModifierRecipeBuilder builder = ModifierRecipeBuilder
        .modifier(ModifierIds.toolBelt)
        .input(Items.LEATHER)
        .input(ingot)
        .input(Items.LEATHER)
        .setTools(TinkerTags.Items.LEGGINGS)
        .exactLevel(level)
        .useSalvageMax();
      if (level == 1) {
        builder.setSlots(SlotType.ABILITY, 1);
        builder.saveSalvage(consumer, prefix(ModifierIds.toolBelt, abilitySalvage));
      } else {
        builder.disallowCrystal(); // prevent cheesing cost by extracting level 1
      }
      builder.save(consumer, wrap(ModifierIds.toolBelt, abilityFolder, "_" + level));
    };
    toolBeltRecipe.accept(1, Tags.Items.INGOTS_IRON);
    toolBeltRecipe.accept(2, Tags.Items.INGOTS_GOLD);
    toolBeltRecipe.accept(3, TinkerMaterials.roseGold.getIngotTag());
    toolBeltRecipe.accept(4, TinkerMaterials.cobalt.getIngotTag());
    toolBeltRecipe.accept(5, TinkerMaterials.hepatizon.getIngotTag());
    toolBeltRecipe.accept(6, TinkerMaterials.manyullyn.getIngotTag());
    ModifierRecipeBuilder.modifier(ModifierIds.soulBelt)
                         .input(Items.LEATHER)
                         .input(Ingredient.of(Items.RECOVERY_COMPASS))
                         .input(Items.LEATHER)
                         .setSlots(SlotType.ABILITY, 1)
                         .setMaxLevel(1)
                         .setTools(TinkerTags.Items.LEGGINGS)
                         .saveSalvage(consumer, prefix(ModifierIds.soulBelt, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.soulBelt, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.workbench)
                         .input(Items.LEATHER)
                         .input(Blocks.CRAFTING_TABLE)
                         .input(Items.LEATHER)
                         .setMaxLevel(1).checkTraitLevel()
                         .setTools(TinkerTags.Items.LEGGINGS)
                         .save(consumer, prefix(ModifierIds.workbench, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.craftingTable)
                         .input(Items.LEATHER)
                         .input(TinkerTables.craftingStation)
                         .input(Items.LEATHER)
                         .setSlots(SlotType.ABILITY, 1)
                         .setMaxLevel(1).checkTraitLevel()
                         .setTools(TinkerTags.Items.LEGGINGS)
                         .saveSalvage(consumer, prefix(ModifierIds.craftingTable, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.craftingTable, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.wetting)
                         .input(Tags.Items.DUSTS_REDSTONE)
                         .input(tanks)
                         .input(Tags.Items.DUSTS_REDSTONE)
                         .input(Tags.Items.INGOTS_COPPER)
                         .input(Tags.Items.INGOTS_COPPER)
                         .setSlots(SlotType.ABILITY, 1)
                         .setTools(ingredientFromTags(TinkerTags.Items.LEGGINGS, TinkerTags.Items.SHIELDS))
                         .saveSalvage(consumer, prefix(TinkerModifiers.wetting, abilitySalvage))
                         .save(consumer, prefix(TinkerModifiers.wetting, abilityFolder));
    // boots
    ModifierRecipeBuilder.modifier(ModifierIds.doubleJump)
                         .setTools(TinkerTags.Items.BOOTS)
                         .input(Items.PISTON)
                         .input(TinkerWorld.slime.get(SlimeType.SKY))
                         .input(Items.PISTON)
                         .input(Items.PHANTOM_MEMBRANE)
                         .input(Items.PHANTOM_MEMBRANE)
                         .setSlots(SlotType.ABILITY, 1)
                         .setMaxLevel(2)
                         .saveSalvage(consumer, prefix(ModifierIds.doubleJump, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.doubleJump, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.bouncy)
                         .setTools(TinkerTags.Items.BOOTS)
                         .input(TinkerWorld.congealedSlime.get(SlimeType.SKY),   4)
                         .input(TinkerWorld.congealedSlime.get(SlimeType.ICHOR), 4)
                         .input(TinkerWorld.congealedSlime.get(SlimeType.SKY),   4)
                         .input(TinkerWorld.congealedSlime.get(SlimeType.EARTH), 4)
                         .input(TinkerWorld.congealedSlime.get(SlimeType.EARTH), 4)
                         .setSlots(SlotType.ABILITY, 1)
                         .setMaxLevel(1).checkTraitLevel()
                         .saveSalvage(consumer, prefix(ModifierIds.bouncy, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.bouncy, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.frostWalker)
                         .setTools(TinkerTags.Items.BOOTS)
                         .input(Items.BLUE_ICE)
                         .input(TinkerWorld.heads.get(TinkerHeadType.STRAY))
                         .input(Items.BLUE_ICE)
                         .input(Items.BLUE_ICE)
                         .input(Items.BLUE_ICE)
                         .setSlots(SlotType.ABILITY, 1)
                         .setMaxLevel(1)
                         .saveSalvage(consumer, prefix(ModifierIds.frostWalker, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.frostWalker, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.snowdrift)
                         .setTools(TinkerTags.Items.BOOTS)
                         .input(Items.SNOW_BLOCK)
                         .input(Items.CARVED_PUMPKIN)
                         .input(Items.SNOW_BLOCK)
                         .input(Items.SNOW_BLOCK)
                         .input(Items.SNOW_BLOCK)
                         .setSlots(SlotType.ABILITY, 1)
                         .setMaxLevel(1)
                         .saveSalvage(consumer, prefix(ModifierIds.snowdrift, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.snowdrift, abilityFolder));

    // transform ingredients
    Ingredient bootsWithDuraibility = IntersectionIngredient.of(Ingredient.of(TinkerTags.Items.BOOTS), Ingredient.of(TinkerTags.Items.DURABILITY));
    ModifierRecipeBuilder.modifier(ModifierIds.flamewake)
                         .setTools(bootsWithDuraibility)
                         .input(Items.FLINT)
                         .input(TinkerTags.Items.INGOTS_NETHERITE_SCRAP)
                         .input(Items.FLINT)
                         .input(Items.FLINT)
                         .input(Items.FLINT)
                         .setMaxLevel(1)
                         .setSlots(SlotType.ABILITY, 1)
                         .saveSalvage(consumer, prefix(ModifierIds.flamewake, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.flamewake, abilityFolder));

    /*
     * ability
     */
    ModifierRecipeBuilder.modifier(ModifierIds.gilded)
                         .setTools(TinkerTags.Items.BONUS_SLOTS)
                         .input(Blocks.GILDED_BLACKSTONE)
                         .setSlots(SlotType.ABILITY, 1)
                         .saveSalvage(consumer, prefix(ModifierIds.gilded, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.gilded, abilityFolder));
    // luck is 3 recipes
    // level 1 always requires a slot
    Ingredient luckSupporting = ingredientFromTags(TinkerTags.Items.MELEE_WEAPON, TinkerTags.Items.HARVEST, TinkerTags.Items.LAUNCHERS);
    ModifierRecipeBuilder.modifier(ModifierIds.luck)
                         .setTools(luckSupporting)
                         .input(Tags.Items.INGOTS_COPPER)
                         .addInput(SizedIngredient.fromItems(Items.CORNFLOWER, Items.BLUE_ORCHID))
                         .input(Tags.Items.INGOTS_COPPER)
                         .input(Tags.Items.STORAGE_BLOCKS_LAPIS)
                         .input(Tags.Items.STORAGE_BLOCKS_LAPIS)
                         .setMaxLevel(1)
                         .setSlots(SlotType.ABILITY, 1)
                         .allowCrystal()
                         .save(consumer, wrap(ModifierIds.luck, abilityFolder, "_level_1"));
    ModifierRecipeBuilder.modifier(ModifierIds.luck)
                         .setTools(luckSupporting)
                         .input(Tags.Items.INGOTS_GOLD)
                         .input(Items.GOLDEN_CARROT)
                         .input(Tags.Items.INGOTS_GOLD)
                         .input(Tags.Items.ENDER_PEARLS)
                         .input(Tags.Items.ENDER_PEARLS)
                         .disallowCrystal() // would allow a cost cheese
                         .exactLevel(2)
                         .save(consumer, wrap(ModifierIds.luck, abilityFolder, "_level_2"));
    ModifierRecipeBuilder.modifier(ModifierIds.luck)
                         .setTools(luckSupporting)
                         .input(TinkerMaterials.roseGold.getIngotTag())
                         .input(Items.RABBIT_FOOT)
                         .input(TinkerMaterials.roseGold.getIngotTag())
                         .input(Tags.Items.GEMS_DIAMOND)
                         .input(Items.NAME_TAG)
                         .disallowCrystal() // would allow a cost cheese
                         .exactLevel(3)
                         .save(consumer, wrap(ModifierIds.luck, abilityFolder, "_level_3"));
    // add recipe to salvage luck from pants
    // TODO 1.21: remove this salvage recipe
    ModifierRecipeBuilder.modifier(ModifierIds.luck)
                         .setTools(TinkerTags.Items.LEGGINGS)
                         .setMaxLevel(1)
                         .setSlots(SlotType.ABILITY, 1)
                         .disallowCrystal() // prevents cheesing cost using luck 1
                         .saveSalvage(consumer, wrap(ModifierIds.luck, abilitySalvage, "_pants"));
    ModifierRecipeBuilder.modifier(ModifierIds.luck)
                         .setTools(ingredientFromTags(TinkerTags.Items.MELEE, TinkerTags.Items.HARVEST, TinkerTags.Items.LAUNCHERS))
                         .exactLevel(1)
                         .useSalvageMax()
                         .setSlots(SlotType.ABILITY, 1)
                         .saveSalvage(consumer, prefix(ModifierIds.luck, abilitySalvage));

    // silky: all the cloth
    ModifierRecipeBuilder.modifier(ModifierIds.silky)
                         .input(TinkerModifiers.silkyCloth)
                         .input(TinkerModifiers.silkyCloth)
                         .input(TinkerModifiers.silkyCloth)
                         .input(TinkerModifiers.silkyCloth)
                         .input(TinkerModifiers.silkyCloth)
                         .setMaxLevel(1).checkTraitLevel()
                         .setSlots(SlotType.ABILITY, 1)
                         .setTools(TinkerTags.Items.HARVEST)
                         .saveSalvage(consumer, prefix(ModifierIds.silky, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.silky, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.exchanging)
                         .input(Items.STICKY_PISTON)
                         .input(TinkerMaterials.hepatizon.getIngotTag())
                         .input(Items.STICKY_PISTON)
                         .input(Tags.Items.ENDER_PEARLS)
                         .input(Tags.Items.ENDER_PEARLS)
                         .setMaxLevel(1).checkTraitLevel()
                         .setSlots(SlotType.ABILITY, 1)
                         .setTools(TinkerTags.Items.HARVEST)
                         .saveSalvage(consumer, prefix(TinkerModifiers.exchanging, abilitySalvage))
                         .save(consumer, prefix(TinkerModifiers.exchanging, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.autosmelt)
                         .input(Tags.Items.RAW_MATERIALS)
                         .input(Blocks.BLAST_FURNACE)
                         .input(Tags.Items.INGOTS)
                         .input(Tags.Items.STORAGE_BLOCKS_COAL)
                         .input(Tags.Items.STORAGE_BLOCKS_COAL)
                         .setMaxLevel(1)
                         .setSlots(SlotType.ABILITY, 1)
                         .setTools(ingredientFromTags(TinkerTags.Items.HARVEST, TinkerTags.Items.FISHING_RODS))
                         .saveSalvage(consumer, prefix(ModifierIds.autosmelt, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.autosmelt, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.channeling)
      .input(Blocks.LIGHTNING_ROD)
      .input(Blocks.CREEPER_HEAD)
      .input(Blocks.LIGHTNING_ROD)
      .input(Blocks.LIGHTNING_ROD)
      .input(Blocks.LIGHTNING_ROD)
      .setMaxLevel(1).checkTraitLevel()
      .setSlots(SlotType.ABILITY, 1)
      .setTools(ingredientFromTags(TinkerTags.Items.MELEE_WEAPON, TinkerTags.Items.FISHING_RODS))
      .saveSalvage(consumer, prefix(ModifierIds.channeling, abilitySalvage))
      .save(consumer, prefix(ModifierIds.channeling, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.fins)
      .input(ItemTags.FISHES)
      .input(Blocks.PRISMARINE_BRICKS)
      .input(ItemTags.FISHES)
      .setMaxLevel(1).checkTraitLevel()
      .setSlots(SlotType.UPGRADE, 1)
      .setTools(TinkerTags.Items.MELEE_WEAPON)
      .saveSalvage(consumer, prefix(ModifierIds.fins, upgradeSalvage))
      .save(consumer, prefix(ModifierIds.fins, upgradeFolder));

    // fluid stuff
    ModifierRecipeBuilder.modifier(TinkerModifiers.melting)
                         .input(Items.BLAZE_ROD)
                         .input(Ingredient.of(TinkerSmeltery.searedMelter, TinkerSmeltery.smelteryController, TinkerSmeltery.foundryController))
                         .input(Items.BLAZE_ROD)
                         .input(Items.LAVA_BUCKET)
                         .input(Items.LAVA_BUCKET)
                         .setMaxLevel(1)
                         .checkTraitLevel()
                         .setSlots(SlotType.ABILITY, 1)
                         .setTools(ingredientFromTags(TinkerTags.Items.MELEE, TinkerTags.Items.HARVEST))
                         .saveSalvage(consumer, prefix(TinkerModifiers.melting, abilitySalvage))
                         .save(consumer, prefix(TinkerModifiers.melting, abilityFolder));
    SizedIngredient faucets = SizedIngredient.fromItems(TinkerSmeltery.searedFaucet, TinkerSmeltery.scorchedFaucet); // no salvage as don't want conversion between seared and scorched
    ModifierRecipeBuilder.modifier(ModifierIds.bucketing)
                         .addInput(faucets)
                         .input(Items.BUCKET)
                         .addInput(faucets)
                         .input(TinkerMaterials.steel.getIngotTag())
                         .input(TinkerMaterials.steel.getIngotTag())
                         .setMaxLevel(1)
                         .setSlots(SlotType.ABILITY, 1)
                         .setTools(TinkerTags.Items.INTERACTABLE)
                         .saveSalvage(consumer, prefix(ModifierIds.bucketing, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.bucketing, abilityFolder));
    SizedIngredient channels = SizedIngredient.fromItems(TinkerSmeltery.searedChannel, TinkerSmeltery.scorchedChannel);
    ModifierRecipeBuilder.modifier(ModifierIds.spilling)
                         .addInput(channels)
                         .input(tanks)
                         .addInput(channels)
                         .input(Tags.Items.INGOTS_COPPER)
                         .input(Tags.Items.INGOTS_COPPER)
                         .setSlots(SlotType.ABILITY, 1)
                         .setTools(TinkerTags.Items.MELEE)
                         .saveSalvage(consumer, prefix(ModifierIds.spilling, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.spilling, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.splashing)
                         .input(MantleTags.Items.SPLASH_BOTTLE)
                         .input(tanks)
                         .input(MantleTags.Items.SPLASH_BOTTLE)
                         .input(Tags.Items.INGOTS_COPPER)
                         .input(Tags.Items.INGOTS_COPPER)
                         .setSlots(SlotType.ABILITY, 1)
                         .setTools(IntersectionIngredient.of(Ingredient.of(TinkerTags.Items.DURABILITY), Ingredient.of(TinkerTags.Items.INTERACTABLE)))
                         .saveSalvage(consumer, prefix(ModifierIds.splashing, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.splashing, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.bursting)
                         .input(Blocks.CACTUS)
                         .input(tanks)
                         .input(Blocks.CACTUS)
                         .input(Tags.Items.INGOTS_COPPER)
                         .input(Tags.Items.INGOTS_COPPER)
                         .setSlots(SlotType.ABILITY, 1)
                         .setTools(ingredientFromTags(TinkerTags.Items.CHESTPLATES, TinkerTags.Items.SHIELDS))
                         .saveSalvage(consumer, prefix(TinkerModifiers.bursting, abilitySalvage))
                         .save(consumer, prefix(TinkerModifiers.bursting, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.spitting)
      .input(bowLimb)
      .input(TinkerSmeltery.searedFluidCannon)
      .input(bowLimb)
      .setSlots(SlotType.ABILITY, 1)
      .setMaxLevel(3).checkTraitLevel()
      // swasher gets spitting to get multishot, rest get to spit with their non-spit. No spitting with arrows
      .setTools(IntersectionIngredient.of(
        Ingredient.of(TinkerTags.Items.DURABILITY),
        Ingredient.of(TinkerTags.Items.INTERACTABLE_CHARGE_MODIFIER)
      ))
      .saveSalvage(consumer, prefix(ModifierIds.spitting, abilitySalvage))
      .save(consumer, prefix(ModifierIds.spitting, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.slimeball)
      .input(TinkerWorld.skySlimeVine)
      .input(TinkerMaterials.slimesteel.getIngotTag())
      .input(TinkerWorld.skySlimeVine)
      .input(TinkerTags.Items.SLIMY_LOGS)
      .input(TinkerTags.Items.SLIMY_LOGS)
      .setSlots(SlotType.ABILITY, 1)
      .setMaxLevel(1).checkTraitLevel()
      // staff exclusive ranged option, though notably melting pan is a staff
      .setTools(TinkerTags.Items.STAFFS)
      .saveSalvage(consumer, prefix(ModifierIds.slimeball, abilitySalvage))
      .save(consumer, prefix(ModifierIds.slimeball, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.tank)
                         .input(tanks)
                         .setSlots(SlotType.UPGRADE, 1)
                         .setTools(ingredientFromTags(TinkerTags.Items.HELD, TinkerTags.Items.ARMOR))
                         .saveSalvage(consumer, prefix(ModifierIds.tank, upgradeSalvage))
                         .save(consumer, prefix(ModifierIds.tank, upgradeFolder));
    // expanders
    ModifierRecipeBuilder.modifier(ModifierIds.expanded)
                         .input(Items.PISTON)
                         .input(TinkerMaterials.amethystBronze.getIngotTag())
                         .input(Items.PISTON)
                         .input(SlimeType.ICHOR.getSlimeballTag())
                         .input(SlimeType.ICHOR.getSlimeballTag())
                         .setSlots(SlotType.ABILITY, 1)
                         .setTools(TinkerTags.Items.AOE)
                         .saveSalvage(consumer, prefix(ModifierIds.expanded, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.expanded, abilityFolder));
    // reach expander
    ModifierRecipeBuilder.modifier(ModifierIds.reach)
                         .setTools(TinkerTags.Items.CHESTPLATES)
                         .input(Items.PISTON)
                         .input(TinkerMaterials.queensSlime.getIngotTag())
                         .input(Items.PISTON)
                         .input(SlimeType.ENDER.getSlimeballTag())
                         .input(SlimeType.ENDER.getSlimeballTag())
                         .setSlots(SlotType.ABILITY, 1)
                         .setMaxLevel(2)
                         .saveSalvage(consumer, prefix(ModifierIds.reach, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.reach, abilityFolder));
    // block transformers
    Ingredient interactableWithDurability = IntersectionIngredient.of(Ingredient.of(TinkerTags.Items.DURABILITY), Ingredient.of(TinkerTags.Items.INTERACTABLE));
    Ingredient interactableBootsWithDurability = IntersectionIngredient.of(Ingredient.of(TinkerTags.Items.DURABILITY), ingredientFromTags(TinkerTags.Items.INTERACTABLE, TinkerTags.Items.BOOTS));
    SizedIngredient roundPlate = SizedIngredient.of(MaterialIngredient.of(TinkerToolParts.adzeHead.get()));
    SizedIngredient smallBlade = SizedIngredient.of(MaterialIngredient.of(TinkerToolParts.smallBlade.get()));
    SizedIngredient toolBinding = SizedIngredient.of(MaterialIngredient.of(TinkerToolParts.toolBinding.get()));
    ModifierRecipeBuilder.modifier(ModifierIds.pathing)
                         .setTools(interactableBootsWithDurability)
                         .addInput(roundPlate)
                         .input(TinkerMaterials.steel.getIngotTag())
                         .addInput(toolBinding)
                         .setMaxLevel(1).checkTraitLevel()
                         .setSlots(SlotType.ABILITY, 1)
                         .saveSalvage(consumer, prefix(ModifierIds.pathing, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.pathing, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.stripping)
                         .setTools(interactableWithDurability)
                         .addInput(SizedIngredient.of(MaterialIngredient.of(TinkerToolParts.smallAxeHead.get())))
                         .input(TinkerMaterials.steel.getIngotTag())
                         .addInput(toolBinding)
                         .setMaxLevel(1).checkTraitLevel()
                         .setSlots(SlotType.ABILITY, 1)
                         .saveSalvage(consumer, prefix(ModifierIds.stripping, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.stripping, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.tilling)
                         .setTools(interactableBootsWithDurability)
                         .addInput(smallBlade)
                         .input(TinkerMaterials.steel.getIngotTag())
                         .addInput(toolBinding)
                         .setMaxLevel(1).checkTraitLevel()
                         .setSlots(SlotType.ABILITY, 1)
                         .saveSalvage(consumer, prefix(ModifierIds.tilling, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.tilling, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.brushing)
      .setTools(IntersectionIngredient.of(Ingredient.of(TinkerTags.Items.DURABILITY), Ingredient.of(TinkerTags.Items.INTERACTABLE_RIGHT)))
      .input(Tags.Items.FEATHERS)
      .input(Tags.Items.INGOTS_COPPER)
      .setMaxLevel(1).checkTraitLevel()
      .setSlots(SlotType.ABILITY, 1)
      .saveSalvage(consumer, prefix(ModifierIds.brushing, abilitySalvage))
      .save(consumer, prefix(ModifierIds.brushing, abilityFolder));

    // glowing
    ModifierRecipeBuilder.modifier(ModifierIds.glowing)
                         .setTools(interactableBootsWithDurability)
                         .input(Items.GLOWSTONE)
                         .input(Items.DAYLIGHT_DETECTOR)
                         .input(Items.SHROOMLIGHT)
                         .setMaxLevel(1).checkTraitLevel()
                         .setSlots(SlotType.ABILITY, 1)
                         .saveSalvage(consumer, prefix(ModifierIds.glowing, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.glowing, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.firestarter)
                         .setTools(interactableWithDurability)
                         .input(TinkerMaterials.steel.getIngotTag())
                         .input(Items.FLINT)
                         .setMaxLevel(1).checkTraitLevel()
                         .setSlots(SlotType.ABILITY, 1)
                         .saveSalvage(consumer, prefix(ModifierIds.firestarter, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.firestarter, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.fireprimer)
                         .setTools(Ingredient.of(TinkerTools.flintAndBrick))
                         .input(TinkerMaterials.amethystBronze.getIngotTag())
                         .input(Items.FLINT)
                         .setMaxLevel(1)
                         .setSlots(SlotType.UPGRADE, 1)
                         .saveSalvage(consumer, prefix(ModifierIds.fireprimer, upgradeSalvage))
                         .save(consumer, prefix(ModifierIds.fireprimer, upgradeFolder));
    // slings
    Ingredient blockWhileCharging = IntersectionIngredient.of(Ingredient.of(TinkerTags.Items.DURABILITY), Ingredient.of(TinkerTags.Items.INTERACTABLE_CHARGE));
    ModifierRecipeBuilder.modifier(ModifierIds.flinging)
                         .setTools(blockWhileCharging)
                         .input(Blocks.VINE)
                         .input(TinkerWorld.earthGeode.asItem())
                         .input(Blocks.VINE)
                         .input(TinkerWorld.congealedSlime.get(SlimeType.EARTH))
                         .input(TinkerWorld.congealedSlime.get(SlimeType.EARTH))
                         .setMaxLevel(1).checkTraitLevel()
                         .setSlots(SlotType.ABILITY, 1)
                         .saveSalvage(consumer, prefix(ModifierIds.flinging, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.flinging, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.springing)
                         .setTools(blockWhileCharging)
                         .input(TinkerWorld.skySlimeVine)
                         .input(TinkerWorld.skyGeode.asItem())
                         .input(TinkerWorld.skySlimeVine)
                         .input(TinkerWorld.congealedSlime.get(SlimeType.SKY))
                         .input(TinkerWorld.congealedSlime.get(SlimeType.SKY))
                         .setMaxLevel(1).checkTraitLevel()
                         .setSlots(SlotType.ABILITY, 1)
                         .saveSalvage(consumer, prefix(ModifierIds.springing, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.springing, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.bonking)
                         .setTools(blockWhileCharging)
                         .input(Blocks.WEEPING_VINES)
                         .input(TinkerWorld.ichorGeode.asItem())
                         .input(Blocks.WEEPING_VINES)
                         .input(TinkerWorld.congealedSlime.get(SlimeType.ICHOR))
                         .input(TinkerWorld.congealedSlime.get(SlimeType.ICHOR))
                         .setMaxLevel(1).checkTraitLevel()
                         .setSlots(SlotType.ABILITY, 1)
                         .saveSalvage(consumer, prefix(ModifierIds.bonking, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.bonking, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.warping)
                         .setTools(blockWhileCharging)
                         .input(TinkerWorld.enderSlimeVine)
                         .input(TinkerWorld.enderGeode.asItem())
                         .input(TinkerWorld.enderSlimeVine)
                         .input(TinkerWorld.congealedSlime.get(SlimeType.ENDER))
                         .input(TinkerWorld.congealedSlime.get(SlimeType.ENDER))
                         .setMaxLevel(1).checkTraitLevel()
                         .setSlots(SlotType.ABILITY, 1)
                         .saveSalvage(consumer, prefix(ModifierIds.warping, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.warping, abilityFolder));

    // unbreakable
    ModifierRecipeBuilder.modifier(ModifierIds.unbreakable)
                         .setTools(TinkerTags.Items.DURABILITY)
                         .input(Items.SHULKER_SHELL)
                         .input(Items.DRAGON_BREATH)
                         .input(Items.SHULKER_SHELL)
                         .input(Tags.Items.INGOTS_NETHERITE)
                         .input(Tags.Items.INGOTS_NETHERITE)
                         .setMaxLevel(1).checkTraitLevel()
                         .setSlots(SlotType.ABILITY, 1)
                         .saveSalvage(consumer, prefix(ModifierIds.unbreakable, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.unbreakable, abilityFolder));
    // weapon
    ModifierRecipeBuilder.modifier(TinkerModifiers.dualWielding)
                         .input(TinkerMaterials.slimesteel.getIngotTag())
                         .input(Items.NAUTILUS_SHELL)
                         .input(TinkerMaterials.slimesteel.getIngotTag())
                         .setMaxLevel(1).checkTraitLevel()
                         .setSlots(SlotType.ABILITY, 1)
                         .setTools(DifferenceIngredient.of(IntersectionIngredient.of(Ingredient.of(TinkerTags.Items.MELEE_WEAPON), Ingredient.of(TinkerTags.Items.INTERACTABLE_RIGHT)), Ingredient.of(TinkerTools.dagger)))
                         .saveSalvage(consumer, prefix(TinkerModifiers.dualWielding, abilitySalvage))
                         .save(consumer, prefix(TinkerModifiers.dualWielding, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.blocking)
                         .setTools(DifferenceIngredient.of(
                           IntersectionIngredient.of(Ingredient.of(TinkerTags.Items.INTERACTABLE_CHARGE), Ingredient.of(TinkerTags.Items.DURABILITY)),
                           ingredientFromTags(TinkerTags.Items.PARRY, TinkerTags.Items.SHIELDS)))
                         .input(ItemTags.PLANKS)
                         .input(TinkerMaterials.steel.getIngotTag())
                         .input(ItemTags.PLANKS)
                         .input(ItemTags.PLANKS)
                         .input(ItemTags.PLANKS)
                         .setMaxLevel(1).checkTraitLevel()
                         .setSlots(SlotType.ABILITY, 1)
                         .saveSalvage(consumer, prefix(ModifierIds.blocking, abilitySalvage))
                         .save(consumer, prefix(ModifierIds.blocking, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.parrying)
                         .setTools(TinkerTags.Items.PARRY)
                         .input(ItemTags.PLANKS)
                         .input(TinkerMaterials.steel.getIngotTag())
                         .input(ItemTags.PLANKS)
                         .setMaxLevel(1).checkTraitLevel()
                         .setSlots(SlotType.ABILITY, 1)
                         .saveSalvage(consumer, prefix(TinkerModifiers.parrying, abilitySalvage))
                         .save(consumer, prefix(TinkerModifiers.parrying, abilityFolder));
    MultilevelModifierRecipeBuilder.modifier(ModifierIds.reflecting)
      .setTools(TinkerTags.Items.SHIELDS)
      .input(TinkerWorld.congealedSlime.get(SlimeType.SKY),   4)
      .input(TinkerWorld.congealedSlime.get(SlimeType.ICHOR), 4)
      .input(TinkerWorld.congealedSlime.get(SlimeType.SKY),   4)
      .input(TinkerWorld.congealedSlime.get(SlimeType.EARTH), 4)
      .input(TinkerWorld.congealedSlime.get(SlimeType.EARTH), 4)
      .addLevel(SlotType.ABILITY, 1, 1)
      .addLevelRange(SlotType.UPGRADE, 1, 2, 3)
      .saveSalvage(consumer, prefix(ModifierIds.reflecting, abilitySalvage))
      .save(consumer, prefix(ModifierIds.reflecting, abilityFolder));

    /*
     * extra modifiers
     */
    ModifierRecipeBuilder.modifier(ModifierIds.writable)
                         .setTools(TinkerTags.Items.BONUS_SLOTS)
                         .input(Items.WRITABLE_BOOK)
                         .setMaxLevel(1)
                         .save(consumer, prefix(ModifierIds.writable, slotlessFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.harmonious)
                         .setTools(TinkerTags.Items.BONUS_SLOTS)
                         .addInput(ItemTags.MUSIC_DISCS)
                         .setMaxLevel(1)
                         .save(consumer, prefix(ModifierIds.harmonious, slotlessFolder));
    Ingredient bonusNoSkull = DifferenceIngredient.of(Ingredient.of(TinkerTags.Items.BONUS_SLOTS), Ingredient.of(TinkerTags.Items.SKULLS));
    SizedIngredient standardSkulls = SizedIngredient.of(DifferenceIngredient.of(Ingredient.of(Tags.Items.HEADS), Ingredient.of(Items.DRAGON_HEAD)));
    ModifierRecipeBuilder.modifier(ModifierIds.recapitated)
      .setTools(bonusNoSkull)
      .addInput(standardSkulls)
      .setMaxLevel(1)
      .save(consumer, prefix(ModifierIds.recapitated, slotlessFolder));
    Ingredient bonusSkulls = IntersectionIngredient.of(Ingredient.of(TinkerTags.Items.BONUS_SLOTS), Ingredient.of(TinkerTags.Items.SKULLS));
    ModifierRecipeBuilder.modifier(ModifierIds.recapitated)
      .setTools(bonusSkulls)
      .addInput(standardSkulls).input(Tags.Items.SLIMEBALLS)
      .setMaxLevel(1)
      .save(consumer, wrap(ModifierIds.recapitated, slotlessFolder, "_for_skull"));
    ModifierRecipeBuilder.modifier(ModifierIds.forecast)
                         .setTools(TinkerTags.Items.BONUS_SLOTS)
                         .input(ingredientFromTags(Tags.Items.ORES_DIAMOND, Tags.Items.ORES_EMERALD, TinkerTags.Items.ORES_COBALT))
                         .setMaxLevel(1)
                         .save(consumer, prefix(ModifierIds.forecast, slotlessFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.embossed)
      .setTools(TinkerTags.Items.BONUS_SLOTS)
      .input(TinkerTags.Items.BOSS_TROPHIES)
      .setMaxLevel(1)
      .save(withCondition(consumer, new TagFilledCondition<>(TinkerTags.Items.BOSS_TROPHIES)), prefix(ModifierIds.embossed, slotlessFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.draconic)
      .setTools(bonusNoSkull)
      .input(Items.DRAGON_HEAD)
      .setMaxLevel(1)
      .save(consumer, wrap(ModifierIds.draconic, slotlessFolder, "_from_head"));
    ModifierRecipeBuilder.modifier(ModifierIds.draconic)
      .setTools(bonusSkulls)
      .input(Items.DRAGON_HEAD).input(Tags.Items.SLIMEBALLS)
      .setMaxLevel(1)
      .save(consumer, wrap(ModifierIds.draconic, slotlessFolder, "_for_skull"));
    ModifierRecipeBuilder.modifier(ModifierIds.draconic)
                         .setTools(TinkerTags.Items.BONUS_SLOTS)
                         .input(Items.DRAGON_BREATH)
                         .input(TinkerModifiers.dragonScale)
                         .input(Items.DRAGON_BREATH)
                         .input(TinkerModifiers.dragonScale)
                         .input(TinkerModifiers.dragonScale)
                         .setMaxLevel(1)
                         .disallowCrystal()
                         .save(consumer, wrap(ModifierIds.draconic, slotlessFolder, "_from_scales"));
    // rebalanced
    SwappableModifierRecipeBuilder.modifier(ModifierIds.rebalanced, SlotType.UPGRADE.getName())
      .setTools(TinkerTags.Items.BONUS_SLOTS)
      .input(TinkerMaterials.roseGold.getNuggetTag())
      .input(Items.END_CRYSTAL)
      .input(TinkerMaterials.roseGold.getNuggetTag())
      .input(TinkerWorld.skyGeode.getBlock())
      .input(TinkerWorld.skyGeode.getBlock())
      .disallowCrystal()
      .save(consumer, wrap(ModifierIds.rebalanced, slotlessFolder, "_" + SlotType.UPGRADE.getName()));
    SwappableModifierRecipeBuilder.modifier(ModifierIds.rebalanced, SlotType.DEFENSE.getName())
      .setTools(IntersectionIngredient.of(ingredientFromTags(TinkerTags.Items.ARMOR, TinkerTags.Items.HELD), Ingredient.of(TinkerTags.Items.BONUS_SLOTS)))
      .input(TinkerMaterials.cobalt.getNuggetTag())
      .input(Items.END_CRYSTAL)
      .input(TinkerMaterials.cobalt.getNuggetTag())
      .input(TinkerWorld.earthGeode.getBlock())
      .input(TinkerWorld.earthGeode.getBlock())
      .disallowCrystal()
      .save(consumer, wrap(ModifierIds.rebalanced, slotlessFolder, "_" + SlotType.DEFENSE.getName()));
    SwappableModifierRecipeBuilder.modifier(ModifierIds.rebalanced, SlotType.ABILITY.getName())
      .setTools(TinkerTags.Items.BONUS_SLOTS)
      .input(TinkerMaterials.queensSlime.getNuggetTag())
      .input(Items.END_CRYSTAL)
      .input(TinkerMaterials.queensSlime.getNuggetTag())
      .input(TinkerWorld.ichorGeode.getBlock())
      .input(TinkerWorld.ichorGeode.getBlock())
      .disallowCrystal()
      .save(consumer, wrap(ModifierIds.rebalanced, slotlessFolder, "_" + SlotType.ABILITY.getName()));
    SwappableModifierRecipeBuilder.modifier(ModifierIds.rebalanced, "traits")
      .setTools(ToolHookIngredient.of(TinkerTags.Items.BONUS_SLOTS, ToolHooks.REBALANCED_TRAIT))
      .input(TinkerMaterials.manyullyn.getNuggetTag())
      .input(Items.END_CRYSTAL)
      .input(TinkerMaterials.manyullyn.getNuggetTag())
      .input(TinkerWorld.enderGeode.getBlock())
      .input(TinkerWorld.enderGeode.getBlock())
      .disallowCrystal()
      .save(consumer, wrap(ModifierIds.rebalanced, slotlessFolder, "_traits"));
    ModifierRecipeBuilder.modifier(ModifierIds.redirected)
      .setTools(ToolHookIngredient.of(TinkerTags.Items.AMMO, ToolHooks.REBALANCED_TRAIT))
      .input(Items.DRAGON_BREATH)
      .save(consumer, prefix(ModifierIds.redirected, slotlessFolder));

    // tipping arrows and shurikens
    PotionCastingRecipeBuilder.tableTipping(ModifierIds.tipped)
      .setBottle(TinkerTags.Items.AMMO)
      .setCoolingTime(20)
      .setFluid(MantleFluids.potion.ingredient(FluidValues.BOTTLE / 5))
      .save(consumer, location(slotlessFolder + "ammo_tipping"));
    PotionCastingRecipeBuilder.tableClearing(ModifierIds.tipped)
      .setBottle(TinkerTags.Items.AMMO)
      .setCoolingTime(20)
      .setFluid(MantleTags.Fluids.WATER, FluidValues.BOTTLE / 5)
      .save(consumer, location(slotlessFolder + "ammo_tip_clearing"));
    PotionCastingRecipeBuilder.tableTipping(ModifierIds.tipped)
      .setBottle(TinkerTags.Items.FISHING_RODS)
      .setCoolingTime(20)
      .setFluid(MantleFluids.potion.ingredient(FluidValues.BOTTLE))
      .save(consumer, location(slotlessFolder + "fishing_rod_tipping"));
    PotionCastingRecipeBuilder.tableClearing(ModifierIds.tipped)
      .setBottle(TinkerTags.Items.FISHING_RODS)
      .setCoolingTime(20)
      .setFluid(MantleTags.Fluids.WATER, FluidValues.BOTTLE)
      .save(consumer, location(slotlessFolder + "fishing_rod_tip_clearing"));

    // removal
    IJsonPredicate<ModifierId> removable = ModifierPredicate.tag(TinkerTags.Modifiers.REMOVE_MODIFIER_BLACKLIST).inverted();
    ModifierRemovalRecipeBuilder.removal()
      .input(Blocks.WET_SPONGE)
      .addLeftover(Blocks.SPONGE)
      .modifierPredicate(removable)
      .save(consumer, location(worktableFolder + "remove_modifier_sponge"));
    ModifierRemovalRecipeBuilder.removal()
      .input(CompoundIngredient.of(
        FluidContainerIngredient.fromFluid(TinkerFluids.venom),
        FluidContainerIngredient.fromIngredient(TinkerFluids.venom.ingredient(FluidValues.BOTTLE), Ingredient.of(TinkerFluids.venomBottle)))
      )
      .modifierPredicate(removable)
      .save(consumer, location(worktableFolder + "remove_modifier_venom"));
    // modifier extracting: sponge + crystal
    IJsonPredicate<ModifierId> extractBlacklist = ModifierPredicate.tag(TinkerTags.Modifiers.EXTRACT_MODIFIER_BLACKLIST).inverted();
    for (boolean dagger : new boolean[]{false, true}) {
      String suffix = dagger ? "_dagger" : "";
      SizedIngredient tools = dagger ? SizedIngredient.fromItems(2, TinkerTools.dagger) : SizedIngredient.of(DifferenceIngredient.of(Ingredient.of(TinkerTags.Items.MODIFIABLE), Ingredient.of(TinkerTags.Items.UNSALVAGABLE)));
      ModifierRemovalRecipeBuilder.extract()
                                  .setTools(tools)
                                  .setName("slotless")
                                  .input(Items.AMETHYST_SHARD)
                                  .input(Items.WET_SPONGE)
                                  .addLeftover(Items.SPONGE)
                                  .modifierPredicate(ModifierPredicate.and(extractBlacklist, new SlotTypeModifierPredicate(null), ModifierPredicate.tag(TinkerTags.Modifiers.EXTRACT_SLOTLESS_BLACKLIST).inverted()))
                                  .save(consumer, location(worktableFolder + "extract/slotless" + suffix));
      ModifierRemovalRecipeBuilder.extract()
                                  .setTools(tools)
                                  .slotName(SlotType.UPGRADE)
                                  .input(TinkerWorld.skyGeode)
                                  .input(Items.WET_SPONGE)
                                  .addLeftover(Items.SPONGE)
                                  .modifierPredicate(ModifierPredicate.and(extractBlacklist, new SlotTypeModifierPredicate(SlotType.UPGRADE)))
                                  .save(consumer, location(worktableFolder + "extract/upgrade" + suffix));
      ModifierRemovalRecipeBuilder.extract()
                                  .setTools(tools)
                                  .slotName(SlotType.DEFENSE)
                                  .input(TinkerWorld.earthGeode)
                                  .input(Items.WET_SPONGE)
                                  .addLeftover(Items.SPONGE)
                                  .modifierPredicate(ModifierPredicate.and(extractBlacklist, new SlotTypeModifierPredicate(SlotType.DEFENSE)))
                                  .save(consumer, location(worktableFolder + "extract/defense" + suffix));
      ModifierRemovalRecipeBuilder.extract()
                                  .setTools(tools)
                                  .slotName(SlotType.ABILITY)
                                  .input(TinkerWorld.ichorGeode)
                                  .input(Items.WET_SPONGE)
                                  .addLeftover(Items.SPONGE)
                                  .modifierPredicate(ModifierPredicate.and(extractBlacklist, new SlotTypeModifierPredicate(SlotType.ABILITY)))
                                  .save(consumer, location(worktableFolder + "extract/ability" + suffix));
      ModifierRemovalRecipeBuilder.extract()
                                  .setTools(tools)
                                  .input(TinkerWorld.enderGeode)
                                  .input(Items.WET_SPONGE)
                                  .addLeftover(Items.SPONGE)
                                  .modifierPredicate(extractBlacklist)
                                  .save(consumer, location(worktableFolder + "extract/modifier" + suffix));

    }
    ModifierSortingRecipeBuilder.sorting()
      .input(Items.COMPASS)
      .save(consumer, location(worktableFolder + "modifier_sorting"));

    // invisible ink
    ResourceLocation hiddenModifiers = TConstruct.getResource("invisible_modifiers");
    IJsonPredicate<ModifierId> blacklist = ModifierPredicate.tag(TinkerTags.Modifiers.INVISIBLE_INK_BLACKLIST).inverted();
    ModifierSetWorktableRecipeBuilder.setAdding(hiddenModifiers)
                                     .modifierPredicate(blacklist)
                                     .addInput(FluidContainerIngredient.fromIngredient(TinkerFluids.skySlime.ingredient(FluidValues.BOTTLE), Ingredient.of(TinkerFluids.slimeBottle.get(SlimeType.SKY))))
                                     .save(consumer, location(worktableFolder + "invisible_ink_adding"));
    ModifierSetWorktableRecipeBuilder.setRemoving(hiddenModifiers)
                                     .modifierPredicate(blacklist)
                                     .addInput(FluidContainerIngredient.fromIngredient(FluidIngredient.of(Fluids.MILK, FluidType.BUCKET_VOLUME), Ingredient.of(Items.MILK_BUCKET)))
                                     .save(consumer, location(worktableFolder + "invisible_ink_removing"));

    // swapping hands
    ToggleInteractionWorktableRecipeBuilder.builder()
      .tools(Ingredient.of(TinkerTags.Items.INTERACTABLE_DUAL))
      .input(Items.LEVER)
      .save(consumer, location(worktableFolder + "toggle_interaction_modifier"));

    // conversion
    for (boolean matchBook : new boolean[]{false, true}) {
      String suffix = matchBook ? "_book" : "_tool";
      EnchantmentConvertingRecipeBuilder.converting("slotless", matchBook)
                                        .input(Items.AMETHYST_SHARD)
                                        .modifierPredicate(ModifierPredicate.and(new SlotTypeModifierPredicate(null),
                                                                                  ModifierPredicate.tag(TinkerTags.Modifiers.EXTRACT_SLOTLESS_BLACKLIST).inverted()))
                                        .save(consumer, location(worktableFolder + "enchantment_converting/slotless" + suffix));
      EnchantmentConvertingRecipeBuilder.converting("upgrades", matchBook)
                                        .input(TinkerWorld.skyGeode.asItem())
                                        .input(Tags.Items.GEMS_LAPIS, 3)
                                        .modifierPredicate(ModifierPredicate.and(new SlotTypeModifierPredicate(SlotType.UPGRADE),
                                          ModifierPredicate.tag(TinkerTags.Modifiers.EXTRACT_UPGRADE_BLACKLIST).inverted()))
                                        .save(consumer, location(worktableFolder + "enchantment_converting/upgrade" + suffix));
      EnchantmentConvertingRecipeBuilder.converting("defense", matchBook)
                                        .input(TinkerWorld.earthGeode.asItem())
                                        .input(Tags.Items.INGOTS_GOLD, 1)
                                        .modifierPredicate(new SlotTypeModifierPredicate(SlotType.DEFENSE))
                                        .save(consumer, location(worktableFolder + "enchantment_converting/defense" + suffix));
      EnchantmentConvertingRecipeBuilder.converting("abilities", matchBook)
                                        .input(TinkerWorld.ichorGeode.asItem())
                                        .input(Tags.Items.GEMS_DIAMOND)
                                        .modifierPredicate(new SlotTypeModifierPredicate(SlotType.ABILITY))
                                        .save(consumer, location(worktableFolder + "enchantment_converting/ability" + suffix));
      EnchantmentConvertingRecipeBuilder.converting("modifiers", matchBook)
                                        .input(TinkerWorld.enderGeode)
                                        .input(Items.DRAGON_BREATH, 5)
                                        .returnInput()
                                        .save(consumer, location(worktableFolder + "enchantment_converting/unenchant" + suffix));
    }

    // compatability
    String theOneProbe = "theoneprobe";
    ResourceLocation probe = new ResourceLocation(theOneProbe, "probe");
    Consumer<FinishedRecipe> topConsumer = withCondition(consumer, modLoaded(theOneProbe));
    ModifierRecipeBuilder.modifier(ModifierIds.theOneProbe)
                         .setTools(ingredientFromTags(TinkerTags.Items.HELMETS, TinkerTags.Items.HELD))
                         .addInput(ItemNameIngredient.from(probe))
                         .setSlots(SlotType.UPGRADE, 1)
                         .setMaxLevel(1).checkTraitLevel()
                         .saveSalvage(topConsumer, prefix(ModifierIds.theOneProbe, compatSalvage))
                         .save(topConsumer, prefix(ModifierIds.theOneProbe, compatFolder));
    Consumer<FinishedRecipe> headlightConsumer = withCondition(consumer, modLoaded("headlight"));
    BiConsumer<Ingredient,String> headlight = (ingredient, light) -> {
      SwappableModifierRecipeBuilder builder = SwappableModifierRecipeBuilder.modifier(ModifierIds.headlight, light);
      builder.variantFormatter(VariantFormatter.PARAMETER)
             .setTools(TinkerTags.Items.HELMETS)
             .input(Items.LEATHER)
             .input(ingredient)
             .input(Items.LEATHER)
             .setSlots(SlotType.UPGRADE, 1)
             .disallowCrystal();
      if ("10".equals(light)) {
        builder.saveSalvage(headlightConsumer, prefix(ModifierIds.headlight, compatSalvage));
      } else {
        builder.disallowCrystal();
      }
      builder.save(headlightConsumer, wrap(ModifierIds.headlight, compatFolder, "_" + light));
    };
    headlight.accept(Ingredient.of(Blocks.LANTERN), "15");
    headlight.accept(Ingredient.of(Blocks.SOUL_LANTERN), "10");
    headlight.accept(Ingredient.of(ItemTags.CANDLES), "5");
  }

  private void addTextureRecipes(Consumer<FinishedRecipe> consumer) {
    String folder = "tools/modifiers/slotless/";

    // slime staff
    woodTexture(consumer, MaterialIds.wood, Items.STICK, folder);
    // nether
    woodTexture(consumer, MaterialIds.crimson, Blocks.CRIMSON_PLANKS, folder);
    woodTexture(consumer, MaterialIds.warped, Blocks.WARPED_PLANKS, folder);
    // slimewood
    woodTexture(consumer, MaterialIds.greenheart, TinkerWorld.greenheart, folder);
    woodTexture(consumer, MaterialIds.skyroot, TinkerWorld.skyroot, folder);
    woodTexture(consumer, MaterialIds.bloodshroom, TinkerWorld.bloodshroom, folder);
    woodTexture(consumer, MaterialIds.enderbark, TinkerWorld.enderbark, folder);
    // special
    woodTexture(consumer, MaterialIds.blazewood, TinkerMaterials.blazewood, folder);
    woodTexture(consumer, MaterialIds.nahuatl, TinkerMaterials.nahuatl, folder);
    woodTexture(consumer, MaterialIds.bamboo, Blocks.BAMBOO, folder);
    woodTexture(consumer, MaterialIds.cactus, Blocks.CACTUS, folder);
    // compat
    TagKey<Item> treatedWood = getItemTag(COMMON, "treated_wood");
    SwappableModifierRecipeBuilder.modifier(TinkerModifiers.embellishment, MaterialIds.treatedWood.toString())
      .variantFormatter(VariantFormatter.MATERIAL)
      .setTools(TinkerTags.Items.EMBELLISHMENT_WOOD)
      .input(treatedWood).input(TinkerTables.pattern).input(treatedWood)
      .save(withCondition(consumer, new TagFilledCondition<>(treatedWood)), wrap(TinkerModifiers.embellishment, folder, "/wood/treated"));
    TagKey<Item> ironwood = getItemTag(COMMON, "ingots/ironwood");
    SwappableModifierRecipeBuilder.modifier(TinkerModifiers.embellishment, MaterialIds.ironwood.toString())
      .variantFormatter(VariantFormatter.MATERIAL)
      .setTools(TinkerTags.Items.EMBELLISHMENT_WOOD)
      .input(ironwood).input(TinkerTables.pattern).input(ironwood)
      .save(withCondition(consumer, new TagFilledCondition<>(ironwood)), wrap(TinkerModifiers.embellishment, folder, "/wood/ironwood"));

    // cosmetics //
    consumer.accept(new SimpleFinishedRecipe(location(folder + "dyeing"), TinkerModifiers.armorDyeingSerializer.get()));
    consumer.accept(new SimpleFinishedRecipe(location(folder + "trim"), TinkerModifiers.armorTrimSerializer.get()));
    LoadableFinishedRecipe.save(consumer, BannerModifierRecipe.LOADER, new BannerModifierRecipe(location(folder + "banner"), Ingredient.of(TinkerFluids.slimeBottle.get(SlimeType.SKY))));
  }

  private void addHeadRecipes(Consumer<FinishedRecipe> consumer) {
    String folder = "tools/severing/";
    // first, beheading
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.ZOMBIE), Items.ZOMBIE_HEAD)
												 .save(consumer, location(folder + "zombie_head"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.SKELETON), Items.SKELETON_SKULL)
												 .save(consumer, location(folder + "skeleton_skull"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.WITHER_SKELETON), Items.WITHER_SKELETON_SKULL)
      .save(consumer, location(folder + "wither_skeleton_skull"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.WITHER), Items.WITHER_SKELETON_SKULL).rareMob()
      .save(consumer, location(folder + "wither_salvage"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.CREEPER), Items.CREEPER_HEAD)
												 .save(consumer, location(folder + "creeper_head"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.PIGLIN), Items.PIGLIN_HEAD)
                         .save(consumer, location(folder + "piglin_head"));
    SpecialSeveringRecipeBuilder.serializer(PlayerBeheadingRecipe::new).rareMob().save(consumer, location(folder + "player_head"));
    SpecialSeveringRecipeBuilder.serializer(SnowGolemBeheadingRecipe::new).save(consumer, location(folder + "snow_golem_head"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.IRON_GOLEM), Blocks.CARVED_PUMPKIN)
                         .save(consumer, location(folder + "iron_golem_head"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.ENDER_DRAGON), Items.DRAGON_HEAD).rareMob()
                         .save(consumer, location(folder + "ender_dragon_head"));
    TinkerWorld.headItems.forEach((type, head) ->
      SeveringRecipeBuilder.severing(EntityIngredient.of(type.getType()), head)
        .save(consumer, location(folder + type.getSerializedName() + "_head")));

    // other body parts
    // hostile
    // beeyeing
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.SPIDER, EntityType.CAVE_SPIDER), Items.SPIDER_EYE)
                         .save(consumer, location(folder + "spider_eye"));
    // besilking
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.SPIDER, EntityType.CAVE_SPIDER), Items.COBWEB)
                         .save(consumer, location(folder + "cobweb"));
    // be-internal-combustion-device
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.CREEPER), Blocks.TNT)
                         .save(consumer, location(folder + "creeper_tnt"));
    // bemembraning?
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.PHANTOM), Items.PHANTOM_MEMBRANE)
                         .save(consumer, location(folder + "phantom_membrane"));
    // beshelling
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.SHULKER), Items.SHULKER_SHELL)
                         .save(consumer, location(folder + "shulker_shell"));
    // deboning
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.SKELETON, EntityType.SKELETON_HORSE, EntityType.STRAY), ItemOutput.fromItem(Items.BONE, 2))
                         .save(consumer, location(folder + "skeleton_bone"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.WITHER_SKELETON), ItemOutput.fromItem(TinkerMaterials.necroticBone, 2))
                         .save(consumer, location(folder + "wither_skeleton_bone"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.BLAZE), ItemOutput.fromItem(Items.BLAZE_ROD, 2))
                         .save(consumer, location(folder + "blaze_rod"));
    // desliming (you cut off a chunk of slime)
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.SLIME), Items.SLIME_BALL)
                         .save(consumer, location(folder + "earthslime_ball"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(TinkerWorld.skySlimeEntity.get()), TinkerCommons.slimeball.get(SlimeType.SKY))
                         .save(consumer, location(folder + "skyslime_ball"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(TinkerWorld.enderSlimeEntity.get()), TinkerCommons.slimeball.get(SlimeType.ENDER))
                         .save(consumer, location(folder + "enderslime_ball"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(TinkerWorld.terracubeEntity.get()), Items.CLAY_BALL)
                         .save(consumer, location(folder + "terracube_clay"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.MAGMA_CUBE), Items.MAGMA_CREAM)
                         .save(consumer, location(folder + "magma_cream"));
    // descaling? I don't know what to call those
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN), ItemOutput.fromItem(Items.PRISMARINE_SHARD, 2))
                         .save(consumer, location(folder + "guardian_shard"));

    // passive
    // befeating
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.RABBIT), Items.RABBIT_FOOT)
                         .noChildOutput()
												 .save(consumer, location(folder + "rabbit_foot"));
    // befeathering
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.CHICKEN), ItemOutput.fromItem(Items.FEATHER, 2))
                         .noChildOutput()
                         .save(consumer, location(folder + "chicken_feather"));
    // beshrooming
    SpecialSeveringRecipeBuilder.serializer(MooshroomDemushroomingRecipe::new).save(consumer, location(folder + "mooshroom_shroom"));
    // beshelling
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.TURTLE), Items.TURTLE_HELMET)
                         .setChildOutput(ItemOutput.fromItem(Items.SCUTE))
                         .save(consumer, location(folder + "turtle_shell"));
    // befleecing
    SpecialSeveringRecipeBuilder.serializer(SheepShearingRecipe::new).save(consumer, location(folder + "sheep_wool"));
  }

  /** Adds recipes for a plate armor texture with a custom tag */
  private void woodTexture(Consumer<FinishedRecipe> consumer, MaterialVariantId material, ItemLike planks, String folder) {
    SwappableModifierRecipeBuilder.modifier(TinkerModifiers.embellishment, material.toString())
                                  .variantFormatter(VariantFormatter.MATERIAL)
                                  .setTools(TinkerTags.Items.EMBELLISHMENT_WOOD)
                                  .input(planks).input(TinkerTables.pattern).input(planks)
                                  .save(consumer, wrap(TinkerModifiers.embellishment, folder, "/wood/" + material.getLocation('_').getPath()));
  }

  /** Adds haste like recipes using redstone */
  public void hasteRecipes(Consumer<FinishedRecipe> consumer, ModifierId modifier, Ingredient tools, int maxLevel, @Nullable String recipeFolder, @Nullable String salvageFolder) {
    IncrementalModifierRecipeBuilder builder = IncrementalModifierRecipeBuilder
      .modifier(modifier)
      .setTools(tools)
      .input(Tags.Items.DUSTS_REDSTONE, 1, 45)
      .setMaxLevel(maxLevel)
      .setSlots(SlotType.UPGRADE, 1);
    if (salvageFolder != null) {
      builder.saveSalvage(consumer, prefix(modifier, salvageFolder));
    }
    if (recipeFolder != null) {
      builder.save(consumer, wrap(modifier, recipeFolder, "_from_dust"));
      IncrementalModifierRecipeBuilder.modifier(modifier)
                                      .setTools(tools)
                                      .input(Tags.Items.STORAGE_BLOCKS_REDSTONE, 9, 45)
                                      .leftover(new ItemStack(Items.REDSTONE))
                                      .setMaxLevel(maxLevel)
                                      .setSlots(SlotType.UPGRADE, 1)
                                      .disallowCrystal() // avoid redundancy, though in this case the end result is the same
                                      .save(consumer, wrap(modifier, recipeFolder, "_from_block"));
    }
  }

  /** Prefixes the modifier ID with the given prefix */
  public ResourceLocation prefix(LazyModifier modifier, String prefix) {
    return prefix(modifier.id(), prefix);
  }

  /** Prefixes the modifier ID with the given prefix and suffix */
  public ResourceLocation wrap(LazyModifier modifier, String prefix, String suffix) {
    return wrap(modifier.id(), prefix, suffix);
  }

  /**
   * Creates a compound ingredient from multiple tags
   * @param tags  Tags to use
   * @return  Compound ingredient
   */
  @SafeVarargs
  private static Ingredient ingredientFromTags(TagKey<Item>... tags) {
    Ingredient[] tagIngredients = new Ingredient[tags.length];
    for (int i = 0; i < tags.length; i++) {
      tagIngredients[i] = Ingredient.of(tags[i]);
    }
    return CompoundIngredient.of(tagIngredients);
  }
}
