package net.lordofthecraft.itemedit;

import co.lotc.core.bukkit.command.Commands;
import net.lordofthecraft.itemedit.command.MainCommands;
import net.lordofthecraft.itemedit.command.SignType;
import net.lordofthecraft.itemedit.sqlite.TransactionsSQL;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Arrays;

public final class ItemEdit extends JavaPlugin implements Listener {

	public static final String PREFIX = ChatColor.AQUA + "";
	public static final String ALT_COLOR = ChatColor.GOLD + "";
	public static final String PERMISSION_START = "itemedit";
	public static final String BONUS_SIGNATURE_PERM = "signature";

	public static final boolean DEBUGGING = false;

	private static int maxWidth;
	public static int getMaxWidth() {
		return maxWidth;
	}
	private static int maxLines;
	public static int getMaxLines() {
		return maxLines;
	}
	private static int refreshTime;
	public static int getRefreshTime() {
		return refreshTime;
	}

	private static ItemEdit instance;
	public static ItemEdit get() {
		return instance;
	}
	private static TransactionsSQL transaction;
	public static TransactionsSQL getTransaction() {
		return transaction;
	}

	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();

		// Grab the width of each item.
		if (this.getConfig().getInt("MAX-WIDTH") > 10) {
			maxWidth = this.getConfig().getInt("MAX-WIDTH");
		} else {
			maxWidth = 50;
		}

		// Grab the max lines for descriptions.
		if (this.getConfig().getInt("MAX-LINES") > 2) {
			maxLines = this.getConfig().getInt("MAX-LINES");
		} else {
			maxLines = 15;
		}

		// Grab our refresh time for VIP tokens.
		if (this.getConfig().getInt("REFRESH-TIME-IN-DAYS") >= 0) {
			refreshTime = this.getConfig().getInt("REFRESH-TIME-IN-DAYS");
		} else {
			refreshTime = 7;
		}

		// Load the tokens we have.
		loadCurrentTokens();

		// Try to grab all Moniker Tokens
		loadLegacyTokens();

		// Register our auto-complete and glow enchant.
		registerParameters();
		registerGlow();

		// Initiate commands if everything else has been successful.
		Commands.build(getCommand("edit"), () -> new MainCommands(transaction));
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}

	// Auto-populate our SignTypes
	private void registerParameters() {
		Commands.defineArgumentType(SignType.class)
				.defaultName("Type")
				.completer((s,$) -> SignType.getAvailableTypes(s))
				.mapperWithSender((sender, type) -> SignType.typeFromString(type))
				.register();
	}

	// Registers our glow enchantment for /edit glow use.
	private void registerGlow() {
		try {
			Field f = Enchantment.class.getDeclaredField("acceptingNew");
			f.setAccessible(true);
			f.set(null, true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		try {
			NamespacedKey key = new NamespacedKey(this, getDescription().getName());
			// 1.12.2 Glow glow = new Glow(70);
			Glow glow = new Glow(key);
			Enchantment.registerEnchantment(glow);
		}
		catch (IllegalArgumentException ignore) {
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	// Loads the tokens and transactions we have registered for each player.
	// Also deletes any transactions from REFRESH_TIME ago.
	private void loadCurrentTokens() {
		transaction = new TransactionsSQL(this);
		transaction.load();
	}

	// Config Editors
	public void setRefreshTime(int i) {
		refreshTime = i;
		getConfig().set("REFRESH-TIME-IN-DAYS", i);
		saveConfig();
	}

	public void setMaxWidth(int i) {
		maxWidth = i;
		getConfig().set("MAX-WIDTH", i);
		saveConfig();
	}

	// HOOKS //

	// Changes the tokens of a player by a given amount (positive or negative).
	public static boolean changeTokens(int amount, String playerName) {
		Player p = TransactionsSQL.getPlayerByName(playerName);
		boolean output = false;
		if (p != null) {
			output = changeTokens(amount, p);
		}
		return output;
	}
	public static boolean changeTokens(int amount, Player player) {
		int newAmount = (getTokens(player) + amount);
		if (newAmount < 0) {
			newAmount = 0;
		}
		return setTokens(newAmount, player);
	}

	// Sets a player's tokens to a value.
	public static boolean setTokens(int amount, String playerName) {
		Player p = TransactionsSQL.getPlayerByName(playerName);
		boolean output = false;
		if (p != null) {
			output = setTokens(amount, p);
		}
		return output;
	}
	public static boolean setTokens(int amount, Player player) {
		transaction.addEntry(0, player, amount);
		return true;
	}

	// Provides how many tokens a player has.
	public static int getTokens(String playerName) {
		Player p = TransactionsSQL.getPlayerByName(playerName);
		int output = Integer.MIN_VALUE;
		if (p != null) {
			output = getTokens(p);
		}
		return output;
	}
	public static int getTokens(Player p) {
		return transaction.getTokens(p);
	}

	// TODO :: Moniker Legacy Garbage
	// Tries to run through all available profiles in Moniker to grab and store their tokens.
	private void loadLegacyTokens() {
		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			transaction.grabMonikerTokens(player.getPlayer());
		}
		Bukkit.getPluginManager().registerEvents(this, this);
		// TODO :: When you delete the above line, get rid of the impelements listener for this class.
	}

	@EventHandler
	public void checkMonikerTokensOnJoin(PlayerJoinEvent e) {
		transaction.grabMonikerTokens(e.getPlayer());
	}

}
