package slimeknights.tconstruct.library.recipe.modifiers.adding.variant;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.modifiers.ModifierHolder;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.component.ResourceMap;

/** {@link SwappableVariant} using {@link TinkerData#MODIFIER_SLOT_TYPE} to set {@link SlotType} */
public record SwappableSlotType(SlotType value) implements SwappableModifierMap<SlotType> {
  public static final RecordLoadable<SwappableSlotType> LOADER = RecordLoadable.create(SlotType.LOADABLE.requiredField("slot_type", SwappableSlotType::value), SwappableSlotType::new);

  @Override
  public RecordLoadable<? extends GenericLoaderRegistry.IHaveLoader> getLoader() {
    return LOADER;
  }

  @Override
  public DataComponentType<ResourceMap<SlotType>> component() {
    return TinkerData.MODIFIER_SLOT_TYPE.get();
  }

  @Override
  public Component formatVariant(ModifierHolder modifier) {
    return value.getUppercaseName();
  }
}
