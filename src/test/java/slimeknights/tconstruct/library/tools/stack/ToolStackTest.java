package slimeknights.tconstruct.library.tools.stack;

import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.fixture.MaterialFixture;
import slimeknights.tconstruct.fixture.ToolDefinitionFixture;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.modifiers.ModifierFixture;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.component.ModifierEntries;
import slimeknights.tconstruct.library.tools.component.ModifierSlots;
import slimeknights.tconstruct.library.tools.component.StatValues;
import slimeknights.tconstruct.library.tools.component.ToolMaterials;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.item.ToolItemTest;
import slimeknights.tconstruct.library.tools.nbt.IModDataView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import static org.assertj.core.api.Assertions.assertThat;

class ToolStackTest extends ToolItemTest {
  private final StatValues testStatValues = StatValues.builder()
    .set(ToolStats.DURABILITY, 100f)
    .set(ToolStats.HARVEST_TIER, Tiers.NETHERITE)
    .set(ToolStats.ATTACK_DAMAGE, 2f)
    .set(ToolStats.MINING_SPEED, 3f)
    .set(ToolStats.ATTACK_SPEED, 5f)
    .build();

  @BeforeAll
  static void before() {
    ModifierFixture.init();
  }

  /* From */

  @Test
  void from_preservesItem() {
    ToolStack stack = ToolStack.create(Items.DIAMOND_PICKAXE, ToolDefinition.EMPTY);
    assertThat(stack.getItem()).isEqualTo(stack.getItem());
  }

  @Test
  void from_findToolCoreDefinition() {
    ItemStack stack = new ItemStack(tool);
    ToolStack toolStack = ToolStack.from(stack);
    assertThat(toolStack.getDefinition()).isEqualTo(tool.getToolDefinition());
  }

  @Test
  void from_shareNBT() {
    ItemStack stack = buildTestTool();
    ToolStack tool = ToolStack.from(stack);
    tool.setDamage(10);
    assertThat(stack.getDamageValue()).overridingErrorMessage("ToolStack damage was not transferred to the original stack").isEqualTo(10);
  }

  @Test
  void copyFrom_notSharedNBT() {
    ItemStack stack = buildTestTool();
    ToolStack tool = ToolStack.copyFrom(stack);
    tool.setDamage(10);
    assertThat(stack.getDamageValue()).overridingErrorMessage("Copied ToolStack damage was transferred to the original stack").isEqualTo(0);
  }

  @Test
  void copy_notSharedNBT() {
    ToolStack tool = ToolStack.from(buildTestTool());
    ToolStack copy = tool.copy();
    tool.setDamage(10);
    assertThat(copy.getDamage()).overridingErrorMessage("Copied ToolStack damage was transferred to the original stack").isEqualTo(0);
  }

  @Test
  void deserialize_empty() {
    ToolStack tool = ToolStack.create(Items.DIAMOND_PICKAXE, ToolDefinition.EMPTY);
    assertThat(tool.getItem()).isNotNull();
    assertThat(tool.getDefinition()).isNotNull();
    assertThat(tool.getDamage()).isEqualTo(0);
    assertThat(tool.isBroken()).isFalse();
    assertThat(tool.getMaterials()).isEqualTo(ToolMaterials.EMPTY);
    assertThat(tool.getCraftedModifiers()).isEqualTo(ModifierEntries.EMPTY);
    assertThat(tool.getPersistentData()).isEqualTo(new ModDataNBT());
    assertThat(tool.getModifiers()).isEqualTo(ModifierEntries.EMPTY);
    assertThat(tool.getStats()).isEqualTo(StatValues.EMPTY);
    assertThat(tool.getVolatileData()).isEqualTo(IModDataView.EMPTY);
  }


  /* Creating and update stacks */

  @Test
  void createStack_sharesNBT() {
    ToolStack tool = ToolStack.create(Items.DIAMOND_PICKAXE, ToolDefinition.EMPTY);
    tool.setBrokenRaw(true);
    ItemStack stack = tool.getStack();
    assertThat(tool.isSameStack(stack)).isTrue();
  }


  /* Damage and broken */

  @Test
  void serialize_damageBroken() {
    ToolStack tool = ToolStack.create(Items.DIAMOND_PICKAXE, ToolDefinitionFixture.getStandardToolDefinition());
    tool.setStats(StatValues.builder().set(ToolStats.DURABILITY, 100f).build());
    tool.setDamage(1);
    tool.setBrokenRaw(true);

    ItemStack stack = tool.getStack();
    assertThat(stack.has(TinkerData.BROKEN)).isTrue();
    assertThat(stack.has(DataComponents.DAMAGE)).isTrue();
    assertThat(stack.get(DataComponents.DAMAGE)).isEqualTo(1);
  }

  @Test
  void deserialize_damageBroken() {
    ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
    stack.set(DataComponents.DAMAGE, 4);
    stack.set(TinkerData.BROKEN, Unit.INSTANCE);

    ToolStack tool = ToolStack.from(stack);
    assertThat(tool.getDamageRaw()).isEqualTo(4);
    assertThat(tool.isBroken()).isTrue();
  }

  @Test
  void damage_getDamageValidates() {
    ItemStack stack = buildTestTool();
    stack.set(DataComponents.DAMAGE, 9999);

    ToolStack tool = ToolStack.from(stack);
    assertThat(tool.getDamage()).isLessThanOrEqualTo(tool.getStats().getInt(ToolStats.DURABILITY));
  }

  @Test
  void damage_setDamageBreaksTool() {
    ToolStack tool = ToolStack.from(buildTestTool());
    assertThat(tool.isBroken()).isFalse();
    tool.setDamage(99999);
    assertThat(tool.isBroken()).isTrue();
  }

  @Test
  void damage_setDamageUnbreaksTool() {
    ItemStack stack = buildTestTool();
    stack.set(TinkerData.BROKEN, Unit.INSTANCE);

    ToolStack tool = ToolStack.from(stack);
    assertThat(tool.isBroken()).isTrue();
    tool.setDamage(10);
    assertThat(tool.isBroken()).isFalse();
  }

  @Test
  void damage_damageTool() {
    ToolStack tool = ToolStack.from(buildTestTool());
    int oldDamage = tool.getDamage();
    ToolDamageUtil.directDamage(tool, 100, null, ItemStack.EMPTY);
    assertThat(tool.getDamage()).isEqualTo(oldDamage + 100);
  }

  @Test
  void damage_repairTool() {
    ToolStack tool = ToolStack.from(buildTestTool());
    tool.setDamage(50);
    int oldDamage = tool.getDamage();
    ToolDamageUtil.repair(tool, 25);
    assertThat(tool.getDamage()).isEqualTo(oldDamage - 25);
  }

  @Test
  void broken_quickCheck() {
    ToolStack tool = ToolStack.from(buildTestTool());
    tool.breakTool();
    ItemStack stack = tool.getStack();
    assertThat(ToolDamageUtil.isBroken(stack)).isTrue();
  }


  /* Stats */

  @Test
  void stats_serialize() {
    ToolStack tool = ToolStack.create(Items.DIAMOND_PICKAXE, ToolDefinition.EMPTY);
    tool.setStats(testStatValues);
    ItemStack stack = tool.getStack();

    assertThat(stack.has(TinkerData.TOOL_STATS)).isTrue();
    // assumes stats NBT properly deserializes
    StatValues stackStats = stack.get(TinkerData.TOOL_STATS);
    assertThat(stackStats).isEqualTo(testStatValues);
  }

  @Test
  void stats_deserialize() {
    ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
    stack.set(TinkerData.TOOL_STATS, testStatValues);

    ToolStack tool = ToolStack.from(stack);
    StatValues readStats = tool.getStats();
    assertThat(readStats).isNotEqualTo(StatValues.EMPTY);
    assertThat(readStats).isEqualTo(testStatValues);
  }

  @Test
  void stats_lowDurabilityUpdatesDurability() {
    ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
    stack.setDamageValue(100);

    ToolStack tool = ToolStack.from(stack);
    tool.setStats(StatValues.builder().set(ToolStats.DURABILITY, 50f).build());
    assertThat(tool.getDamageRaw()).isEqualTo(50);
    assertThat(tool.isBroken()).isTrue();
  }


  /* Materials */

  @Test
  void materials_serialize() {
    ToolStack toolStack = ToolStack.create(tool, tool.getToolDefinition());
    ToolMaterials setMaterials = ToolMaterials.of(MaterialFixture.MATERIAL_WITH_HEAD, MaterialFixture.MATERIAL_WITH_HANDLE, MaterialFixture.MATERIAL_WITH_EXTRA);
    toolStack.setMaterialsRaw(setMaterials);

    ItemStack stack = toolStack.getStack();
    assertThat(stack.has(TinkerData.MATERIALS)).isTrue();
    ToolMaterials stackMaterials = stack.get(TinkerData.MATERIALS);
    assertThat(stackMaterials).isNotEqualTo(ToolMaterials.EMPTY);
    assertThat(stackMaterials).isEqualTo(setMaterials);
  }

  @Test
  void materials_deserialize() {
    ItemStack stack = new ItemStack(tool);
    ToolMaterials setMaterials = ToolMaterials.of(MaterialFixture.MATERIAL_WITH_HEAD, MaterialFixture.MATERIAL_WITH_HANDLE, MaterialFixture.MATERIAL_WITH_EXTRA);
    stack.set(TinkerData.MATERIALS, setMaterials);

    ToolStack tool = ToolStack.from(stack);
    ToolMaterials toolMaterials = tool.getMaterials();
    assertThat(toolMaterials).isNotEqualTo(ToolMaterials.EMPTY);
    assertThat(toolMaterials).isEqualTo(setMaterials);
  }

  @Test
  void materials_replaceMaterial() {
    ToolStack toolStack = ToolStack.from(buildTestTool());
    assertThat(toolStack.getMaterials().size()).isEqualTo(3);
    assertThat(toolStack.getMaterial(0).get()).isEqualTo(MaterialFixture.MATERIAL_WITH_HEAD);
    assertThat(toolStack.getMaterial(1).get()).isEqualTo(MaterialFixture.MATERIAL_WITH_HANDLE);
    assertThat(toolStack.getMaterial(2).get()).isEqualTo(MaterialFixture.MATERIAL_WITH_EXTRA);

    // ensure it updated and no side-effects
    toolStack.replaceMaterial(0, MaterialFixture.MATERIAL_WITH_ALL_STATS.getId());
    assertThat(toolStack.getMaterials().size()).isEqualTo(3);
    assertThat(toolStack.getMaterial(0).get()).isEqualTo(MaterialFixture.MATERIAL_WITH_ALL_STATS);
    assertThat(toolStack.getMaterial(1).get()).isEqualTo(MaterialFixture.MATERIAL_WITH_HANDLE);
    assertThat(toolStack.getMaterial(2).get()).isEqualTo(MaterialFixture.MATERIAL_WITH_EXTRA);
  }


  /* Modifiers */

  @Test
  void modifiers_addModifier() {
    ToolStack toolStack = ToolStack.from(buildTestTool());
    assertThat(toolStack.getCraftedModifiers().getLevel(ModifierFixture.TEST_1)).isEqualTo(0);
    toolStack.addModifier(ModifierFixture.TEST_1, 1);
    assertThat(toolStack.getCraftedModifiers().getLevel(ModifierFixture.TEST_1)).isEqualTo(1);
  }

  @Test
  void modifiers_serialize() {
    ToolStack toolStack = ToolStack.from(buildTestTool());
    toolStack.addModifier(ModifierFixture.TEST_1, 1);

    ItemStack stack = toolStack.getStack();
    assertThat(stack.has(TinkerData.CRAFTED_MODIFIERS)).isTrue();
    ModifierEntries stackModifiers = stack.get(TinkerData.CRAFTED_MODIFIERS);
    assertThat(stackModifiers).isNotEqualTo(ModifierEntries.EMPTY);
    assertThat(stackModifiers).isEqualTo(ModifierEntries.EMPTY.withModifier(ModifierFixture.TEST_1, 1));
  }

  @Test
  void modifiers_deserialize() {
    ItemStack stack = buildTestTool();
    ModifierEntries setModifiers = ModifierEntries.EMPTY.withModifier(ModifierFixture.TEST_1, 1);
    stack.set(TinkerData.CRAFTED_MODIFIERS, setModifiers);

    ToolStack tool = ToolStack.from(stack);
    ModifierEntries readModifiers = tool.getCraftedModifiers();
    assertThat(readModifiers).isNotEqualTo(ModifierEntries.EMPTY);
    assertThat(readModifiers).isEqualTo(setModifiers);
  }

  @Test
  void allMods_serialize() {
    ToolStack toolStack = ToolStack.from(buildTestTool());
    ModifierEntries setModifiers = ModifierEntries.EMPTY.withModifier(ModifierFixture.TEST_1, 1);
    toolStack.setModifiers(setModifiers);

    ItemStack stack = toolStack.getStack();
    assertThat(stack.has(TinkerData.ALL_MODIFIERS)).isTrue();
    ModifierEntries readModifiers = stack.get(TinkerData.ALL_MODIFIERS);
    assertThat(readModifiers).isNotEqualTo(ModifierEntries.EMPTY);
    assertThat(readModifiers).isEqualTo(setModifiers);
  }

  @Test
  void allMods_deserialize() {
    ItemStack stack = buildTestTool();
    ModifierEntries setModifiers = ModifierEntries.EMPTY.withModifier(ModifierFixture.TEST_1, 1);
    stack.set(TinkerData.ALL_MODIFIERS, setModifiers);

    ToolStack tool = ToolStack.from(stack);
    ModifierEntries readModifiers = tool.getModifiers();
    assertThat(readModifiers).isNotEqualTo(ModifierEntries.EMPTY);
    assertThat(readModifiers).isEqualTo(setModifiers);
  }


  /* Mod data */

  @Test
  void persistentModData_serialize() {
    ToolStack toolStack = ToolStack.create(Items.DIAMOND_PICKAXE, ToolDefinition.EMPTY);
    assertThat(toolStack.getNbt().contains(ToolStack.TAG_PERSISTENT_MOD_DATA)).isFalse();

    ModDataNBT modData = toolStack.getPersistentData();
    modData.putString(TConstruct.getResource("test"), "test");

    assertThat(toolStack.getNbt().contains(ToolStack.TAG_PERSISTENT_MOD_DATA)).isTrue();
    assertThat(toolStack.getNbt().getCompound(ToolStack.TAG_PERSISTENT_MOD_DATA)).isEqualTo(modData.getData());
  }

  @Test
  void persistentModData_deserialize() {
    ItemStack stack = buildTestTool();
    ModDataNBT modData = new ModDataNBT();
    modData.putString(TConstruct.getResource("test"), "test");
    stack.getOrCreateTag().put(ToolStack.TAG_PERSISTENT_MOD_DATA, modData.getData());

    ToolStack toolStack = ToolStack.from(stack);
    assertThat(toolStack.getPersistentData().getData()).isEqualTo(modData.getData());
  }

  @Test
  void volatileModData_serialize() {
    ToolStack toolStack = ToolStack.create(Items.DIAMOND_PICKAXE, ToolDefinition.EMPTY);
    ModDataNBT modData = new ModDataNBT();
    modData.putString(TConstruct.getResource("test"), "test");
    toolStack.setVolatileModData(modData);

    assertThat(toolStack.getNbt().contains(ToolStack.TAG_VOLATILE_MOD_DATA)).isTrue();
    assertThat(toolStack.getNbt().getCompound(ToolStack.TAG_VOLATILE_MOD_DATA)).isEqualTo(modData.getData());
  }

  @Test
  void volatileModData_deserialize() {
    ItemStack stack = buildTestTool();
    ModDataNBT modData = new ModDataNBT();
    modData.putString(TConstruct.getResource("test"), "test");
    stack.getOrCreateTag().put(ToolStack.TAG_VOLATILE_MOD_DATA, modData.getData());

    ToolStack toolStack = ToolStack.from(stack);
    assertThat(toolStack.getVolatileData()).isEqualTo(modData);
  }


  /* Rebuild */

  @Test
  void setMaterials_refreshesData() {
    ToolStack toolStack = ToolStack.create(tool, tool.getToolDefinition());
    assertThat(toolStack.getStats()).isEqualTo(StatValues.EMPTY);

    ToolMaterials materials = ToolMaterials.of(MaterialFixture.MATERIAL_WITH_HEAD, MaterialFixture.MATERIAL_WITH_HANDLE, MaterialFixture.MATERIAL_WITH_EXTRA);
    toolStack.setMaterials(materials);
    assertThat(toolStack.getStats()).isNotEqualTo(StatValues.EMPTY);
  }

  @Test
  void addModifier_refreshesData() {
    ToolStack toolStack = ToolStack.create(tool, tool.getToolDefinition());
    // need materials for rebuild
    toolStack.setMaterialsRaw(ToolMaterials.of(MaterialFixture.MATERIAL_WITH_HEAD, MaterialFixture.MATERIAL_WITH_HANDLE, MaterialFixture.MATERIAL_WITH_EXTRA));
    // set some data that will get cleared out
    ModifierSlots volatileSlots = ModifierSlots.builder().add(SlotType.UPGRADE, 4).build();
    toolStack.setVolatileSlots(volatileSlots);
    assertThat(toolStack.getModifiers()).isEqualTo(ModifierEntries.EMPTY);

    toolStack.addModifier(ModifierFixture.TEST_1, 2);
    assertThat(toolStack.getVolatileSlots()).isNotEqualTo(volatileSlots);
    assertThat(toolStack.getModifiers().getLevel(ModifierFixture.TEST_1)).isEqualTo(2);
  }
}
