package slimeknights.tconstruct.library.tools.capability.fluid;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.registry.NamedComponentRegistry;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.fluid.item.SimpleFluidContent;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.build.ModifierTraitModule;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;
import slimeknights.tconstruct.library.tools.stat.CapacityStat;
import slimeknights.tconstruct.library.tools.stat.INumericToolStat;
import slimeknights.tconstruct.tools.TinkerModifiers;

import java.util.function.Supplier;

/** Helper methods for handling fluids in tools */
public interface ToolTankHelper {
  /** Format key for the stat */
  String MB_FORMAT = Mantle.makeDescriptionId("gui", "fluid.millibucket");
  /** Stat controlling the max for the default helper */
  CapacityStat CAPACITY_STAT = new CapacityStat(TConstruct.resourceId("tank_capacity"), 0xA0A0A0, MB_FORMAT);
  /** Default tank helper for setting fluids */
  ToolTankHelper TANK_HELPER = new ToolTankHelper.Component(CAPACITY_STAT, TinkerData.TANK);
  /** Module ensuring the tool has the tank */
  ModifierModule TANK_HANDLER = new ModifierTraitModule(TinkerModifiers.tankHandler.id(), 1, true);

  /** Loadable instance for JSON */
  NamedComponentRegistry<ToolTankHelper> LOADABLE = new NamedComponentRegistry<>("Unknown Tool Tank Helper");

  /** Gets the capacity for the tool */
  int getCapacity(ToolStackView tool);


  /* Fluid */

  /** Gets the fluid stack in the tank. May be less efficient than other methods such as  */
  FluidStack getFluid(ToolStackView tool);

  /**
   * Sets the fluid in the tank
   * @return Updated fluid instance after setting.
   */
  FluidStack setFluid(ToolStackView tool, FluidStack fluid);

  /** Default implementation using a capacity stat and a component for contents */
  record Component(INumericToolStat<?> capacityStat, Supplier<DataComponentType<SimpleFluidContent>> component) implements ToolTankHelper {

    @Override
    public int getCapacity(ToolStackView tool) {
      return tool.getStats().getInt(capacityStat);
    }

    /** Gets the contents instance for the given tool */
    private SimpleFluidContent getContents(ToolStackView tool) {
      return tool.getOrDefault(component, SimpleFluidContent.EMPTY);
    }

    @Override
    public FluidStack getFluid(ToolStackView tool) {
      return getContents(tool).copy();
    }

    @Override
    public FluidStack setFluid(ToolStackView tool, FluidStack fluid) {
      if (fluid.isEmpty()) {
        tool.remove(component);
        return FluidStack.EMPTY;
      }
      int capacity = getCapacity(tool);
      // fluid will be copied again as we set the component, so only copy if needed
      if (fluid.getAmount() > capacity) {
        fluid = fluid.copyWithAmount(capacity);
      }
      tool.set(component, SimpleFluidContent.copyOf(fluid));
      return fluid;
    }
  }
}
