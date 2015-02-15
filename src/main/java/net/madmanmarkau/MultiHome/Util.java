package net.madmanmarkau.MultiHome;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.madmanmarkau.MultiHome.Data.HomeEntry;
import net.madmanmarkau.MultiHome.Data.InviteEntry;
import org.bukkit.Bukkit;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Util {
	public static String newLine() {
		return System.getProperty("line.separator");
	}

	public static String joinString(String string[], int startLocation, String delimiter) {
		StringBuilder builder = new StringBuilder();
		
		for (int index = startLocation; index < string.length; index++) {
			builder.append(string[index]);
			
			if (index < string.length - 1) {
				builder.append(delimiter);
			}
		}

		return builder.toString();
	}

	public static String joinString(String string[], int startLocation, int endLocation, String delimiter) {
		StringBuilder builder = new StringBuilder();
		
		for (int index = startLocation; index < string.length && index <= endLocation; index++) {
			builder.append(string[index]);
			
			if (index < string.length - 1) {
				builder.append(delimiter);
			}
		}

		return builder.toString();
	}
	
	/**
	 * Searches for a player using a partial search string. Returns null if no players found, or multiple matches found.
	 * @param username Username to search for.
	 * @param plugin Plugin searching for player.
	 * @return Reference to player object, or null.
	 */
	public static Player getPlayer(String username, JavaPlugin plugin) {
		List<Player> players = plugin.getServer().matchPlayer(username);
		
		if (players.size() == 1) return players.get(0);
		
		return null;
	}
	
	/**
	 * Searches for a player using an exact search string. Returns null if no player found.
	 * @param username Username to search for.
	 * @param plugin Plugin searching for player.
	 * @return Reference to player object, or null.
	 */
	public static Player getExactPlayer(String username, JavaPlugin plugin) {
		List<Player> players = plugin.getServer().matchPlayer(username);
		
		if (players.size() == 1) {
			if (players.get(0).getName().compareToIgnoreCase(username) == 0) {
				return players.get(0);
			}
		}
		
		return null;
	}
	
	/**
	 * Gets the full name of a player given a partial search string. Returns null if no players found, or multiple matches found. 
	 * @param username Username to search for.
	 * @param plugin Plugin searching for player.
	 * @return Player's name, or null.
	 */
	public static String expandUsername(String username, JavaPlugin plugin) {
		List<Player> players = plugin.getServer().matchPlayer(username);
		
		if (players.size() == 1) return players.get(0).getName();
		
		return null;
	}

	/**
	 * Faith-based teleporting.  Second chunk send removed based on feedback that issue has
	 * been fixed in 1.2.
	 */
	public static void teleportPlayer(Player player, Location location, JavaPlugin plugin) {
            if (player.isInsideVehicle()) {
                Entity vehicle = player.getVehicle();                    
                if(Settings.isMountTeleportEnabled() &&
                        Settings.getTeleportableMounts(player).contains(vehicle.getType())) {
                    if (!location.getWorld().equals(player.getWorld()) &&
                            !Settings.isMountTeleportWorldEnabled()) {
                        Settings.sendMessageCannotTeleportWithMount(player);
                        return;
                    }

                    teleportPlayerWithVehicle(player, vehicle, location, plugin);
                    return;
                }
                
                player.leaveVehicle(); // leave vehicle before teleporting
            }
            player.teleport(location);
	}
        
        public static void teleportPlayerWithVehicle(final Player player, final Entity vehicle, final Location location, JavaPlugin plugin) {
            player.leaveVehicle(); // eject player from any vehicle to prevent weirdness
            player.teleport(location); // teleport player
           
            vehicle.teleport(player); // teleport vehicle to player

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    vehicle.setPassenger(player);
                }
            }, 5L);
        }
        
	public static Date dateInFuture(int seconds) {
		Date now = new Date();
		
		return new Date(now.getTime() + seconds * 1000);
	}
	
	public static String compileHomeList(ArrayList<HomeEntry> homes) {
		String userResponse = "";
		for (HomeEntry thisLocation : homes) {
			if (thisLocation.getHomeName().length() == 0) {
				userResponse = userResponse + ", [Default]";
			} else {
				userResponse = userResponse + ", " + thisLocation.getHomeName();
			}
		}
		if (!userResponse.isEmpty() && userResponse.length() > 2) {
			return userResponse.substring(2);
		}
		return "";
	}

	public static String compileInviteListForMe(String requestingPlayer, ArrayList<InviteEntry> invites) {
		String userResponse = "";
		for (InviteEntry thisInvite : invites) {
			if (thisInvite.getInviteSource().compareToIgnoreCase(requestingPlayer) != 0) {
				if (thisInvite.getInviteHome().length() == 0) {
					userResponse = userResponse + ", " + thisInvite.getInviteSource() + ":[Default]";
					if (thisInvite.getInviteExpires() != null) {
						long expiry = (new Date()).getTime() - thisInvite.getInviteExpires().getTime();
						
						DateFormat df = new SimpleDateFormat("HH:mm:ss");
						String expireCountdown = df.format(new Date(expiry));
						
						userResponse += "(" + expireCountdown + ")";
					}
				} else {
					userResponse = userResponse + ", " + thisInvite.getInviteSource() + ":" + thisInvite.getInviteHome();
					if (thisInvite.getInviteExpires() != null) {
						long expiry = (new Date()).getTime() - thisInvite.getInviteExpires().getTime();
						
						DateFormat df = new SimpleDateFormat("HH:mm:ss");
						String expireCountdown = df.format(new Date(expiry));
						
						userResponse += "(" + expireCountdown + ")";
					}
				}
			}
		}
		if (!userResponse.isEmpty() && userResponse.length() > 2) {
			return userResponse.substring(2);
		}
		return "";
	}

	public static String compileInviteListForOthers(ArrayList<InviteEntry> invites) {
		String userResponse = "";
		for (InviteEntry thisInvite : invites) {
			if (thisInvite.getInviteHome().length() == 0) {
				userResponse += ", " + thisInvite.getInviteTarget() + "->[Default]";
				if (thisInvite.getInviteExpires() != null) {
					long expiry = (new Date()).getTime() - thisInvite.getInviteExpires().getTime();
					
					DateFormat df = new SimpleDateFormat("HH:mm:ss");
					String expireCountdown = df.format(new Date(expiry));
					
					userResponse += "(" + expireCountdown + ")";
				}
			} else {
				userResponse = userResponse + ", " + thisInvite.getInviteTarget() + "->" + thisInvite.getInviteHome();
				if (thisInvite.getInviteExpires() != null) {
					long expiry = (new Date()).getTime() - thisInvite.getInviteExpires().getTime();
					
					DateFormat df = new SimpleDateFormat("HH:mm:ss");
					String expireCountdown = df.format(new Date(expiry));
					
					userResponse += "(" + expireCountdown + ")";
				}
			}
		}
		if (!userResponse.isEmpty() && userResponse.length() > 2) {
			return userResponse.substring(2);
		}
		return "";
	}

	public static int decodeTime(String time) {
		// Parse integer seconds
		try {
			int silenceTime = Integer.parseInt(time);

			if (silenceTime >= 0) return silenceTime;
		} catch (Exception e) {}

		// Parse 1d2h3m4s format
		try {
			int dayIndex = time.indexOf("d");
			int hourIndex = time.indexOf("h");
			int minuteIndex = time.indexOf("m");
			int secondIndex = time.indexOf("s");
			int lastIndex = 0;

			if (dayIndex > -1 || hourIndex > -1 || minuteIndex > -1 || secondIndex > -1) {
				int timeInSeconds = 0;
				
				if (dayIndex > -1) {
					timeInSeconds += Integer.parseInt(time.substring(lastIndex, dayIndex)) * 60 * 60 * 24;
					lastIndex = dayIndex + 1;
				}
				
				if (hourIndex > -1) {
					timeInSeconds += Integer.parseInt(time.substring(lastIndex, hourIndex)) * 60 * 60;
					lastIndex = hourIndex + 1;
				}
				
				if (minuteIndex > -1) {
					timeInSeconds += Integer.parseInt(time.substring(lastIndex, minuteIndex)) * 60;
					lastIndex = minuteIndex + 1;
				}
				
				if (secondIndex > -1) {
					timeInSeconds += Integer.parseInt(time.substring(lastIndex, secondIndex));
					lastIndex = secondIndex + 1;
				}
	
				if (timeInSeconds >= 0) return timeInSeconds;
			}
		} catch (Exception e) {}

		// Parse 1:02:03:04 format
		try {
			int timeInSeconds = 0;
			int lastIndex = 0;

			int thisIndex;
			
			thisIndex = time.indexOf(":", lastIndex);

			if (thisIndex > -1) {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex, thisIndex));
				lastIndex = thisIndex + 1;
			} else {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex));
				if (timeInSeconds >= 0) return timeInSeconds;
			}

			thisIndex = time.indexOf(":", lastIndex);
			timeInSeconds *= 60;
			
			if (thisIndex > -1) {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex, thisIndex));
				lastIndex = thisIndex + 1;
			} else {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex));
				if (timeInSeconds >= 0) return timeInSeconds;
			}

			thisIndex = time.indexOf(":", lastIndex);
			timeInSeconds *= 60;
			
			if (thisIndex > -1) {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex, thisIndex));
				lastIndex = thisIndex + 1;
			} else {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex));
				if (timeInSeconds >= 0) return timeInSeconds;
			}

			thisIndex = time.indexOf(":", lastIndex);
			timeInSeconds *= 24;
			
			if (thisIndex > -1) {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex, thisIndex));
				lastIndex = thisIndex + 1;
			} else {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex));
				if (timeInSeconds >= 0) return timeInSeconds;
			}
			
		} catch (Exception e) {}

		return -1;
	}

	public static String[] splitHome(String parameter) {
		if (parameter.contains(":")) {
			String args[] = parameter.split("\\:");
			String result[] = new String[2];
			
			if (args.length >= 1) {
				result[0] = args[0];
			} else {
				result[0] = "";
			}
			if (args.length >= 2) {
				result[1] = args[1];
			} else {
				result[1] = "";
			}
			
			return result;
		} else {
			String result[] = new String[1];
			
			result[0] = parameter;
			
			return result;
		}

	}
}
