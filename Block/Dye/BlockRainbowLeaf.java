/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2016
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.Block.Dye;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import Reika.ChromatiCraft.ChromatiCraft;
import Reika.ChromatiCraft.Auxiliary.RainbowTreeEffects;
import Reika.ChromatiCraft.Auxiliary.ProgressionManager.ProgressStage;
import Reika.ChromatiCraft.Entity.EntityBallLightning;
import Reika.ChromatiCraft.Registry.ChromaBlocks;
import Reika.ChromatiCraft.Registry.ChromaItems;
import Reika.ChromatiCraft.Registry.ChromaOptions;
import Reika.ChromatiCraft.Registry.CrystalElement;
import Reika.ChromatiCraft.TileEntity.AOE.TileEntityRainbowBeacon;
import Reika.ChromatiCraft.World.BiomeRainbowForest;
import Reika.DragonAPI.Base.BlockCustomLeaf;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockRainbowLeaf extends BlockCustomLeaf {

	private static final boolean TILE = false;

	public BlockRainbowLeaf() {
		super();
	}

	@Override
	public boolean decays() {
		return true;
	}

	@Override
	public boolean showInCreative() {
		return true;
	}

	@Override
	public boolean allowModDecayControl() {
		return false;
	}

	@Override
	public CreativeTabs getCreativeTab() {
		return ChromatiCraft.tabChromaGen;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public final int getRenderColor(int dmg)
	{
		if (dmg == 3) {
			return Color.HSBtoRGB((System.currentTimeMillis()%7200)/7200F, 0.7F, 1F);
		}
		//return Color.HSBtoRGB(((System.currentTimeMillis()/60)%360)/360F, 0.8F, 1);
		World world = Minecraft.getMinecraft().theWorld;
		EntityPlayer ep = Minecraft.getMinecraft().thePlayer;
		int x = MathHelper.floor_double(ep.posX);
		int y = MathHelper.floor_double(ep.posY+ep.getEyeHeight());
		int z = MathHelper.floor_double(ep.posZ);
		return this.colorMultiplier(world, x, y, z);
	}

	@Override
	public final int colorMultiplier(IBlockAccess iba, int x, int y, int z)
	{
		int sc = 32;
		int meta = iba.getBlockMetadata(x, y, z);
		float hue = meta == 3 ? (System.currentTimeMillis()%7200)/7200F : (float)(ReikaMathLibrary.py3d(x, y*3, z+x)%sc)/sc;
		boolean dmgd = BiomeRainbowForest.isDamaged(iba, x, z);
		return Color.HSBtoRGB(hue, dmgd ? 0.4F : 0.7F, dmgd ? 0.6F : 1F);
	}

	@Override
	public final Item getItemDropped(int id, Random r, int fortune)
	{
		return Item.getItemFromBlock(ChromaBlocks.RAINBOWSAPLING.getBlockInstance());
	}

	@Override
	public int damageDropped(int dmg)
	{
		return 0;
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
		if (willHarvest)
			ProgressStage.RAINBOWLEAF.stepPlayerTo(player);
		return super.removedByPlayer(world, player, x, y, z, willHarvest);
	}

	@Override
	public final ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
		ArrayList<ItemStack> li = new ArrayList();
		float saplingChance = 0.0125F;
		float appleChance = 0.1F;
		float goldAppleChance = 0.025F;
		float rareGoldAppleChance = ChromaOptions.getRainbowLeafGoldAppleDropChance(); //0.0025F default

		saplingChance *= (1+fortune);
		appleChance *= (1+fortune*5);
		goldAppleChance *= (1+fortune*3);
		rareGoldAppleChance *= (1+fortune*3);

		if (ReikaRandomHelper.doWithChance(saplingChance))
			li.add(new ItemStack(ChromaBlocks.RAINBOWSAPLING.getBlockInstance(), 1, meta));
		if (ReikaRandomHelper.doWithChance(appleChance))
			li.add(new ItemStack(Items.apple, 1, 0));
		if (ReikaRandomHelper.doWithChance(goldAppleChance))
			li.add(new ItemStack(Items.golden_apple, 1, 0));
		if (ReikaRandomHelper.doWithChance(rareGoldAppleChance))
			li.add(new ItemStack(Items.golden_apple, 1, 1));
		li.addAll(this.getDyes(world, x, y, z, fortune));
		return li;
	}

	@Override
	public final void dropBlockAsItemWithChance(World world, int x, int y, int z, int metadata, float chance, int fortune)
	{
		if (!world.isRemote) {
			ArrayList<ItemStack> li = this.getDrops(world, x, y, z, metadata, fortune);
			for (int i = 0; i < li.size(); i++) {
				if (chance >= 1 || ReikaRandomHelper.doWithChance(chance))
					this.dropBlockAsItem(world, x, y, z, li.get(i));
			}
		}
	}

	private final ArrayList<ItemStack> getDyes(World world, int x, int y, int z, int fortune) {
		int drop = this.getDyeDropCount(fortune);
		ArrayList<ItemStack> li = new ArrayList();
		for (int i = 0; i < drop; i++) {
			if (ReikaRandomHelper.doWithChance(ChromaOptions.DYEFRAC.getValue())) {
				li.add(new ItemStack(Items.dye, 1, rand.nextInt(16)));
			}
			else {
				li.add(new ItemStack(ChromaItems.DYE.getItemInstance(), 1, rand.nextInt(16)));
			}
		}
		return li;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block b, int meta) {
		super.breakBlock(world, x, y, z, b, meta);

		if (rand.nextInt(10) == 0) {
			if (!world.isRemote && ChromaOptions.BALLLIGHTNING.getState())
				world.spawnEntityInWorld(new EntityBallLightning(world, CrystalElement.randomElement(), x+0.5, y+0.5, z+0.5).setNoDrops());
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase e, ItemStack is) {
		if (e instanceof EntityPlayer)
			world.setBlockMetadataWithNotify(x, y, z, 1, 3);
	}

	private int getDyeDropCount(int fortune) {
		return 1+rand.nextInt(3*(1+fortune))+fortune+rand.nextInt(1+fortune*fortune);
	}

	@Override
	public final ArrayList<ItemStack> onSheared(ItemStack item, IBlockAccess world, int x, int y, int z, int fortune)
	{
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		ret.add(new ItemStack(ChromaBlocks.RAINBOWLEAF.getBlockInstance()));
		return ret;
	}

	@Override
	protected final ItemStack createStackedBlock(int par1)
	{
		return new ItemStack(ChromaBlocks.RAINBOWLEAF.getBlockInstance());
	}

	@Override
	public final void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		int color = this.colorMultiplier(world, x, y, z);
		Color c = new Color(color);
		float r = c.getRed()/255F;
		float g = c.getGreen()/255F;
		float b = c.getBlue()/255F;
		world.spawnParticle("reddust", x+rand.nextDouble(), y, z+rand.nextDouble(), r, g, b);
	}

	@Override
	public final Item getItem(World par1World, int par2, int par3, int par4)
	{
		return Item.getItemFromBlock(ChromaBlocks.RAINBOWLEAF.getBlockInstance());
	}

	@Override
	public String getFastGraphicsIcon(int meta) {
		return "ChromatiCraft:dye/leaves_opaque";
	}

	@Override
	public String getFancyGraphicsIcon(int meta) {
		return "ChromatiCraft:dye/leaves";
	}

	@Override
	public boolean shouldTryDecay(World world, int x, int y, int z, int meta) {
		return meta%2 == 0;
	}

	@Override
	public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face)
	{
		return 90;
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		if (world instanceof World)
			RainbowTreeEffects.addInstability((World)world, x, y, z); //make burning the tree add the instability back
		return 180;
	}

	@Override
	public void onBlockDestroyedByExplosion(World world, int x, int y, int z, Explosion e) {
		RainbowTreeEffects.addInstability(world, x, y, z); //make exploding the tree add the instability back
	}

	@Override
	public boolean hasTileEntity(int meta) {
		return TILE ? meta == 2 || meta == 3 : false;
	}

	@Override
	public TileEntity createTileEntity(World world, int meta) {
		if (TILE) {
			if (meta == 2 || meta == 3) {
				return new TileEntityRainbowBeacon();
			}
		}
		return null;
	}

	@Override
	protected void onRandomUpdate(World world, int x, int y, int z, Random r) {/*
		if (r.nextInt(20) == 0)
			this.dropDye(world, x, y, z, 0);

		if (!world.isRemote && r.nextInt(400) == 0 && ChromaOptions.RAINBOWSPREAD.getState()) {
			int rx = ReikaRandomHelper.getRandomPlusMinus(x, 32);
			int rz = ReikaRandomHelper.getRandomPlusMinus(z, 32);
			ReikaWorldHelper.setBiomeForXZ(world, rx, rz, ChromatiCraft.forest);
			ReikaJavaLibrary.pConsole(rx+", "+rz);
			for (int i = 0; i < 256; i++) {
				ReikaWorldHelper.temperatureEnvironment(world, rx, i, rz, ReikaWorldHelper.getBiomeTemp(ChromatiCraft.forest));
				world.func_147479_m(rx, i, rz);
				world.markBlockForUpdate(rx, i, rz);
			}
		}*/

		RainbowTreeEffects.doRainbowTreeEffects(world, x, y, z, 1, 1, r, true);
	}

	@Override
	public boolean shouldRandomTick() {
		return true;
	}


}
