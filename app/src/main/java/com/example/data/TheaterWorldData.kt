package com.example.data

import com.example.model.*
import com.example.ui.theme.*
import java.time.LocalDate

object TheaterWorldData {

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
            startingManpower = 1_550_000L,
            startingStability = 0.72,
            startingWarSupport = 0.76,
            summary = "Central European industrial powerhouse with mobile Panzer doctrine.",
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
            startingManpower = 4_200_000L,
            startingStability = 0.64,
            startingWarSupport = 0.70,
            summary = "Vast Eurasian superpower stretching from Europe to Vladivostok.",
            capitalProvinceId = "moscow"
        ),
        NationDef(
            id = "USA",
            name = "United States",
            shortTag = "USA",
            ideology = Ideology.DEMOCRACY,
            leaderName = "Franklin D. Roosevelt",
            leaderTitle = "President",
            factionName = "Allies",
            color = NationUsaColor,
            flagAccent = ParchmentText,
            startingPP = 95.0,
            startingManpower = 2_400_000L,
            startingStability = 0.85,
            startingWarSupport = 0.40,
            summary = "Trans-oceanic industrial giant poised to project power across Atlantic & Pacific.",
            capitalProvinceId = "washington"
        ),
        NationDef(
            id = "ENG",
            name = "British Empire",
            shortTag = "ENG",
            ideology = Ideology.DEMOCRACY,
            leaderName = "Stanley Baldwin",
            leaderTitle = "Prime Minister",
            factionName = "Allies",
            color = NationUkColor,
            flagAccent = AlliedCobalt,
            startingPP = 90.0,
            startingManpower = 1_850_000L,
            startingStability = 0.82,
            startingWarSupport = 0.44,
            summary = "Global maritime empire spanning Britain, Canada, Suez, India, Malaya, and Australia.",
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
            startingManpower = 1_250_000L,
            startingStability = 0.56,
            startingWarSupport = 0.48,
            summary = "Fortified western frontier with colonial territories across Africa and Indochina.",
            capitalProvinceId = "paris"
        ),
        NationDef(
            id = "JPN",
            name = "Empire of Japan",
            shortTag = "JPN",
            ideology = Ideology.AUTOCRACY,
            leaderName = "Admiral Yamamoto",
            leaderTitle = "Supreme Command",
            factionName = "Co-Prosperity",
            color = NationJapanColor,
            flagAccent = ParchmentText,
            startingPP = 70.0,
            startingManpower = 1_400_000L,
            startingStability = 0.74,
            startingWarSupport = 0.78,
            summary = "Pacific naval and amphibious power holding Korea and Manchuria.",
            capitalProvinceId = "tokyo"
        ),
        NationDef(
            id = "CHI",
            name = "Republic of China",
            shortTag = "CHI",
            ideology = Ideology.NON_ALIGNED,
            leaderName = "Generalissimo Chiang",
            leaderTitle = "Chairman",
            factionName = "United Front",
            color = NationChinaColor,
            flagAccent = AlliedCobalt,
            startingPP = 55.0,
            startingManpower = 4_800_000L,
            startingStability = 0.52,
            startingWarSupport = 0.72,
            summary = "Enormous manpower reserves defending the East Asian mainland.",
            capitalProvinceId = "chongqing"
        ),
        NationDef(
            id = "INS",
            name = "Nusantara Archipelago",
            shortTag = "INS",
            ideology = Ideology.NON_ALIGNED,
            leaderName = "Sukarno-Hatta Council",
            leaderTitle = "National Command",
            factionName = "Maritime League",
            color = NationNusantaraColor,
            flagAccent = ParchmentText,
            startingPP = 65.0,
            startingManpower = 1_600_000L,
            startingStability = 0.72,
            startingWarSupport = 0.62,
            summary = "Resource-rich maritime crossroads across Java, Sumatra, and Kalimantan.",
            capitalProvinceId = "jakarta"
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
            startingManpower = 980_000L,
            startingStability = 0.66,
            startingWarSupport = 0.68,
            summary = "Mediterranean power controlling Libya and the Horn of Africa.",
            capitalProvinceId = "rome"
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
            startingManpower = 820_000L,
            startingStability = 0.68,
            startingWarSupport = 0.74,
            summary = "Strategic Eastern European crossroads between Berlin and Moscow.",
            capitalProvinceId = "warsaw"
        ),
        NationDef(
            id = "TUR",
            name = "Republic of Turkey",
            shortTag = "TUR",
            ideology = Ideology.NON_ALIGNED,
            leaderName = "Ismet Inonu",
            leaderTitle = "President",
            factionName = "Balkan Entente",
            color = NationTurkeyColor,
            flagAccent = ParchmentText,
            startingPP = 70.0,
            startingManpower = 760_000L,
            startingStability = 0.78,
            startingWarSupport = 0.58,
            summary = "Controls the strategic Anatolian bridge between Europe, Russia, and the Middle East.",
            capitalProvinceId = "ankara"
        ),
        NationDef(
            id = "BRA",
            name = "Republic of Brazil",
            shortTag = "BRA",
            ideology = Ideology.DEMOCRACY,
            leaderName = "Getulio Vargas",
            leaderTitle = "President",
            factionName = "South American Pact",
            color = NationBrazilColor,
            flagAccent = BrassGold,
            startingPP = 68.0,
            startingManpower = 1_100_000L,
            startingStability = 0.70,
            startingWarSupport = 0.52,
            summary = "Dominant South American power bordering the South Atlantic shipping lanes.",
            capitalProvinceId = "rio"
        ),
        NationDef(
            id = "SPR",
            name = "Spanish Republic",
            shortTag = "SPR",
            ideology = Ideology.DEMOCRACY,
            leaderName = "Manuel Azana",
            leaderTitle = "President",
            factionName = "Non-Aligned",
            color = NationSpainColor,
            flagAccent = FrontlineCrimson,
            startingPP = 52.0,
            startingManpower = 680_000L,
            startingStability = 0.52,
            startingWarSupport = 0.60,
            summary = "Iberian gateway between the Atlantic and Mediterranean.",
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
            startingManpower = 560_000L,
            startingStability = 0.88,
            startingWarSupport = 0.45,
            summary = "Northern bastion guarding Scandinavian steel and Baltic sea lanes.",
            capitalProvinceId = "stockholm"
        ),
        NationDef(
            id = "NEU",
            name = "Regional States",
            shortTag = "NEU",
            ideology = Ideology.NON_ALIGNED,
            leaderName = "Regional Coalitions",
            leaderTitle = "Council",
            factionName = "Independent",
            color = NationMinorColor,
            flagAccent = MutedSteelText,
            startingPP = 45.0,
            startingManpower = 650_000L,
            startingStability = 0.65,
            startingWarSupport = 0.40,
            summary = "Independent nations across Central Europe, Latin America, Middle East, and Africa.",
            capitalProvinceId = "prague"
        )
    ).associateBy { it.id }

    val playableNations: List<NationDef> = nations.values.filter { it.id != "NEU" }

    private fun hexPoly(cx: Float, cy: Float, rx: Float, ry: Float): List<MapPoint> {
        return listOf(
            MapPoint(cx - rx * 0.55f, cy - ry * 0.92f),
            MapPoint(cx + rx * 0.60f, cy - ry * 0.88f),
            MapPoint(cx + rx * 0.98f, cy - ry * 0.04f),
            MapPoint(cx + rx * 0.52f, cy + ry * 0.90f),
            MapPoint(cx - rx * 0.60f, cy + ry * 0.86f),
            MapPoint(cx - rx * 0.96f, cy + ry * 0.04f)
        )
    }

    // 70 Interconnected Global Provinces across all Continents (x: 0..1 West->East, y: 0..1 North->South)
    val provinces: List<ProvinceDef> = listOf(
        // --- NORTH AMERICA & PACIFIC ---
        ProvinceDef("alaska", "Alaska", "North America", 0.07f, 0.17f, hexPoly(0.07f, 0.17f, 0.036f, 0.044f), TerrainType.MOUNTAIN, 2, "USA", false, 1, 1, 0, 4, listOf("vancouver", "siberia")),
        ProvinceDef("vancouver", "Western Canada", "North America", 0.13f, 0.24f, hexPoly(0.13f, 0.24f, 0.042f, 0.045f), TerrainType.FOREST, 4, "ENG", false, 2, 1, 0, 6, listOf("alaska", "ottawa", "california")),
        ProvinceDef("ottawa", "Eastern Canada", "North America", 0.22f, 0.23f, hexPoly(0.22f, 0.23f, 0.044f, 0.045f), TerrainType.FOREST, 6, "ENG", false, 3, 2, 0, 7, listOf("vancouver", "newyork", "scotland")),
        ProvinceDef("california", "California", "United States", 0.12f, 0.36f, hexPoly(0.12f, 0.36f, 0.038f, 0.044f), TerrainType.URBAN, 8, "USA", false, 4, 3, 0, 8, listOf("vancouver", "texas", "hawaii")),
        ProvinceDef("texas", "Texas-Midwest", "United States", 0.17f, 0.39f, hexPoly(0.17f, 0.39f, 0.040f, 0.044f), TerrainType.PLAINS, 6, "USA", false, 4, 3, 0, 8, listOf("california", "washington", "mexico")),
        ProvinceDef("washington", "Washington D.C.", "United States", 0.22f, 0.37f, hexPoly(0.22f, 0.37f, 0.034f, 0.038f), TerrainType.URBAN, 16, "USA", true, 6, 5, 1, 9, listOf("texas", "newyork", "mexico")),
        ProvinceDef("newyork", "New York", "United States", 0.26f, 0.31f, hexPoly(0.26f, 0.31f, 0.034f, 0.038f), TerrainType.URBAN, 12, "USA", false, 6, 4, 0, 9, listOf("ottawa", "washington", "london")),
        ProvinceDef("mexico", "Mexico City", "Central America", 0.17f, 0.49f, hexPoly(0.17f, 0.49f, 0.040f, 0.044f), TerrainType.MOUNTAIN, 6, "NEU", false, 2, 1, 0, 5, listOf("texas", "washington", "colombia")),
        ProvinceDef("hawaii", "Pearl Harbor", "Pacific", 0.04f, 0.49f, hexPoly(0.04f, 0.49f, 0.026f, 0.034f), TerrainType.PLAINS, 5, "USA", false, 1, 2, 2, 7, listOf("california", "tokyo", "sydney")),

        // --- SOUTH AMERICA ---
        ProvinceDef("colombia", "Bogota-Panama", "South America", 0.24f, 0.58f, hexPoly(0.24f, 0.58f, 0.036f, 0.042f), TerrainType.MOUNTAIN, 4, "NEU", false, 2, 1, 0, 5, listOf("mexico", "peru", "amazon")),
        ProvinceDef("peru", "Andes-Peru", "South America", 0.24f, 0.69f, hexPoly(0.24f, 0.69f, 0.034f, 0.045f), TerrainType.MOUNTAIN, 4, "NEU", false, 1, 1, 0, 4, listOf("colombia", "amazon", "argentina")),
        ProvinceDef("amazon", "Amazonia", "Brazil", 0.29f, 0.64f, hexPoly(0.29f, 0.64f, 0.042f, 0.046f), TerrainType.FOREST, 5, "BRA", false, 2, 1, 0, 4, listOf("colombia", "peru", "rio", "westafrica")),
        ProvinceDef("rio", "Rio de Janeiro", "Brazil", 0.33f, 0.72f, hexPoly(0.33f, 0.72f, 0.038f, 0.046f), TerrainType.URBAN, 12, "BRA", true, 4, 3, 0, 7, listOf("amazon", "argentina", "capetown")),
        ProvinceDef("argentina", "Buenos Aires", "South America", 0.27f, 0.82f, hexPoly(0.27f, 0.82f, 0.038f, 0.052f), TerrainType.PLAINS, 7, "NEU", false, 3, 2, 0, 6, listOf("peru", "rio")),

        // --- WESTERN & NORTHERN EUROPE ---
        ProvinceDef("scotland", "Scotland", "Britain", 0.45f, 0.21f, hexPoly(0.45f, 0.21f, 0.022f, 0.028f), TerrainType.MOUNTAIN, 3, "ENG", false, 1, 1, 0, 6, listOf("ottawa", "london", "oslo")),
        ProvinceDef("london", "London", "Britain", 0.45f, 0.27f, hexPoly(0.45f, 0.27f, 0.023f, 0.028f), TerrainType.URBAN, 15, "ENG", true, 5, 4, 1, 9, listOf("scotland", "newyork", "paris", "brussels", "casablanca")),
        ProvinceDef("oslo", "Oslo", "Scandinavia", 0.50f, 0.16f, hexPoly(0.50f, 0.16f, 0.024f, 0.032f), TerrainType.MOUNTAIN, 6, "SWE", false, 2, 1, 0, 6, listOf("scotland", "stockholm")),
        ProvinceDef("stockholm", "Stockholm", "Scandinavia", 0.54f, 0.16f, hexPoly(0.54f, 0.16f, 0.026f, 0.032f), TerrainType.FOREST, 12, "SWE", true, 4, 3, 1, 7, listOf("oslo", "hamburg", "leningrad")),
        ProvinceDef("madrid", "Madrid", "Iberia", 0.44f, 0.39f, hexPoly(0.44f, 0.39f, 0.028f, 0.032f), TerrainType.MOUNTAIN, 10, "SPR", true, 3, 2, 1, 5, listOf("paris", "casablanca")),
        ProvinceDef("paris", "Paris", "France", 0.47f, 0.32f, hexPoly(0.47f, 0.32f, 0.022f, 0.026f), TerrainType.URBAN, 15, "FRA", true, 5, 4, 0, 8, listOf("london", "madrid", "brussels", "maginot", "lyon")),
        ProvinceDef("brussels", "Brussels", "Low Countries", 0.49f, 0.28f, hexPoly(0.49f, 0.28f, 0.018f, 0.022f), TerrainType.URBAN, 6, "FRA", false, 3, 2, 1, 8, listOf("london", "paris", "maginot", "rhineland", "hamburg")),
        ProvinceDef("maginot", "Maginot Line", "France", 0.50f, 0.34f, hexPoly(0.50f, 0.34f, 0.018f, 0.022f), TerrainType.FOREST, 4, "FRA", false, 2, 2, 4, 7, listOf("paris", "brussels", "lyon", "rhineland", "bavaria")),
        ProvinceDef("lyon", "Marseille", "France", 0.48f, 0.37f, hexPoly(0.48f, 0.37f, 0.020f, 0.024f), TerrainType.MOUNTAIN, 6, "FRA", false, 3, 2, 1, 6, listOf("paris", "maginot", "milan", "algiers")),

        // --- CENTRAL, SOUTHERN & EASTERN EUROPE ---
        ProvinceDef("hamburg", "Hamburg", "Germany", 0.52f, 0.24f, hexPoly(0.52f, 0.24f, 0.020f, 0.024f), TerrainType.PLAINS, 6, "GER", false, 3, 2, 0, 8, listOf("stockholm", "brussels", "rhineland", "berlin")),
        ProvinceDef("rhineland", "Rhineland", "Germany", 0.52f, 0.30f, hexPoly(0.52f, 0.30f, 0.020f, 0.024f), TerrainType.URBAN, 8, "GER", false, 5, 3, 1, 9, listOf("brussels", "maginot", "hamburg", "berlin", "bavaria")),
        ProvinceDef("berlin", "Berlin", "Germany", 0.55f, 0.27f, hexPoly(0.55f, 0.27f, 0.022f, 0.026f), TerrainType.URBAN, 16, "GER", true, 5, 5, 1, 9, listOf("hamburg", "rhineland", "bavaria", "prague", "warsaw", "konigsberg")),
        ProvinceDef("bavaria", "Munich", "Germany", 0.54f, 0.34f, hexPoly(0.54f, 0.34f, 0.020f, 0.024f), TerrainType.FOREST, 6, "GER", false, 3, 3, 0, 8, listOf("maginot", "rhineland", "berlin", "prague", "milan")),
        ProvinceDef("konigsberg", "Konigsberg", "East Prussia", 0.58f, 0.23f, hexPoly(0.58f, 0.23f, 0.020f, 0.022f), TerrainType.PLAINS, 4, "GER", false, 1, 2, 1, 6, listOf("berlin", "warsaw", "leningrad", "minsk")),
        ProvinceDef("prague", "Prague-Vienna", "Central Europe", 0.57f, 0.33f, hexPoly(0.57f, 0.33f, 0.022f, 0.024f), TerrainType.FOREST, 8, "NEU", true, 3, 3, 2, 7, listOf("berlin", "bavaria", "warsaw", "balkans")),
        ProvinceDef("milan", "Milan", "Italy", 0.52f, 0.39f, hexPoly(0.52f, 0.39f, 0.020f, 0.024f), TerrainType.MOUNTAIN, 8, "ITA", false, 4, 3, 1, 7, listOf("lyon", "bavaria", "rome", "balkans")),
        ProvinceDef("rome", "Rome", "Italy", 0.54f, 0.43f, hexPoly(0.54f, 0.43f, 0.020f, 0.026f), TerrainType.URBAN, 14, "ITA", true, 4, 3, 0, 7, listOf("milan", "tripoli", "athens")),
        ProvinceDef("warsaw", "Warsaw", "Poland", 0.60f, 0.28f, hexPoly(0.60f, 0.28f, 0.024f, 0.028f), TerrainType.URBAN, 14, "POL", true, 3, 3, 1, 7, listOf("berlin", "konigsberg", "prague", "minsk", "kiev")),
        ProvinceDef("balkans", "Balkans", "Southeastern Europe", 0.59f, 0.38f, hexPoly(0.59f, 0.38f, 0.024f, 0.028f), TerrainType.MOUNTAIN, 7, "NEU", false, 3, 2, 0, 6, listOf("prague", "milan", "athens", "kiev", "ankara")),
        ProvinceDef("athens", "Athens", "Greece", 0.59f, 0.44f, hexPoly(0.59f, 0.44f, 0.020f, 0.024f), TerrainType.MOUNTAIN, 6, "NEU", false, 2, 1, 0, 5, listOf("rome", "balkans", "ankara", "cairo")),

        // --- AFRICA & MIDDLE EAST ---
        ProvinceDef("casablanca", "Casablanca", "North Africa", 0.44f, 0.46f, hexPoly(0.44f, 0.46f, 0.028f, 0.032f), TerrainType.MOUNTAIN, 4, "FRA", false, 1, 1, 0, 5, listOf("london", "madrid", "algiers", "westafrica")),
        ProvinceDef("algiers", "Algiers", "North Africa", 0.49f, 0.46f, hexPoly(0.49f, 0.46f, 0.028f, 0.030f), TerrainType.PLAINS, 5, "FRA", false, 2, 1, 0, 6, listOf("lyon", "casablanca", "tripoli")),
        ProvinceDef("tripoli", "Libya", "North Africa", 0.54f, 0.50f, hexPoly(0.54f, 0.50f, 0.030f, 0.034f), TerrainType.PLAINS, 4, "ITA", false, 1, 1, 0, 5, listOf("rome", "algiers", "cairo", "congo")),
        ProvinceDef("cairo", "Cairo-Suez", "Egypt", 0.60f, 0.50f, hexPoly(0.60f, 0.50f, 0.030f, 0.036f), TerrainType.URBAN, 10, "ENG", false, 2, 2, 1, 7, listOf("athens", "tripoli", "ankara", "arabia", "ethiopia")),
        ProvinceDef("westafrica", "Dakar-West Africa", "Africa", 0.45f, 0.58f, hexPoly(0.45f, 0.58f, 0.038f, 0.044f), TerrainType.FOREST, 3, "FRA", false, 1, 1, 0, 4, listOf("casablanca", "amazon", "congo")),
        ProvinceDef("congo", "Central Africa", "Africa", 0.54f, 0.64f, hexPoly(0.54f, 0.64f, 0.042f, 0.048f), TerrainType.MARSH, 4, "NEU", false, 1, 1, 0, 3, listOf("westafrica", "tripoli", "ethiopia", "capetown")),
        ProvinceDef("ethiopia", "Horn of Africa", "East Africa", 0.62f, 0.61f, hexPoly(0.62f, 0.61f, 0.034f, 0.044f), TerrainType.MOUNTAIN, 5, "ITA", false, 1, 2, 0, 4, listOf("cairo", "congo", "capetown", "bombay")),
        ProvinceDef("capetown", "South Africa", "Southern Africa", 0.56f, 0.78f, hexPoly(0.56f, 0.78f, 0.038f, 0.050f), TerrainType.PLAINS, 8, "ENG", false, 3, 2, 0, 6, listOf("rio", "congo", "ethiopia", "perth")),
        ProvinceDef("ankara", "Ankara", "Anatolia", 0.64f, 0.41f, hexPoly(0.64f, 0.41f, 0.032f, 0.030f), TerrainType.MOUNTAIN, 10, "TUR", true, 3, 3, 1, 6, listOf("balkans", "athens", "cairo", "arabia", "stalingrad")),
        ProvinceDef("arabia", "Middle East", "Arabia", 0.67f, 0.51f, hexPoly(0.67f, 0.51f, 0.038f, 0.042f), TerrainType.PLAINS, 6, "NEU", false, 2, 1, 0, 5, listOf("cairo", "ankara", "tehran", "bombay")),

        // --- SOVIET UNION, PERSIA & INDIA ---
        ProvinceDef("leningrad", "Leningrad", "Russia", 0.62f, 0.17f, hexPoly(0.62f, 0.17f, 0.032f, 0.034f), TerrainType.MARSH, 12, "SOV", false, 4, 3, 1, 7, listOf("stockholm", "konigsberg", "minsk", "moscow")),
        ProvinceDef("minsk", "Minsk", "Belarus", 0.63f, 0.25f, hexPoly(0.63f, 0.25f, 0.026f, 0.028f), TerrainType.MARSH, 6, "SOV", false, 2, 2, 0, 5, listOf("konigsberg", "warsaw", "leningrad", "moscow", "kiev")),
        ProvinceDef("moscow", "Moscow", "Russia", 0.68f, 0.22f, hexPoly(0.68f, 0.22f, 0.034f, 0.036f), TerrainType.URBAN, 18, "SOV", true, 6, 6, 2, 8, listOf("leningrad", "minsk", "kiev", "stalingrad", "urals")),
        ProvinceDef("kiev", "Kiev", "Ukraine", 0.64f, 0.33f, hexPoly(0.64f, 0.33f, 0.028f, 0.030f), TerrainType.PLAINS, 10, "SOV", false, 4, 3, 0, 6, listOf("warsaw", "balkans", "minsk", "moscow", "stalingrad")),
        ProvinceDef("stalingrad", "Stalingrad", "Volga-Caucasus", 0.70f, 0.33f, hexPoly(0.70f, 0.33f, 0.032f, 0.034f), TerrainType.URBAN, 14, "SOV", false, 4, 5, 1, 6, listOf("ankara", "moscow", "kiev", "urals", "tehran")),
        ProvinceDef("urals", "Urals-Central Asia", "Siberia", 0.76f, 0.22f, hexPoly(0.76f, 0.22f, 0.044f, 0.048f), TerrainType.MOUNTAIN, 8, "SOV", false, 3, 4, 0, 5, listOf("moscow", "stalingrad", "siberia", "chongqing")),
        ProvinceDef("siberia", "Eastern Siberia", "Siberia", 0.85f, 0.18f, hexPoly(0.85f, 0.18f, 0.050f, 0.046f), TerrainType.FOREST, 4, "SOV", false, 1, 2, 0, 4, listOf("alaska", "urals", "vladivostok", "manchuria")),
        ProvinceDef("vladivostok", "Vladivostok", "Far East", 0.91f, 0.25f, hexPoly(0.91f, 0.25f, 0.030f, 0.032f), TerrainType.FOREST, 6, "SOV", false, 2, 2, 1, 5, listOf("siberia", "manchuria", "korea", "tokyo")),
        ProvinceDef("tehran", "Persia", "Middle East", 0.72f, 0.43f, hexPoly(0.72f, 0.43f, 0.034f, 0.036f), TerrainType.MOUNTAIN, 5, "NEU", false, 2, 1, 0, 5, listOf("arabia", "stalingrad", "delhi")),
        ProvinceDef("delhi", "Delhi", "India", 0.76f, 0.48f, hexPoly(0.76f, 0.48f, 0.030f, 0.034f), TerrainType.URBAN, 10, "ENG", false, 3, 2, 0, 6, listOf("tehran", "bombay", "calcutta", "chongqing")),
        ProvinceDef("bombay", "Bombay", "India", 0.74f, 0.56f, hexPoly(0.74f, 0.56f, 0.028f, 0.036f), TerrainType.PLAINS, 7, "ENG", false, 3, 2, 0, 6, listOf("ethiopia", "arabia", "delhi", "calcutta", "sumatra")),
        ProvinceDef("calcutta", "Calcutta-Burma", "India", 0.79f, 0.52f, hexPoly(0.79f, 0.52f, 0.028f, 0.034f), TerrainType.MARSH, 7, "ENG", false, 2, 2, 0, 5, listOf("delhi", "bombay", "indochina", "chongqing")),

        // --- EAST ASIA, NUSANTARA & OCEANIA ---
        ProvinceDef("chongqing", "Chongqing", "China", 0.82f, 0.43f, hexPoly(0.82f, 0.43f, 0.034f, 0.038f), TerrainType.MOUNTAIN, 12, "CHI", true, 3, 3, 1, 5, listOf("urals", "delhi", "calcutta", "beijing", "nanjing", "indochina")),
        ProvinceDef("beijing", "Beijing", "China", 0.85f, 0.34f, hexPoly(0.85f, 0.34f, 0.030f, 0.032f), TerrainType.URBAN, 10, "CHI", false, 3, 2, 0, 6, listOf("chongqing", "nanjing", "manchuria")),
        ProvinceDef("nanjing", "Shanghai-Nanjing", "China", 0.86f, 0.43f, hexPoly(0.86f, 0.43f, 0.028f, 0.032f), TerrainType.URBAN, 10, "CHI", false, 4, 3, 0, 7, listOf("chongqing", "beijing", "korea", "tokyo", "manila")),
        ProvinceDef("manchuria", "Manchuria", "East Asia", 0.88f, 0.28f, hexPoly(0.88f, 0.28f, 0.030f, 0.030f), TerrainType.PLAINS, 6, "JPN", false, 3, 3, 0, 6, listOf("siberia", "vladivostok", "beijing", "korea")),
        ProvinceDef("korea", "Seoul-Korea", "East Asia", 0.90f, 0.35f, hexPoly(0.90f, 0.35f, 0.020f, 0.028f), TerrainType.MOUNTAIN, 5, "JPN", false, 2, 2, 0, 6, listOf("vladivostok", "manchuria", "nanjing", "tokyo")),
        ProvinceDef("tokyo", "Tokyo", "Japan", 0.94f, 0.37f, hexPoly(0.94f, 0.37f, 0.026f, 0.040f), TerrainType.URBAN, 16, "JPN", true, 5, 5, 1, 9, listOf("vladivostok", "nanjing", "korea", "hawaii", "manila")),
        ProvinceDef("indochina", "Indochina", "Southeast Asia", 0.82f, 0.54f, hexPoly(0.82f, 0.54f, 0.026f, 0.034f), TerrainType.FOREST, 5, "FRA", false, 2, 1, 0, 5, listOf("calcutta", "chongqing", "malaya", "manila")),
        ProvinceDef("malaya", "Singapore-Malaya", "Southeast Asia", 0.82f, 0.61f, hexPoly(0.82f, 0.61f, 0.022f, 0.026f), TerrainType.MARSH, 7, "ENG", false, 2, 2, 2, 7, listOf("indochina", "sumatra", "kalimantan")),
        ProvinceDef("sumatra", "Sumatra", "Nusantara", 0.80f, 0.67f, hexPoly(0.80f, 0.67f, 0.024f, 0.028f), TerrainType.FOREST, 5, "INS", false, 2, 2, 0, 6, listOf("bombay", "malaya", "jakarta")),
        ProvinceDef("jakarta", "Jakarta-Java", "Nusantara", 0.84f, 0.71f, hexPoly(0.84f, 0.71f, 0.028f, 0.024f), TerrainType.URBAN, 12, "INS", true, 4, 3, 1, 7, listOf("sumatra", "kalimantan", "perth")),
        ProvinceDef("kalimantan", "Kalimantan", "Nusantara", 0.86f, 0.65f, hexPoly(0.86f, 0.65f, 0.026f, 0.028f), TerrainType.FOREST, 5, "INS", false, 2, 2, 0, 5, listOf("malaya", "jakarta", "manila", "sydney")),
        ProvinceDef("manila", "Manila", "Philippines", 0.89f, 0.56f, hexPoly(0.89f, 0.56f, 0.022f, 0.030f), TerrainType.MOUNTAIN, 6, "USA", false, 2, 1, 1, 6, listOf("nanjing", "tokyo", "indochina", "kalimantan")),
        ProvinceDef("perth", "Western Australia", "Oceania", 0.87f, 0.81f, hexPoly(0.87f, 0.81f, 0.034f, 0.042f), TerrainType.PLAINS, 4, "ENG", false, 2, 1, 0, 5, listOf("capetown", "jakarta", "sydney")),
        ProvinceDef("sydney", "Sydney-Canberra", "Oceania", 0.94f, 0.82f, hexPoly(0.94f, 0.82f, 0.034f, 0.044f), TerrainType.URBAN, 10, "ENG", false, 3, 2, 0, 7, listOf("hawaii", "kalimantan", "perth"))
    )

    val provinceMap: Map<String, ProvinceDef> = provinces.associateBy { it.id }

    // Stylized simplified landmass silhouettes in normalized [0..1] world coordinates for visual map backdrop
    val continentSilhouettes: List<List<MapPoint>> = listOf(
        // North America + Central America
        listOf(
            MapPoint(0.04f, 0.14f), MapPoint(0.18f, 0.11f), MapPoint(0.30f, 0.16f),
            MapPoint(0.29f, 0.31f), MapPoint(0.22f, 0.43f), MapPoint(0.21f, 0.54f),
            MapPoint(0.15f, 0.51f), MapPoint(0.10f, 0.39f), MapPoint(0.08f, 0.24f), MapPoint(0.04f, 0.21f)
        ),
        // Greenland
        listOf(
            MapPoint(0.32f, 0.07f), MapPoint(0.39f, 0.07f), MapPoint(0.37f, 0.16f), MapPoint(0.33f, 0.15f)
        ),
        // South America
        listOf(
            MapPoint(0.22f, 0.55f), MapPoint(0.31f, 0.58f), MapPoint(0.36f, 0.69f),
            MapPoint(0.30f, 0.87f), MapPoint(0.25f, 0.88f), MapPoint(0.22f, 0.69f)
        ),
        // Europe & Eurasia & Asia
        listOf(
            MapPoint(0.43f, 0.20f), MapPoint(0.54f, 0.11f), MapPoint(0.75f, 0.12f), MapPoint(0.93f, 0.16f),
            MapPoint(0.92f, 0.36f), MapPoint(0.87f, 0.48f), MapPoint(0.83f, 0.61f),
            MapPoint(0.76f, 0.59f), MapPoint(0.70f, 0.53f), MapPoint(0.62f, 0.45f),
            MapPoint(0.55f, 0.43f), MapPoint(0.45f, 0.41f), MapPoint(0.42f, 0.34f)
        ),
        // Africa
        listOf(
            MapPoint(0.42f, 0.44f), MapPoint(0.58f, 0.46f), MapPoint(0.65f, 0.58f),
            MapPoint(0.60f, 0.82f), MapPoint(0.53f, 0.83f), MapPoint(0.49f, 0.65f), MapPoint(0.41f, 0.58f)
        ),
        // Australia
        listOf(
            MapPoint(0.84f, 0.76f), MapPoint(0.96f, 0.75f), MapPoint(0.97f, 0.87f), MapPoint(0.85f, 0.86f)
        )
    )

    val focusTree: List<FocusNode> = listOf(
        FocusNode("focus_four_year", "Four-Year Industry Plan", "Expand domestic civil engineering", 21, emptyList(), 0, 0, "+2 Civilian Factories in Capital"),
        FocusNode("focus_synthetic", "Heavy Machine Works", "Tooling for mass war production", 28, listOf("focus_four_year"), 0, 1, "+2 Military Factories, +80 Infantry Gear"),
        FocusNode("focus_total_war", "Total Industrial Mobilization", "Convert civil plants to munitions", 35, listOf("focus_synthetic"), 0, 2, "+3 Military Factories, +10% War Support"),

        FocusNode("focus_general_staff", "Reform General Staff", "Modernize officer command corps", 21, emptyList(), 1, 0, "+35 Command Power, +50 PP"),
        FocusNode("focus_blitz", "Combined Arms Doctrine", "Synchronize armor and mobile troops", 28, listOf("focus_general_staff"), 1, 1, "+40 Light Tanks, +15% Breakthrough"),
        FocusNode("focus_fortress", "Fortress Frontier", "Reinforce border defensive belts", 28, listOf("focus_general_staff"), 1, 2, "+2 Land Forts in Capital & Border"),

        FocusNode("focus_rally_nation", "National Mobilization Speech", "Unify public resolve for security", 21, emptyList(), 2, 0, "+120 PP, +8% Stability"),
        FocusNode("focus_rearmament", "Emergency Rearmament Act", "Conscript reserve cadres early", 28, listOf("focus_rally_nation"), 2, 1, "+180K Manpower, +10% War Support"),
        FocusNode("focus_hegemony", "Global Hegemony", "Assert strategic claims across theaters", 35, listOf("focus_rearmament", "focus_blitz"), 2, 2, "+150 PP, +10% World Tension, Fast War Goals")
    )

    val techTree: List<TechNode> = listOf(
        TechNode("tech_inf_1", "Submachine Squads", TechCategory.INFANTRY, 1, 25, emptyList(), "+12% Infantry Soft Attack"),
        TechNode("tech_inf_2", "Anti-Tank Rifles", TechCategory.INFANTRY, 2, 35, listOf("tech_inf_1"), "+25% Infantry Hard Attack & Defense"),
        TechNode("tech_inf_3", "Heavy Field Howitzers", TechCategory.INFANTRY, 3, 45, listOf("tech_inf_2"), "+18% All Division Attack"),

        TechNode("tech_arm_1", "Welded Tank Hulls", TechCategory.ARMOR, 1, 28, emptyList(), "+15% Tank Breakthrough & Armor"),
        TechNode("tech_arm_2", "Medium Battle Tank", TechCategory.ARMOR, 2, 40, listOf("tech_arm_1"), "+25% Tank Hard & Soft Attack"),
        TechNode("tech_arm_3", "Heavy Breakthrough Armor", TechCategory.ARMOR, 3, 50, listOf("tech_arm_2"), "+30% Armor Combat Power"),

        TechNode("tech_ind_1", "Concentrated Tooling", TechCategory.INDUSTRY, 1, 24, emptyList(), "+15% Factory Production Output"),
        TechNode("tech_ind_2", "Assembly Line Mass Prod.", TechCategory.INDUSTRY, 2, 34, listOf("tech_ind_1"), "+20% Construction & Output Speed"),
        TechNode("tech_ind_3", "Advanced Radar & Computing", TechCategory.INDUSTRY, 3, 42, listOf("tech_ind_2"), "+20% Research Speed & Air Bonus"),

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

        val divisions = mutableListOf<DivisionState>()
        var divSeq = 1
        val ownedByNation = provinces.groupBy { it.initialOwnerId }
        for ((nationId, provList) in ownedByNation) {
            val baseCount = when (nationId) {
                "SOV" -> 14
                "GER" -> 11
                "ENG" -> 11
                "USA" -> 10
                "FRA" -> 9
                "JPN" -> 8
                "CHI" -> 8
                "ITA" -> 7
                "INS" -> 6
                "POL" -> 5
                "BRA" -> 4
                "TUR" -> 4
                "SPR" -> 4
                "SWE" -> 4
                else -> 8
            }
            for (i in 0 until baseCount) {
                val prov = provList[i % provList.size]
                val template = when {
                    i == 0 && nationId in setOf("GER", "SOV", "USA", "ENG", "FRA", "JPN") -> DivisionTemplate.ARMOR
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

        val alliesGroup = setOf("ENG", "FRA", "USA")
        val pactGroup = setOf("GER", "ITA")
        val diplomacy = nations.keys
            .filter { it != playerNationId }
            .associateWith { other ->
                when {
                    playerNationId in alliesGroup && other in alliesGroup -> DiplomacyStatus.ALLIANCE
                    playerNationId in pactGroup && other in pactGroup -> DiplomacyStatus.NON_AGGRESSION
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
                EquipmentType.INFANTRY_EQ to 1800,
                EquipmentType.ARTILLERY to 160,
                EquipmentType.LIGHT_TANK to 54,
                EquipmentType.FIGHTER to 40
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
            newsLog = listOf("1936.01.01 — High Command assumes global control of ${player.name}.")
        )
    }
}
