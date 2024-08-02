package com.mythmc.commands.gui;

import com.mythmc.MineBBSTopper;
import com.mythmc.externs.HookManager;
import com.mythmc.externs.hook.placeholders.PlaceholderCache;
import com.mythmc.file.statics.ConfigFile;
import com.mythmc.file.statics.GUIFile;
import com.mythmc.externs.hook.placeholders.PlaceholderHook;
import com.mythmc.impl.cache.TargetManager;
import com.mythmc.impl.cache.target.GlobalInfo;
import com.mythmc.impl.cache.target.PlayerInfo;
import com.mythmc.tools.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainGUI implements Listener {
    // 定义一个静态的Map，用于存储槽位列表和对应的命令列表
    public static Map<List<Integer>, List<String>> slotCommands;

    // MainGUI类的构造函数，初始化槽位命令
    public MainGUI() {
        initializeSlotCommands();
    }

    // 初始化槽位命令的方法
    public void initializeSlotCommands() {
        slotCommands = null; // 先将slotCommands设为null
        slotCommands = new HashMap<>(); // 初始化slotCommands为一个空的HashMap
        ConfigurationSection menuItems = GUIFile.mainMenuItems; // 从GUIFile中获取菜单项配置

        // 遍历所有菜单项的键
        for (String key : menuItems.getKeys(false)) {
            ConfigurationSection itemSection = menuItems.getConfigurationSection(key); // 获取每个菜单项的配置
            Object slotObj = itemSection.get("slot"); // 获取槽位配置
            List<String> commands = itemSection.getStringList("commands"); // 获取命令列表

            // 如果槽位配置是一个整数
            if (slotObj instanceof Integer) {
                int slot = (Integer) slotObj; // 将槽位配置转为整数
                // 将整数槽位转为单元素列表
                List<Integer> slots = Collections.singletonList(slot);
                slotCommands.put(slots, commands); // 将槽位列表和命令列表存入slotCommands
            }
            // 如果槽位配置是一个整数列表
            else if (slotObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Integer> slots = (List<Integer>) slotObj; // 将槽位配置转为整数列表
                slotCommands.put(slots, commands); // 将槽位列表和命令列表存入slotCommands
            }
        }
    }
    // 打开菜单的方法
    public static void openMenu(Player player) {
        // 创建一个新的Inventory对象，标题根据是否使用PlaceholderHook来决定
        Inventory mainGUI = Bukkit.createInventory(null, GUIFile.mainMenuSize, PAPIUtil.set(player,GUIFile.mainMenuName));

        ConfigurationSection menuItems = GUIFile.mainMenuItems; // 从GUIFile中获取菜单项配置

        // 获取玩家信息
        PlayerInfo playerInfo = TargetManager.getPlayerInfo(player);
        int amount = playerInfo.getAmount(); // 获取玩家记录的数量
        String lastTime = playerInfo.getLastTimeStr(); // 获取玩家的最新记录时间

        // 获取全局信息
        GlobalInfo globalInfo = TargetManager.getGlobalInfo();
        long cooldownTimestamp = globalInfo.getCooldownTimestamp(); // 获取冷却时间戳
        String cooldownTimeStr = globalInfo.getCooldownTimeStr(); // 获取冷却时间数据
        // 判断是否可以顶贴
        String cantop = cooldownTimestamp <= System.currentTimeMillis() / 1000 ? "§a可顶贴" : "§c冷却中";
        cantop = TimeUtil.isCurrentHourWithinSpan() ? cantop : "§c不在顶贴时间段"; // 判断是否在顶贴时间段内

        // 遍历所有菜单项
        for (String key : menuItems.getKeys(false)) {
            ConfigurationSection itemSection = menuItems.getConfigurationSection(key); // 获取每个菜单项的配置

            ItemStack itemStack; // 定义ItemStack对象
            String materialName = itemSection.getString("mat", "BEDROCK").toUpperCase(); // 获取物品材质名称，默认为BEDROCK
            int materialAmount = itemSection.getInt("amount", 1); // 获取物品数量，默认为1

            itemStack = ItemUtil.handleItemStack(player, materialName, materialAmount); // 处理物品堆栈

            ItemMeta itemMeta = itemStack.getItemMeta(); // 获取物品的Meta数据

            // 设置物品的显示名称，考虑是否使用PlaceholderHook
            itemMeta.setDisplayName(ColorUtil.colorize(PAPIUtil.set(player,itemSection.getString("name", " "))));
            // 如果不是低版本，则设置自定义模型数据
            if (!MineBBSTopper.isLowVersion) {
                String cmd = itemSection.getString("cmd");
                int customModelData = (cmd != null) ? Integer.parseInt(cmd) : 0;
                itemMeta.setCustomModelData(customModelData);
            }

            List<String> lore = itemSection.getStringList("lore"); // 获取物品的lore
            List<String> cachedRank = PlaceholderCache.getCache(); // 获取缓存的排名数据

            int rankInsertIndex = -1; // 用于记录%rank%所在的索引
            int size = lore.size();
            // 遍历lore，找到%rank%并记录索引，同时处理%amount%
            for (int i = 0; i < size; i++) {
                String line = lore.get(i);
                if (line.contains("%rank%")) {
                    lore.remove(i);
                    rankInsertIndex = i; // %rank%的下一行开始插入
                    break; // 找到第一个%rank%后就退出循环
                }
                StringBuilder sb = new StringBuilder(ColorUtil.colorize(line));

                // 使用正则替换，减少多次调用contains和replace
                sb = new StringBuilder(sb.toString().replace("%amount%", String.valueOf(amount))
                        .replace("%lasttime%", lastTime)
                        .replace("%cantop%", cantop)
                        .replace("%cooldown%", cooldownTimeStr));

                lore.set(i, sb.toString()); // 更新lore
            }

            // 如果找到了%rank%，并且排名列表不为空
            if (rankInsertIndex != -1 && !cachedRank.isEmpty()) {
                // 从rankInsertIndex开始，将所有排名添加到lore中
                for (String rank : cachedRank) {
                    lore.add(rankInsertIndex++, ColorUtil.colorize(rank));
                }
            }
            // 处理papi
            lore = PAPIUtil.set(player, lore);
            itemMeta.setLore(lore); // 设置物品的lore
            itemStack.setItemMeta(itemMeta); // 将Meta数据应用到物品

            // 设置槽位物品
            Object slotObj = itemSection.get("slot");
            if (slotObj instanceof List) {
                List<Integer> slots = (List<Integer>) slotObj;
                for (int slot : slots) {
                    mainGUI.setItem(slot, itemStack); // 将物品设置到每个槽位
                }
            } else {
                int slot = itemSection.getInt("slot");
                mainGUI.setItem(slot, itemStack); // 将物品设置到指定槽位
            }
        }
        player.openInventory(mainGUI); // 打开菜单给玩家
        // player.playSound(player.getLocation(), Sound.valueOf(GUIFile.mainMenuOpenSound), 1, 1); // Bukkit播放声音
        // XSound.matchXSound(GUIFile.mainMenuOpenSound).get().play(player); // XSound播放声音
    }
}
