package slimeknights.tconstruct.library.json.variable.tool;

import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.tconstruct.library.json.predicate.tool.ToolContextPredicate;
import slimeknights.tconstruct.library.json.variable.ConditionalVariable;
import slimeknights.tconstruct.library.tools.stack.ToolContext;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;

/**
 * Gets one of two entity properties based on the condition
 */
public record ConditionalToolVariable(IJsonPredicate<ToolContext> condition, ToolVariable ifTrue, ToolVariable ifFalse) implements ToolVariable, ConditionalVariable<IJsonPredicate<ToolContext>,ToolVariable> {
  public static final RecordLoadable<ConditionalToolVariable> LOADER = ConditionalVariable.loadable(ToolContextPredicate.LOADER, ToolVariable.LOADER, ConditionalToolVariable::new);

  public ConditionalToolVariable(IJsonPredicate<ToolContext> condition, float ifTrue, float ifFalse) {
    this(condition, new ToolVariable.Constant(ifTrue), new ToolVariable.Constant(ifFalse));
  }

  @Override
  public float getValue(ToolStackView tool) {
    return condition.matches(tool) ? ifTrue.getValue(tool) : ifFalse.getValue(tool);
  }

  @Override
  public RecordLoadable<ConditionalToolVariable> getLoader() {
    return LOADER;
  }
}
