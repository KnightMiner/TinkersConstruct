package slimeknights.tconstruct.library.tools.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierFixture;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

class ModifierEntriesTest extends BaseMcTest {
  @BeforeAll
  static void before() {
    ModifierFixture.init();
  }

  @Test
  void modifierBuilder_empty() {
    ModifierEntries modifierEntries = ModifierEntries.builder().build();

    assertThat(modifierEntries.modifiers().size()).isEqualTo(0);
    assertThat(modifierEntries.getLevel(ModifierFixture.TEST_1)).isEqualTo(0);
  }

  @Test
  void modifierBuilder_single() {
    ModifierEntries.Builder builder = ModifierEntries.builder();
    builder.add(ModifierFixture.TEST_MODIFIER_1, 2);
    ModifierEntries modifierEntries = builder.build();

    assertThat(modifierEntries.modifiers().size()).isEqualTo(1);
    assertThat(modifierEntries.getLevel(ModifierFixture.TEST_1)).isEqualTo(2);
  }

  @Test
  void modifierBuilder_added() {
    ModifierEntries.Builder builder = ModifierEntries.builder();
    builder.add(ModifierFixture.TEST_MODIFIER_1, 2);
    builder.add(ModifierFixture.TEST_MODIFIER_1, 1);
    ModifierEntries modifierEntries = builder.build();

    assertThat(modifierEntries.modifiers().size()).isEqualTo(1);
    assertThat(modifierEntries.getLevel(ModifierFixture.TEST_1)).isEqualTo(3);
  }

  @Test
  void modifierBuilder_multiple() {
    ModifierEntries.Builder builder = ModifierEntries.builder();
    builder.add(ModifierFixture.TEST_MODIFIER_1, 2);
    builder.add(ModifierFixture.TEST_MODIFIER_2, 1);
    ModifierEntries modifierEntries = builder.build();

    assertThat(modifierEntries.modifiers().size()).isEqualTo(2);
    assertThat(modifierEntries.getLevel(ModifierFixture.TEST_1)).isEqualTo(2);
    assertThat(modifierEntries.getLevel(ModifierFixture.TEST_2)).isEqualTo(1);
  }

  @Test
  void withModifier() {
    ModifierEntries modifierEntries = ModifierEntries.EMPTY.withModifier(ModifierFixture.TEST_1, 1);
    assertThat(modifierEntries.modifiers().size()).isEqualTo(1);
    assertThat(modifierEntries.getLevel(ModifierFixture.TEST_1)).isEqualTo(1);
  }

  @Test
  void serialize() {
    ModifierEntries.Builder builder = ModifierEntries.builder();
    builder.add(ModifierFixture.TEST_MODIFIER_1, 2);
    builder.add(ModifierFixture.TEST_MODIFIER_2, 3);

    ListTag list = (ListTag)builder.build().serializeToNBT();
    assertThat(list.size()).isEqualTo(2);
    CompoundTag tag = list.getCompound(0);
    assertThat(tag.getString(ModifierEntry.TAG_MODIFIER)).isEqualTo(ModifierFixture.TEST_1.toString());
    assertThat(tag.getInt(ModifierEntry.TAG_LEVEL)).isEqualTo(2);

    tag = list.getCompound(1);
    assertThat(tag.getString(ModifierEntry.TAG_MODIFIER)).isEqualTo(ModifierFixture.TEST_2.toString());
    assertThat(tag.getInt(ModifierEntry.TAG_LEVEL)).isEqualTo(3);
  }

  @Test
  void deserialize() {
    ListTag list = new ListTag();
    CompoundTag tag = new CompoundTag();
    tag.putString(ModifierEntry.TAG_MODIFIER, ModifierFixture.TEST_1.toString());
    tag.putInt(ModifierEntry.TAG_LEVEL, 2);
    list.add(tag);
    tag = new CompoundTag();
    tag.putString(ModifierEntry.TAG_MODIFIER, ModifierFixture.TEST_2.toString());
    tag.putInt(ModifierEntry.TAG_LEVEL, 3);
    list.add(tag);

    ModifierEntries modifierEntries = ModifierEntries.readFromNBT(list);
    assertThat(modifierEntries.getLevel(ModifierFixture.TEST_1)).isEqualTo(2);
    assertThat(modifierEntries.getLevel(ModifierFixture.TEST_2)).isEqualTo(3);
  }

  @Test
  void deserializeNoData_empty() {
    ListTag nbt = new ListTag();

    ModifierEntries modifierEntries = ModifierEntries.readFromNBT(nbt);

    assertThat(modifierEntries).isEqualTo(ModifierEntries.EMPTY);
  }

  @Test
  void wrongNbtType_empty() {
    Tag wrongNbt = new CompoundTag();

    ModifierEntries modifierEntries = ModifierEntries.readFromNBT(wrongNbt);

    assertThat(modifierEntries).isEqualTo(ModifierEntries.EMPTY);
  }
}
