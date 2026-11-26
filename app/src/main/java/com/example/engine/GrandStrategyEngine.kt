package com.example.engine

import com.example.data.TheaterWorldData
import com.example.model.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object GrandStrategyEngine {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

    fun formatDate(epochDay: Long, hour: Int): String {
        val dateStr = LocalDate.ofEpochDay(epochDay).format(dateFormatter)
        val hh = hour.toString().padStart(2, '0')
        return "$dateStr $hh:00"
    }

    fun formatShortDate(epochDay: Long): String {
        return LocalDate.ofEpochDay(epochDay).format(dateFormatter)
    }

    fun advanceHour(state: CampaignState): CampaignState {
        if (state.status != "active") return state

        var nextHour = state.gameHour + 1
        var nextEpochDay = state.gameDateEpochDay
        val isNewDay = nextHour >= 24
        if (isNewDay) {
            nextHour = 0
            nextEpochDay += 1
        }

        var working = state.copy(gameHour = nextHour, gameDateEpochDay = nextEpochDay)

        // 1. Hourly Movement & Combat Resolution
        working = resolveMovementAndCombat(working)

        // 2. Daily Ticks (at 00:00)
        if (isNewDay) {
            working = resolveDailyEconomyAndPolitics(working)
            working = resolveAiNationsDaily(working)
            if (working.battleplanAutoAdvance) {
                working = applyBattleplanAutoOrders(working)
            }
            working = checkVictoryOrDefeat(working)
        }

        return working
    }

    private fun resolveMovementAndCombat(state: CampaignState): CampaignState {
        val owners = state.provinceOwners.toMutableMap()
        val divisions = state.divisions.map { it.copy() }.toMutableList()
        val news = state.newsLog.toMutableList()
        val diplomacy = state.diplomacyStates.toMutableMap()
        val bubbles = mutableListOf<CombatBubble>()

        val hasDoctrineSpeed = "tech_doc_2" in state.unlockedTechIds
        val hasDoctrineOrg = "tech_doc_1" in state.unlockedTechIds
        val hasDoctrineEntrench = "tech_doc_3" in state.unlockedTechIds
        val hasInf1 = "tech_inf_1" in state.unlockedTechIds
        val hasInf2 = "tech_inf_2" in state.unlockedTechIds
        val hasInf3 = "tech_inf_3" in state.unlockedTechIds
        val hasArm1 = "tech_arm_1" in state.unlockedTechIds
        val hasArm2 = "tech_arm_2" in state.unlockedTechIds
        val hasArm3 = "tech_arm_3" in state.unlockedTechIds

        val playerTechAttackMult = 1.0 +
            (if (hasInf1) 0.12 else 0.0) +
            (if (hasInf3) 0.18 else 0.0) +
            (if (hasArm2) 0.15 else 0.0) +
            (if (hasArm3) 0.20 else 0.0)
        val playerTechDefMult = 1.0 +
            (if (hasInf2) 0.18 else 0.0) +
            (if (hasDoctrineEntrench) 0.15 else 0.0)
        val playerBreakthroughMult = 1.0 +
            (if (hasArm1) 0.15 else 0.0) +
            (if ("focus_blitz" in state.completedFocusIds) 0.15 else 0.0)

        fun areAtWar(a: String, b: String): Boolean {
            if (a == b) return false
            if (a == state.playerNationId) return diplomacy[b] == DiplomacyStatus.WAR
            if (b == state.playerNationId) return diplomacy[a] == DiplomacyStatus.WAR
            // AI-vs-AI war when world tension >= 0.45 and different ideologies
            val natA = TheaterWorldData.nations[a]
            val natB = TheaterWorldData.nations[b]
            return state.worldTension >= 0.45 && natA != null && natB != null && natA.ideology != natB.ideology
        }

        // Group attacking divisions by target province
        val attackersByTarget = divisions
            .filter { it.targetProvinceId.isNotBlank() && it.org > 4.0 }
            .groupBy { it.targetProvinceId }

        val capturedProvinces = mutableListOf<String>()

        for ((targetId, attackers) in attackersByTarget) {
            val targetProv = TheaterWorldData.provinceMap[targetId] ?: continue
            val currentOwner = owners[targetId] ?: continue

            // Check if any attacker is actually hostile to currentOwner
            val hostileAttackers = attackers.filter { areAtWar(it.nationId, currentOwner) }
            val friendlyMovers = attackers.filter { !areAtWar(it.nationId, currentOwner) && it.nationId == currentOwner }

            // Friendly strategic redeployment / movement
            for (div in friendlyMovers) {
                val idx = divisions.indexOfFirst { it.id == div.id }
                if (idx >= 0) {
                    val infra = state.provinceInfra[targetId] ?: 5
                    val speed = div.template.speedKph * targetProv.terrain.speedMod * (1.0 + infra * 0.04) *
                        (if (div.nationId == state.playerNationId && hasDoctrineSpeed) 1.15 else 1.0)
                    val nextProg = div.moveProgress + (speed * 0.014)
                    if (nextProg >= 1.0) {
                        divisions[idx] = div.copy(provinceId = targetId, targetProvinceId = "", moveProgress = 0.0, entrenchment = 1.0)
                    } else {
                        divisions[idx] = div.copy(moveProgress = nextProg, entrenchment = 0.0)
                    }
                }
            }

            if (hostileAttackers.isEmpty()) continue

            val defenders = divisions.filter { it.provinceId == targetId && it.nationId == currentOwner && it.org > 1.0 }
            if (defenders.isEmpty()) {
                // Uncontested advance!
                for (att in hostileAttackers) {
                    val idx = divisions.indexOfFirst { it.id == att.id }
                    if (idx >= 0) {
                        val infra = state.provinceInfra[targetId] ?: 5
                        val speed = att.template.speedKph * targetProv.terrain.speedMod * (1.0 + infra * 0.04)
                        val nextProg = att.moveProgress + (speed * 0.018)
                        if (nextProg >= 1.0) {
                            val newOwner = att.nationId
                            owners[targetId] = newOwner
                            divisions[idx] = att.copy(provinceId = targetId, targetProvinceId = "", moveProgress = 0.0, entrenchment = 1.0)
                            if (targetId !in capturedProvinces) {
                                capturedProvinces += targetId
                                val nationName = TheaterWorldData.nations[newOwner]?.shortTag ?: newOwner
                                news.add(0, "${formatShortDate(state.gameDateEpochDay)} — $nationName forces captured ${targetProv.name}!")
                            }
                        } else {
                            divisions[idx] = att.copy(moveProgress = nextProg, entrenchment = 0.0)
                        }
                    }
                }
            } else {
                // Active Frontline Battle!
                val fortLevel = state.provinceForts[targetId] ?: 0
                val fortPenalty = (1.0 - fortLevel * 0.11).coerceAtLeast(0.45)

                var totalAttackPower = 0.0
                for (att in hostileAttackers) {
                    val baseAtk = att.template.softAttack * 0.90 + att.template.hardAttack * 0.35 + att.template.breakthrough * 0.55
                    val bonus = if (att.nationId == state.playerNationId) {
                        state.difficulty.playerBonus * playerTechAttackMult * playerBreakthroughMult
                    } else {
                        state.difficulty.aiBonus
                    }
                    totalAttackPower += baseAtk * bonus * (att.hp / 100.0) * targetProv.terrain.attackMod * fortPenalty
                }

                var totalDefensePower = 0.0
                for (def in defenders) {
                    val baseDef = def.template.defense * 0.72 + def.template.softAttack * 0.30
                    val entrenchMult = 1.0 + (def.entrenchment * 0.025)
                    val bonus = if (def.nationId == state.playerNationId) {
                        state.difficulty.playerBonus * playerTechDefMult
                    } else {
                        state.difficulty.aiBonus
                    }
                    totalDefensePower += baseDef * bonus * entrenchMult * (def.hp / 100.0) * targetProv.terrain.defenseMod
                }

                val ratio = (totalAttackPower / max(1.0, totalAttackPower + totalDefensePower))
                val advantagePct = (ratio * 100.0).roundToInt().coerceIn(5, 95)

                bubbles += CombatBubble(
                    provinceId = targetId,
                    attackerNationId = hostileAttackers.first().nationId,
                    defenderNationId = currentOwner,
                    attackerAdvantagePct = advantagePct,
                    attackerCount = hostileAttackers.size,
                    defenderCount = defenders.size
                )

                // Apply hourly ORG and minor HP damage
                val defOrgDamage = (totalAttackPower / max(1, defenders.size)) * 0.068
                val attOrgDamage = (totalDefensePower / max(1, hostileAttackers.size)) * 0.036

                for (def in defenders) {
                    val idx = divisions.indexOfFirst { it.id == def.id }
                    if (idx >= 0) {
                        val nextOrg = (def.org - defOrgDamage).coerceAtLeast(0.0)
                        val nextHp = (def.hp - defOrgDamage * 0.16).coerceAtLeast(18.0)
                        divisions[idx] = def.copy(org = nextOrg, hp = nextHp, entrenchment = (def.entrenchment - 0.2).coerceAtLeast(0.0))
                    }
                }

                for (att in hostileAttackers) {
                    val idx = divisions.indexOfFirst { it.id == att.id }
                    if (idx >= 0) {
                        val nextOrg = (att.org - attOrgDamage).coerceAtLeast(0.0)
                        val nextHp = (att.hp - attOrgDamage * 0.14).coerceAtLeast(18.0)
                        val marchBump = if (ratio > 0.52) 0.02 else 0.005
                        val nextProg = if (nextOrg > 5.0) (att.moveProgress + marchBump).coerceAtMost(0.95) else att.moveProgress
                        divisions[idx] = att.copy(
                            org = nextOrg,
                            hp = nextHp,
                            moveProgress = nextProg,
                            targetProvinceId = if (nextOrg <= 3.0) "" else att.targetProvinceId,
                            entrenchment = 0.0
                        )
                    }
                }

                // Check if defenders broke (all <= 2.0 ORG)
                val broken = defenders.all { d ->
                    val updated = divisions.find { it.id == d.id }
                    (updated?.org ?: 0.0) <= 2.0
                }
                if (broken) {
                    val retreatProvId = targetProv.neighbors.firstOrNull { owners[it] == currentOwner }
                    for (def in defenders) {
                        val idx = divisions.indexOfFirst { it.id == def.id }
                        if (idx >= 0) {
                            if (retreatProvId != null) {
                                divisions[idx] = divisions[idx].copy(provinceId = retreatProvId, targetProvinceId = "", org = 10.0)
                            } else {
                                // Encircled & shattered!
                                divisions[idx] = divisions[idx].copy(hp = 0.0, org = 0.0)
                            }
                        }
                    }
                    divisions.removeAll { it.hp <= 0.0 }

                    val victorNation = hostileAttackers.first().nationId
                    owners[targetId] = victorNation
                    val leadAttackerIdx = divisions.indexOfFirst { it.id == hostileAttackers.first().id }
                    if (leadAttackerIdx >= 0) {
                        divisions[leadAttackerIdx] = divisions[leadAttackerIdx].copy(
                            provinceId = targetId,
                            targetProvinceId = "",
                            moveProgress = 0.0,
                            entrenchment = 1.0
                        )
                    }
                    val tag = TheaterWorldData.nations[victorNation]?.shortTag ?: victorNation
                    news.add(0, "${formatShortDate(state.gameDateEpochDay)} — Breakthrough! $tag seized ${targetProv.name}.")
                }
            }
        }

        // Passive hourly ORG recovery and entrenchment for stationary units not in combat
        val activeCombatTargets = bubbles.map { it.provinceId }.toSet()
        for (i in divisions.indices) {
            val d = divisions[i]
            if (d.targetProvinceId.isBlank() && d.provinceId !in activeCombatTargets) {
                val maxOrgBonus = if (d.nationId == state.playerNationId && hasDoctrineOrg) 10.0 else 0.0
                val maxOrg = d.template.maxOrg + maxOrgBonus
                val recRate = if (d.nationId == state.playerNationId && hasDoctrineSpeed) 0.9 else 0.65
                val nextOrg = min(maxOrg, d.org + recRate)
                val maxEntrench = if (d.nationId == state.playerNationId && hasDoctrineEntrench) 16.0 else 10.0
                val nextEntrench = min(maxEntrench, d.entrenchment + 0.15)
                val nextHp = min(d.template.maxHp, d.hp + 0.12)
                divisions[i] = d.copy(org = nextOrg, entrenchment = nextEntrench, hp = nextHp)
            }
        }

        // Check Capitulation of any non-player or player nation
        val resultAfterCap = checkNationCapitulation(owners, divisions, diplomacy, news, state)

        return state.copy(
            provinceOwners = resultAfterCap.owners,
            divisions = resultAfterCap.divisions,
            diplomacyStates = resultAfterCap.diplomacy,
            combatBubbles = bubbles,
            newsLog = resultAfterCap.news.take(30)
        )
    }

    private data class CapResult(
        val owners: Map<String, String>,
        val divisions: List<DivisionState>,
        val diplomacy: Map<String, DiplomacyStatus>,
        val news: List<String>
    )

    private fun checkNationCapitulation(
        owners: MutableMap<String, String>,
        divisions: MutableList<DivisionState>,
        diplomacy: MutableMap<String, DiplomacyStatus>,
        news: MutableList<String>,
        state: CampaignState
    ): CapResult {
        val currentNations = owners.values.toSet()
        for (nationId in TheaterWorldData.nations.keys) {
            if (nationId == state.playerNationId || nationId == "NEU") continue
            val nationProvs = TheaterWorldData.provinces.filter { it.initialOwnerId == nationId }
            val totalCoreVp = nationProvs.sumOf { it.victoryPoints }.coerceAtLeast(1)
            val heldVp = TheaterWorldData.provinces.filter { owners[it.id] == nationId }.sumOf { it.victoryPoints }
            val capProvince = TheaterWorldData.nations[nationId]?.capitalProvinceId
            val lostCapital = capProvince != null && owners[capProvince] != nationId

            if (nationId in currentNations && lostCapital && heldVp <= totalCoreVp * 0.30) {
                val conqueror = owners[capProvince] ?: state.playerNationId
                for ((provId, owner) in owners.toMap()) {
                    if (owner == nationId) owners[provId] = conqueror
                }
                divisions.removeAll { it.nationId == nationId }
                diplomacy[nationId] = DiplomacyStatus.PEACE
                val loserName = TheaterWorldData.nations[nationId]?.name ?: nationId
                val winnerTag = TheaterWorldData.nations[conqueror]?.shortTag ?: conqueror
                news.add(0, "${formatShortDate(state.gameDateEpochDay)} — $loserName has CAPITULATED to $winnerTag!")
            }
        }
        return CapResult(owners, divisions, diplomacy, news)
    }

    private fun resolveDailyEconomyAndPolitics(state: CampaignState): CampaignState {
        var pp = state.politicalPower
        var stability = state.stability
        var warSupport = state.warSupport
        var manpower = state.manpower
        var commandPower = state.commandPower
        var worldTension = state.worldTension

        val owners = state.provinceOwners.toMutableMap()
        val civs = state.provinceCivFactories.toMutableMap()
        val mils = state.provinceMilFactories.toMutableMap()
        val forts = state.provinceForts.toMutableMap()
        val infra = state.provinceInfra.toMutableMap()
        val stockpile = state.equipmentStockpile.toMutableMap()
        val news = state.newsLog.toMutableList()

        // 1. Daily Political Power & Command Power & Manpower
        val focusBonusPP = if (state.activeFocusId.isNotBlank()) 1.0 else 2.0
        val stabMult = 0.75 + stability * 0.5
        pp = (pp + focusBonusPP * stabMult).coerceAtMost(2000.0)
        commandPower = (commandPower + 1.2).coerceAtMost(200.0)

        val ownedProvs = owners.count { it.value == state.playerNationId }
        val dailyManpower = (ownedProvs * 450L * (state.conscriptionLaw.recruitablePct / 0.03)).toLong()
        manpower += dailyManpower

        // 2. National Focus Progress
        var activeFocusId = state.activeFocusId
        var focusDays = state.activeFocusProgressDays
        val completedFocuses = state.completedFocusIds.toMutableList()
        if (activeFocusId.isNotBlank()) {
            val focus = TheaterWorldData.focusTree.find { it.id == activeFocusId }
            if (focus != null) {
                focusDays += 1
                if (focusDays >= focus.daysRequired) {
                    completedFocuses += focus.id
                    activeFocusId = ""
                    focusDays = 0
                    val capId = TheaterWorldData.nations[state.playerNationId]?.capitalProvinceId
                    when (focus.id) {
                        "focus_four_year" -> if (capId != null) civs[capId] = (civs[capId] ?: 2) + 2
                        "focus_synthetic" -> {
                            if (capId != null) mils[capId] = (mils[capId] ?: 2) + 2
                            stockpile[EquipmentType.INFANTRY_EQ] = (stockpile[EquipmentType.INFANTRY_EQ] ?: 0) + 80
                        }
                        "focus_total_war" -> {
                            if (capId != null) mils[capId] = (mils[capId] ?: 2) + 3
                            warSupport = (warSupport + 0.10).coerceAtMost(1.0)
                        }
                        "focus_general_staff" -> {
                            commandPower = (commandPower + 35.0).coerceAtMost(200.0)
                            pp += 50.0
                        }
                        "focus_blitz" -> {
                            stockpile[EquipmentType.LIGHT_TANK] = (stockpile[EquipmentType.LIGHT_TANK] ?: 0) + 40
                        }
                        "focus_fortress" -> {
                            if (capId != null) forts[capId] = (forts[capId] ?: 0) + 2
                            val borderProv = owners.keys.firstOrNull { owners[it] == state.playerNationId && it != capId }
                            if (borderProv != null) forts[borderProv] = (forts[borderProv] ?: 0) + 2
                        }
                        "focus_rally_nation" -> {
                            pp += 120.0
                            stability = (stability + 0.08).coerceAtMost(1.0)
                        }
                        "focus_rearmament" -> {
                            manpower += 180_000L
                            warSupport = (warSupport + 0.10).coerceAtMost(1.0)
                        }
                        "focus_hegemony" -> {
                            pp += 150.0
                            worldTension = (worldTension + 0.10).coerceAtMost(1.0)
                        }
                    }
                    news.add(0, "${formatShortDate(state.gameDateEpochDay)} — National Focus Completed: ${focus.title}.")
                }
            }
        }

        // 3. Technology Research Progress
        var activeResearchId = state.activeResearchId
        var researchDays = state.activeResearchProgressDays
        val unlockedTechs = state.unlockedTechIds.toMutableList()
        if (activeResearchId.isNotBlank()) {
            val tech = TheaterWorldData.techTree.find { it.id == activeResearchId }
            if (tech != null) {
                val researchSpeed = 1.0 + state.tradeLaw.researchBonus +
                    (if ("tech_ind_3" in unlockedTechs) 0.20 else 0.0)
                val step = if (researchSpeed >= 1.2) 2 else 1
                researchDays += step
                if (researchDays >= tech.daysRequired) {
                    unlockedTechs += tech.id
                    activeResearchId = ""
                    researchDays = 0
                    news.add(0, "${formatShortDate(state.gameDateEpochDay)} — Research Complete: ${tech.title}.")
                }
            }
        }

        // 4. Construction Queue Progress (powered by usable Civilian Factories)
        val totalCiv = state.playerCivFactories
        val usableCiv = max(1, (totalCiv * (1.0 - state.economyLaw.consumerGoodsPct)).roundToInt())
        val buildSpeedBonus = 1.0 + (if ("tech_ind_2" in unlockedTechs) 0.20 else 0.0)
        val nextConstruction = state.constructionQueue.map { it.copy() }.toMutableList()
        if (nextConstruction.isNotEmpty()) {
            val top = nextConstruction[0]
            val milMod = if (top.buildingType == BuildingType.MIL_FACTORY) state.economyLaw.milBuildBonus else 1.0
            val dailyBuildPoints = max(1, ((usableCiv * 0.45 + 1.0) * buildSpeedBonus * milMod).roundToInt())
            val updatedProg = top.progressDays + dailyBuildPoints
            if (updatedProg >= top.totalDays) {
                nextConstruction.removeAt(0)
                if (owners[top.provinceId] == state.playerNationId) {
                    when (top.buildingType) {
                        BuildingType.CIV_FACTORY -> civs[top.provinceId] = (civs[top.provinceId] ?: 0) + 1
                        BuildingType.MIL_FACTORY -> mils[top.provinceId] = (mils[top.provinceId] ?: 0) + 1
                        BuildingType.LAND_FORT -> forts[top.provinceId] = ((forts[top.provinceId] ?: 0) + 1).coerceAtMost(10)
                        BuildingType.INFRASTRUCTURE -> infra[top.provinceId] = ((infra[top.provinceId] ?: 5) + 1).coerceAtMost(10)
                    }
                    val pName = TheaterWorldData.provinceMap[top.provinceId]?.name ?: top.provinceId
                    news.add(0, "${formatShortDate(state.gameDateEpochDay)} — Built ${top.buildingType.displayName} in $pName.")
                }
            } else {
                nextConstruction[0] = top.copy(progressDays = updatedProg)
            }
        }

        // 5. Military Factory Production Lines
        val outputBonus = 1.0 + state.tradeLaw.factoryOutputBonus +
            (if ("tech_ind_1" in unlockedTechs) 0.15 else 0.0) +
            (if ("tech_ind_2" in unlockedTechs) 0.10 else 0.0)
        val nextLines = state.productionLines.map { line ->
            if (line.assignedFactories <= 0) {
                line
            } else {
                val dailyIc = line.assignedFactories * 5.2 * outputBonus
                val totalProgress = line.progress + (dailyIc / line.equipmentType.icCost)
                val producedUnits = totalProgress.toInt()
                if (producedUnits > 0) {
                    stockpile[line.equipmentType] = (stockpile[line.equipmentType] ?: 0) + producedUnits
                }
                line.copy(progress = totalProgress - producedUnits)
            }
        }

        // 6. Division Training Queue
        val nextDivisions = state.divisions.toMutableList()
        val nextTraining = mutableListOf<TrainingOrder>()
        var divCounter = (nextDivisions.maxOfOrNull { it.id.removePrefix("div_").toIntOrNull() ?: 0 } ?: 100) + 1
        for (order in state.trainingQueue) {
            val nextProg = order.progressDays + 1
            if (nextProg >= order.totalDays) {
                val deployProv = if (owners[order.deployProvinceId] == state.playerNationId) {
                    order.deployProvinceId
                } else {
                    owners.entries.firstOrNull { it.value == state.playerNationId }?.key ?: order.deployProvinceId
                }
                val tag = TheaterWorldData.nations[state.playerNationId]?.shortTag ?: state.playerNationId
                nextDivisions += DivisionState(
                    id = "div_${divCounter++}",
                    name = "${state.playerDivisionCount + 1}. $tag ${order.template.symbolCode}",
                    nationId = state.playerNationId,
                    template = order.template,
                    provinceId = deployProv,
                    hp = 100.0,
                    org = order.template.maxOrg,
                    entrenchment = 2.0
                )
                val pName = TheaterWorldData.provinceMap[deployProv]?.name ?: deployProv
                news.add(0, "${formatShortDate(state.gameDateEpochDay)} — Deployed new ${order.template.displayName} in $pName.")
            } else {
                nextTraining += order.copy(progressDays = nextProg)
            }
        }

        // 7. War Goal Justifications Countdown
        val nextWarGoals = state.warGoalProgress.mapValues { (_, daysLeft) ->
            if (daysLeft > 0) daysLeft - 1 else 0
        }.toMutableMap()

        // Slight natural world tension drift
        if (state.isAtWar) {
            worldTension = (worldTension + 0.002).coerceAtMost(1.0)
        }

        return state.copy(
            politicalPower = pp,
            stability = stability,
            warSupport = warSupport,
            manpower = manpower,
            commandPower = commandPower,
            worldTension = worldTension,
            activeFocusId = activeFocusId,
            activeFocusProgressDays = focusDays,
            completedFocusIds = completedFocuses,
            activeResearchId = activeResearchId,
            activeResearchProgressDays = researchDays,
            unlockedTechIds = unlockedTechs,
            provinceCivFactories = civs,
            provinceMilFactories = mils,
            provinceForts = forts,
            provinceInfra = infra,
            equipmentStockpile = stockpile,
            productionLines = nextLines,
            constructionQueue = nextConstruction,
            trainingQueue = nextTraining,
            warGoalProgress = nextWarGoals,
            divisions = nextDivisions,
            newsLog = news.take(30)
        )
    }

    private fun resolveAiNationsDaily(state: CampaignState): CampaignState {
        val owners = state.provinceOwners
        val divisions = state.divisions.map { it.copy() }.toMutableList()
        val diplomacy = state.diplomacyStates.toMutableMap()
        val news = state.newsLog.toMutableList()
        var worldTension = state.worldTension

        // Every ~12 days AI nations can recruit a division if under cap
        val dayIndex = (state.gameDateEpochDay - TheaterWorldData.EPOCH_DAY_1936_01_01).toInt()
        if (dayIndex > 0 && dayIndex % 14 == 0) {
            var nextSeq = (divisions.maxOfOrNull { it.id.removePrefix("div_").toIntOrNull() ?: 0 } ?: 200) + 1
            for ((nationId, nation) in TheaterWorldData.nations) {
                if (nationId == state.playerNationId || nationId == "NEU") continue
                val ownedProvs = TheaterWorldData.provinces.filter { owners[it.id] == nationId }
                if (ownedProvs.isEmpty()) continue
                val currentCount = divisions.count { it.nationId == nationId }
                val cap = (ownedProvs.size * 3).coerceAtMost(18)
                if (currentCount < cap) {
                    val spawn = ownedProvs[dayIndex % ownedProvs.size]
                    val tpl = if (currentCount % 4 == 0) DivisionTemplate.ARMOR else DivisionTemplate.INFANTRY
                    divisions += DivisionState(
                        id = "div_${nextSeq++}",
                        name = "${currentCount + 1}. ${nation.shortTag} ${tpl.symbolCode}",
                        nationId = nationId,
                        template = tpl,
                        provinceId = spawn.id,
                        hp = 100.0,
                        org = tpl.maxOrg
                    )
                }
            }
        }

        // AI War & Frontline Movement
        val aiNations = TheaterWorldData.nations.keys.filter { it != state.playerNationId }
        for (aiId in aiNations) {
            val aiDivs = divisions.filter { it.nationId == aiId && it.targetProvinceId.isBlank() && it.org > 25.0 }
            if (aiDivs.isEmpty()) continue

            val atWarWithPlayer = diplomacy[aiId] == DiplomacyStatus.WAR
            for (div in aiDivs) {
                val prov = TheaterWorldData.provinceMap[div.provinceId] ?: continue
                val hostileNeighbors = prov.neighbors.filter { nId ->
                    val nOwner = owners[nId] ?: return@filter false
                    if (nOwner == state.playerNationId) atWarWithPlayer
                    else if (nOwner != aiId && worldTension >= 0.45) {
                        val aIdeo = TheaterWorldData.nations[aiId]?.ideology
                        val bIdeo = TheaterWorldData.nations[nOwner]?.ideology
                        aIdeo != bIdeo
                    } else false
                }

                if (hostileNeighbors.isNotEmpty()) {
                    // Pick weakest neighbor
                    val target = hostileNeighbors.minByOrNull { nId ->
                        divisions.count { it.provinceId == nId && it.nationId == owners[nId] }
                    }
                    if (target != null) {
                        val enemyStrength = divisions.count { it.provinceId == target && it.nationId == owners[target] }
                        val localFriends = divisions.count { it.provinceId == div.provinceId && it.nationId == aiId }
                        if (localFriends >= enemyStrength) {
                            val idx = divisions.indexOfFirst { it.id == div.id }
                            if (idx >= 0) divisions[idx] = div.copy(targetProvinceId = target)
                        }
                    }
                } else if (atWarWithPlayer) {
                    // Move toward frontline bordering player
                    val stepTowardFront = prov.neighbors.firstOrNull { nId ->
                        owners[nId] == aiId &&
                            (TheaterWorldData.provinceMap[nId]?.neighbors?.any { owners[it] == state.playerNationId } == true)
                    }
                    if (stepTowardFront != null) {
                        val idx = divisions.indexOfFirst { it.id == div.id }
                        if (idx >= 0) divisions[idx] = div.copy(targetProvinceId = stepTowardFront)
                    }
                }
            }
        }

        // Scripted historical escalation flavor if world tension is still low
        if (dayIndex == 60 && worldTension < 0.25) {
            worldTension = (worldTension + 0.08).coerceAtMost(1.0)
            news.add(0, "${formatShortDate(state.gameDateEpochDay)} — Global rearmament crises raise World Tension.")
        }

        return state.copy(
            divisions = divisions,
            worldTension = worldTension,
            newsLog = news.take(30)
        )
    }

    private fun applyBattleplanAutoOrders(state: CampaignState): CampaignState {
        val owners = state.provinceOwners
        val divisions = state.divisions.map { it.copy() }.toMutableList()
        val atWarSet = state.atWarNations
        if (atWarSet.isEmpty()) return state

        for (i in divisions.indices) {
            val d = divisions[i]
            if (d.nationId != state.playerNationId || d.targetProvinceId.isNotBlank() || d.org < 28.0) continue
            val prov = TheaterWorldData.provinceMap[d.provinceId] ?: continue

            val enemyAdj = prov.neighbors.filter { nId -> owners[nId] in atWarSet }
            if (enemyAdj.isNotEmpty()) {
                val bestTarget = enemyAdj.minByOrNull { nId ->
                    divisions.count { it.provinceId == nId && it.nationId == owners[nId] }
                }
                if (bestTarget != null) {
                    divisions[i] = d.copy(targetProvinceId = bestTarget)
                }
            } else {
                // Advance idle rear divisions toward the active front
                val towardFront = prov.neighbors.firstOrNull { nId ->
                    owners[nId] == state.playerNationId &&
                        (TheaterWorldData.provinceMap[nId]?.neighbors?.any { owners[it] in atWarSet } == true)
                }
                if (towardFront != null) {
                    divisions[i] = d.copy(targetProvinceId = towardFront)
                }
            }
        }
        return state.copy(divisions = divisions)
    }

    private fun checkVictoryOrDefeat(state: CampaignState): CampaignState {
        val playerProvs = state.provinceOwners.count { it.value == state.playerNationId }
        val totalProvs = state.provinceOwners.size.coerceAtLeast(1)
        val capId = TheaterWorldData.nations[state.playerNationId]?.capitalProvinceId

        if (playerProvs == 0 || (capId != null && state.provinceOwners[capId] != state.playerNationId && playerProvs <= 1)) {
            return state.copy(
                status = "defeat",
                newsLog = listOf("${formatShortDate(state.gameDateEpochDay)} — Our capital has fallen. High Command surrenders.") + state.newsLog
            )
        }
        if (playerProvs >= (totalProvs * 0.55).roundToInt()) {
            return state.copy(
                status = "victory",
                newsLog = listOf("${formatShortDate(state.gameDateEpochDay)} — Global Hegemony Achieved! The world bows to our command.") + state.newsLog
            )
        }
        return state
    }
}
