package net.madmanmarkau.MultiHome;

import java.util.List;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
*
* @author Sleaker
*
*/
public class HomePermissions {

    private static PermissionsHandler handler;
    private static Permission vault = null;

    private enum PermissionsHandler {

	VAULT, SUPERPERMS, NONE
    }

    public static boolean initialize(JavaPlugin plugin) {
		RegisteredServiceProvider<Permission> vaultPermissionProvider = null;

		try {
			vaultPermissionProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		} catch (NoClassDefFoundError e) {
		} catch (Exception e) {
			// Eat errors
		}

		if (vaultPermissionProvider != null) {
			vault = vaultPermissionProvider.getProvider();
			handler = PermissionsHandler.VAULT;
			Messaging.logInfo("Using Vault for permissions system.", plugin);
			return true;
		} else {
			handler = PermissionsHandler.SUPERPERMS;
			Messaging.logWarning("A permission plugin was not detected! Defaulting to CraftBukkit permissions system.", plugin);
			Messaging.logWarning("Groups disabled. All players defaulting to \"default\" group.", plugin);
			return true;
		}
	}

	public static boolean has(Player player, String permission) {
		boolean blnHasPermission;

		switch (handler) {
			case VAULT:
				blnHasPermission = vault.has(player, permission);
				break;
			case SUPERPERMS:
				blnHasPermission = player.hasPermission(permission);
				break;
			default:
				blnHasPermission = player.isOp();
				break;
		}

		return blnHasPermission;
	}

	public static String getGroup(Player player) {
		if (player != null) {
			switch (handler) {
				case VAULT:
					return vault.getPrimaryGroup(player);
	
				case SUPERPERMS:
					break; // Groups not supported.
			}
		}

		return "default";
	}
}
