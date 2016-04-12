package slimeknights.tconstruct.gadgets.block;

import java.util.List;

import com.google.common.collect.ImmutableMap;

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
    	if (!world.isRemote)
    	{
            ((TileItemRack) world.getTileEntity(pos)).interact(player);
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
        if ( ( meta & 1 ) == 1 )
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
        	.withProperty(FACING, EnumOrientation.byMetadata(meta >> 1))
        	.withProperty(DRYING, Boolean.valueOf((meta & 1) == 1));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        int i = 0;
        i = i | state.getValue(FACING).getMetadata() << 1;

        if (state.getValue(DRYING).booleanValue())
        {
            i |= 1;
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

                switch (state.getValue(FACING))
                {
                    case EAST:
                        return state.withProperty(FACING, EnumOrientation.WEST);
                    case WEST:
                        return state.withProperty(FACING, EnumOrientation.EAST);
                    case SOUTH:
                        return state.withProperty(FACING, EnumOrientation.NORTH);
                    case NORTH:
                        return state.withProperty(FACING, EnumOrientation.SOUTH);
                    default:
                        return state;
                }

            case COUNTERCLOCKWISE_90:

                switch (state.getValue(FACING))
                {
                    case EAST:
                        return state.withProperty(FACING, EnumOrientation.NORTH);
                    case WEST:
                        return state.withProperty(FACING, EnumOrientation.SOUTH);
                    case SOUTH:
                        return state.withProperty(FACING, EnumOrientation.EAST);
                    case NORTH:
                        return state.withProperty(FACING, EnumOrientation.WEST);
                    case UP_Z:
                        return state.withProperty(FACING, EnumOrientation.UP_X);
                    case UP_X:
                        return state.withProperty(FACING, EnumOrientation.UP_Z);
                    case DOWN_X:
                        return state.withProperty(FACING, EnumOrientation.DOWN_Z);
                    case DOWN_Z:
                        return state.withProperty(FACING, EnumOrientation.DOWN_X);
                }

            case CLOCKWISE_90:

                switch (state.getValue(FACING))
                {
                    case EAST:
                        return state.withProperty(FACING, EnumOrientation.SOUTH);
                    case WEST:
                        return state.withProperty(FACING, EnumOrientation.NORTH);
                    case SOUTH:
                        return state.withProperty(FACING, EnumOrientation.WEST);
                    case NORTH:
                        return state.withProperty(FACING, EnumOrientation.EAST);
                    case UP_Z:
                        return state.withProperty(FACING, EnumOrientation.UP_X);
                    case UP_X:
                        return state.withProperty(FACING, EnumOrientation.UP_Z);
                    case DOWN_X:
                        return state.withProperty(FACING, EnumOrientation.DOWN_Z);
                    case DOWN_Z:
                        return state.withProperty(FACING, EnumOrientation.DOWN_X);
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
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING).getFacing()));
    }
    
    /* Bounding boxes */

    
    private static final ImmutableMap<EnumOrientation, AxisAlignedBB> BOUNDS;
    static {
    	ImmutableMap.Builder<EnumOrientation, AxisAlignedBB> builder = ImmutableMap.builder();
    	builder.put(EnumOrientation.DOWN_X, new AxisAlignedBB(0.375, 0,    0,     0.625, 0.25, 1    ));
    	builder.put(EnumOrientation.DOWN_Z, new AxisAlignedBB(0,     0,    0.375, 1,     0.25, 0.625));
    	builder.put(EnumOrientation.UP_X,   new AxisAlignedBB(0.375, 0.75, 0,     0.625, 1,    1    ));
    	builder.put(EnumOrientation.UP_Z,   new AxisAlignedBB(0,     0.75, 0.375, 1,     1,    0.625));
    	builder.put(EnumOrientation.NORTH,  new AxisAlignedBB(0,     0.75, 0,     1,     1,    0.25 ));
    	builder.put(EnumOrientation.SOUTH,  new AxisAlignedBB(0,     0.75, 0.75,  1,     1,    1    ));
    	builder.put(EnumOrientation.EAST,   new AxisAlignedBB(0.75,  0.75, 0,     1,     1,    1    ));
    	builder.put(EnumOrientation.WEST,   new AxisAlignedBB(0,     0.75, 0,     0.25,  1,    1    ));
    	BOUNDS = builder.build();
    }
    
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, World worldIn, BlockPos pos)
    {
        return BOUNDS.get(state.getValue(FACING));
    }
    
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return BOUNDS.get(state.getValue(FACING));
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
