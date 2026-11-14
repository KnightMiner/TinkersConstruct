package slimeknights.tconstruct.library.materials.definition;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;

public class TestMaterial extends Material {
  public TestMaterial(ResourceLocation identifier, boolean craftable, boolean hidden) {
    super(identifier, 0, -1, Rarity.COMMON, craftable, hidden);
  }
}
