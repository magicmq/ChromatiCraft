/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2016
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.Registry;

import java.util.Calendar;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import Reika.DragonAPI.DragonOptions;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public enum ChromaIcons {

	TRANSPARENT("transparent"),
	GUARDIANOUTER("guardian_outer"),
	GUARDIANMIDDLE("guardian_middle"),
	GUARDIANINNER("guardian_inner"),
	SPARKLEPARTICLE("sparkle-particle"),
	SPARKLE("sparkle2"),
	SPARKLE_ROUNDED("roundsparkle"),
	GLOWSECTION("glowsections"),
	RADIATE("plant_glow"),
	FLARE("flare"),
	RINGFLARE("flare4"),
	REPEATER("repeater"),
	MULTIREPEATER("multirepeater"),
	BROADCAST("broadcaster"),
	SPINFLARE("rotating flare_pulse"),
	LASER("laser_2"),
	LASEREND("laser_end_2"),
	BIGFLARE("bigflare"),
	ROUNDFLARE(loadAprilTextures() ? "smileflare" : "roundflare"),
	RINGS("ringrow_fade"),
	RIFT("rift"),
	RIFTHALO("rift_halo"),
	FADE("fade"),
	FADE_GENTLE("fade_gentle"),
	FADE_STAR("fade_star"),
	FADE_RAY("fade_ray"),
	FADE_CLOUD("fade_cloud"),
	CLOUDGROUP("cloudgroup"),
	CLOUDGROUP_TRANS("cloudgroup_trans"),
	CLOUDGROUP_TRANS_BLUR("cloudgroup_trans_blur"),
	TRANSFADE("transfade"),
	CENTER("centerblur3"),
	CHROMA("chroma_particle"),
	NOENTER("noentry"),
	CHECK("check"),
	DIAMOND("diamond"),
	QUESTION("question"),
	BLUEFIRE("bluefire"),
	BATTERY("battery_outer"),
	BLANK("blank"),
	GLOWFRAME("glowframe"),
	GLOWFRAMEDOT("glowframe_dot"),
	GLOWFRAME_TRANS("glowframe2"),
	GLOWFRAMEDOT_TRANS("glowframe_dot2"),
	ALLCOLORS("allcolors"),
	NODE("node"),
	NODE2("node2"),
	TURBO("turbo"),
	TRIDOT("tridot-strip"),
	PURPLESPIN("purplespin"),
	ROSES("roses"),
	ROSES_WHITE("roses_w"),
	BASICFADE("basicfade"),
	BASICFADE_FAST("basicfade_f"),
	FRAME("frame"),
	//PINWHEEL("pinwheel"),
	LATTICE("lattice"),
	REGIONS("regions"),
	CAUSTICS("caustics"),
	CAUSTICS_GENTLE("caustics-g"),
	OVALS("oval-rings"),
	WEAKREPEATER("weakrepeater"),
	SMOKE("smoke"),
	BLACKHOLE("blackhole"),
	BLURFLARE("blurflare2"),
	HOLE("hole"),
	PINWHEEL("pinwheel"),
	HEXFLARE("hexflare"),
	HEXFLARE2("hexflare2"),
	CONCENTRIC("concentric"),
	CONCENTRIC2("concentric2"),
	CONCENTRIC2REV("concentric2rev"),
	FAN("fan"),
	SIDEDFLOW("sidedflow"),
	SIDEDFLOWBI("sidedflow-bi"),
	RING0("ring0"),
	RING1("ring1"),
	RING2("ring2"),
	RING3("ring3"),
	WIDEBAR("widebar"),
	PORTALRING("portalring"),
	CONDENSEFLARE("condenseflare4"),
	;

	private IIcon icon;
	private final String iconName;

	public static final ChromaIcons[] iconList = values();

	private ChromaIcons(String icon) {
		iconName = icon;
	}

	public IIcon getIcon() {
		return icon;
	}

	@SideOnly(Side.CLIENT)
	private void register(IIconRegister ico) {
		String s = this.getIconName();
		icon = ico.registerIcon(s);
	}

	private String getIconName() {
		return "chromaticraft:icons/"+iconName;
	}

	/*
	private void register(IIconRegister ico) {
		String s = this.getIconName();
		//icon = ico.registerIcon(s);
		icon = new HardPathIcon(s).register((TextureMap)ico);
	}

	private String getIconName() {
		return "Reika/ChromatiCraft/Textures/Icons/Animated/"+iconName+".png";
	}
	 */

	private static boolean loadAprilTextures() {
		if (!DragonOptions.APRIL.getState())
			return false;
		Calendar c = Calendar.getInstance();
		return c.get(Calendar.MONTH) == Calendar.APRIL && c.get(Calendar.DAY_OF_MONTH) <= 2;
	}

	private static boolean loadXmasTextures() {
		Calendar c = Calendar.getInstance();
		return c.get(Calendar.MONTH) == Calendar.DECEMBER && c.get(Calendar.DAY_OF_MONTH) >= 23;
	}

	@SideOnly(Side.CLIENT)
	public static void registerAll(TextureMap ico) {
		for (int i = 0; i < iconList.length; i++) {
			ChromaIcons c = iconList[i];
			c.register(ico);
		}
	}

	public boolean allowTextureModification() {
		switch(this) {
			case QUESTION:
			case FRAME:
			case GLOWFRAME:
			case GLOWFRAMEDOT:
			case GLOWFRAME_TRANS:
			case GLOWFRAMEDOT_TRANS:
			case BASICFADE:
			case BASICFADE_FAST:
			case REPEATER:
			case MULTIREPEATER:
			case BROADCAST:
			case WEAKREPEATER:
			case SPARKLEPARTICLE:
				return true;
			default:
				return false;
		}
	}

	public boolean isTransparent() {
		switch(this) {
			case BASICFADE:
			case BASICFADE_FAST:
			case FRAME:
			case BATTERY:
			case REPEATER:
			case MULTIREPEATER:
			case BROADCAST:
			case WEAKREPEATER:
			case SPARKLEPARTICLE:
			case TRANSPARENT:
			case GUARDIANOUTER:
			case GUARDIANMIDDLE:
			case GLOWFRAME_TRANS:
			case GLOWFRAMEDOT_TRANS:
			case PURPLESPIN:
			case TRANSFADE:
			case CHROMA:
			case NOENTER:
			case CHECK:
			case DIAMOND:
			case QUESTION:
			case CLOUDGROUP_TRANS:
			case CLOUDGROUP_TRANS_BLUR:
			case SMOKE:
				return true;
			default:
				return false;
		}
	}

}
