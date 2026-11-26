package com.example

import com.example.data.TheaterWorldData
import com.example.engine.GrandStrategyEngine
import com.example.model.Difficulty
import com.example.model.DiplomacyStatus
import com.example.model.EquipmentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GrandStrategyEngineTest {

    @Test
    fun provinceAdjacencyGraphIsSymmetric() {
        for (prov in TheaterWorldData.provinces) {
            for (neighborId in prov.neighbors) {
                val neighbor = TheaterWorldData.provinceMap[neighborId]
                assertTrue("Neighbor $neighborId missing for ${prov.id}", neighbor != null)
                assertTrue(
                    "Asymmetric edge: ${prov.id} -> $neighborId",
                    prov.id in neighbor!!.neighbors
                )
            }
        }
    }

    @Test
    fun allPlayableNationsInitializeCleanly() {
        for (nation in TheaterWorldData.playableNations) {
            val state = TheaterWorldData.createInitialCampaignState(
                playerNationId = nation.id,
                campaignName = "${nation.name} Test",
                difficulty = Difficulty.REGULAR
            )
            assertTrue("${nation.id} should own at least 1 province", state.playerProvinceCount >= 1)
            assertTrue("${nation.id} should have divisions", state.playerDivisionCount >= 4)
            assertTrue("${nation.id} should have factories", state.playerCivFactories + state.playerMilFactories > 0)
        }
    }

    @Test
    fun nationalFocusCompletesAndGrantsFactories() {
        var state = TheaterWorldData.createInitialCampaignState("GER", "Test", Difficulty.REGULAR)
            .copy(activeFocusId = "focus_four_year", activeFocusProgressDays = 0)
        val initialCiv = state.playerCivFactories

        // Advance 21 full days (21 * 24 hours)
        repeat(21 * 24) {
            state = GrandStrategyEngine.advanceHour(state)
        }

        assertTrue("Focus should be completed", "focus_four_year" in state.completedFocusIds)
        assertEquals("", state.activeFocusId)
        assertEquals(initialCiv + 2, state.playerCivFactories)
    }

    @Test
    fun militaryProductionAddsEquipmentStockpile() {
        var state = TheaterWorldData.createInitialCampaignState("GER", "ProdTest", Difficulty.REGULAR)
        val initialInf = state.equipmentStockpile[EquipmentType.INFANTRY_EQ] ?: 0

        repeat(48) {
            state = GrandStrategyEngine.advanceHour(state)
        }

        val afterInf = state.equipmentStockpile[EquipmentType.INFANTRY_EQ] ?: 0
        assertTrue("Infantry equipment stockpile should increase ($initialInf -> $afterInf)", afterInf > initialInf)
    }

    @Test
    fun wartimeAdvanceCapturesEnemyProvince() {
        var state = TheaterWorldData.createInitialCampaignState("GER", "WarTest", Difficulty.RECRUIT)
        val nextDip = state.diplomacyStates.toMutableMap().apply { put("POL", DiplomacyStatus.WAR) }
        // Stage all German divisions along the Polish border (Berlin & Konigsberg) and launch a concentrated offensive on Warsaw
        val attackingDivs = state.divisions.map { d ->
            if (d.nationId == "GER") {
                val startProv = if (d.provinceId == "konigsberg") "konigsberg" else "berlin"
                d.copy(provinceId = startProv, targetProvinceId = "warsaw")
            } else d
        }
        state = state.copy(diplomacyStates = nextDip, divisions = attackingDivs)

        // Advance 21 days of combat
        repeat(21 * 24) {
            state = GrandStrategyEngine.advanceHour(state)
        }

        assertEquals("GER", state.provinceOwners["warsaw"])
    }
}
