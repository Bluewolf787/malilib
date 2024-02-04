package malilib.util.game.wrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class RegistryUtils
{
    public static Block getBlockByIdStr(String name)
    {
        try
        {
            return getBlockById(new ResourceLocation(name));
        }
        catch (Exception e)
        {
            return Blocks.AIR;
        }
    }

    public static Block getBlockById(ResourceLocation id)
    {
        Block block = Block.REGISTRY.getObject(id);
        return block != null ? block : Blocks.AIR;
    }

    @Nullable
    public static ResourceLocation getBlockId(Block block)
    {
        return Block.REGISTRY.getNameForObject(block);
    }

    @Nullable
    public static ResourceLocation getBlockId(IBlockState state)
    {
        return getBlockId(state.getBlock());
    }

    public static String getBlockIdStr(Block block)
    {
        ResourceLocation id = getBlockId(block);
        return id != null ? id.toString() : "?";
    }

    public static String getBlockIdStr(IBlockState state)
    {
        return getBlockIdStr(state.getBlock());
    }

    public static Collection<ResourceLocation> getRegisteredBlockIds()
    {
        return Block.REGISTRY.getKeys();
    }

    public static List<Block> getSortedBlockList()
    {
        List<Block> blocks = new ArrayList<>();

        for (Block block : Block.REGISTRY)
        {
            blocks.add(block);
        }

        blocks.sort(Comparator.comparing(RegistryUtils::getBlockIdStr));

        return blocks;
    }

    public static Item getItemByIdStr(String name)
    {
        try
        {
            return getItemById(new ResourceLocation(name));
        }
        catch (Exception e)
        {
            return Items.AIR;
        }
    }

    public static Item getItemById(ResourceLocation id)
    {
        Item item = Item.REGISTRY.getObject(id);
        return item != null ? item : Items.AIR;
    }

    @Nullable
    public static ResourceLocation getItemId(Item item)
    {
        return Item.REGISTRY.getNameForObject(item);
    }

    public static String getItemIdStr(Item item)
    {
        ResourceLocation id = getItemId(item);
        return id != null ? id.toString() : "?";
    }

    public static Collection<ResourceLocation> getRegisteredItemIds()
    {
        return Item.REGISTRY.getKeys();
    }

    public static List<Item> getSortedItemList()
    {
        List<Item> items = new ArrayList<>();

        for (Item item : Item.REGISTRY)
        {
            items.add(item);
        }

        items.sort(Comparator.comparing(RegistryUtils::getItemIdStr));

        return items;
    }
}
