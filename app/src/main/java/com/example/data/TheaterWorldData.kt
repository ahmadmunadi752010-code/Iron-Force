package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.model.*
import com.example.ui.theme.*
import java.time.LocalDate

object TheaterWorldData {

    // Jan 1, 1936 epoch day (-12418)
    val EPOCH_DAY_1936_01_01: Long = LocalDate.of(1936, 1, 1).toEpochDay()

    val nations: Map<String, NationDef> = listOf(
        NationDef(
            id = "GER",
            name = "German Reich",
            shortTag = "GER",
            ideology = Ideology.AUTOCRACY,
            leaderName = "Field Marshal von Beck",
            leaderTitle = "Supreme Commander",
            factionName = "Steel Pact",
            color = NationGermanyColor,
            flagAccent = FrontlineCrimson,
            startingPP = 75.0,
            startingManpower = 1_450_000L,
            startingStability = 0.72,
            startingWarSupport = 0.76,
            summary = "Industrial powerhouse in Central Europe with mobile Panzer doctrine.",
            capitalProvinceId = "berlin"
        ),
        NationDef(
            id = "SOV",
            name = "Soviet Union",
            shortTag = "SOV",
            ideology = Ideology.COMMUNISM,
            leaderName = "Marshal Voroshilov",
            leaderTitle = "General Secretary",
            factionName = "Comintern",
            color = NationSovietColor,
            flagAccent = BrassGold,
            startingPP = 65.0,
            startingManpower = 3_800_000L,
            startingStability = 0.64,
            startingWarSupport = 0.70,
            summary = "Vast Eurasian manpower reserves and deep strategic defense.",
            capitalProvinceId = "moscow"
        ),
        NationDef(
            id = "ENG",
            name = "United Kingdom",
            shortTag = "ENG",
            ideology = Ideology.DEMOCRACY,
            leaderName = "Prime Minister Baldwin",
            leaderTitle = "Prime Minister",
            factionName = "Allies",
            color = NationUkColor,
            flagAccent = AlliedCobalt,
            startingPP = 90.0,
            startingManpower = 1_100_000L,
            startingStability = 0.84,
            startingWarSupport = 0.42,
            summary = "Island bastion with high civilian industry and global supply reach.",
            capitalProvinceId = "london"
        ),
        NationDef(
            id = "FRA",
            name = "French Republic",
            shortTag = "FRA",
            ideology = Ideology.DEMOCRACY,
            leaderName = "Premier Daladier",
            leaderTitle = "President of Council",
            factionName = "Allies",
            color = NationFranceColor,
            flagAccent = ParchmentText,
            startingPP = 55.0,
            startingManpower = 1_050_000L,
            startingStability = 0.56,
            startingWarSupport = 0.48,
            summary = "Fortified western frontier along the Maginot Line.",
            capitalProvinceId = "paris"
        ),
        NationDef(
            id = "POL",
            name = "Polish Republic",
            shortTag = "POL",
            ideology = Ideology.NON_ALIGNED,
            leaderName = "Marshal Rydz-Smigly",
            leaderTitle = "Marshal of Poland",
            factionName = "Intermarium",
            color = NationPolandColor,
            flagAccent = ParchmentText,
            startingPP = 60.0,
            startingManpower = 780_000L,
            startingStability = 0.68,
            startingWarSupport = 0.74,
            summary = "Strategic crossroads between Berlin and Moscow. High difficulty survival.",
            capitalProvinceId = "warsaw"
        ),
        NationDef(
            id = "ITA",
            name = "Kingdom of Italy",
            shortTag = "ITA",
            ideology = Ideology.AUTOCRACY,
            leaderName = "Marshal Badoglio",
            leaderTitle = "Duce of State",
            factionName = "Steel Pact",
            color = NationItalyColor,
            flagAccent = BrassGold,
            startingPP = 65.0,
            startingManpower = 920_000L,
            startingStability = 0.66,
            startingWarSupport = 0.68,
            summary = "Mediterranean peninsular power guarding Alpine passes.",
            capitalProvinceId = "rome"
        ),
        NationDef(
            id = "SPR",
            name = "Iberian Republic",
            shortTag = "SPR",
            ideology = Ideology.DEMOCRACY,
            leaderName = "Manuel Azana",
            leaderTitle = "President",
            factionName = "Non-Aligned",
            color = NationSpainColor,
            flagAccent = FrontlineCrimson,
            startingPP = 50.0,
            startingManpower = 650_000L,
            startingStability = 0.52,
            startingWarSupport = 0.60,
            summary = "Mountainous Iberian bastion poised to reshape Western Europe.",
            capitalProvinceId = "madrid"
        ),
        NationDef(
            id = "SWE",
            name = "Nordic Union",
            shortTag = "SWE",
            ideology = Ideology.DEMOCRACY,
            leaderName = "Per Albin Hansson",
            leaderTitle = "Prime Minister",
            factionName = "Northern League",
            color = NationSwedenColor,
            flagAccent = BrassGold,
            startingPP = 70.0,
            startingManpower = 540_000L,
            startingStability = 0.88,
            startingWarSupport = 0.45,
            summary = "Resource-rich Scandinavian state defending the northern seas.",
            capitalProvinceId = "stockholm"
        ),
        NationDef(
            id = "NEU",
            name = "Central & Balkan States",
            shortTag = "NEU",
            ideology = Ideology.NON_ALIGNED,
            leaderName = "Regional Coalitions",
            leaderTitle = "Council",
            factionName = "Independent",
            color = NationMinorColor,
            flagAccent = MutedSteelText,
            startingPP = 45.0,
            startingManpower = 500_000L,
            startingStability = 0.65,
            startingWarSupport = 0.40,
            summary = "Smaller independent buffer states across Central Europe and the Balkans.",
            capitalProvinceId = "vienna"
        )
    ).associateBy { it.id }

    val playableNations: List<NationDef> = nations.values.filter { it.id != "NEU" }

    private fun hexPoly(cx: Float, cy: Float, rx: Float, ry: Float, skewX: Float = 0f): List<MapPoint> {
        return listOf(
            MapPoint(cx - rx * 0.55f + skewX, cy - ry * 0.92f),
            MapPoint(cx + rx * 0.60f + skewX, cy - ry * 0.88f),
            MapPoint(cx + rx * 0.98f, cy - ry * 0.05f),
            MapPoint(cx + rx * 0.52f - skewX, cy + ry * 0.90f),
            MapPoint(cx - rx * 0.60f - skewX, cy + ry * 0.86f),
            MapPoint(cx - rx * 0.96f, cy + ry * 0.05f)
        )
    }

    val provinces: List<ProvinceDef> = listOf(
        // British Isles
        ProvinceDef("scotland", "Scotland", "Britain", 0.17f, 0.17f, hexPoly(0.17f, 0.17f, 0.048f, 0.055f), TerrainType.MOUNTAIN, 3, "ENG", false, 1, 1, 0, 5, listOf("midlands", "oslo")),
        ProvinceDef("midlands", "Birmingham", "Britain", 0.18f, 0.27f, hexPoly(0.18f, 0.27f, 0.048f, 0.048f), TerrainType.URBAN, 5, "ENG", false, 3, 2, 0, 7, listOf("scotland", "london")),
        ProvinceDef("london", "London", "Britain", 0.20f, 0.36f, hexPoly(0.20f, 0.36f, 0.052f, 0.045f), TerrainType.URBAN, 15, "ENG", true, 5, 4, 1, 9, listOf("midlands", "normandy", "brussels")),

        // Iberia
        ProvinceDef("madrid", "Madrid", "Iberia", 0.11f, 0.72f, hexPoly(0.11f, 0.72f, 0.062f, 0.058f), TerrainType.MOUNTAIN, 10, "SPR", true, 3, 2, 1, 5, listOf("andalusia", "catalonia", "lyon")),
        ProvinceDef("andalusia", "Seville", "Iberia", 0.10f, 0.84f, hexPoly(0.10f, 0.84f, 0.058f, 0.052f), TerrainType.PLAINS, 4, "SPR", false, 2, 1, 0, 4, listOf("madrid", "catalonia")),
        ProvinceDef("catalonia", "Barcelona", "Iberia", 0.20f, 0.74f, hexPoly(0.20f, 0.74f, 0.048f, 0.055f), TerrainType.URBAN, 5, "SPR", false, 2, 2, 0, 6, listOf("madrid", "andalusia", "lyon")),

        // France
        ProvinceDef("normandy", "Normandy", "France", 0.21f, 0.47f, hexPoly(0.21f, 0.47f, 0.052f, 0.048f), TerrainType.PLAINS, 4, "FRA", false, 2, 1, 1, 6, listOf("london", "paris", "lyon")),
        ProvinceDef("paris", "Paris", "France", 0.28f, 0.49f, hexPoly(0.28f, 0.49f, 0.050f, 0.048f), TerrainType.URBAN, 15, "FRA", true, 5, 4, 0, 8, listOf("normandy", "brussels", "maginot", "lyon")),
        ProvinceDef("maginot", "Alsace-Maginot", "France", 0.35f, 0.51f, hexPoly(0.35f, 0.51f, 0.040f, 0.045f), TerrainType.FOREST, 4, "FRA", false, 2, 2, 4, 7, listOf("paris", "brussels", "rhineland", "bavaria", "bern", "lyon")),
        ProvinceDef("lyon", "Lyon-Marseille", "France", 0.28f, 0.63f, hexPoly(0.28f, 0.63f, 0.058f, 0.060f), TerrainType.MOUNTAIN, 6, "FRA", false, 3, 2, 1, 6, listOf("normandy", "paris", "maginot", "bern", "milan", "catalonia", "madrid")),

        // Benelux & Alps
        ProvinceDef("brussels", "Brussels", "Low Countries", 0.32f, 0.41f, hexPoly(0.32f, 0.41f, 0.038f, 0.036f), TerrainType.URBAN, 6, "FRA", false, 3, 2, 1, 8, listOf("london", "paris", "maginot", "amsterdam", "rhineland")),
        ProvinceDef("amsterdam", "Amsterdam", "Low Countries", 0.34f, 0.33f, hexPoly(0.34f, 0.33f, 0.040f, 0.036f), TerrainType.MARSH, 5, "NEU", false, 3, 1, 0, 8, listOf("brussels", "rhineland", "hamburg")),
        ProvinceDef("bern", "Alpine Zurich", "Alps", 0.38f, 0.60f, hexPoly(0.38f, 0.60f, 0.042f, 0.038f), TerrainType.MOUNTAIN, 4, "NEU", false, 2, 1, 3, 6, listOf("maginot", "lyon", "bavaria", "vienna", "milan")),

        // Scandinavia
        ProvinceDef("oslo", "Oslo", "Scandinavia", 0.38f, 0.14f, hexPoly(0.38f, 0.14f, 0.058f, 0.056f), TerrainType.MOUNTAIN, 6, "SWE", false, 2, 1, 0, 5, listOf("scotland", "stockholm", "copenhagen")),
        ProvinceDef("stockholm", "Stockholm", "Scandinavia", 0.49f, 0.14f, hexPoly(0.49f, 0.14f, 0.060f, 0.056f), TerrainType.FOREST, 12, "SWE", true, 4, 3, 1, 7, listOf("oslo", "copenhagen", "leningrad")),
        ProvinceDef("copenhagen", "Copenhagen", "Scandinavia", 0.43f, 0.25f, hexPoly(0.43f, 0.25f, 0.042f, 0.038f), TerrainType.PLAINS, 5, "SWE", false, 2, 1, 0, 7, listOf("oslo", "stockholm", "hamburg")),

        // Germany
        ProvinceDef("hamburg", "Hamburg", "Germany", 0.42f, 0.34f, hexPoly(0.42f, 0.34f, 0.044f, 0.040f), TerrainType.PLAINS, 6, "GER", false, 3, 2, 0, 8, listOf("amsterdam", "copenhagen", "rhineland", "berlin")),
        ProvinceDef("rhineland", "Rhineland", "Germany", 0.40f, 0.44f, hexPoly(0.40f, 0.44f, 0.044f, 0.042f), TerrainType.URBAN, 8, "GER", false, 5, 3, 1, 9, listOf("amsterdam", "brussels", "maginot", "hamburg", "berlin", "bavaria")),
        ProvinceDef("berlin", "Berlin", "Germany", 0.49f, 0.39f, hexPoly(0.49f, 0.39f, 0.048f, 0.046f), TerrainType.URBAN, 16, "GER", true, 5, 5, 1, 9, listOf("hamburg", "rhineland", "bavaria", "prague", "danzig", "konigsberg")),
        ProvinceDef("bavaria", "Munich", "Germany", 0.45f, 0.52f, hexPoly(0.45f, 0.52f, 0.046f, 0.044f), TerrainType.FOREST, 6, "GER", false, 3, 3, 0, 8, listOf("rhineland", "maginot", "bern", "berlin", "prague", "vienna")),
        ProvinceDef("konigsberg", "Konigsberg", "East Prussia", 0.58f, 0.30f, hexPoly(0.58f, 0.30f, 0.044f, 0.038f), TerrainType.PLAINS, 4, "GER", false, 1, 2, 1, 6, listOf("berlin", "danzig", "warsaw", "leningrad", "minsk")),

        // Central Europe
        ProvinceDef("prague", "Prague", "Bohemia", 0.52f, 0.49f, hexPoly(0.52f, 0.49f, 0.042f, 0.038f), TerrainType.FOREST, 7, "NEU", false, 3, 3, 2, 7, listOf("berlin", "bavaria", "vienna", "krakow", "danzig")),
        ProvinceDef("vienna", "Vienna", "Austria", 0.52f, 0.58f, hexPoly(0.52f, 0.58f, 0.044f, 0.040f), TerrainType.MOUNTAIN, 8, "NEU", true, 3, 2, 0, 7, listOf("bavaria", "bern", "prague", "budapest", "milan", "belgrade")),
        ProvinceDef("budapest", "Budapest", "Danube", 0.60f, 0.59f, hexPoly(0.60f, 0.59f, 0.048f, 0.044f), TerrainType.PLAINS, 7, "NEU", false, 2, 2, 0, 6, listOf("vienna", "krakow", "belgrade", "bucharest")),

        // Italy
        ProvinceDef("milan", "Milan", "Italy", 0.42f, 0.69f, hexPoly(0.42f, 0.69f, 0.050f, 0.048f), TerrainType.MOUNTAIN, 8, "ITA", false, 4, 3, 1, 7, listOf("lyon", "bern", "vienna", "rome")),
        ProvinceDef("rome", "Rome", "Italy", 0.47f, 0.79f, hexPoly(0.47f, 0.79f, 0.048f, 0.052f), TerrainType.URBAN, 14, "ITA", true, 4, 3, 0, 7, listOf("milan", "sicily", "belgrade")),
        ProvinceDef("sicily", "Naples-Sicily", "Italy", 0.50f, 0.90f, hexPoly(0.50f, 0.90f, 0.048f, 0.042f), TerrainType.MOUNTAIN, 4, "ITA", false, 2, 1, 0, 5, listOf("rome", "athens")),

        // Poland & Belarus
        ProvinceDef("danzig", "Danzig", "Poland", 0.57f, 0.38f, hexPoly(0.57f, 0.38f, 0.042f, 0.038f), TerrainType.PLAINS, 5, "POL", false, 2, 1, 0, 6, listOf("berlin", "konigsberg", "warsaw", "prague")),
        ProvinceDef("warsaw", "Warsaw", "Poland", 0.64f, 0.42f, hexPoly(0.64f, 0.42f, 0.048f, 0.046f), TerrainType.URBAN, 14, "POL", true, 3, 3, 1, 7, listOf("danzig", "konigsberg", "krakow", "minsk", "kiev")),
        ProvinceDef("krakow", "Krakow", "Poland", 0.61f, 0.51f, hexPoly(0.61f, 0.51f, 0.046f, 0.042f), TerrainType.FOREST, 6, "POL", false, 2, 2, 0, 6, listOf("prague", "warsaw", "budapest", "kiev")),

        // Balkans
        ProvinceDef("belgrade", "Belgrade", "Balkans", 0.61f, 0.70f, hexPoly(0.61f, 0.70f, 0.048f, 0.048f), TerrainType.MOUNTAIN, 6, "NEU", false, 2, 1, 0, 5, listOf("vienna", "budapest", "rome", "bucharest", "sofia", "athens")),
        ProvinceDef("bucharest", "Bucharest", "Balkans", 0.70f, 0.66f, hexPoly(0.70f, 0.66f, 0.050f, 0.046f), TerrainType.PLAINS, 7, "NEU", false, 3, 2, 0, 6, listOf("budapest", "belgrade", "sofia", "kiev", "sevastopol")),
        ProvinceDef("sofia", "Sofia", "Balkans", 0.69f, 0.76f, hexPoly(0.69f, 0.76f, 0.046f, 0.044f), TerrainType.MOUNTAIN, 5, "NEU", false, 1, 1, 0, 5, listOf("belgrade", "bucharest", "athens")),
        ProvinceDef("athens", "Athens", "Balkans", 0.66f, 0.87f, hexPoly(0.66f, 0.87f, 0.050f, 0.050f), TerrainType.MOUNTAIN, 6, "NEU", false, 2, 1, 0, 5, listOf("sicily", "belgrade", "sofia")),

        // Soviet Union
        ProvinceDef("leningrad", "Leningrad", "Russia", 0.73f, 0.21f, hexPoly(0.73f, 0.21f, 0.066f, 0.058f), TerrainType.MARSH, 12, "SOV", false, 4, 3, 1, 7, listOf("stockholm", "konigsberg", "minsk", "moscow")),
        ProvinceDef("minsk", "Minsk", "Belarus", 0.73f, 0.36f, hexPoly(0.73f, 0.36f, 0.056f, 0.052f), TerrainType.MARSH, 6, "SOV", false, 2, 2, 0, 5, listOf("konigsberg", "warsaw", "leningrad", "moscow", "kiev")),
        ProvinceDef("moscow", "Moscow", "Russia", 0.86f, 0.31f, hexPoly(0.86f, 0.31f, 0.072f, 0.068f), TerrainType.URBAN, 18, "SOV", true, 6, 6, 2, 8, listOf("leningrad", "minsk", "kiev", "stalingrad")),
        ProvinceDef("kiev", "Kiev", "Ukraine", 0.78f, 0.51f, hexPoly(0.78f, 0.51f, 0.060f, 0.056f), TerrainType.PLAINS, 10, "SOV", false, 4, 3, 0, 6, listOf("warsaw", "krakow", "minsk", "moscow", "bucharest", "sevastopol", "stalingrad")),
        ProvinceDef("sevastopol", "Crimea", "Ukraine", 0.81f, 0.66f, hexPoly(0.81f, 0.66f, 0.054f, 0.048f), TerrainType.URBAN, 6, "SOV", false, 2, 2, 2, 6, listOf("bucharest", "kiev", "stalingrad")),
        ProvinceDef("stalingrad", "Stalingrad", "Volga", 0.90f, 0.54f, hexPoly(0.90f, 0.54f, 0.066f, 0.068f), TerrainType.URBAN, 14, "SOV", false, 4, 5, 1, 6, listOf("moscow", "kiev", "sevastopol"))
    )

    val provinceMap: Map<String, ProvinceDef> = provinces.associateBy { it.id }

    val focusTree: List<FocusNode> = listOf(
        // Column 0: Economy & Industry
        FocusNode("focus_four_year", "Four-Year Industry Plan", "Expand domestic civil engineering", 21, emptyList(), 0, 0, "+2 Civilian Factories in Capital"),
        FocusNode("focus_synthetic", "Heavy Machine Works", "Tooling for mass war production", 28, listOf("focus_four_year"), 0, 1, "+2 Military Factories, +80 Infantry Gear"),
        FocusNode("focus_total_war", "Total Industrial Mobilization", "Convert civil plants to munitions", 35, listOf("focus_synthetic"), 0, 2, "+3 Military Factories, +10% War Support"),

        // Column 1: Military Doctrine
        FocusNode("focus_general_staff", "Reform General Staff", "Modernize officer command corps", 21, emptyList(), 1, 0, "+35 Command Power, +50 PP"),
        FocusNode("focus_blitz", "Combined Arms Doctrine", "Synchronize armor and mobile troops", 28, listOf("focus_general_staff"), 1, 1, "+40 Light Tanks, +15% Breakthrough"),
        FocusNode("focus_fortress", "Fortress Frontier", "Reinforce border defensive belts", 28, listOf("focus_general_staff"), 1, 2, "+2 Land Forts in Capital & Border"),

        // Column 2: Politics & Expansion
        FocusNode("focus_rally_nation", "National Mobilization Speech", "Unify public resolve for security", 21, emptyList(), 2, 0, "+120 PP, +8% Stability"),
        FocusNode("focus_rearmament", "Emergency Rearmament Act", "Conscript reserve cadres early", 28, listOf("focus_rally_nation"), 2, 1, "+180K Manpower, +10% War Support"),
        FocusNode("focus_hegemony", "Continental Hegemony", "Assert strategic claims over rivals", 35, listOf("focus_rearmament", "focus_blitz"), 2, 2, "+150 PP, +10% World Tension, Fast War Goals")
    )

    val techTree: List<TechNode> = listOf(
        // Infantry
        TechNode("tech_inf_1", "Submachine Squads", TechCategory.INFANTRY, 1, 25, emptyList(), "+12% Infantry Soft Attack"),
        TechNode("tech_inf_2", "Anti-Tank Rifles", TechCategory.INFANTRY, 2, 35, listOf("tech_inf_1"), "+25% Infantry Hard Attack & Defense"),
        TechNode("tech_inf_3", "Heavy Field Howitzers", TechCategory.INFANTRY, 3, 45, listOf("tech_inf_2"), "+18% All Division Attack"),

        // Armor
        TechNode("tech_arm_1", "Welded Tank Hulls", TechCategory.ARMOR, 1, 28, emptyList(), "+15% Tank Breakthrough & Armor"),
        TechNode("tech_arm_2", "Medium Battle Tank", TechCategory.ARMOR, 2, 40, listOf("tech_arm_1"), "+25% Tank Hard & Soft Attack"),
        TechNode("tech_arm_3", "Heavy Breakthrough Armor", TechCategory.ARMOR, 3, 50, listOf("tech_arm_2"), "+30% Armor Combat Power"),

        // Industry
        TechNode("tech_ind_1", "Concentrated Tooling", TechCategory.INDUSTRY, 1, 24, emptyList(), "+15% Factory Production Output"),
        TechNode("tech_ind_2", "Assembly Line Mass Prod.", TechCategory.INDUSTRY, 2, 34, listOf("tech_ind_1"), "+20% Construction & Output Speed"),
        TechNode("tech_ind_3", "Advanced Radar & Computing", TechCategory.INDUSTRY, 3, 42, listOf("tech_ind_2"), "+20% Research Speed & Air Bonus"),

        // Doctrine
        TechNode("tech_doc_1", "Mobile Frontline Tactics", TechCategory.DOCTRINE, 1, 26, emptyList(), "+10 Max Organization"),
        TechNode("tech_doc_2", "Deep Operational Encirclement", TechCategory.DOCTRINE, 2, 38, listOf("tech_doc_1"), "+15% Movement Speed & Org Recovery"),
        TechNode("tech_doc_3", "Grand Battleplan Supremacy", TechCategory.DOCTRINE, 3, 48, listOf("tech_doc_2"), "+15% Entrenchment & Combat Defense")
    )

    fun createInitialCampaignState(
        playerNationId: String,
        campaignName: String,
        difficulty: Difficulty
    ): CampaignState {
        val player = nations[playerNationId] ?: nations.getValue("GER")
        val owners = provinces.associate { it.id to it.initialOwnerId }
        val civs = provinces.associate { it.id to it.initialCiv }
        val mils = provinces.associate { it.id to it.initialMil }
        val forts = provinces.associate { it.id to it.initialForts }
        val infra = provinces.associate { it.id to it.initialInfra }

        // Spawn starting divisions per nation across their owned provinces
        val divisions = mutableListOf<DivisionState>()
        var divSeq = 1
        val ownedByNation = provinces.groupBy { it.initialOwnerId }
        for ((nationId, provList) in ownedByNation) {
            val baseCount = when (nationId) {
                "GER" -> 11
                "SOV" -> 13
                "FRA" -> 9
                "ENG" -> 7
                "ITA" -> 7
                "POL" -> 6
                "SPR" -> 5
                "SWE" -> 4
                else -> 6
            }
            for (i in 0 until baseCount) {
                val prov = provList[i % provList.size]
                val template = when {
                    i == 0 && (nationId == "GER" || nationId == "SOV" || nationId == "FRA") -> DivisionTemplate.ARMOR
                    i == 1 && nationId != "NEU" -> DivisionTemplate.MOTORIZED
                    prov.terrain == TerrainType.MOUNTAIN -> DivisionTemplate.MOUNTAINEER
                    else -> DivisionTemplate.INFANTRY
                }
                val prefix = nations[nationId]?.shortTag ?: nationId
                divisions += DivisionState(
                    id = "div_${divSeq++}",
                    name = "${i + 1}. $prefix ${template.symbolCode}",
                    nationId = nationId,
                    template = template,
                    provinceId = prov.id,
                    hp = 100.0,
                    org = template.maxOrg,
                    entrenchment = 5.0
                )
            }
        }

        val diplomacy = nations.keys
            .filter { it != playerNationId }
            .associateWith { other ->
                when {
                    (playerNationId == "ENG" && other == "FRA") || (playerNationId == "FRA" && other == "ENG") ->
                        DiplomacyStatus.ALLIANCE
                    (playerNationId == "GER" && other == "ITA") || (playerNationId == "ITA" && other == "GER") ->
                        DiplomacyStatus.NON_AGGRESSION
                    else -> DiplomacyStatus.PEACE
                }
            }

        val relations = nations.keys
            .filter { it != playerNationId }
            .associateWith { other ->
                when (diplomacy[other]) {
                    DiplomacyStatus.ALLIANCE -> 75
                    DiplomacyStatus.NON_AGGRESSION -> 35
                    else -> if (nations[other]?.ideology == player.ideology) 20 else -20
                }
            }

        val totalMil = provinces.filter { it.initialOwnerId == playerNationId }.sumOf { it.initialMil }
        val infAssign = (totalMil / 2).coerceAtLeast(1)
        val artAssign = (totalMil / 4).coerceAtLeast(1)
        val tankAssign = (totalMil - infAssign - artAssign).coerceAtLeast(0)

        return CampaignState(
            campaignId = "",
            campaignName = campaignName,
            playerNationId = playerNationId,
            difficulty = difficulty,
            gameDateEpochDay = EPOCH_DAY_1936_01_01,
            gameHour = 8,
            politicalPower = player.startingPP,
            stability = player.startingStability,
            warSupport = player.startingWarSupport,
            manpower = player.startingManpower,
            commandPower = 25.0,
            worldTension = 0.08,
            conscriptionLaw = ConscriptionLaw.LIMITED,
            economyLaw = EconomyLaw.CIVILIAN,
            tradeLaw = TradeLaw.EXPORT_FOCUS,
            provinceOwners = owners,
            provinceCivFactories = civs,
            provinceMilFactories = mils,
            provinceForts = forts,
            provinceInfra = infra,
            equipmentStockpile = mapOf(
                EquipmentType.INFANTRY_EQ to 1600,
                EquipmentType.ARTILLERY to 140,
                EquipmentType.LIGHT_TANK to 48,
                EquipmentType.FIGHTER to 36
            ),
            diplomacyStates = diplomacy,
            nationRelations = relations,
            warGoalProgress = emptyMap(),
            divisions = divisions,
            productionLines = listOf(
                ProductionLine(EquipmentType.INFANTRY_EQ, infAssign, 0.0),
                ProductionLine(EquipmentType.ARTILLERY, artAssign, 0.0),
                ProductionLine(EquipmentType.LIGHT_TANK, tankAssign, 0.0),
                ProductionLine(EquipmentType.FIGHTER, 0, 0.0)
            ),
            constructionQueue = emptyList(),
            trainingQueue = emptyList(),
            status = "active",
            newsLog = listOf("1936.01.01 — High Command assumes control of ${player.name}.")
        )
    }
}
