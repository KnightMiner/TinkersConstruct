package slimeknights.tconstruct.smeltery.block.entity.component;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import slimeknights.tconstruct.common.multiblock.IMasterLogic;
import slimeknights.tconstruct.library.client.model.ModelProperties;
import slimeknights.tconstruct.library.fluid.FluidTankAnimated;
import slimeknights.tconstruct.library.utils.NBTTags;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.component.ITankBlock;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock;
import slimeknights.tconstruct.smeltery.block.entity.ITankBlockEntity;

/** Smeltery component with a simple fluid tank */
public class TankBlockEntity extends SmelteryComponentBlockEntity implements ITankBlockEntity {
  /** Internal fluid tank instance */
  @Getter
  protected final FluidTankAnimated tank;
  /** Last comparator strength to reduce block updates */
  @Getter @Setter
  private int lastStrength = -1;

  /** Block entity type constructor */
  public TankBlockEntity(BlockPos pos, BlockState state) {
    this(pos, state, ITankBlock.getCapacity(state.getBlock()));
  }

  /** Main constructor */
  public TankBlockEntity(BlockPos pos, BlockState state, ITankBlock block) {
    this(TinkerSmeltery.tank.get(), pos, state, block.getCapacity());
  }

  /** Capacity constructor */
  private TankBlockEntity(BlockPos pos, BlockState state, int capacity) {
    this(TinkerSmeltery.tank.get(), pos, state, capacity);
  }

  /** Extendable constructor */
  @SuppressWarnings("WeakerAccess")
  protected TankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int capacity) {
    super(type, pos, state);
    tank = new FluidTankAnimated(capacity, this);
  }


  /*
   * Tank methods
   */

  @Override
  public ModelData getModelData() {
    return ModelData.builder()
      .with(ModelProperties.FLUID_STACK, tank.getFluid())
      .with(ModelProperties.TANK_CAPACITY, tank.getCapacity())
      .build();
  }

  /** Updates the light for this tank using {@link SearedTankBlock#LIGHT} */
  public static void updateLight(BlockEntity be, IFluidTank tank) {
    Level level = be.getLevel();
    if (level != null && !level.isClientSide) {
      FluidStack fluid = tank.getFluid();
      int light = fluid.isEmpty() ? 0 : fluid.getFluid().getFluidType().getLightLevel(fluid);
      BlockState state = be.getBlockState();
      if (light != state.getValue(SearedTankBlock.LIGHT)) {
        level.setBlock(be.getBlockPos(), state.setValue(SearedTankBlock.LIGHT, light), Block.UPDATE_CLIENTS);
      }
    }
  }

  @Override
  public void onTankContentsChanged() {
    ITankBlockEntity.super.onTankContentsChanged();
    if (this.level != null) {
      updateLight(this, tank);
      this.requestModelDataUpdate();
    }
  }

  @Override
  public void onLoad() {
    super.onLoad();
    if (level != null && !level.isClientSide) {
      BlockPos masterPos = getMasterPos();
      if (masterPos != null && level.getBlockEntity(masterPos) instanceof IMasterLogic master) {
        master.onServantLoad(this);
      }
    }
  }


  /* Components */

  @Override
  protected void applyImplicitComponents(DataComponentInput components) {
    ITankBlockEntity.applyImplicitComponents(tank, components);
  }

  @Override
  protected void collectImplicitComponents(DataComponentMap.Builder components) {
    ITankBlockEntity.collectImplicitComponents(tank, components);
  }


  /*
   * NBT
   */

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  protected void loadAdditional(CompoundTag tags, HolderLookup.Provider registries) {
    super.loadAdditional(tags, registries);
    tank.setCapacity(ITankBlock.getCapacity(getBlockState().getBlock()));
    CompoundTag tankTags = tags.getCompound(NBTTags.TANK);
    if (tankTags.isEmpty()) {
      tank.setFluid(FluidStack.EMPTY);
    } else {
      tank.readFromNBT(registries, tankTags);
      updateLight(this, tank);
    }
  }

  @Override
  public void saveSynced(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveSynced(tag, registries);
    // want tank on the client on world load
    if (!tank.isEmpty()) {
      tag.put(NBTTags.TANK, tank.writeToNBT(registries, new CompoundTag()));
    }
  }
}
