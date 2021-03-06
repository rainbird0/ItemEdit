package net.lordofthecraft.itemedit.enums;

import co.lotc.core.agnostic.Sender;
import co.lotc.core.util.ColorUtil;
import net.lordofthecraft.itemedit.ItemEdit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public enum Approval {

	// STAFF AND DEFAULT SIGNATURES
	PLUGIN("PLUGIN", ChatColor.DARK_GRAY,                   "A Plugin",       ItemEdit.PERMISSION_START + ".plugin"),
	ADMIN( "ADMIN",  ChatColor.of(Color.decode("#1abc9c")), "A Team Lead",    ItemEdit.PERMISSION_START + ".admin"),
	MOD(   "MOD",    ChatColor.of(Color.decode("#1a67ed")), "A Moderator",    ItemEdit.PERMISSION_START + ".mod"),
	TECH(  "TECH",   ChatColor.of(Color.decode("#95de16")), "A Technician",   ItemEdit.PERMISSION_START + ".tech"),
	LORE(  "LORE",   ChatColor.of(Color.decode("#fae36e")), "A Lore Herald",  ItemEdit.PERMISSION_START + ".lore"),
	EVENT( "EVENT",  ChatColor.of(Color.decode("#cf0606")), "An Event Host",  ItemEdit.PERMISSION_START + ".event"),
	BUILD( "BUILD",  ChatColor.of(Color.decode("#ec9706")), "A Builder",      ItemEdit.PERMISSION_START + ".build"),
	DESIGN("DESIGN", ChatColor.of(Color.decode("#8634b3")), "A Designer",     ItemEdit.PERMISSION_START + ".design"),
	PLAYER("PLAYER", ChatColor.GRAY,                        "A Player",       ItemEdit.PERMISSION_START + ".use");

	// Permissions note: Add .use to use, and .edit to be able to edit over.
	// e.g. Mods having PERM.mod.use, PERM.mod.edit, PERM.lore.edit, PERM.event.edit
	// will make them able to use mod approval only, but able to edit over mod, lore, and event approval.

	// INSTANCE //
	public final String name;
	public final ChatColor color;
	public final String aRank;
	public final String permission;

	Approval(String name, ChatColor color, String aRank, String permission) {
		this.name = name;
		this.color = color;
		this.aRank = aRank;
		this.permission = permission;
	}

	/**
	 * @param player The player to reference in the approval. If null or type PLUGIN, no player is referenced.
	 * @return Returns an approval string of this type for the given player.
	 */
	public String formatApproval(Player player, boolean approval) {
		String output = DGRAY_ITALIC;
		if (approval) {
			output += "Approved By " + this.aRank;
		} else {
			output += "Created By " + this.aRank;
		}
		if (this != PLUGIN && player != null) {
			output += " (" + this.color + ChatColor.ITALIC + player.getName() + DGRAY_ITALIC + ")";
		}
		return output;
	}

	// STATIC //
	private static final String DGRAY_ITALIC = ChatColor.DARK_GRAY + "" + ChatColor.ITALIC;
	public static final Approval DEFAULT = PLAYER;

	/**
	 * @param name The quality name to search for.
	 * @return Returns the Approval object if found for the given name.
	 */
	public static Approval getByName(String name) {
		for (Approval approval : values()) {
			if (approval.name.equalsIgnoreCase(name)) {
				return approval;
			}
		}
		return DEFAULT;
	}

	/**
	 * @param player The player who's permissions we reference.
	 * @return Returns a list of Approval types based on the given player's permissions.
	 */
	public static List<String> getAvailable(Sender player) {
		ArrayList<String> list = new ArrayList<>();
		for (Approval approval : values()) {
			if (player.hasPermission(approval.permission)) {
				list.add(approval.name);
			}
		}
		return list;
	}

}
