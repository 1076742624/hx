package hx.utils;

import java.lang.reflect.Field;
import java.util.TreeMap;

import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.Configuration;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

@TransformerExclusions( {"hx.utils"})
public class HyperMod
{
    public int BLOCK_BASE_ID;
    public int BLOCK_LOADED_ID = 0;

    public int ITEM_BASE_ID;
    public int ITEM_LOADED_ID = 0;

    public String MAIN_TEXTURE;

    private TreeMap<String, BlockLoader> blockLoaders = new TreeMap<String, BlockLoader>();
    private TreeMap<String, ItemLoader> itemLoaders = new TreeMap<String, ItemLoader>();

    public void addBlocks(String[] names)
    {
        for (String name : names)
        {
            blockLoaders.put(name, new BlockLoader(this, name));
        }
    }

    public BlockLoader block(String name)
    {
        return blockLoaders.get(name);
    }

    public ItemLoader item(String name)
    {
        return itemLoaders.get(name);
    }

    public void addItems(String[] items)
    {
        for (String name : items)
        {
            itemLoaders.put(name, new ItemLoader(this, name));
        }
    }

    public void preInit(FMLPreInitializationEvent event)
    {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();

        for (BlockLoader bl : blockLoaders.values())
        {
            bl.preInit(config);
        }

        for (ItemLoader il : itemLoaders.values())
        {
            il.preInit(config);
        }

        for (Field f : this.getClass().getFields())
        {
            Configurable anno = f.getAnnotation(Configurable.class);

            if (anno != null)
            {
                try
                {
                    Class type = f.getType();

                    if (type.equals(int.class))
                    {
                        f.set(this, config.get(anno.value(), f.getName(), f.getInt(this)).getInt());
                    }
                    else if (type.equals(double.class))
                    {
                        f.set(this, config.get(anno.value(), f.getName(), f.getDouble(this)).getDouble(f.getDouble(this)));
                    }
                    else if (type.equals(boolean.class))
                    {
                        f.set(this, config.get(anno.value(), f.getName(), f.getBoolean(this)).getBoolean(f.getBoolean(this)));
                    }
                    else if (f.getGenericType().equals(int[].class))
                    {
                        f.set(this, config.get(anno.value(), f.getName(), (int[]) f.get(this)).getIntList());
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        config.save();
    }

    public void load(FMLInitializationEvent event)
    {
        for (BlockLoader bl : blockLoaders.values())
        {
            bl.load();
        }

        for (ItemLoader il : itemLoaders.values())
        {
            il.load();
        }
    }

    public void registerRendering(Object proxy)
    {
        for (BlockLoader bl : blockLoaders.values())
        {
            bl.registerRenderers(proxy);
        }

        if (proxy.getClass().getName().endsWith("ClientProxy"))
        {
            MinecraftForgeClient.preloadTexture(this.MAIN_TEXTURE);
        }
    }
}