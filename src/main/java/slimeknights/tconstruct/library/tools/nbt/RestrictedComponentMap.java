package slimeknights.tconstruct.library.tools.nbt;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.common.MutableDataComponentHolder;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/** Wrapper around {@link MutableDataComponentHolder} that limits what may be set */
@RequiredArgsConstructor
public class RestrictedComponentMap implements MutableDataComponentHolder {
  private final MutableDataComponentHolder base;
  private final Predicate<DataComponentType<?>> allowed;

  @Override
  public DataComponentMap getComponents() {
    return base.getComponents();
  }

  @Nullable
  @Override
  public <T> T set(DataComponentType<? super T> componentType, @Nullable T value) {
    return allowed.test(componentType) ? base.set(componentType, value) : null;
  }

  @Nullable
  @Override
  public <T> T remove(DataComponentType<? extends T> componentType) {
    return allowed.test(componentType) ? base.remove(componentType) : null;
  }

  @Override
  public void applyComponents(DataComponentPatch patch) {
    base.applyComponents(patch.forget(allowed.negate()));
  }

  @Override
  public void applyComponents(DataComponentMap components) {
    base.applyComponents(components.filter(allowed));
  }

  /** Creates the builder for {@link slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook} and {@link slimeknights.tconstruct.library.tools.definition.module.build.VolatileDataToolHook}. */
  public static MutableDataComponentHolder volatileData(MutableDataComponentHolder base) {
    return new RestrictedComponentMap(base, type -> ToolStack.isVolatile(type) && !ToolStack.isRestricted(type));
  }
}
