package nl.Aurorion.BlockRegen.Commands;

import nl.Aurorion.BlockRegen.Main;
import nl.Aurorion.BlockRegen.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class TabCompleterBR implements TabCompleter {

    // Todo add new world and region subcommands along with block format fills

    // Subcommand lists
    private final String[] subcommands = {"reload", "bypass", "check", "region", "events", "world", "debug"};
    private final String[] regionCommands = {"set", "remove", "list", "fromWG", "listBlocks", "add", "useall", "enabled", "clear"};
    private final String[] worldCommands = {"listBlocks", "remove", "add", "clear", "useall", "enabled"};
    private final String[] eventCommands = {"activate", "deactivate"};

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completeList = new ArrayList<>();

        if (args.length == 1) {
            for (String subCommand : subcommands) {
                if (!args[0].equals("")) {
                    if (subCommand.toLowerCase().startsWith(args[0].toLowerCase()))
                        completeList.add(subCommand);
                    continue;
                }
                completeList.add(subCommand);
            }
        } else if (args.length == 2) {
            if (args[0].equals("region")) {
                for (String regionCommand : regionCommands) {
                    if (!args[1].equals("")) {
                        if (regionCommand.toLowerCase().startsWith(args[1].toLowerCase()))
                            completeList.add(regionCommand);
                        continue;
                    }
                    completeList.add(regionCommand);
                }

                // Add WorldGuard command if hooked
                if (Main.getInstance().worldGuard != null) {
                    if (!args[1].equals("")) {
                        if ("fromwg".startsWith(args[1].toLowerCase()))
                            completeList.add("fromWG");
                    } else completeList.add("fromWG");
                }
            } else if (args[0].equals("events"))
                for (String eventCommand : eventCommands) {
                    if (!args[1].equals("")) {
                        if (eventCommand.toLowerCase().startsWith(args[1].toLowerCase()))
                            completeList.add(eventCommand);
                        continue;
                    }
                    completeList.add(eventCommand);
                }
        } else if (args.length == 3) {
            if (args[0].equals("region")) {
                if (!args[1].equalsIgnoreCase("fromWG"))
                    for (String regionName : Main.getInstance().getFiles().getRegions().getConfigurationSection("Regions").getKeys(false)) {
                        if (!args[2].equals("")) {
                            if (regionName.toLowerCase().startsWith(args[2].toLowerCase()))
                                completeList.add(regionName);
                            continue;
                        }

                        completeList.add(regionName);
                    }
            } else if (args[0].equals("events"))
                for (String eventName : Utils.events.keySet()) {
                    if (!args[2].equals("")) {
                        if (eventName.toLowerCase().startsWith(args[2].toLowerCase()))
                            completeList.add(eventName);
                        continue;
                    }
                    completeList.add(eventName);
                }
        }

        if (completeList.isEmpty())
            return null;

        return completeList;
    }
}
