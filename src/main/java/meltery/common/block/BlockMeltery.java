package meltery.common.block;

import meltery.Ported;
import meltery.common.MelteryHandler;
import meltery.common.MelteryRecipe;
import meltery.common.tile.TileMeltery;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by tyler on 6/1/17.
 */

@SuppressWarnings("deprecation")
public class BlockMeltery extends BlockDirectional {
	public static final DamageSource BURN = new DamageSource("meltery.burn");
	public static final PropertyBool ENABLED = PropertyBool.create("enabled");
	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	protected static final AxisAlignedBB AABB_LEGS = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 2 / 16D, 1.0D);
	protected static final AxisAlignedBB AABB_WALL_NORTH = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1 / 16D);
	protected static final AxisAlignedBB AABB_WALL_SOUTH = new AxisAlignedBB(0.0D, 0.0D, 15 / 16D, 1.0D, 1.0D, 1.0D);
	protected static final AxisAlignedBB AABB_WALL_EAST = new AxisAlignedBB(15 / 16D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
	protected static final AxisAlignedBB AABB_WALL_WEST = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1 / 16D, 1.0D, 1.0D);

	public BlockMeltery() {
		super(Material.ROCK);
		setCreativeTab(CreativeTabs.MISC);
		setRegistryName("meltery");
		setHardness(1.5F);
		setResistance(10.0F);
		setHarvestLevel("pickaxe", 1);
	}


//    @Override
//    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_) {
//        addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 4 / 16D, 1.0D));
//        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_WALL_WEST);
//        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_WALL_NORTH);
//        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_WALL_EAST);
//        addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_WALL_SOUTH);
//    }

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
		tooltip.add(I18n.format("tooltip.meltry"));
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileMeltery();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, ENABLED);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex();

	}

//    @Override
//    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
//        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(FACING, placer.getHorizontalFacing());
//    }


	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (!worldIn.isRemote && tileEntity instanceof TileMeltery) {
			((TileMeltery) tileEntity).onBreak();
			worldIn.updateComparatorOutputLevel(pos, this);
		}
		super.breakBlock(worldIn, pos, state);
	}


	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null) {
			return super.getActualState(state, worldIn, pos);
		}
		TileMeltery meltery = (TileMeltery) te;
		return super.getActualState(state, worldIn, pos).withProperty(ENABLED, meltery.isRunning());
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TileMeltery tile = (TileMeltery) worldIn.getTileEntity(pos);
		if (tile == null || !tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing) || !tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) {
			return false;
		}

		TileMeltery.SimpleStackHandler itemHandler = (TileMeltery.SimpleStackHandler) tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);

		if (itemHandler.canInput(heldItem)) {
			ItemStack insert = heldItem.copy();
			insert.stackSize=1;
			boolean isEmpty = Ported.isEmpty(itemHandler.getStackInSlot(0));
			ItemStack result = ItemHandlerHelper.insertItem(itemHandler, insert, false);
			if (result == null || result.stackSize < heldItem.stackSize) {
				worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0f, 1.0f);
				heldItem.stackSize--;
				if (isEmpty)
					tile.setProgress(0);
				return true;
			}
		} else if (playerIn.isSneaking() && hand == EnumHand.MAIN_HAND) {
			ItemStack stack = itemHandler.getStackInSlot(0);
			if (!Ported.isEmpty(stack)) {
				if (stack.stackSize == 1)
					tile.setProgress(0);
				ItemStack copy = stack.copy();
				copy.stackSize = 1;
				ItemHandlerHelper.giveItemToPlayer(playerIn, copy);
				stack.stackSize -= 1;
				playerIn.swingArm(EnumHand.MAIN_HAND);
			}
			return false;
		}
		IFluidHandler fluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing);

		boolean result = FluidUtil.interactWithFluidHandler(heldItem, fluidHandler, playerIn);
		// prevent interaction so stuff like buckets and other things don't place the liquid block
		return result || FluidUtil.getFluidHandler(heldItem) != null;
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state.getValue(ENABLED) ? 15 : 0;
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		TileMeltery tile = (TileMeltery) worldIn.getTileEntity(pos);
		assert tile != null;
		if (entityIn instanceof EntityItem) {
			TileMeltery.SimpleStackHandler itemHandler = (TileMeltery.SimpleStackHandler) tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
			ItemStack stack = ((EntityItem) entityIn).getEntityItem();
			MelteryRecipe recipe = MelteryHandler.getMelteryRecipe(stack);

			if (itemHandler.canInput(stack) && recipe != null && (tile.getInternalTank().getFluid() == null || recipe.output.isFluidEqual(tile.getInternalTank().getFluid()))) {
				boolean isEmpty = Ported.isEmpty(itemHandler.getStackInSlot(0));
				ItemStack result = ItemHandlerHelper.insertItem(itemHandler, stack, false);
				if (Ported.isEmpty(result)) {
					entityIn.setDead();
				} else {
					((EntityItem) entityIn).setEntityItemStack(result);
				}
				worldIn.playSound(null, pos, SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, SoundCategory.BLOCKS, 1.0f, 1.0f);
				if (isEmpty) {
					tile.setProgress(0);
				}
			}
		} else {
			FluidStack fluid = tile.getInternalTank().getFluid();
			if (fluid != null && entityIn instanceof EntityLivingBase) {
				if (fluid.getFluid().getTemperature() > 350) {
					entityIn.attackEntityFrom(BURN, 2);
				}
			}
		}


	}
}
