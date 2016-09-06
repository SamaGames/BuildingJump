package fr.azuxul.buildingjump.jump;

import fr.azuxul.buildingjump.BuildingJumpGame;
import fr.azuxul.buildingjump.jump.block.BlockType;
import fr.azuxul.buildingjump.jump.block.JumpBlock;
import fr.azuxul.buildingjump.jump.block.effect.BlockEffect;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Jump
 *
 * @author Azuxul
 * @version 1.0
 */
public class Jump {

    private final JumpMeta jumpMeta;
    private final Map<JumpLocation, JumpBlock> blocks;
    private final Map<Location, BlockEffect> effectBlocks;
    private final int size;
    private final boolean firstInit;
    private final BuildingJumpGame buildingJumpGame;

    private Location worldLoc;
    private Location spawn;
    private boolean loaded;

    public Jump(JumpMeta jumpMeta, int size, Map<JumpLocation, JumpBlock> blocks, BuildingJumpGame buildingJumpGame, boolean newJump, Location spawn) {

        this.jumpMeta = jumpMeta;
        this.size = size;
        this.blocks = blocks;
        this.buildingJumpGame = buildingJumpGame;
        this.spawn = spawn;

        this.effectBlocks = new HashMap<>();
        this.firstInit = newJump;
    }

    public Jump(UUID uuid, int size, Map<JumpLocation, JumpBlock> blocks, BuildingJumpGame buildingJumpGame) {

        this(new JumpMeta(buildingJumpGame.getJumpManager().getNewJumpUUID(), uuid, new Date().getTime(), "Nouveau jump", -1, -1), size, blocks, buildingJumpGame, true, new Location(null, 0, 3, 0, 0, 0));
    }

    public JumpMeta getJumpMeta() {
        return jumpMeta;
    }

    public Map<JumpLocation, JumpBlock> getBlocks() {
        return blocks;
    }

    public int getSize() {
        return size;
    }

    public void registerWorldLoc(Location location) {
        this.worldLoc = location.clone();
    }

    public void load() {

        if (loaded)
            return;

        effectBlocks.clear();

        blocks.entrySet().forEach(e -> {

            Block block = worldLoc.clone().add(e.getKey().getX(), e.getKey().getY(), e.getKey().getZ()).getBlock();
            e.getValue().getJumpLocation().setJumpCenter(worldLoc);
            block.setTypeIdAndData(e.getValue().getMaterial().getId(), e.getValue().getDataValue(), true);

            if (!e.getValue().getBlockType().equals(BlockType.NORMAL)) {
                effectBlocks.put(e.getKey().getLocation(), e.getValue().getBlockEffect());
            }
        });

        if (firstInit) {
            for (int x = -2; x < 2; x++) {
                for (int z = -2; z < 2; z++) {
                    worldLoc.clone().add(x, 0, z).getBlock().setType(buildingJumpGame.getConfiguration().getDefaultPlatformMaterial());
                    JumpLocation jumpLocation = new JumpLocation(x, 0, z);
                    blocks.put(jumpLocation, new JumpBlock(buildingJumpGame.getConfiguration().getDefaultPlatformMaterial(), (byte) 0, BlockType.NORMAL, jumpLocation));
                }
            }
        }

        loaded = true;
    }

    public boolean update(Block updatedBlock, BlockType blockType) {

        int x = updatedBlock.getX() - worldLoc.getBlockX();
        int y = updatedBlock.getY() - worldLoc.getBlockY();
        int z = updatedBlock.getZ() - worldLoc.getBlockZ();

        if (Math.abs(x) >= size || Math.abs(z) >= size) {
            return false;
        } else {
            JumpLocation jumpLocation = new JumpLocation(x, y, z);
            jumpLocation.setJumpCenter(worldLoc);
            JumpBlock jumpBlock;

            if (blockType != BlockType.NORMAL) {
                jumpBlock = new JumpBlock(blockType.getRealMaterial(), (byte) 0, blockType, jumpLocation);
                effectBlocks.put(jumpLocation.getLocation(), jumpBlock.getBlockEffect());
            } else {
                jumpBlock = new JumpBlock(updatedBlock.getType(), updatedBlock.getData(), blockType, jumpLocation);
                effectBlocks.remove(jumpLocation.getLocation());
            }

            blocks.put(jumpLocation, jumpBlock);
            return true;
        }
    }

    public Location getSpawnInJump() {
        return spawn.clone();
    }

    public Location getSpawn() {
        spawn.setWorld(worldLoc.getWorld());

        Location spawnRealLoc = worldLoc.clone().add(spawn);

        spawnRealLoc.setYaw(spawn.getYaw());
        spawnRealLoc.setPitch(spawn.getPitch());

        return spawnRealLoc;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn.clone().subtract(worldLoc);
        this.spawn.setYaw(spawn.getYaw());
        this.spawn.setPitch(spawn.getPitch());
    }

    public Map<Location, BlockEffect> getEffectBlocks() {
        return effectBlocks;
    }

    public Location getWorldLocOfJumpLoc(JumpLocation jumpLocation) {

        return worldLoc.clone().add(jumpLocation.getX(), jumpLocation.getY(), jumpLocation.getZ());
    }
}
