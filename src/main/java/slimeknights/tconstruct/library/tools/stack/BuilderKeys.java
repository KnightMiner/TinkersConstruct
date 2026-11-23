package slimeknights.tconstruct.library.tools.stack;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.tools.component.ModifierSlots;
import slimeknights.tconstruct.library.tools.component.SetComponent;
import slimeknights.tconstruct.library.tools.stack.VolatileBuilder.BuilderKey;
import slimeknights.tconstruct.library.tools.stack.VolatileBuilder.CollectionBuilderKey;

/**
 * Holds Tinkers' Construct keys for {@link VolatileBuilder}. Additional keys may be added by addons.
 * Does not require registration, though if multiple keys have the same component it leads to undefined behavior.
 */
public interface BuilderKeys {
  /** Builder for {@link TinkerData#VOLATILE_FLAGS} */
  BuilderKey<SetComponent<ResourceLocation>, ImmutableSet.Builder<ResourceLocation>> FLAGS = new CollectionBuilderKey<>() {
    @Override
    public ImmutableSet.Builder<ResourceLocation> createBuilder() {
      return ImmutableSet.builder();
    }

    @Override
    public DataComponentType<SetComponent<ResourceLocation>> component() {
      return TinkerData.VOLATILE_FLAGS.get();
    }

    @Override
    public SetComponent<ResourceLocation> createCollection(ImmutableSet.Builder<ResourceLocation> builder) {
      return SetComponent.create(builder.build());
    }
  };

  /** Builder for {@link TinkerData#VOLATILE_SLOTS} */
  BuilderKey<ModifierSlots, ModifierSlots.Builder> SLOTS = new CollectionBuilderKey<>() {
    @Override
    public ModifierSlots.Builder createBuilder() {
      return ModifierSlots.builder();
    }

    @Override
    public DataComponentType<ModifierSlots> component() {
      return TinkerData.VOLATILE_SLOTS.get();
    }

    @Override
    public ModifierSlots createCollection(ModifierSlots.Builder builder) {
      return builder.build();
    }
  };
}
