package com.mythmc.file.statics;

import com.mythmc.MineBBSTopper;
import com.mythmc.tools.utils.ColorUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class GUIFile {
    public static String mainMenuName,mainMenuOpenSound,rankFormat;
    public static int mainMenuSize;
    public static ConfigurationSection mainMenuItems,mainMenuEvents;

    // 累计GUI
    public static ConfigurationSection rewardMenuItems,rewardMenuEvents;
    public static String rewardMenuName,rewardMenuOpenSound,rewardMenuStatusYes,rewardMenuStatusNo,rewardMenuStatusClaim,rewardMenuClaimedMat;
    public static int rewardMenuSize;
    public static boolean rewardMenuEnable;
    public  void load(){
        File guiFile = new File(MineBBSTopper.instance.getDataFolder(), "gui/main.yml");
        File rewardFile = new File(MineBBSTopper.instance.getDataFolder(), "gui/reward.yml");
        if (!guiFile.exists()) MineBBSTopper.instance.saveResource("gui/main.yml", false);
        if (!rewardFile.exists()) MineBBSTopper.instance.saveResource("gui/reward.yml", false);

        YamlConfiguration mainMenu = YamlConfiguration.loadConfiguration(guiFile);
        YamlConfiguration rewardMenu = YamlConfiguration.loadConfiguration(rewardFile);
        // 主菜单的配置
        mainMenuName = ColorUtil.colorize(mainMenu.getString("Menu.title"));
        mainMenuSize = mainMenu.getInt("Menu.size");
       // mainMenuOpenSound = mainMenu.getString("Menu.sound");
        mainMenuItems = mainMenu.getConfigurationSection("Menu.items");
        mainMenuEvents = mainMenu.getConfigurationSection("Menu.events");
        rankFormat = ColorUtil.colorize(mainMenu.getString("Menu.rankFormat"));

        // 累计奖励菜单配置
        rewardMenuItems = rewardMenu.getConfigurationSection("Menu.items");
        rewardMenuEvents = rewardMenu.getConfigurationSection("Menu.events");
        rewardMenuName = ColorUtil.colorize(rewardMenu.getString("Menu.title"));
        rewardMenuSize = rewardMenu.getInt("Menu.size");
       // rewardMenuOpenSound = rewardMenu.getString("Menu.sound");
        rewardMenuEnable = rewardMenu.getBoolean("Enable",true);
        rewardMenuStatusYes = ColorUtil.colorize(rewardMenu.getString("Status.yes"));
        rewardMenuStatusNo = ColorUtil.colorize(rewardMenu.getString("Status.no"));
        rewardMenuStatusClaim = ColorUtil.colorize(rewardMenu.getString("Status.claimed"));
        rewardMenuClaimedMat = rewardMenu.getString("Menu.claimedMat");



    }
    public void clear() {
        mainMenuItems = null;
        rewardMenuItems = null;
    }
    
}
