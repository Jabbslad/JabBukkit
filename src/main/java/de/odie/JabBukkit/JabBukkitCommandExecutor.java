package de.odie.JabBukkit;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JabBukkitCommandExecutor implements CommandExecutor {
	
	private JabBukkit plugin;
	
	public JabBukkitCommandExecutor(JabBukkit plugin) {
		this.plugin = plugin;
	}

	public JabBukkitCommandExecutor getCommandListener() {
		return this;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){

		Player player = null;
		if(sender instanceof Player) {
			player = (Player)sender;
		}
		
		if(cmd.getName().equalsIgnoreCase("jb")){
			if(player != null) {
				if (!player.hasPermission("jabbukkit.user")) {
				return false;
				}
			}
			switch(args[0]) {
			case "pm":
				execSendPrivateMessage(args);
				return true;
			case "users":
				if(player.hasPermission("jabbukkit.userlist"))
					execPrintUsers(sender);
				return true;
			}
			return false;
		}
		if(cmd.getName().equalsIgnoreCase("jbadmin")){
			if(player != null) {
				if (!player.hasPermission("jabbukkit.admin")) {
				return false;
				}
			}
			switch(args[0]) {
			case "adduser":
				execAddUser(args);
				return true;
			case "reconnect":
				execReconnect();
				return true;
			case "reload":
				execReload();
				return true;
			case "setstatus":
				execUpdateStatus(args);
				return true;
			}
				
		}//If this has happened the function will break and return true. if this hasn't happened the a value of false will be returned.
		return false; 
	}
	
	public void execReload() {
		plugin.getXMPP().doDisconnect();
		plugin.log.info("[" + plugin.getName() + "] reloading config now");
		plugin.reloadConfig();
		plugin.getXMPP().doConnect();
	}
	
	public void execReconnect() {
		plugin.getXMPP().doDisconnect();
		plugin.getXMPP().doConnect();
	}
	
	public void execAddUser(String[] args) {
		plugin.log.info("Adding user now!");
		if(args.length == 3) {
			List<String> users = plugin.getConfig().getStringList("users");
			users.add(args[2] + "|" + args[1]);
			plugin.getConfig().set("users", users);
			plugin.saveConfig();
			plugin.getXMPP().addUsersToRoster();
		}		
}
	
	public void execUpdateStatus(String[] args) {
		if(args.length > 2) {
			String temp = "";
			for(int i = 1; i < args.length; i++)
				temp = temp + args[i] + " ";
			plugin.getConfig().set("xmpp.status", temp);
			plugin.saveConfig();
			plugin.getXMPP().updateStatusMessage();
			plugin.log.info("[" + plugin.getName() + "] XMPP status message set to: " + temp);
		}
	}
	
	public boolean execSendPrivateMessage(String[] args) {
		String address = args[1];
		String message = "";
		for(int i = 2; i < args.length; i++) {
			message = message + args[i] + " ";
		}
		for(String users : plugin.getConfig().getStringList("users")) {
			if(address.equalsIgnoreCase(users.substring(0, users.indexOf("|")))) {
				plugin.getXMPP().sendMessage(address, message);
			} 
			if(address.equalsIgnoreCase(users.substring(users.indexOf("|") + 1 , users.length()))) {
				plugin.getXMPP().sendMessage(users.substring(0, users.indexOf("|")), message);
			}
		}
		return false;
	}
	
	public void execPrintUsers(CommandSender sender) {
		String users[] = new String[plugin.getConfig().getStringList("users").size() + 2];
		users[0] = "Users recieving Jabber messages";
		users[1] = " ";
		int i = 2;
		for(String user : plugin.getConfig().getStringList("users")) {
			users[i] = "<" + user.substring(user.indexOf("|") + 1, user.length()) + "> - " + user.substring(0, user.indexOf("|"));
			i++;
		}
		sender.sendMessage(users);
	}
}
