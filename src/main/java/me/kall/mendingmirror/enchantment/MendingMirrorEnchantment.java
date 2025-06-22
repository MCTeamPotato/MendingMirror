package me.kall.mendingmirror.enchantment;


import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.MendingEnchantment;

public class MendingMirrorEnchantment extends Enchantment {
    public MendingMirrorEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BREAKABLE, EquipmentSlot.values());
    }

    @Override
    public int getMaxLevel() {
        return super.getMaxLevel();
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return !(other instanceof MendingEnchantment) && super.checkCompatibility(other);
    }
}