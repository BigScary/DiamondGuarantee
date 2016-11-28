//Copyright 2015 Ryan Hamshire

package me.ryanhamshire.DiamondGuarantee;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class DGEventHandler implements Listener
{
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if(event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        
        Player player = event.getPlayer();
        if(!DiamondGuarantee.instance.worldSettingsManager.Get(player.getWorld()).generateDiamonds) return;
        BlockFace face = event.getBlockFace();
        PlayerData data = PlayerData.FromPlayer(player);
        
        data.lastClickedFace = face;
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event)
    {
        Player player = event.getPlayer();
        if(player == null) return;
        
        if(!DiamondGuarantee.instance.worldSettingsManager.Get(player.getWorld()).generateDiamonds) return;
        
        Block block = event.getBlock();
        
        if(block.getWorld().getEnvironment() != Environment.NORMAL) return;
        
        long value = this.getBlockValue(block);
        if(value == 0) return;
        
        PlayerData data = PlayerData.FromPlayer(player);
        if(data.lastClickedFace == null) return;
        long newScore = data.adjustDiamondScore(value);
        
        if(newScore >= DiamondGuarantee.instance.worldSettingsManager.Get(player.getWorld()).diamondValue)
        {
            //verify in diamond zone
            int y = block.getY();
            if(y >DiamondGuarantee.instance.worldSettingsManager.Get(player.getWorld()).diamondZoneMaxY || y < DiamondGuarantee.instance.worldSettingsManager.Get(player.getWorld()).diamondZoneMinY) return;
            
            //find block on other side of broken block
            BlockFace direction; 
            switch(data.lastClickedFace)
            {
                case NORTH:direction = BlockFace.SOUTH; break;
                case SOUTH:direction = BlockFace.NORTH; break;
                case EAST:direction = BlockFace.WEST; break;
                case WEST:direction = BlockFace.EAST; break;
                case UP:direction = BlockFace.DOWN; break;
                default:direction = BlockFace.UP; break;
            }
            
            Block newBlock = block.getRelative(direction);
            Material newBlockType = newBlock.getType();
            
            //only stone stone will convert to diamond
            if(newBlockType != Material.STONE) return;
            
            //confirm block is entirely enclosed
            if(newBlock.getLightLevel() > 0) return;
            BlockFace [] adjacentFaces = new BlockFace [] {BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};
            for(BlockFace face : adjacentFaces)
            {
                Block nearbyBlock = newBlock.getRelative(face);
                if(nearbyBlock.getType().isTransparent()) return;
            }
            
            //convert to diamond ore
            newBlock.setType(Material.DIAMOND_ORE);
            
            if(DiamondGuarantee.instance.worldSettingsManager.Get(player.getWorld()).generateDiamondsLog)
            {
                String logEntry = "Generated diamond ore for " + player.getName() + " @ " + block.getWorld().getName() + "(" + block.getX() + ", " + block.getY() + ", " + block.getZ() + ").";
                DiamondGuarantee.AddLogEntry(logEntry);
            }
            
            //mark as generated ore which won't cost to break
            newBlock.setMetadata("DG_noValue", new FixedMetadataValue(DiamondGuarantee.instance, true));
            
            //deduct diamond points for generating a new ore as if it had been broken already
            data.adjustDiamondScore(-DiamondGuarantee.instance.worldSettingsManager.Get(player.getWorld()).diamondValue);
        }
    }
    
    private long getBlockValue(Block block)
    {
        Material type = block.getType();
        if(type != Material.STONE && type != Material.DIAMOND_ORE) return 0;
        
        if(block.hasMetadata("DG_noValue")) return 0;
        World world = block.getWorld();
        
        if(type == Material.STONE)
        {
            //if in diamond zone
            if(block.getY() <= DiamondGuarantee.instance.worldSettingsManager.Get(world).diamondZoneMaxY && block.getY() >= DiamondGuarantee.instance.worldSettingsManager.Get(world).diamondZoneMinY)
            {
                return DiamondGuarantee.instance.worldSettingsManager.Get(world).stoneValueInsideZone;
            }
            else
            {
                return DiamondGuarantee.instance.worldSettingsManager.Get(world).stoneValueOutsideZone;
            }
        }
        else if(type == Material.DIAMOND_ORE)
        {
            return -DiamondGuarantee.instance.worldSettingsManager.Get(world).diamondValue;
        }
        
        return 0;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        Block block = event.getBlock();
        World world = block.getWorld();
        
        if(world.getEnvironment() != Environment.NORMAL) return;
        
        if(!DiamondGuarantee.instance.worldSettingsManager.Get(world).generateDiamonds) return;
        
        //placed blocks don't provide or cost any points when broken
        block.setMetadata("DG_noValue", new FixedMetadataValue(DiamondGuarantee.instance, true));
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockExplode(BlockExplodeEvent event)
    {
        World world = event.getBlock().getWorld();
        if(!DiamondGuarantee.instance.worldSettingsManager.Get(world).generateDiamonds) return;
        
        List<Block> blocks = event.blockList();
        for(int i = 0; i < blocks.size(); i++)
        {
            Block block = blocks.get(i);
            if(block.getType() == Material.DIAMOND_ORE)
            {
                blocks.remove(i--);
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event)
    {
        EntityType type = event.getEntityType();
        if(type == EntityType.GHAST)
        {
            int count = DiamondGuarantee.instance.worldSettingsManager.Get(event.getEntity().getWorld()).ghastDustCount;
            if(count > 0)
            {
                event.getDrops().add(new ItemStack(Material.GLOWSTONE_DUST, count));
            }
        }
        
        else if(type == EntityType.PIG_ZOMBIE)
        {
            Entity entity = event.getEntity();
            World world = entity.getWorld();
            if(world.getEnvironment() != Environment.NETHER) return;
            
            if(Math.random() * 100 < DiamondGuarantee.instance.worldSettingsManager.Get(entity.getWorld()).wartChance)
            {
                Location location = entity.getLocation();
                if(location.getBlock().getRelative(BlockFace.DOWN).getType() == Material.NETHER_BRICK)
                {
                    event.getDrops().add(new ItemStack(Material.NETHER_STALK));
                }
            }
        }
        
        else if(type == EntityType.ENDER_DRAGON)
        {
            World world = event.getEntity().getWorld();
            if(DiamondGuarantee.instance.worldSettingsManager.Get(world).dragonDropsEggs)
            {
                event.getDrops().add(new ItemStack(Material.DRAGON_EGG));
            }
            
            if(DiamondGuarantee.instance.worldSettingsManager.Get(world).dragonDropsElytras)
            {
                event.getDrops().add(new ItemStack(Material.ELYTRA));
            }
            
            if(DiamondGuarantee.instance.worldSettingsManager.Get(world).dragonDropsHeads)
            {
                event.getDrops().add(new ItemStack(Material.SKULL_ITEM, 1, (byte)5));
            }
        }
    }
}