package me.kall.mendingmirror.mixin;

import me.kall.mendingmirror.MendingMirror;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "hurtAndBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private <T extends LivingEntity> void onItemBroken(int amount, T entity, Consumer<T> onBroken, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getEnchantmentLevel(MendingMirror.MENDING_MIRROR.get()) > 0) {
            MendingMirror.recordBrokenItem(entity, stack);
        }
    }
}