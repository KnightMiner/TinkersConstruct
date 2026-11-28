package slimeknights.tconstruct.smeltery.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredBlock;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.fluid.FluidTransferHelper;
import slimeknights.mantle.fluid.tooltip.FluidTooltipHandler;
import slimeknights.mantle.fluid.transfer.FluidContainerTransferManager;
import slimeknights.mantle.fluid.transfer.IFluidContainerTransfer.TransferDirection;
import slimeknights.mantle.fluid.transfer.IFluidContainerTransfer.TransferResult;
import slimeknights.mantle.item.tooltip.FixedTooltipBlockItem;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.fluid.item.ItemFluidTank;
import slimeknights.tconstruct.library.fluid.item.SimpleFluidContent;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.component.ITankBlock;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock.TankType;

import java.util.List;
import java.util.function.Consumer;

public class TankItem extends FixedTooltipBlockItem {
  public static final String FLUID_ID = TConstruct.makeTranslationKey("item", "tank.fluid_id");
  private final boolean limitStackSize;
  public TankItem(Block blockIn, Properties builder, boolean limitStackSize) {
    super(blockIn, builder);
    this.limitStackSize = limitStackSize;
  }

  /** Gets the tank capacity */
  private static int getCapacity(ItemStack stack) {
    return ITankBlock.getCapacity(stack.getItem());
  }

  /** Checks if the tank item is filled */
  private static boolean isFilled(ItemStack stack) {
    return !SimpleFluidContent.get(stack).isEmpty();
  }

  @Override
  public boolean hasCraftingRemainingItem(ItemStack stack) {
    return isFilled(stack);
  }

  @Override
  public ItemStack getCraftingRemainingItem(ItemStack stack) {
    return isFilled(stack) ? new ItemStack(this) : ItemStack.EMPTY;
  }

  @Override
  public int getMaxStackSize(ItemStack stack) {
    if (!limitStackSize) {
      return super.getMaxStackSize(stack);
    }
    return isFilled(stack) ? 16: 64;
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    SimpleFluidContent contents = SimpleFluidContent.get(stack);
    if (!contents.isEmpty()) {
      tooltip.add(contents.getHoverName().plainCopy().withStyle(ChatFormatting.GRAY));
      Fluid fluid = contents.getFluid();
      if (flag.isAdvanced()) {
        tooltip.add(Component.translatable(FLUID_ID, Loadables.FLUID.getKey(fluid)).withStyle(ChatFormatting.DARK_GRAY));
      }
      FluidTooltipHandler.appendMaterial(fluid, contents.getAmount(), tooltip);
    }
    else {
      super.appendHoverText(stack, context, tooltip, flag);
    }
  }

  /** Checks if the given stack has fluid transfer */
  public static boolean mayHaveFluid(ItemStack stack) {
    return FluidContainerTransferManager.INSTANCE.mayHaveTransfer(stack) || stack.getCapability(Capabilities.FluidHandler.ITEM) != null;
  }

  @Override
  public boolean overrideStackedOnOther(ItemStack held, Slot slot, ClickAction action, Player player) {
    // take over right click, assuming the target has an item. If not, then we want to place 1 item in the slot
    if (action == ClickAction.SECONDARY && slot.allowModification(player)) {
      ItemStack slotStack = slot.getItem();
      // if it's the same item, we might want to transfer fluid or just move 1 item; overrideOtherStackedOnMe will handle deciding which to take
      if (!slotStack.isEmpty() && held.getItem() != slotStack.getItem() && mayHaveFluid(slotStack)) {
        // target must be stack size 1, if not then it's not safe to modify it
        if (slotStack.getCount() == 1) {
          // transfer fluid - but we work with just 1 tank at a time instead of trying to transfer the whole stack
          ItemStack split = held;
          if (held.getCount() > 1) {
            split = held.copyWithCount(1);
          }
          ItemFluidTank tank = new ItemFluidTank(split, getCapacity(held));
          TransferResult result = FluidTransferHelper.interactWithStack(tank, slotStack, TransferDirection.REVERSE);
          // update held tank and slot item if something changed
          if (result != null) {
            // play sound
            if (player.level().isClientSide) {
              player.playSound(result.getSound());
            }
            // update stack
            slot.set(FluidTransferHelper.getOrTransferFilled(player, slotStack, result.stack()));
            // if held is size 1, everything is already updated
            // if its size 2+, then we modified a copy, so update the stack
            if (held.getCount() > 1) {
              // consume item from held and toss the update anywhere
              held.shrink(1);
              if (!player.getInventory().add(split)) {
                player.drop(split, false);
              }
            }
          }
        } else {
          // we don't try filling items with a larger stack size as our transfer logic does not support that
          // however, supposing that item accepts it in their stack on me logic, let them respond
          // this won't run twice as we will be returning true regardless
          if (slotStack.isItemEnabled(player.level().enabledFeatures())) {
            AbstractContainerMenu menu = player.containerMenu;
            slotStack.overrideOtherStackedOnMe(held, slot, action, player, new SlotAccess() {
              @Override
              public ItemStack get() {
                return menu.getCarried();
              }

              @Override
              public boolean set(ItemStack stack) {
                menu.setCarried(stack);
                return true;
              }
            });
          }
        }
        return true;
      }
    }
    return false;
  }

  /** Updates the item the player is holding from the old instance */
  public static void updateHeldItem(Player player, ItemStack held, ItemStack result) {
    if (player.containerMenu.getCarried() == held) {
      player.containerMenu.setCarried(FluidTransferHelper.getOrTransferFilled(player, held, result));
    } else if (!player.getInventory().add(result)) {
      player.drop(result, false);
    }
  }

  @Override
  public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack held, Slot slot, ClickAction action, Player player, SlotAccess pAccess) {
    // take over right click, unless there is no held item (we still want split stack support)
    if (action == ClickAction.SECONDARY && slot.allowModification(player) && !held.isEmpty() && mayHaveFluid(held)) {
      // we can safely modify tanks of size larger than 1,
      // though our fluid transfer logic does not handle well transferring between two tanks with no 1mb increments
      if (stack.getCount() == 1 || held.getItem() instanceof TankItem) {
        // transfer the fluid
        ItemFluidTank tank = new ItemFluidTank(stack, getCapacity(stack));
        // if both tanks are empty, just do standard stack operations; makes it nice and easy to move just 1 item at a time
        if (tank.isEmpty() && ItemStack.isSameItemSameComponents(stack, held)) {
          return false;
        }
        // updates fluid on the stack in slot via tank
        TransferResult result = FluidTransferHelper.interactWithStack(tank, held, TransferDirection.AUTO);
        if (result != null) {
          // play sound
          if (player.level().isClientSide) {
            player.playSound(result.getSound());
          }
          // update held item, assuming its actually held
          updateHeldItem(player, held, result.stack());
        }
      }
      return true;
    }
    return false;
  }

  /** Fills a tank stack with the given fluid */
  public static ItemStack fillTank(ItemLike item, Fluid fluid, int capacity) {
    return SimpleFluidContent.set(new ItemStack(item), new FluidStack(fluid, capacity));
  }

  /** Fills a tank stack with the given fluid */
  public static ItemStack fillTank(DeferredBlock<? extends ITankBlock> tank, Fluid fluid) {
    return fillTank(tank, fluid, tank.get().getCapacity());
  }

  /** Fills a tank stack with the given fluid */
  public static ItemStack fillTank(EnumObject<TankType,? extends ItemLike> tank, TankType type, Fluid fluid) {
    return fillTank(tank.get(type), fluid, type.getCapacity());
  }

  /** Adds filled variants of all standard tank items to the given consumer */
  public static void addFilledVariants(Consumer<ItemStack> output) {
    BuiltInRegistries.FLUID.holders().filter(holder -> {
      Fluid fluid = holder.value();
      return fluid.isSource(fluid.defaultFluidState()) && !holder.is(TinkerTags.Fluids.HIDE_IN_CREATIVE_TANKS);
    }).forEachOrdered(holder -> {
      // use an ingot variety for metals
      TankType tank, gauge;
      if (holder.is(TinkerTags.Fluids.METAL_TOOLTIPS)) {
        tank = TankType.INGOT_TANK;
        gauge = TankType.INGOT_GAUGE;
      } else {
        tank = TankType.FUEL_TANK;
        gauge = TankType.FUEL_GAUGE;
      }
      Fluid fluid = holder.value();
      output.accept(fillTank(TinkerSmeltery.searedLantern, fluid));
      output.accept(fillTank(TinkerSmeltery.searedTank, tank, fluid));
      output.accept(fillTank(TinkerSmeltery.searedTank, gauge, fluid));
      output.accept(fillTank(TinkerSmeltery.scorchedLantern, fluid));
      output.accept(fillTank(TinkerSmeltery.scorchedTank, tank, fluid));
      output.accept(fillTank(TinkerSmeltery.scorchedTank, gauge, fluid));
    });
  }
}
