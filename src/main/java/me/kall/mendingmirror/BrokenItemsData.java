package me.kall.mendingmirror;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class BrokenItemsData extends SavedData {
    private final Map<UUID, Set<CompoundTag>> brokenItems = new HashMap<>();

    public BrokenItemsData() {
        super();
    }

    public static BrokenItemsData get(@NotNull ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(BrokenItemsData::load, BrokenItemsData::new, MendingMirror.MOD_ID + "_data");
    }

    public void addData(UUID player, CompoundTag item) {
        brokenItems.computeIfAbsent(player, k -> new HashSet<>()).add(item);
        setDirty();
    }

    public Set<CompoundTag> removeData(UUID player) {
        return brokenItems.remove(player);
    }

    public static BrokenItemsData load(CompoundTag tag) {
        BrokenItemsData data = new BrokenItemsData();
        data.loadData(tag);
        return data;
    }

    private void loadData(CompoundTag tag) {
        brokenItems.clear();
        ListTag entries = tag.getList("entries", Tag.TAG_COMPOUND);

        for (Tag entry : entries) {
            CompoundTag entryNBT = (CompoundTag) entry;
            UUID uuid = entryNBT.getUUID("player");
            ListTag tagList = entryNBT.getList("items", Tag.TAG_COMPOUND);

            Set<CompoundTag> tags = tagList.stream()
                    .map(CompoundTag.class::cast)
                    .collect(Collectors.toSet());

            brokenItems.put(uuid, tags);
        }
    }

    @Override
    public CompoundTag save(@NotNull CompoundTag compoundTag) {
        ListTag entries = new ListTag();
        brokenItems.forEach((uuid, compoundTags) -> {
            CompoundTag entryNBT = new CompoundTag();
            entryNBT.putUUID("player", uuid);

            ListTag tagList = new ListTag();
            tagList.addAll(compoundTags);

            entryNBT.put("items", tagList);
            entries.add(entryNBT);
        });
        compoundTag.put("entries", entries);
        return compoundTag;
    }
}