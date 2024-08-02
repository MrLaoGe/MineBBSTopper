package com.mythmc.tools.utils;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.ProfileInputType;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Pattern;

public class ItemUtil {
    private static final String BASE64 = "^[A-Za-z0-9+/\\-]{100,}={0,3}$";
    private static final Pattern PATTERN = Pattern.compile(BASE64);

    public static ItemStack handleItemStack(Player player, String mat, int amount) {
        ItemStack itemStack;
        if (!mat.startsWith("HEAD:")) {
            // 尝试使用 XMaterial 来获取 Material
            XMaterial xMaterial = XMaterial.matchXMaterial(mat).orElse(null);

            if (xMaterial != null) {
                // 创建 ItemStack 时使用 XMaterial.getMaterial()
                itemStack = new ItemStack(xMaterial.parseMaterial(), amount);
            } else {
                // 如果 XMaterial 无法匹配，则使用默认材料（例如 BEDROCK）
                itemStack = new ItemStack(Material.BEDROCK);
            }
        } else {
            // 获取玩家头颅材质
            itemStack = getSkullItemStack(player, mat);
        }
        return itemStack;
    }

    private static ItemStack getSkullItemStack(Player player, String input) {
        // 创建一个玩家头颅的 ItemStack
        ItemStack skull;
        String[] part = input.split(":");
        if (PATTERN.matcher(part[1]).matches()) {
            // 加载BASE64编码格式的头颅，并缓存
            skull = XSkull.createItem().profile(new Profileable.StringProfileable(part[1], ProfileInputType.BASE64)).apply();
        } else {
            if (part[1].equals("%PLAYER%")) {
                // 加载玩家自己的头颅，并缓存
                skull = XSkull.createItem().profile(new Profileable.PlayerProfileable(player)).apply();
            } else {
                // 加载提供的玩家名头颅，并缓存
                skull = XSkull.createItem().profile(new Profileable.UsernameProfileable(part[1])).apply();
            }
        }
        return skull;
    }
}
