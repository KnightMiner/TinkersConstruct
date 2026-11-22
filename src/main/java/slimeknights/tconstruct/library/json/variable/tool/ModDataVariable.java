package slimeknights.tconstruct.library.json.variable.tool;

import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.registration.object.IdAwareObject;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;

/** Variable that fetches a number from modifier data */
public record ModDataVariable(ResourceLocation key, ModDataSource source) implements ToolVariable {
  public static final RecordLoadable<ModDataVariable> LOADER = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("key", ModDataVariable::key),
    ModDataSource.LOADABLE.requiredField("source", ModDataVariable::source),
    ModDataVariable::new);

  public ModDataVariable(IdAwareObject key, ModDataSource source) {
    this(key.id(), source);
  }

  @Override
  public float getValue(ToolStackView tool) {
    return source.getData(tool).getFloat(key);
  }

  @Override
  public RecordLoadable<ModDataVariable> getLoader() {
    return LOADER;
  }
}
