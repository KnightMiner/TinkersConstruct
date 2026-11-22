package slimeknights.tconstruct.library.recipe.modifiers.adding.variant;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.modifiers.ModifierHolder;
import slimeknights.tconstruct.library.tools.component.ResourceMap;

/** {@link SwappableVariant} using {@link TinkerData#MODIFIER_INTEGER} to set integers. */
public record SwappableInteger(Integer value) implements SwappableModifierMap<Integer> {
  public static final RecordLoadable<SwappableInteger> LOADER = RecordLoadable.create(IntLoadable.ANY_FULL.requiredField("value", SwappableInteger::value), SwappableInteger::new);

  @Override
  public RecordLoadable<? extends GenericLoaderRegistry.IHaveLoader> getLoader() {
    return LOADER;
  }

  @Override
  public DataComponentType<ResourceMap<Integer>> component() {
    return TinkerData.MODIFIER_INTEGER.get();
  }

  @Override
  public Component formatVariant(ModifierHolder modifier) {
    return VariantFormatter.INTEGER.format(modifier, value);
  }
}
