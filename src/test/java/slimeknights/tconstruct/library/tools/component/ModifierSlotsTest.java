package slimeknights.tconstruct.library.tools.component;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

public class ModifierSlotsTest extends BaseMcTest {
  @Test
  void empty() {
    for (SlotType type : SlotType.getAllSlotTypes()) {
      assertThat(ModifierSlots.EMPTY.get(type)).isEqualTo(0);
    }
  }

  @Test
  void defaults() {
    ModifierSlots nbt = ModifierSlots.builder().add(SlotType.UPGRADE, 1).build();
    assertThat(nbt.get(SlotType.UPGRADE)).isEqualTo(1);
    for (SlotType type : SlotType.getAllSlotTypes()) {
      if (type != SlotType.UPGRADE) {
        assertThat(nbt.get(type)).isEqualTo(0);
      }
    }
  }

  @Test
  void serialize() {
    ModifierSlots slots = ModifierSlots.builder()
      .add(SlotType.UPGRADE, 2)
      .add(SlotType.ABILITY, 3)
      .add(SlotType.DEFENSE, 4)
      .build();

    CompoundTag nbt = (CompoundTag) TagUtil.encode(ModifierSlots.CODEC, slots);
    assertThat(nbt.getInt(SlotType.UPGRADE.getName())).isEqualTo(2);
    assertThat(nbt.getInt(SlotType.ABILITY.getName())).isEqualTo(3);
    assertThat(nbt.getInt(SlotType.SOUL.getName())).isEqualTo(4);
  }

  @Test
  void deserialize() {
    CompoundTag nbt = new CompoundTag();
    nbt.putInt(SlotType.UPGRADE.getName(), 4);
    nbt.putInt(SlotType.ABILITY.getName(), 5);
    nbt.putInt(SlotType.DEFENSE.getName(), 6);

    ModifierSlots slots = TagUtil.decode(ModifierSlots.CODEC, "slots", nbt).orElse(ModifierSlots.EMPTY);
    assertThat(slots.get(SlotType.UPGRADE)).isEqualTo(4);
    assertThat(slots.get(SlotType.ABILITY)).isEqualTo(5);
    assertThat(slots.get(SlotType.DEFENSE)).isEqualTo(6);
  }
}
