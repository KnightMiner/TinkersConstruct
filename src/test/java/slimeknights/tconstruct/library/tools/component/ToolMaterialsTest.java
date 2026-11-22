package slimeknights.tconstruct.library.tools.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import slimeknights.tconstruct.fixture.MaterialFixture;
import slimeknights.tconstruct.library.materials.MaterialRegistryExtension;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MaterialRegistryExtension.class)
class ToolMaterialsTest extends BaseMcTest {

  public static final IMaterial TEST_MATERIAL_1 = MaterialFixture.MATERIAL_1;
  public static final IMaterial TEST_MATERIAL_2 = MaterialFixture.MATERIAL_2;
  private final ToolMaterials testToolMaterials = ToolMaterials.of(TEST_MATERIAL_1, TEST_MATERIAL_2);

  @Test
  void serialize() {
    ListTag nbtList = testToolMaterials.serializeToNBT();

    assertThat(nbtList).hasSize(2);
    assertThat(nbtList).extracting(Tag::getAsString).containsExactly(
      TEST_MATERIAL_1.getId().toString(),
      TEST_MATERIAL_2.getId().toString()
    );
  }

  @Test
  void serializeEmpty_emptyList() {
    ListTag nbtList = ToolMaterials.EMPTY.serializeToNBT();

    assertThat(nbtList).isEmpty();
  }

  @Test
  void deserialize() {
    ListTag nbtList = new ListTag();
    // note we switched the order here to ensure that the order is as defined, and not ordered in some way
    nbtList.add(StringTag.valueOf(TEST_MATERIAL_2.getId().toString()));
    nbtList.add(StringTag.valueOf(TEST_MATERIAL_1.getId().toString()));

    ToolMaterials toolMaterials = ToolMaterials.readFromNBT(nbtList);

    assertThat(toolMaterials.getList()).hasSize(2);
    assertThat(toolMaterials.get(0).get()).isEqualTo(TEST_MATERIAL_2);
    assertThat(toolMaterials.get(1).get()).isEqualTo(TEST_MATERIAL_1);
  }

  @Test
  void deserialize_emptyList() {
    ListTag nbtList = new ListTag();

    ToolMaterials toolMaterials = ToolMaterials.readFromNBT(nbtList);

    assertThat(toolMaterials.getList()).isEmpty();
  }

  @Test
  void wrongNbtType_emptyList() {
    Tag wrongNbt = new CompoundTag();

    ToolMaterials toolMaterials = ToolMaterials.readFromNBT(wrongNbt);

    assertThat(toolMaterials.getList()).isEmpty();
  }

  @Test
  void wrongListNbtType_emptyList() {
    ListTag wrongNbt = new ListTag();
    wrongNbt.add(new CompoundTag());

    ToolMaterials toolMaterials = ToolMaterials.readFromNBT(wrongNbt);

    assertThat(toolMaterials.getList()).isEmpty();
  }

  @Test
  void replaceMaterial() {
    ToolMaterials nbt = ToolMaterials.EMPTY;
    assertThat(nbt.get(1).get()).isEqualTo(IMaterial.UNKNOWN);

    // note 1 is out of bounds of empty, but replace material needs to be able to extend the material list to deal with errors
    nbt = nbt.replaceMaterial(1, MaterialFixture.MATERIAL_1.getId());

    assertThat(nbt).overridingErrorMessage("replaceMaterial should not modify the original").isNotSameAs(ToolMaterials.EMPTY);
    assertThat(nbt.get(1).get()).isEqualTo(MaterialFixture.MATERIAL_1);
  }
}
