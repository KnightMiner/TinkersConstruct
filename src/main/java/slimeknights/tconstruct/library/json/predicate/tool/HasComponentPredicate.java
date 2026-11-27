package slimeknights.tconstruct.library.json.predicate.tool;

import net.minecraft.core.component.DataComponentType;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.tconstruct.library.tools.stack.ToolContext;

import java.util.function.Supplier;

/** Predicate matching tools that contain the given component */
public record HasComponentPredicate(DataComponentType<?> component) implements ToolContextPredicate {
  public static final RecordLoadable<HasComponentPredicate> LOADABLE = RecordLoadable.create(Loadables.DATA_COMPONENT_TYPE.requiredField("component", HasComponentPredicate::component), HasComponentPredicate::new);

  public HasComponentPredicate(Supplier<? extends DataComponentType<?>> supplier) {
    this(supplier.get());
  }

  @Override
  public boolean matches(ToolContext tool) {
    return tool.has(component);
  }

  @Override
  public RecordLoadable<? extends IJsonPredicate<ToolContext>> getLoader() {
    return LOADABLE;
  }
}
