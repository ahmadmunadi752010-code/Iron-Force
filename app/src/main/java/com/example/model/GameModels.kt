package com.example.model

import androidx.compose.ui.graphics.Color

enum class Ideology(val label: String) {
    AUTOCRACY("Autocracy"),
    DEMOCRACY("Democracy"),
    COMMUNISM("Communism"),
    NON_ALIGNED("Non-Aligned")
}

enum class Difficulty(val label: String, val aiBonus: Double, val playerBonus: Double) {
    RECRUIT("Recruit", 0.85, 1.15),
    REGULAR("Regular", 1.0, 1.0),
    VETERAN("Veteran", 1.25, 0.90)
}

enum class TerrainType(
    val label: String,
    val attackMod: Double,
    val defenseMod: Double,
    val speedMod: Double,
    val mapTint: Long
) {
    PLAINS("Plains", 1.0, 1.0, 1.0, 0xFF4E5E44),
    FOREST("Forest", 0.88, 1.15, 0.85, 0xFF334B35),
    MOUNTAIN("Mountain", 0.65, 1.45, 0.60, 0xFF5D534B),
    URBAN("Urban", 0.78, 1.35, 0.90, 0xFF5A5D63),
    MARSH("Marsh", 0.72, 1.20, 0.65, 0xFF3B4D4B)
}

enum class EquipmentType(
    val displayName: String,
    val shortCode: String,
    val icCost: Double
) {
    INFANTRY_EQ("Infantry Gear", "INF", 4.0),
    ARTILLERY("Field Artillery", "ART", 10.0),
    LIGHT_TANK("Armored Tanks", "ARM", 22.0),
    FIGHTER("Interceptor Wing", "AIR", 28.0)
}

enum class DivisionTemplate(
    val displayName: String,
    val symbolCode: String,
    val softAttack: Double,
    val hardAttack: Double,
    val defense: Double,
    val breakthrough: Double,
    val maxOrg: Double,
    val maxHp: Double,
    val speedKph: Double,
    val manpowerCost: Long,
    val trainDays: Int,
    val equipmentCost: Map<EquipmentType, Int>
) {
    INFANTRY(
        displayName = "Infantry Division",
        symbolCode = "INF",
        softAttack = 28.0,
        hardAttack = 6.0,
        defense = 36.0,
        breakthrough = 14.0,
        maxOrg = 60.0,
        maxHp = 100.0,
        speedKph = 4.0,
        manpowerCost = 10000L,
        trainDays = 30,
        equipmentCost = mapOf(EquipmentType.INFANTRY_EQ to 120, EquipmentType.ARTILLERY to 12)
    ),
    ARMOR(
        displayName = "Panzer Division",
        symbolCode = "ARM",
        softAttack = 42.0,
        hardAttack = 28.0,
        defense = 26.0,
        breakthrough = 48.0,
        maxOrg = 45.0,
        maxHp = 100.0,
        speedKph = 8.5,
        manpowerCost = 7500L,
        trainDays = 45,
        equipmentCost = mapOf(EquipmentType.INFANTRY_EQ to 60, EquipmentType.LIGHT_TANK to 36)
    ),
    MOTORIZED(
        displayName = "Motorized Rifle",
        symbolCode = "MOT",
        softAttack = 34.0,
        hardAttack = 12.0,
        defense = 32.0,
        breakthrough = 26.0,
        maxOrg = 56.0,
        maxHp = 100.0,
        speedKph = 7.0,
        manpowerCost = 9000L,
        trainDays = 35,
        equipmentCost = mapOf(EquipmentType.INFANTRY_EQ to 100, EquipmentType.ARTILLERY to 18)
    ),
    MOUNTAINEER(
        displayName = "Mountain Corps",
        symbolCode = "MTN",
        softAttack = 31.0,
        hardAttack = 8.0,
        defense = 42.0,
        breakthrough = 18.0,
        maxOrg = 68.0,
        maxHp = 100.0,
        speedKph = 4.5,
        manpowerCost = 9500L,
        trainDays = 38,
        equipmentCost = mapOf(EquipmentType.INFANTRY_EQ to 130, EquipmentType.ARTILLERY to 14)
    )
}

enum class BuildingType(val displayName: String, val baseDays: Int) {
    CIV_FACTORY("Civilian Factory", 42),
    MIL_FACTORY("Military Factory", 34),
    LAND_FORT("Land Bunker Fort", 22),
    INFRASTRUCTURE("Rail & Supply", 20)
}

enum class ConscriptionLaw(
    val displayName: String,
    val recruitablePct: Double,
    val ppCost: Double,
    val trainingSpeedMod: Double
) {
    VOLUNTEER("Volunteer Only", 0.015, 0.0, 1.0),
    LIMITED("Limited Conscription", 0.030, 75.0, 1.0),
    EXTENSIVE("Extensive Conscription", 0.055, 150.0, 0.92),
    SERVICE_REQ("Service by Requirement", 0.095, 225.0, 0.84)
}

enum class EconomyLaw(
    val displayName: String,
    val consumerGoodsPct: Double,
    val milBuildBonus: Double,
    val ppCost: Double
) {
    CIVILIAN("Civilian Economy", 0.35, 0.85, 0.0),
    PARTIAL("Partial Mobilization", 0.25, 1.05, 75.0),
    WAR_ECONOMY("War Economy", 0.15, 1.25, 150.0),
    TOTAL_MOB("Total Mobilization", 0.10, 1.40, 225.0)
}

enum class TradeLaw(
    val displayName: String,
    val factoryOutputBonus: Double,
    val researchBonus: Double,
    val ppCost: Double
) {
    FREE_TRADE("Free Trade", 0.12, 0.10, 75.0),
    EXPORT_FOCUS("Export Focus", 0.06, 0.05, 50.0),
    CLOSED("Closed Economy", 0.0, 0.0, 75.0)
}

enum class DiplomacyStatus(val label: String) {
    PEACE("Peace"),
    WAR("At War"),
    ALLIANCE("Allied"),
    NON_AGGRESSION("Non-Aggression")
}

enum class MapMode(val label: String) {
    POLITICAL("Political"),
    TERRAIN_SUPPLY("Terrain & Supply"),
    DIPLOMACY("Diplomacy")
}

enum class GameSpeed(val label: String, val tickDelayMs: Long) {
    PAUSED("||", 0L),
    SPEED_1("1x", 900L),
    SPEED_2("2x", 500L),
    SPEED_3("3x", 260L),
    SPEED_4("4x", 130L),
    SPEED_5("5x", 60L)
}

enum class TechCategory(val label: String) {
    INFANTRY("Infantry & Firepower"),
    ARMOR("Armor & Blitz"),
    INDUSTRY("Industry & Engineering"),
    DOCTRINE("Land Doctrine")
}

data class MapPoint(val x: Float, val y: Float)

data class ProvinceDef(
    val id: String,
    val name: String,
    val region: String,
    val centerX: Float,
    val centerY: Float,
    val polygon: List<MapPoint>,
    val terrain: TerrainType,
    val victoryPoints: Int,
    val initialOwnerId: String,
    val isCapital: Boolean,
    val initialCiv: Int,
    val initialMil: Int,
    val initialForts: Int,
    val initialInfra: Int,
    val neighbors: List<String>
)

data class NationDef(
    val id: String,
    val name: String,
    val shortTag: String,
    val ideology: Ideology,
    val leaderName: String,
    val leaderTitle: String,
    val factionName: String,
    val color: Color,
    val flagAccent: Color,
    val startingPP: Double,
    val startingManpower: Long,
    val startingStability: Double,
    val startingWarSupport: Double,
    val summary: String,
    val capitalProvinceId: String
)

data class FocusNode(
    val id: String,
    val title: String,
    val subtitle: String,
    val daysRequired: Int,
    val prerequisites: List<String>,
    val branchCol: Int,
    val tierRow: Int,
    val rewardText: String
)

data class TechNode(
    val id: String,
    val title: String,
    val category: TechCategory,
    val tier: Int,
    val daysRequired: Int,
    val prerequisites: List<String>,
    val bonusText: String
)

data class DivisionState(
    val id: String,
    val name: String,
    val nationId: String,
    val template: DivisionTemplate,
    val provinceId: String,
    val targetProvinceId: String = "",
    val moveProgress: Double = 0.0,
    val hp: Double = 100.0,
    val org: Double = template.maxOrg,
    val entrenchment: Double = 4.0
)

data class ProductionLine(
    val equipmentType: EquipmentType,
    val assignedFactories: Int,
    val progress: Double = 0.0
)

data class ConstructionOrder(
    val provinceId: String,
    val buildingType: BuildingType,
    val progressDays: Int = 0,
    val totalDays: Int = buildingType.baseDays
)

data class TrainingOrder(
    val template: DivisionTemplate,
    val deployProvinceId: String,
    val progressDays: Int = 0,
    val totalDays: Int = template.trainDays
)

data class CombatBubble(
    val provinceId: String,
    val attackerNationId: String,
    val defenderNationId: String,
    val attackerAdvantagePct: Int,
    val attackerCount: Int,
    val defenderCount: Int
)

data class CampaignState(
    val campaignId: String = "",
    val campaignName: String,
    val playerNationId: String,
    val difficulty: Difficulty,
    val gameDateEpochDay: Long,
    val gameHour: Int,
    val politicalPower: Double,
    val stability: Double,
    val warSupport: Double,
    val manpower: Long,
    val commandPower: Double,
    val worldTension: Double,
    val conscriptionLaw: ConscriptionLaw,
    val economyLaw: EconomyLaw,
    val tradeLaw: TradeLaw,
    val activeFocusId: String = "",
    val activeFocusProgressDays: Int = 0,
    val completedFocusIds: List<String> = emptyList(),
    val activeResearchId: String = "",
    val activeResearchProgressDays: Int = 0,
    val unlockedTechIds: List<String> = emptyList(),
    val provinceOwners: Map<String, String>,
    val provinceCivFactories: Map<String, Int>,
    val provinceMilFactories: Map<String, Int>,
    val provinceForts: Map<String, Int>,
    val provinceInfra: Map<String, Int>,
    val equipmentStockpile: Map<EquipmentType, Int>,
    val diplomacyStates: Map<String, DiplomacyStatus>,
    val nationRelations: Map<String, Int>,
    val warGoalProgress: Map<String, Int>,
    val divisions: List<DivisionState>,
    val productionLines: List<ProductionLine>,
    val constructionQueue: List<ConstructionOrder>,
    val trainingQueue: List<TrainingOrder>,
    val status: String = "active",
    val battleplanAutoAdvance: Boolean = false,
    val combatBubbles: List<CombatBubble> = emptyList(),
    val newsLog: List<String> = emptyList()
) {
    val playerCivFactories: Int
        get() = provinceOwners.entries.sumOf { (provId, owner) ->
            if (owner == playerNationId) provinceCivFactories[provId] ?: 0 else 0
        }

    val playerMilFactories: Int
        get() = provinceOwners.entries.sumOf { (provId, owner) ->
            if (owner == playerNationId) provinceMilFactories[provId] ?: 0 else 0
        }

    val assignedMilFactories: Int
        get() = productionLines.sumOf { it.assignedFactories }

    val freeMilFactories: Int
        get() = (playerMilFactories - assignedMilFactories).coerceAtLeast(0)

    val playerDivisionCount: Int
        get() = divisions.count { it.nationId == playerNationId }

    val playerProvinceCount: Int
        get() = provinceOwners.values.count { it == playerNationId }

    val atWarNations: Set<String>
        get() = diplomacyStates.filterValues { it == DiplomacyStatus.WAR }.keys

    val isAtWar: Boolean
        get() = atWarNations.isNotEmpty()
}
