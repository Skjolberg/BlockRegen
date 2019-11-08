package nl.Aurorion.BlockRegen.Configurations;

import nl.Aurorion.BlockRegen.Main;
import org.bukkit.configuration.file.FileConfiguration;

public class Files {

    // Switched recovery options from Recovery.yml to Data.yml
    private Configuration settings, messages, blocklist, regions, data;

    public Files(Main plugin) {

        // Plugin configuration
        settings = new Configuration(plugin, "Settings");

        // Language file
        messages = new Configuration(plugin, "Messages");

        // Blocklist configuration
        blocklist = new Configuration(plugin, "Blocklist");

        // Region storage
        regions = new Configuration(plugin, "Regions");

        // Data.yml ==> storing regen times, Recovery data
        data = new Configuration(plugin, "Data");

        // Recovery section created if needed
        if (settings.getYaml().getBoolean("Data-Recovery"))
            if (!data.getYaml().contains("Recovery"))
                data.getYaml().createSection("Recovery");
    }

    public FileConfiguration getSettings() {
        return settings.getYaml();
    }

    public void reloadSettings() {
        settings.reload();
    }

    public FileConfiguration getMessages() {
        return messages.getYaml();
    }

    public void reloadMessages() {
        messages.reload();
    }

    public FileConfiguration getBlocklist() {
        return blocklist.getYaml();
    }

    public void reloadBlocklist() {
        blocklist.reload();
    }

    public FileConfiguration getRegions() {
        return regions.getYaml();
    }

    public Configuration getRegionsCfg() {
        return regions;
    }

    public void reloadRegions() {
        regions.reload();
    }

    public void saveRegions() {
        regions.save();
    }

    public void saveMessages() {
        messages.save();}

    public void reload() {
        reloadBlocklist();
        reloadMessages();
        reloadSettings();
        reloadRegions();
    }

    public void saveData() {
        data.save();
    }

    public FileConfiguration getData() {
        return data.getYaml();
    }
}