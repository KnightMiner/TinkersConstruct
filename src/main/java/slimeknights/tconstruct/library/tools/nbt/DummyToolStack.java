package slimeknights.tconstruct.library.tools.nbt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;

import javax.annotation.Nullable;

/** Tool stack instance in a context that does not have a proper tool, used for projectils */
@RequiredArgsConstructor @Getter
public class DummyToolStack implements IToolStackView {
  private final Item item;
  private final ModifierNBT modifiers;
  private final PatchedDataComponentMap components;

  @Override
  public ToolDefinition getDefinition() {
    return ToolDefinition.EMPTY;
  }

  @Override
  public MaterialNBT getMaterials() {
    return MaterialNBT.EMPTY;
  }

  @Override
  public ModifierNBT getCraftedModifiers() {
    return ModifierNBT.EMPTY;
  }

  @Override
  public SlotsNBT getPersistentSlots() {
    return SlotsNBT.EMPTY;
  }

  @Override
  public SlotsNBT getVolatileSlots() {
    return SlotsNBT.EMPTY;
  }

  @Override
  public int getDamage() {
    return 0;
  }

  @Override
  public int getCurrentDurability() {
    return 0;
  }

  @Override
  public boolean isBroken() {
    return false;
  }

  @Override
  public boolean isUnbreakable() {
    return false;
  }

  @Override
  public void setDamage(int damage) {}

  @Override
  public StatsNBT getStats() {
    return StatsNBT.EMPTY;
  }

  @Override
  public MultiplierNBT getMultipliers() {
    return MultiplierNBT.EMPTY;
  }


  /* Data */

  @Nullable
  @Override
  public <T> T set(DataComponentType<? super T> componentType, @Nullable T value) {
    return components.set(componentType, value);
  }

  @Nullable
  @Override
  public <T> T remove(DataComponentType<? extends T> componentType) {
    return components.remove(componentType);
  }

  @Override
  public void applyComponents(DataComponentPatch patch) {
    components.applyPatch(patch);
  }

  @Override
  public void applyComponents(DataComponentMap components) {
    this.components.setAll(components);
  }
}
