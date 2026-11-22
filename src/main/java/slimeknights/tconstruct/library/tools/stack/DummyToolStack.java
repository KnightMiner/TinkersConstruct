package slimeknights.tconstruct.library.tools.stack;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;
import slimeknights.tconstruct.library.tools.component.ModifierEntries;
import slimeknights.tconstruct.library.tools.component.ModifierSlots;
import slimeknights.tconstruct.library.tools.component.StatMultipliers;
import slimeknights.tconstruct.library.tools.component.StatValues;
import slimeknights.tconstruct.library.tools.component.ToolMaterials;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;

import javax.annotation.Nullable;

/** Tool stack instance in a context that does not have a proper tool, used for projectils */
@RequiredArgsConstructor @Getter
public class DummyToolStack implements ToolStackView {
  private final Item item;
  private final ModifierEntries modifiers;
  private final PatchedDataComponentMap components;

  @Override
  public ToolDefinition getDefinition() {
    return ToolDefinition.EMPTY;
  }

  @Override
  public ToolMaterials getMaterials() {
    return ToolMaterials.EMPTY;
  }

  @Override
  public ModifierEntries getCraftedModifiers() {
    return ModifierEntries.EMPTY;
  }

  @Override
  public ModifierSlots getPersistentSlots() {
    return ModifierSlots.EMPTY;
  }

  @Override
  public ModifierSlots getVolatileSlots() {
    return ModifierSlots.EMPTY;
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
  public StatValues getStats() {
    return StatValues.EMPTY;
  }

  @Override
  public StatMultipliers getMultipliers() {
    return StatMultipliers.EMPTY;
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
