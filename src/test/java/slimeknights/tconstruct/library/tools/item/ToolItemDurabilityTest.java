package slimeknights.tconstruct.library.tools.item;

import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import static org.assertj.core.api.Assertions.assertThat;

public class ToolItemDurabilityTest extends ToolItemTest {

  @Test
  void testNewToolDurability() {
    ItemStack stack = buildTestTool();
    int statDurability = ToolStack.from(stack).getStats().getInt(ToolStats.DURABILITY);

    assertThat(stack.getDamageValue()).isEqualTo(0);
    assertThat(stack.getMaxDamage()).isEqualTo(statDurability);
    assertThat(ToolDamageUtil.isBroken(stack)).isFalse();
  }

  @Test
  void testSettingDamage() {
    ItemStack stack = buildTestTool();
    int statDurability = ToolStack.from(stack).getStats().getInt(ToolStats.DURABILITY);

    stack.setDamageValue(1);

    assertThat(stack.getDamageValue()).isEqualTo(1);
    assertThat(stack.getMaxDamage()).isEqualTo(statDurability);
    assertThat(ToolDamageUtil.isBroken(stack)).isFalse();
  }

  /*
  @Test
  void testDealingDamage() {
    testItemStack.damageItem(10, TestLivingEntity.getTestLivingEntity(), testLivingEntity -> {});

    assertThat(testItemStack.getDamage()).isEqualTo(10);
    assertThat(isTestitemBroken()).isFalse();
  }
  */

  @Test
  void testMaxDamageBreaksTool() {
    ItemStack stack = buildTestTool();
    ToolStack tool = ToolStack.from(stack);
    int statDurability = tool.getStats().getInt(ToolStats.DURABILITY);

    tool.setDamage(statDurability);

    assertThat(tool.getDamage()).isEqualTo(statDurability);
    assertThat(tool.isBroken()).isTrue();
  }

  @Test
  void testMoreThanMaxDamageBreaksTool() {
    ItemStack stack = buildTestTool();
    ToolStack tool = ToolStack.from(stack);
    int statDurability = tool.getStats().getInt(ToolStats.DURABILITY);

    stack.setDamageValue(99999999);

    assertThat(tool.getDamage()).isEqualTo(statDurability);
    assertThat(tool.isBroken()).isTrue();
  }

  /*
  @Test
  void testVanillaBreakCallback() {
    AtomicBoolean callbackCalled = new AtomicBoolean(false);
    testItemStack.damageItem(99999, TestLivingEntity.getTestLivingEntity(),
      testLivingEntity -> callbackCalled.set(true));

    // this works because the callback is called synchronously
    assertThat(callbackCalled.get()).isTrue();
    assertThat(isTestitemBroken()).isTrue();
  }
  */

  /*
  @Test
  void testVanillaBreakDoesNotReduceStacksize() {
    testItemStack.damageItem(99999, TestLivingEntity.getTestLivingEntity(),
      testLivingEntity -> {});

    assertThat(isTestitemBroken()).isTrue();
    assertThat(testItemStack.isEmpty()).isFalse();
  }
  */
}
