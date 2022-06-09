package nl.aurorion.blockregen.commands;

import nl.aurorion.blockregen.BlockRegen;
import nl.aurorion.blockregen.Message;
import nl.aurorion.blockregen.StringUtil;
import nl.aurorion.blockregen.system.event.struct.PresetEvent;
import nl.aurorion.blockregen.system.region.struct.RegenerationRegion;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class Commands implements CommandExecutor {

    private static final String HELP = "&8&m        &r &3BlockRegen &f%version% &8&m        &r"
            + "\n&3/%label% reload &8- &7Reload the plugin."
            + "\n&3/%label% debug &8- &7Turn on debug. Receive debug messages in chat."
            + "\n&3/%label% bypass &8- &7Bypass block regeneration."
            + "\n&3/%label% check &8- &7Check the correct material name to use. Just hit a block."
            + "\n&3/%label% regions &8- &7List regions."
            + "\n&3/%label% region set <name> &8- &7Create a region from your WorldEdit selection."
            + "\n&3/%label% region remove <name> &8- &7Delete a region."
            + "\n&3/%label% events &8- &7Event management."
            + "\n&3/%label% discord &8- &7BlockRegen discord invite. Ask for support there.";

    private final BlockRegen plugin;

    public Commands(BlockRegen plugin) {
        this.plugin = plugin;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(StringUtil.color(HELP
                .replace("%version%", plugin.getDescription().getVersion())
                .replace("%label%", label)));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label,
                             @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender, label);
            return false;
        }

        Player player;

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("blockregen.admin")) {
                    Message.NO_PERM.send(sender);
                    return false;
                }

                plugin.reload(sender);
                break;
            case "bypass":
                if (checkConsole(sender))
                    return false;

                player = (Player) sender;

                if (!player.hasPermission("blockregen.bypass")) {
                    Message.NO_PERM.send(player);
                    return false;
                }

                if (plugin.getRegenerationManager().switchBypass(player)) {
                    Message.BYPASS_ON.send(player);
                } else {
                    Message.BYPASS_OFF.send(player);
                }
                break;
            case "check":
                if (checkConsole(sender))
                    return false;

                player = (Player) sender;

                if (!player.hasPermission("blockregen.datacheck")) {
                    Message.NO_PERM.send(player);
                    return false;
                }

                if (plugin.getRegenerationManager().switchDataCheck(player)) {
                    Message.DATA_CHECK_ON.send(player);
                } else {
                    Message.DATA_CHECK_OFF.send(player);
                }
                break;
            case "regions": {

                if (checkConsole(sender)) {
                    return false;
                }

                player = (Player) sender;

                if (args.length > 1) {
                    sender.sendMessage(Message.TOO_MANY_ARGS.get(player)
                            .replace("%help%", String.format("/%s regions", label)));
                    return false;
                }

                StringBuilder message = new StringBuilder("&8&m    &3 BlockRegen Regions &8&m    &r");
                for (String name : plugin.getRegionManager().getLoadedRegions().keySet()) {
                    message.append("\n&8  - &f").append(name);
                }
                message.append("\n");
                sender.sendMessage(StringUtil.color(message.toString()));
                break;
            }
            case "region": {

                if (checkConsole(sender))
                    return false;

                player = (Player) sender;

                if (!player.hasPermission("blockregen.admin")) {
                    player.sendMessage(Message.NO_PERM.get(player));
                    return false;
                }

                if (args.length == 1) {
                    sendHelp(sender, label);
                    return false;
                }

                if (args[1].equalsIgnoreCase("list")) {
                    if (args.length > 2) {
                        sender.sendMessage(Message.TOO_MANY_ARGS.get(player)
                                .replace("%help%", String.format("/%s region list", label)));
                        return false;
                    }

                    StringBuilder message = new StringBuilder("&8&m    &3 BlockRegen Regions &8&m    &r");
                    for (String name : plugin.getRegionManager().getLoadedRegions().keySet()) {
                        message.append("\n&8  - &f").append(name);
                    }
                    message.append("\n");
                    sender.sendMessage(StringUtil.color(message.toString()));
                    return false;
                } else if (args[1].equalsIgnoreCase("set")) {

                    if (args.length > 3) {
                        sender.sendMessage(Message.TOO_MANY_ARGS.get(player)
                                .replace("%help%", String.format("/%s region set <name>", label)));
                        return false;
                    } else if (args.length < 3) {
                        sender.sendMessage(Message.NOT_ENOUGH_ARGS.get(player)
                                .replace("%help%", String.format("/%s region set <name>", label)));
                        return false;
                    }

                    if (plugin.getRegionManager().exists(args[2])) {
                        Message.DUPLICATED_REGION.send(player);
                        return false;
                    }

                    RegenerationRegion region;

                    if (plugin.getVersionManager().getWorldEditProvider() != null) {
                        region = plugin.getVersionManager().getWorldEditProvider()
                                .createFromSelection(args[2], player);

                        if (region == null) {
                            Message.NO_SELECTION.send(player);
                            return false;
                        }

                        plugin.getRegionManager().addRegion(region);
                    } else {
                        if (!plugin.getRegionManager().finishSelection(player, args[2])) {
                            sender.sendMessage(Message.COULD_NOT_CREATE_REGION.get(player));
                            return false;
                        }
                    }

                    Message.SET_REGION.send(player);
                    return false;
                } else if (args[1].equalsIgnoreCase("remove")) {

                    if (args.length > 3) {
                        sender.sendMessage(Message.TOO_MANY_ARGS.get(player)
                                .replace("%help%", String.format("/%s region remove <name>", label)));
                        return false;
                    } else if (args.length < 3) {
                        sender.sendMessage(Message.NOT_ENOUGH_ARGS.get(player)
                                .replace("%help%", String.format("/%s region remove <name>", label)));
                        return false;
                    }

                    if (!plugin.getRegionManager().exists(args[2])) {
                        Message.UNKNOWN_REGION.send(player);
                        return false;
                    }

                    plugin.getRegionManager().removeRegion(args[2]);
                    Message.REMOVE_REGION.send(player);
                    return false;
                } else {
                    sendHelp(sender, label);
                }
                break;
            }
            case "debug":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Message.ONLY_PLAYERS.get());
                    return false;
                }

                player = (Player) sender;

                if (plugin.getConsoleHandler().getListeners().contains(sender)) {
                    // Change log level if the debug is not configured.
                    if (!plugin.getFiles().getSettings().getFileConfiguration().getBoolean("Debug-Enabled", false) && plugin.getLogLevel().intValue() <= Level.FINE.intValue()) {
                        plugin.setLogLevel(Level.INFO);
                    }

                    plugin.getConsoleHandler().removeListener(sender);
                    sender.sendMessage(Message.DEBUG_OFF.get(player));
                } else {
                    // Change log level.
                    if (plugin.getLogLevel().intValue() > Level.FINE.intValue()) {
                        plugin.setLogLevel(Level.FINE);
                    }

                    plugin.getConsoleHandler().addListener(sender);
                    sender.sendMessage(Message.DEBUG_ON.get(player));
                }
                break;
            case "discord":
                sender.sendMessage(StringUtil.color("&8&m      &3 BlockRegen Discord Server" +
                        "\n&6>> &7https://discord.gg/ZCxMca5"));
                break;
            case "events":
                if (!sender.hasPermission("blockregen.admin")) {
                    sender.sendMessage(Message.NO_PERM.get());
                    return false;
                }

                if (args.length < 3) {

                    if (plugin.getEventManager().getLoadedEvents().isEmpty()) {
                        sender.sendMessage(StringUtil.color("&8&m     &r &3BlockRegen Events &8&m     "
                                + "\n&cYou haven't made any events yet."
                                + "\n&8&m                       "));
                        return false;
                    }

                    StringBuilder list = new StringBuilder("&8&m     &r &3BlockRegen Events &8&m     \n" +
                            "&7You have the following events loaded:").append("\n&r ");

                    for (PresetEvent event : plugin.getEventManager().getEvents(e -> true)) {
                        list.append("\n&8 - &r").append(event.getDisplayName()).append(" &7(Name: &f")
                                .append(event.getName()).append("&7) ")
                                .append(event.isEnabled() ? " &a(active)&r" : " &c(inactive)&r");
                    }

                    list.append("\n&r \n&7Use &3/").append(label).append(" events activate <name> &7to activate it.\n")
                            .append("&7Use &3/").append(label).append(" events deactivate <name> &7to de-activate it.");
                    sender.sendMessage(StringUtil.color(list.toString()));
                } else {
                    if (args[1].equalsIgnoreCase("activate")) {
                        String name = args[2];

                        PresetEvent event = plugin.getEventManager().getEvent(name);

                        if (event == null) {
                            sender.sendMessage(Message.EVENT_NOT_FOUND.get());
                            return false;
                        }

                        if (event.isEnabled()) {
                            sender.sendMessage(Message.EVENT_ALREADY_ACTIVE.get());
                            return false;
                        }

                        plugin.getEventManager().enableEvent(event);
                        sender.sendMessage(StringUtil
                                .color(Message.ACTIVATE_EVENT.get().replace("%event%", event.getDisplayName())));
                        return false;
                    }

                    if (args[1].equalsIgnoreCase("deactivate")) {
                        String name = args[2];

                        PresetEvent event = plugin.getEventManager().getEvent(name);

                        if (event == null) {
                            sender.sendMessage(Message.EVENT_NOT_FOUND.get());
                            return false;
                        }

                        if (!event.isEnabled()) {
                            sender.sendMessage(Message.EVENT_NOT_ACTIVE.get());
                            return false;
                        }

                        plugin.getEventManager().disableEvent(event);
                        sender.sendMessage(StringUtil
                                .color(Message.DEACTIVATE_EVENT.get().replace("%event%", event.getDisplayName())));
                        return false;
                    }
                }
                break;
            default:
                sendHelp(sender, label);
        }
        return false;
    }

    private boolean checkConsole(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Message.ONLY_PLAYERS.get());
            return true;
        }

        return false;
    }
}