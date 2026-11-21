package slimeknights.tconstruct.library.tools.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.test.TestHelper;

import static org.assertj.core.api.Assertions.assertThat;

class ToolDataNBTTest extends BaseMcTest {
  private static final ResourceLocation testKey = TestHelper.id("test");
  private static final ResourceLocation testKey2 = TestHelper.id("test2");

  @Test
  void empty() {
    CompoundTag nbt = IModDataView.EMPTY.getCompound(testKey);
    nbt.putInt("test", 1);
    nbt = IModDataView.EMPTY.getCompound(testKey);
    assertThat(nbt.contains("test")).overridingErrorMessage("NBT not saved in empty").isFalse();
  }

  @Test
  void defaults() {
    ModDataNBT nbt = new ModDataNBT();
    assertThat(nbt.getData().isEmpty()).isTrue();
  }

  @Test
  void serializeData() {
    ModDataNBT modData = new ModDataNBT();
    modData.putInt(testKey, 1);
    modData.put(testKey2, new CompoundTag());

    CompoundTag nbt = modData.getData();
    assertThat(nbt.getInt(testKey.toString())).isEqualTo(1);
    assertThat(nbt.contains(testKey2.toString(), Tag.TAG_COMPOUND)).isTrue();
  }

  @Test
  void deserialize() {
    CompoundTag nbt = new CompoundTag();
    nbt.putString(testKey.toString(), "Not sure why you need strings");
    CompoundTag tag = new CompoundTag();
    tag.putInt("test", 1);
    nbt.put(testKey2.toString(), tag);

    ModDataNBT modData = ModDataNBT.readFromNBT(nbt);
    assertThat(modData.getString(testKey)).isEqualTo("Not sure why you need strings");

    tag = modData.getCompound(testKey2);
    assertThat(tag.isEmpty()).isFalse();
    assertThat(tag.contains("test", Tag.TAG_ANY_NUMERIC)).isTrue();
    assertThat(tag.getInt("test")).isEqualTo(1);
  }
}
