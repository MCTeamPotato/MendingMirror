package me.kall.mendingmirror;

import me.kall.mendingmirror.enchantment.MendingMirrorEnchantment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

@Mod(MendingMirror.MOD_ID)
public final class MendingMirror {
    public static final String MOD_ID = "mendingmirror";
    public static final String MOD_NAME = "MendingMirror";

    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, MOD_ID);

    public static final RegistryObject<Enchantment> MENDING_MIRROR = ENCHANTMENTS.register("mending_mirror", MendingMirrorEnchantment::new);

    @SuppressWarnings("all")
    public MendingMirror() {
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerWakeUp);
        ENCHANTMENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.CONFIG);
    }

    public static void recordBrokenItem(@NotNull Entity entity, @NotNull ItemStack stack) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        BrokenItemsData data = BrokenItemsData.get(level);

        UUID playerId = entity.getUUID();
        ItemStack copy = stack.copy();
        copy.setDamageValue(0);

        CompoundTag tag = copy.getOrCreateTag();
        if (tag.contains("Enchantments", 9)) {
            ListTag enchants = tag.getList("Enchantments", 10);
            for (int i = 0; i < enchants.size(); i++) {
                CompoundTag enchantTag = enchants.getCompound(i);
                if ("mendingmirror:mending_mirror".equals(enchantTag.getString("id"))) {
                    enchants.remove(i);
                    break;
                }
            }
        }

        int maxDurability = copy.getMaxDamage();
        copy.setDamageValue((int) ((double)maxDurability * (1.00D - Config.REMAINING_DURABILITY.get())));

        CompoundTag itemNbt = copy.save(new CompoundTag());
        data.addData(playerId, itemNbt);
    }

    public void onPlayerWakeUp(@NotNull PlayerWakeUpEvent event) {
        Player player = event.getEntity();
        UUID playerId = player.getUUID();

        if (!(player.level() instanceof ServerLevel level)) return;
        BrokenItemsData data = BrokenItemsData.get(level);

        Set<CompoundTag> recovered = data.removeData(playerId);
        if (recovered != null && !recovered.isEmpty()) {
            recovered.forEach(compoundTag -> {
                ItemStack stack = ItemStack.of(compoundTag);
                if (!player.addItem(stack)) player.drop(stack, false);
            });
            if (Config.PLAY_SOUND.get()) player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.0F);
        }
    }

    public static final class Config {
        public static final ForgeConfigSpec CONFIG;
        public static final ForgeConfigSpec.DoubleValue REMAINING_DURABILITY;
        public static final ForgeConfigSpec.BooleanValue PLAY_SOUND;

        static {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
            builder.push(MOD_NAME);
            REMAINING_DURABILITY = builder.defineInRange("RemainingDurability", 0.30, 0.01, 1.00);
            PLAY_SOUND = builder.define("PlaySoundOnRecovering", true);
            builder.pop();
            CONFIG = builder.build();
        }
    }
}
