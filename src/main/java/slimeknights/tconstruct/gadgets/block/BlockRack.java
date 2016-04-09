package slimeknights.tconstruct.gadgets.block;

import java.util.List;

import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockLever.EnumOrientation;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import slimeknights.mantle.block.BlockInventory;
import slimeknights.tconstruct.common.PlayerHelper;
import slimeknights.tconstruct.gadgets.TinkerGadgets;
import slimeknights.tconstruct.gadgets.tileentity.TileDryingRack;
import slimeknights.tconstruct.gadgets.tileentity.TileItemRack;
import slimeknights.tconstruct.library.TinkerRegistry;

public class BlockRack extends BlockInventory {

	// pull the facing enums from the lever, since the standard facing does not have quite enough, but the lever's facing is perfect
    public static final PropertyEnum<EnumOrientation> FACING = PropertyEnum.<EnumOrientation>create("facing", EnumOrientation.class);
    public static final PropertyBool DRYING = PropertyBool.create("drying");
    
	public BlockRack() {
		super(Material.wood);
	    this.setSoundType(SoundType.WOOD);
	    this.setCreativeTab(TinkerRegistry.tabGadgets);
	    this.setHardness(2.0F);
	    
	    this.setDefaultState(getBlockState().getBaseState()
	    		.withProperty(FACING, EnumOrientation.NORTH)
	    		.withProperty(DRYING, Boolean.valueOf(false))
	    	);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
	  list.add(new ItemStack(this, 1, 0));
	  list.add(new ItemStack(this, 1, 1));
	}
	
    public int damageDropped(IBlockState state)
    {
    	if (state.getValue(DRYING))
    		return 1;
    	else
    		return 0;
    }
	
	/* Inventory stuffs */
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		if ( getStateFromMeta(meta).getValue(DRYING) )
			return new TileDryingRack();
		else
			return new TileItemRack();
	}

	@Override
	protected boolean openGui(EntityPlayer player, World world, BlockPos pos) {
		return false;
	}
	
    /* Activation */
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        return activateRack(world, pos, player, stack);
    }

    // TODO: this was separate from the former method in the original, does it still need to be?
    boolean activateRack (World world, BlockPos pos, EntityPlayer player, ItemStack stack)
    {
        if (!world.isRemote)
        {
            TileItemRack tile = (TileItemRack) world.getTileEntity(pos);

            // empty rack behavior
            if (!tile.isStackInSlot(0)) {
                if (stack != null)
                {
                    stack = player.inventory.decrStackSize(player.inventory.currentItem, 1);
                    tile.setInventorySlotContents(0, stack);
                }
            
            // filled rack behavior
        	} else {
                ItemStack decrStack = tile.decrStackSize(0, 1);
                if (decrStack != null)
                    PlayerHelper.spawnItemAtPlayer(player, decrStack);
            }

            world.scheduleUpdate(pos, this, 0);
        }
        return true;
    }
	
	/* Block state */
	@Override
	protected BlockStateContainer createBlockState() {
	  return new BlockStateContainer(this, FACING, DRYING);
	}
	
    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
    public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        IBlockState state = this.getDefaultState();
        
        // playing a drying rack instead of an item rack
        if ( ( meta & 8 ) > 0 )
        	state = state.withProperty(DRYING, Boolean.valueOf(true));
        
        IBlockState placedOn = world.getBlockState(pos.offset(facing.getOpposite()));

        // if placing on another item rack or drying rack, use the same orientation to make building easier
        // this is for the sake of making elevated, standalone rows of racks or easier building up on walls
        if (placedOn.getBlock() == TinkerGadgets.rack)
            return state.withProperty(FACING, placedOn.getValue(FACING));
            
        // otherwise place it based on side/player facing
        else
            return state.withProperty(FACING, EnumOrientation.forFacings(facing.getOpposite(), placer.getHorizontalFacing()));
    }
	
	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState()
        	.withProperty(FACING, BlockLever.EnumOrientation.byMetadata(meta & 7))
        	.withProperty(DRYING, Boolean.valueOf((meta & 8) > 0));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        int i = 0;
        i = i | ((EnumOrientation)state.getValue(FACING)).getMetadata();

        if (((Boolean)state.getValue(DRYING)).booleanValue())
        {
            i |= 8;
        }

        return i;
    }
	
	/**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        switch (rot)
        {
            case CLOCKWISE_180:

                switch ((EnumOrientation)state.getValue(FACING))
                {
                    case EAST:
                        return state.withProperty(FACING, BlockLever.EnumOrientation.WEST);
                    case WEST:
                        return state.withProperty(FACING, BlockLever.EnumOrientation.EAST);
                    case SOUTH:
                        return state.withProperty(FACING, BlockLever.EnumOrientation.NORTH);
                    case NORTH:
                        return state.withProperty(FACING, BlockLever.EnumOrientation.SOUTH);
                    default:
                        return state;
                }

            case COUNTERCLOCKWISE_90:

                switch ((EnumOrientation)state.getValue(FACING))
                {
                    case EAST:
                        return state.withProperty(FACING, BlockLever.EnumOrientation.NORTH);
                    case WEST:
                        return state.withProperty(FACING, BlockLever.EnumOrientation.SOUTH);
                    case SOUTH:
                        return state.withProperty(FACING, BlockLever.EnumOrientation.EAST);
                    case NORTH:
                        return state.withProperty(FACING, BlockLever.EnumOrientation.WEST);
                    case UP_Z:
                        return state.withProperty(FACING, BlockLever.EnumOrientation.UP_X);
                    case UP_X:
                        return state.withProperty(FACING, BlockLever.EnumOrientation.UP_Z);
                    case DOWN_X:
                        return state.withProperty(FACING, BlockLever.EnumOrientation.DOWN_Z);
                    case DOWN_Z:
                        return state.withProperty(FACING, BlockLever.EnumOrientation.DOWN_X);
                }

            case CLOCKWISE_90:

                switch ((EnumOrientation)state.getValue(FACING))
                {
                    case EAST:
                        return state.withProperty(FACING, BlockLever.EnumOrientation.SOUTH);
                    case WEST:
                        return state.withProperty(FACING, BlockLever.EnumOrientation.NORTH);
                    case SOUTH:
                        return state.withProperty(FACING, BlockLever.EnumOrientation.WEST);
                    case NORTH:
                        return state.withProperty(FACING, BlockLever.EnumOrientation.EAST);
                    case UP_Z:
                        return state.withProperty(FACING, BlockLever.EnumOrientation.UP_X);
                    case UP_X:
                        return state.withProperty(FACING, BlockLever.EnumOrientation.UP_Z);
                    case DOWN_X:
                        return state.withProperty(FACING, BlockLever.EnumOrientation.DOWN_Z);
                    case DOWN_Z:
                        return state.withProperty(FACING, BlockLever.EnumOrientation.DOWN_X);
                }

            default:
                return state;
        }
    }

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation(((BlockLever.EnumOrientation)state.getValue(FACING)).getFacing()));
    }
    
    /* Bounding boxes */
    
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, World worldIn, BlockPos pos)
    {
        return getBoundingBox(state);
    }
    
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return getBoundingBox(state);
    }
    
    private static AxisAlignedBB getBoundingBox(IBlockState state)
    {
    	float xMin = 0F;
        float yMin = 0F;
        float zMin = 0F;
        float xMax = 1F;
        float yMax = 1F;
        float zMax = 1F;
        switch (state.getValue(FACING)) {
        case DOWN_X:
            xMin = 0.375F;
            yMax = 0.25F;
            xMax = 0.625F;
            break;
        case DOWN_Z:
            zMin = 0.375F;
            yMax = 0.25F;
            zMax = 0.625F;
            break;
        case UP_X:
            xMin = 0.375F;
            yMin = 0.75F;
            xMax = 0.625F;
            break;
        case UP_Z:
            zMin = 0.375F;
            yMin = 0.75F;
            zMax = 0.625F;
            break;
        case EAST:
            xMin = 0.75F;
            yMin = 0.75F;
            break;
        case WEST:
            xMax = 0.25F;
            yMin = 0.75F;
            break;
        case SOUTH:
            zMin = 0.75F;
            yMin = 0.75F;
            break;
        case NORTH:
            zMax = 0.25F;
            yMin = 0.75F;
            break;
        }
        return new AxisAlignedBB(xMin, yMin, zMin, xMax, yMax, zMax);
    }
	
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
      return EnumBlockRenderType.MODEL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
      return true;
    }
    
    @Override
    public boolean isFullCube(IBlockState state) {
      return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
      return false;
    }
}
