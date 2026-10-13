
package pl.zeus.zeusset;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;

public class ZeusSet extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("ZeusSet wlaczony!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("zeus")) {
            return false;
        }

        if (!sender.hasPermission("zeus.give")) {
            sender.sendMessage(Component.text("Nie masz uprawnien do tej komendy!", NamedTextColor.RED));
            return true;
        }

        Player target;
        if (args.length >= 1) {
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(Component.text("Nie znaleziono gracza: " + args[0], NamedTextColor.RED));
                return true;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage(Component.text("Uzycie: /zeus <gracz>", NamedTextColor.RED));
            return true;
        }

        giveSet(target);

        target.sendMessage(Component.text("⚡ Otrzymales set Zeusa! ⚡", NamedTextColor.GOLD, TextDecoration.BOLD));
        if (!target.equals(sender)) {
            sender.sendMessage(Component.text("Dales set Zeusa graczowi " + target.getName() + ".", NamedTextColor.GREEN));
        }
        return true;
    }

    private void giveSet(Player player) {
        giveSafely(player, "glowa", this::createHelmet);
        giveSafely(player, "tunika", this::createChestplate);
        giveSafely(player, "miecz", this::createSword);
    }

    // Kazdy przedmiot tworzony osobno - jesli jeden sie wywroci, reszta dziala,
    // a prawdziwy blad jest pokazany w czacie i w konsoli.
    private void giveSafely(Player player, String what, Supplier<ItemStack> factory) {
        try {
            giveOrDrop(player, factory.get());
        } catch (Throwable t) {
            getLogger().log(Level.SEVERE, "Blad przy tworzeniu przedmiotu: " + what, t);
            player.sendMessage(Component.text("Blad (" + what + "): " + t, NamedTextColor.RED));
        }
    }

    private void giveOrDrop(Player player, ItemStack item) {
        HashMap<Integer, ItemStack> left = player.getInventory().addItem(item);
        for (ItemStack rest : left.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), rest);
        }
    }

    // Glowa Zeusa (helm)
    private ItemStack createHelmet() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        String owner = getConfig().getString("head-owner", "Zeus");
        try {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
        } catch (Throwable t) {
            getLogger().log(Level.WARNING, "Nie udalo sie ustawic skorki glowy dla: " + owner, t);
        }

        meta.displayName(name("⚡ Glowa Zeusa ⚡"));
        meta.lore(List.of(
                Component.empty(),
                lore("Glowa wladcy gromow i niebios.", NamedTextColor.YELLOW),
                lore("Czesc setu Zeusa.", NamedTextColor.GOLD)
        ));
        meta.addEnchant(Enchantment.PROTECTION, 52, true);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        head.setItemMeta(meta);
        return head;
    }

    // Zolta tunika (skorzany napiersnik) - ochrona 52 + nie zniszczalnosc
    private ItemStack createChestplate() {
        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta meta = (LeatherArmorMeta) chest.getItemMeta();

        meta.setColor(Color.YELLOW);
        meta.displayName(name("⚡ Tunika Zeusa ⚡"));
        meta.lore(List.of(
                Component.empty(),
                lore("Tunika wladcy Olimpu.", NamedTextColor.YELLOW),
                lore("Czesc setu Zeusa.", NamedTextColor.GOLD)
        ));
        meta.addEnchant(Enchantment.PROTECTION, 52, true);
        meta.addEnchant(Enchantment.UNBREAKING, 52, true);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_DYE);
        chest.setItemMeta(meta);
        return chest;
    }

    // Drewniany miecz Zeusa - ostrosc 52 + nie zniszczalnosc 53
    private ItemStack createSword() {
        ItemStack sword = new ItemStack(Material.WOODEN_SWORD);
        ItemMeta meta = sword.getItemMeta();

        meta.displayName(name("⚡ Miecz Zeusa ⚡"));
        meta.lore(List.of(
                Component.empty(),
                lore("Miecz wladcy gromow.", NamedTextColor.YELLOW),
                lore("Czesc setu Zeusa.", NamedTextColor.GOLD)
        ));
        meta.addEnchant(Enchantment.SHARPNESS, 52, true);
        meta.addEnchant(Enchantment.UNBREAKING, 53, true);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        sword.setItemMeta(meta);
        return sword;
    }

    private Component name(String text) {
        return Component.text(text, NamedTextColor.GOLD, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false);
    }

    private Component lore(String text, NamedTextColor color) {
        return Component.text(text, color).decoration(TextDecoration.ITALIC, false);
    }
}
