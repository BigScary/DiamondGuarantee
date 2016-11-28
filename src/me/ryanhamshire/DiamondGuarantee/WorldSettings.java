package me.ryanhamshire.DiamondGuarantee;

class WorldSettings
{
    boolean generateDiamonds;
    boolean generateDiamondsLog;
    long diamondValue;
    long stoneValueOutsideZone;
    long stoneValueInsideZone;
    int diamondZoneMaxY;
    int diamondZoneMinY;
    int ghastDustCount;
    int wartChance;
    boolean dragonDropsEggs;
    boolean dragonDropsElytras;
    boolean dragonDropsHeads;
    
    WorldSettings(){ }
    
    WorldSettings(WorldSettings other)
    {
        this.generateDiamonds = other.generateDiamonds;
        this.generateDiamondsLog = other.generateDiamondsLog;
        this.diamondValue = other.diamondValue;
        this.stoneValueInsideZone = other.stoneValueInsideZone;
        this.stoneValueOutsideZone = other.stoneValueOutsideZone;
        this.diamondZoneMaxY = other.diamondZoneMaxY;
        this.diamondZoneMinY = other.diamondZoneMinY;
        this.ghastDustCount = other.ghastDustCount;
        this.wartChance = other.wartChance;
        this.dragonDropsEggs = other.dragonDropsEggs;
        this.dragonDropsElytras = other.dragonDropsElytras;
        this.dragonDropsHeads = other.dragonDropsHeads;
    }
}
