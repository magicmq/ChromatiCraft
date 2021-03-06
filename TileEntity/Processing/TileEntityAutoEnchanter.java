/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2016
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.TileEntity.Processing;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import Reika.ChromatiCraft.API.Interfaces.EnchantableItem;
import Reika.ChromatiCraft.Auxiliary.Interfaces.ChromaPowered;
import Reika.ChromatiCraft.Auxiliary.Interfaces.OperationInterval;
import Reika.ChromatiCraft.Base.TileEntity.FluidReceiverInventoryBase;
import Reika.ChromatiCraft.Registry.ChromaEnchants;
import Reika.ChromatiCraft.Registry.ChromaTiles;
import Reika.DragonAPI.Instantiable.StepTimer;
import Reika.DragonAPI.Libraries.ReikaEnchantmentHelper;

public class TileEntityAutoEnchanter extends FluidReceiverInventoryBase implements ChromaPowered, OperationInterval {

	private HashMap<Enchantment, Integer> selected = new HashMap();

	public static final int CHROMA_PER_LEVEL_BASE = 500;
	private static final HashMap<Enchantment, EnchantmentTier> tiers = new HashMap();
	private static final HashMap<Enchantment, Integer> boostedLevels = new HashMap();

	static {
		tiers.put(Enchantment.baneOfArthropods, EnchantmentTier.WORTHLESS);
		tiers.put(Enchantment.smite, EnchantmentTier.WORTHLESS);

		tiers.put(Enchantment.knockback, EnchantmentTier.BASIC);
		tiers.put(Enchantment.punch, EnchantmentTier.BASIC);
		tiers.put(Enchantment.field_151369_A, EnchantmentTier.BASIC);
		tiers.put(ChromaEnchants.FASTSINK.getEnchantment(), EnchantmentTier.BASIC);

		tiers.put(Enchantment.fortune, EnchantmentTier.VALUABLE);
		tiers.put(Enchantment.sharpness, EnchantmentTier.VALUABLE);
		tiers.put(Enchantment.looting, EnchantmentTier.VALUABLE);
		tiers.put(Enchantment.power, EnchantmentTier.VALUABLE);
		tiers.put(Enchantment.protection, EnchantmentTier.VALUABLE);
		tiers.put(ChromaEnchants.USEREPAIR.getEnchantment(), EnchantmentTier.VALUABLE);
		tiers.put(ChromaEnchants.ENDERLOCK.getEnchantment(), EnchantmentTier.VALUABLE);
		tiers.put(ChromaEnchants.AGGROMASK.getEnchantment(), EnchantmentTier.VALUABLE);

		tiers.put(Enchantment.silkTouch, EnchantmentTier.RARE);
		tiers.put(Enchantment.infinity, EnchantmentTier.RARE);
		tiers.put(ChromaEnchants.RARELOOT.getEnchantment(), EnchantmentTier.RARE);
		tiers.put(ChromaEnchants.WEAPONAOE.getEnchantment(), EnchantmentTier.RARE);
		tiers.put(ChromaEnchants.HARVESTLEVEL.getEnchantment(), EnchantmentTier.RARE);

		boostedLevels.put(Enchantment.fortune, 5);
		boostedLevels.put(Enchantment.looting, 5);
		boostedLevels.put(Enchantment.respiration, 5);
		boostedLevels.put(Enchantment.field_151370_z, 5); //luck of sea
		boostedLevels.put(Enchantment.power, 10);
		boostedLevels.put(Enchantment.sharpness, 10);
	}

	private StepTimer progress = new StepTimer(40);
	public int progressTimer;

	public static Map<Enchantment, Integer> getBoostedLevels() {
		return Collections.unmodifiableMap(boostedLevels);
	}

	@Override
	public ChromaTiles getTile() {
		return ChromaTiles.ENCHANTER;
	}

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {

		if (this.canProgress()) {
			progress.update();
			if (progress.checkCap()) {
				if (!world.isRemote)
					this.applyEnchants();
			}
		}
		else {
			progress.reset();
		}
		progressTimer = progress.getTick();
	}

	public int getProgressScaled(int a) {
		return a * progressTimer / progress.getCap();
	}

	private boolean canProgress() {
		return this.isValid(inv[0]) && this.hasSufficientChroma() && this.enchanting();
	}

	private boolean hasSufficientChroma() {
		return this.getChroma() >= this.getConsumedChroma();
	}

	private boolean enchanting() {
		if (selected.isEmpty())
			return false;
		for (Enchantment e : selected.keySet()) {
			int level = selected.get(e);
			if (level > 0)
				return true;
		}
		return false;
	}

	public int getChroma() {
		return tank.getLevel();
	}

	public boolean addChroma(int amt) {
		if (tank.canTakeIn(amt)) {
			tank.addLiquid(amt, FluidRegistry.getFluid("chroma"));
			return true;
		}
		return false;
	}

	private boolean isValid(ItemStack is) {
		return is != null && this.isItemEnchantable(is) && this.areEnchantsValid(is);
	}

	private boolean isItemEnchantable(ItemStack is) {
		if (is.getItem() == Items.book)
			return true;
		if (is.getItem() instanceof EnchantableItem)
			return true;
		if (is.getItem() instanceof ItemShears)
			return true;
		return is.getItem().getItemEnchantability(is) > 0;
	}

	private boolean areEnchantsValid(ItemStack is) {
		Item i = is.getItem();
		for (Enchantment e : selected.keySet()) {
			if (i == Items.book) {
				if (!e.isAllowedOnBooks()) {
					return false;
				}
			}
			else if (!e.canApply(is) && !(i instanceof EnchantableItem && ((EnchantableItem)i).isEnchantValid(e, is))) {
				return false;
			}

			if (ReikaEnchantmentHelper.getEnchantmentLevel(e, is) >= selected.get(e))
				return false;
		}
		return true;
	}

	private void applyEnchants() {
		if (inv[0].getItem() == Items.book)
			inv[0] = new ItemStack(Items.enchanted_book);
		ReikaEnchantmentHelper.removeEnchantments(inv[0], selected.keySet());
		ReikaEnchantmentHelper.applyEnchantments(inv[0], selected);
		tank.removeLiquid(this.getConsumedChroma());
		this.syncAllData(true);
	}

	private int getConsumedChroma() {
		int total = 0;
		for (Enchantment e : selected.keySet()) {
			int level = selected.get(e);
			total += level*CHROMA_PER_LEVEL_BASE*this.getCostFactor(e);
		}
		return total;
	}

	private float getCostFactor(Enchantment e) {
		EnchantmentTier t = tiers.get(e);
		if (t == null)
			t = EnchantmentTier.NORMAL;
		return t.costFactor;
	}

	public boolean setEnchantment(Enchantment e, int level) {
		level = Math.min(this.getMaxEnchantmentLevel(e), level);
		if (level <= 0) {
			this.removeEnchantment(e);
			return true;
		}
		else {
			if (this.getEnchantment(e) == 0) {
				if (!ReikaEnchantmentHelper.isCompatible(selected.keySet(), e)) {
					return false;
				}
			}
			selected.put(e, level);
			return true;
		}
	}

	public int getMaxEnchantmentLevel(Enchantment e) {
		if (e == Enchantment.fortune)
			return 5;
		if (e == Enchantment.looting)
			return 5;
		if (e == Enchantment.respiration)
			return 5;
		if (e == Enchantment.field_151370_z) //luck of sea
			return 5;
		if (e == Enchantment.power)
			return 10;
		if (e == Enchantment.sharpness)
			return 10;
		return e.getMaxLevel();
	}

	public void removeEnchantment(Enchantment e) {
		selected.remove(e);
	}

	public boolean incrementEnchantment(Enchantment e) {
		int level = this.getEnchantment(e);
		return this.setEnchantment(e, level+1);
	}

	public void decrementEnchantment(Enchantment e) {
		int level = this.getEnchantment(e);
		int newlevel = Math.max(level-1, 0);
		this.setEnchantment(e, newlevel);
	}

	public void clearEnchantments() {
		selected.clear();
	}

	public int getEnchantment(Enchantment e) {
		return selected.containsKey(e) ? selected.get(e) : 0;
	}

	public Map<Enchantment, Integer> getEnchantments() {
		return Collections.unmodifiableMap(selected);
	}

	@Override
	protected void animateWithTick(World world, int x, int y, int z) {

	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return this.isValid(itemstack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
		return ReikaEnchantmentHelper.hasEnchantments(itemstack);
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	protected void writeSyncTag(NBTTagCompound NBT)
	{
		super.writeSyncTag(NBT);

		for (int i = 0; i < Enchantment.enchantmentsList.length; i++) {
			if (Enchantment.enchantmentsList[i] != null) {
				int lvl = this.getEnchantment(Enchantment.enchantmentsList[i]);
				NBT.setInteger(Enchantment.enchantmentsList[i].getName(), lvl);
			}
		}
	}

	@Override
	protected void readSyncTag(NBTTagCompound NBT)
	{
		super.readSyncTag(NBT);

		selected = new HashMap();
		for (int i = 0; i < Enchantment.enchantmentsList.length; i++) {
			if (Enchantment.enchantmentsList[i] != null) {
				int lvl = NBT.getInteger(Enchantment.enchantmentsList[i].getName());
				if (lvl > 0)
					selected.put(Enchantment.enchantmentsList[i], lvl);
			}
		}
	}

	@Override
	public int getCapacity() {
		return 12000;//6000;
	}

	@Override
	public Fluid getInputFluid() {
		return FluidRegistry.getFluid("chroma");
	}

	@Override
	public boolean canReceiveFrom(ForgeDirection from) {
		return true;
	}

	@Override
	public float getOperationFraction() {
		return !this.canProgress() ? 0 : progress.getFraction();
	}

	@Override
	public OperationState getState() {
		return this.isValid(inv[0]) && this.enchanting() ? (this.hasSufficientChroma() ? OperationState.RUNNING : OperationState.PENDING) : OperationState.INVALID;
	}

	private static enum EnchantmentTier {
		WORTHLESS(0.25F),
		BASIC(0.75F),
		NORMAL(1F),
		VALUABLE(1.5F),
		RARE(2);

		public final float costFactor;

		private EnchantmentTier(float f) {
			costFactor = f;
		}
	}

}
