package com.example.data

import android.content.Context
import androidx.annotation.Keep
import com.example.R
import com.example.model.*
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val commanderName: String = "",
    val totalCampaigns: Long = 0L,
    val victories: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CampaignSave(
    @DocumentId val id: String = "",
    val userId: String = "",
    val campaignName: String = "",
    val playerNationId: String = "GER",
    val difficulty: String = "REGULAR",
    val gameDateEpochDay: Long = TheaterWorldData.EPOCH_DAY_1936_01_01,
    val gameHour: Long = 8L,
    val politicalPower: Double = 75.0,
    val stability: Double = 0.7,
    val warSupport: Double = 0.7,
    val manpower: Long = 1_000_000L,
    val commandPower: Double = 25.0,
    val worldTension: Double = 0.08,
    val conscriptionLaw: String = ConscriptionLaw.LIMITED.name,
    val economyLaw: String = EconomyLaw.CIVILIAN.name,
    val tradeLaw: String = TradeLaw.EXPORT_FOCUS.name,
    val activeFocusId: String = "",
    val activeFocusProgressDays: Long = 0L,
    val completedFocusIds: List<String> = emptyList(),
    val activeResearchId: String = "",
    val activeResearchProgressDays: Long = 0L,
    val unlockedTechIds: List<String> = emptyList(),
    val provinceOwners: Map<String, String> = emptyMap(),
    val provinceCivFactories: Map<String, Long> = emptyMap(),
    val provinceMilFactories: Map<String, Long> = emptyMap(),
    val provinceForts: Map<String, Long> = emptyMap(),
    val provinceInfra: Map<String, Long> = emptyMap(),
    val equipmentStockpile: Map<String, Long> = emptyMap(),
    val diplomacyStates: Map<String, String> = emptyMap(),
    val nationRelations: Map<String, Long> = emptyMap(),
    val warGoalProgress: Map<String, Long> = emptyMap(),
    val divisionsData: List<Map<String, Any>> = emptyList(),
    val productionLinesData: List<Map<String, Any>> = emptyList(),
    val constructionQueueData: List<Map<String, Any>> = emptyList(),
    val trainingQueueData: List<Map<String, Any>> = emptyList(),
    val status: String = "active",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCampaignState(): CampaignState {
        val diff = enumValues<Difficulty>().firstOrNull { it.name == difficulty } ?: Difficulty.REGULAR
        val conscription = enumValues<ConscriptionLaw>().firstOrNull { it.name == conscriptionLaw } ?: ConscriptionLaw.LIMITED
        val economy = enumValues<EconomyLaw>().firstOrNull { it.name == economyLaw } ?: EconomyLaw.CIVILIAN
        val trade = enumValues<TradeLaw>().firstOrNull { it.name == tradeLaw } ?: TradeLaw.EXPORT_FOCUS

        val stockpile = EquipmentType.entries.associateWith { eq ->
            (equipmentStockpile[eq.name] as? Number)?.toInt() ?: 0
        }

        val dipMap = diplomacyStates.mapValues { (_, raw) ->
            enumValues<DiplomacyStatus>().firstOrNull { it.name == raw } ?: DiplomacyStatus.PEACE
        }

        val divisions = divisionsData.mapNotNull { m ->
            val divId = m["id"] as? String ?: return@mapNotNull null
            val tplRaw = m["templateId"] as? String ?: DivisionTemplate.INFANTRY.name
            val tpl = enumValues<DivisionTemplate>().firstOrNull { it.name == tplRaw } ?: DivisionTemplate.INFANTRY
            DivisionState(
                id = divId,
                name = m["name"] as? String ?: "Division",
                nationId = m["nationId"] as? String ?: playerNationId,
                template = tpl,
                provinceId = m["provinceId"] as? String ?: "berlin",
                targetProvinceId = m["targetProvinceId"] as? String ?: "",
                moveProgress = (m["moveProgress"] as? Number)?.toDouble() ?: 0.0,
                hp = (m["hp"] as? Number)?.toDouble() ?: 100.0,
                org = (m["org"] as? Number)?.toDouble() ?: tpl.maxOrg,
                entrenchment = (m["entrenchment"] as? Number)?.toDouble() ?: 4.0
            )
        }

        val prodLines = productionLinesData.mapNotNull { m ->
            val eqRaw = m["equipmentType"] as? String ?: return@mapNotNull null
            val eq = enumValues<EquipmentType>().firstOrNull { it.name == eqRaw } ?: return@mapNotNull null
            ProductionLine(
                equipmentType = eq,
                assignedFactories = (m["assignedFactories"] as? Number)?.toInt() ?: 0,
                progress = (m["progress"] as? Number)?.toDouble() ?: 0.0
            )
        }

        val constQueue = constructionQueueData.mapNotNull { m ->
            val pId = m["provinceId"] as? String ?: return@mapNotNull null
            val bRaw = m["buildingType"] as? String ?: return@mapNotNull null
            val bType = enumValues<BuildingType>().firstOrNull { it.name == bRaw } ?: return@mapNotNull null
            ConstructionOrder(
                provinceId = pId,
                buildingType = bType,
                progressDays = (m["progressDays"] as? Number)?.toInt() ?: 0,
                totalDays = (m["totalDays"] as? Number)?.toInt() ?: bType.baseDays
            )
        }

        val trainQueue = trainingQueueData.mapNotNull { m ->
            val tplRaw = m["templateId"] as? String ?: return@mapNotNull null
            val tpl = enumValues<DivisionTemplate>().firstOrNull { it.name == tplRaw } ?: return@mapNotNull null
            TrainingOrder(
                template = tpl,
                deployProvinceId = m["deployProvinceId"] as? String ?: "berlin",
                progressDays = (m["progressDays"] as? Number)?.toInt() ?: 0,
                totalDays = (m["totalDays"] as? Number)?.toInt() ?: tpl.trainDays
            )
        }

        return CampaignState(
            campaignId = id,
            campaignName = campaignName,
            playerNationId = playerNationId,
            difficulty = diff,
            gameDateEpochDay = gameDateEpochDay,
            gameHour = gameHour.toInt().coerceIn(0, 23),
            politicalPower = politicalPower,
            stability = stability.coerceIn(0.0, 1.0),
            warSupport = warSupport.coerceIn(0.0, 1.0),
            manpower = manpower.coerceAtLeast(0L),
            commandPower = commandPower.coerceIn(0.0, 5000.0),
            worldTension = worldTension.coerceIn(0.0, 1.0),
            conscriptionLaw = conscription,
            economyLaw = economy,
            tradeLaw = trade,
            activeFocusId = activeFocusId,
            activeFocusProgressDays = activeFocusProgressDays.toInt(),
            completedFocusIds = completedFocusIds,
            activeResearchId = activeResearchId,
            activeResearchProgressDays = activeResearchProgressDays.toInt(),
            unlockedTechIds = unlockedTechIds,
            provinceOwners = provinceOwners,
            provinceCivFactories = provinceCivFactories.mapValues { it.value.toInt() },
            provinceMilFactories = provinceMilFactories.mapValues { it.value.toInt() },
            provinceForts = provinceForts.mapValues { it.value.toInt() },
            provinceInfra = provinceInfra.mapValues { it.value.toInt() },
            equipmentStockpile = stockpile,
            diplomacyStates = dipMap,
            nationRelations = nationRelations.mapValues { it.value.toInt() },
            warGoalProgress = warGoalProgress.mapValues { it.value.toInt() },
            divisions = divisions,
            productionLines = prodLines,
            constructionQueue = constQueue,
            trainingQueue = trainQueue,
            status = status,
            newsLog = listOf("Campaign loaded at ${com.example.engine.GrandStrategyEngine.formatDate(gameDateEpochDay, gameHour.toInt())}.")
        )
    }
}

class CampaignRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    suspend fun ensureUserProfile(userId: String, displayName: String?): Result<Unit> {
        val docPath = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch." }
            val docRef = db.collection("users").document(uid)
            val existing = docRef.get().await()
            if (!existing.exists()) {
                val safeName = (displayName?.trim()?.take(40)).takeUnless { it.isNullOrBlank() } ?: "Commander"
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "commanderName" to safeName,
                        "totalCampaigns" to 0L,
                        "victories" to 0L,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    fun observeCampaigns(userId: String): Flow<List<CampaignSave>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch." }
        val path = "users/$uid/campaigns"
        emitAll(
            db.collection("users").document(uid).collection("campaigns")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(CampaignSave::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun loadCampaign(userId: String, campaignId: String): Result<CampaignState> {
        val docPath = "users/$userId/campaigns/$campaignId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch." }
            require(campaignId.isNotBlank()) { "Campaign ID required." }
            val snap = db.collection("users").document(uid).collection("campaigns").document(campaignId)
                .get()
                .await()
            val save = snap.toObject(CampaignSave::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: return Result.failure(IllegalStateException("Campaign save not found"))
            Result.success(save.toCampaignState())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docPath)
            Result.failure(e)
        }
    }

    suspend fun createCampaign(state: CampaignState): Result<String> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauth"}/campaigns"
        return try {
            val uid = requireUserId()
            val cleanName = state.campaignName.trim().take(60).ifBlank { "1936 Campaign" }
            val docRef = db.collection("users").document(uid).collection("campaigns").document()
            docPath = docRef.path
            val payload = stateToCreateMap(uid, state.copy(campaignName = cleanName))
            docRef.set(payload).await()
            // Best-effort increment totalCampaigns on user profile
            try {
                db.collection("users").document(uid).update(
                    mapOf(
                        "totalCampaigns" to FieldValue.increment(1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (_: Exception) { /* Non-fatal if profile doc wasn't created yet */ }
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun saveCampaign(state: CampaignState): Result<Unit> {
        val cid = state.campaignId
        var docPath = "users/${auth.currentUser?.uid ?: "unauth"}/campaigns/$cid"
        return try {
            val uid = requireUserId()
            require(cid.isNotBlank()) { "Campaign ID must not be blank for update" }
            val docRef = db.collection("users").document(uid).collection("campaigns").document(cid)
            docPath = docRef.path
            val payload = stateToUpdateMap(state)
            docRef.update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteCampaign(userId: String, campaignId: String): Result<Unit> {
        val docPath = "users/$userId/campaigns/$campaignId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch." }
            require(campaignId.isNotBlank()) { "Campaign ID required" }
            db.collection("users").document(uid).collection("campaigns").document(campaignId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    private fun stateToCommonFields(state: CampaignState): Map<String, Any> {
        val cleanName = state.campaignName.trim().take(60).ifBlank { "1936 Campaign" }
        val divsList = state.divisions.take(150).map { d ->
            mapOf(
                "id" to d.id,
                "name" to d.name,
                "nationId" to d.nationId,
                "templateId" to d.template.name,
                "provinceId" to d.provinceId,
                "targetProvinceId" to d.targetProvinceId,
                "moveProgress" to d.moveProgress,
                "hp" to d.hp,
                "org" to d.org,
                "entrenchment" to d.entrenchment
            )
        }
        val prodList = state.productionLines.take(20).map { p ->
            mapOf(
                "equipmentType" to p.equipmentType.name,
                "assignedFactories" to p.assignedFactories.toLong(),
                "progress" to p.progress
            )
        }
        val constList = state.constructionQueue.take(30).map { c ->
            mapOf(
                "provinceId" to c.provinceId,
                "buildingType" to c.buildingType.name,
                "progressDays" to c.progressDays.toLong(),
                "totalDays" to c.totalDays.toLong()
            )
        }
        val trainList = state.trainingQueue.take(20).map { t ->
            mapOf(
                "templateId" to t.template.name,
                "deployProvinceId" to t.deployProvinceId,
                "progressDays" to t.progressDays.toLong(),
                "totalDays" to t.totalDays.toLong()
            )
        }
        return mapOf(
            "campaignName" to cleanName,
            "gameDateEpochDay" to state.gameDateEpochDay,
            "gameHour" to state.gameHour.toLong().coerceIn(0L, 23L),
            "politicalPower" to state.politicalPower.coerceIn(-500.0, 50000.0),
            "stability" to state.stability.coerceIn(0.0, 1.0),
            "warSupport" to state.warSupport.coerceIn(0.0, 1.0),
            "manpower" to state.manpower.coerceIn(0L, 200_000_000L),
            "commandPower" to state.commandPower.coerceIn(0.0, 5000.0),
            "worldTension" to state.worldTension.coerceIn(0.0, 1.0),
            "conscriptionLaw" to state.conscriptionLaw.name,
            "economyLaw" to state.economyLaw.name,
            "tradeLaw" to state.tradeLaw.name,
            "activeFocusId" to state.activeFocusId.take(64),
            "activeFocusProgressDays" to state.activeFocusProgressDays.toLong().coerceIn(0L, 365L),
            "completedFocusIds" to state.completedFocusIds.take(100),
            "activeResearchId" to state.activeResearchId.take(64),
            "activeResearchProgressDays" to state.activeResearchProgressDays.toLong().coerceIn(0L, 500L),
            "unlockedTechIds" to state.unlockedTechIds.take(100),
            "provinceOwners" to state.provinceOwners,
            "provinceCivFactories" to state.provinceCivFactories.mapValues { it.value.toLong() },
            "provinceMilFactories" to state.provinceMilFactories.mapValues { it.value.toLong() },
            "provinceForts" to state.provinceForts.mapValues { it.value.toLong() },
            "provinceInfra" to state.provinceInfra.mapValues { it.value.toLong() },
            "equipmentStockpile" to state.equipmentStockpile.entries.associate { it.key.name to it.value.toLong() },
            "diplomacyStates" to state.diplomacyStates.mapValues { it.value.name },
            "nationRelations" to state.nationRelations.mapValues { it.value.toLong() },
            "warGoalProgress" to state.warGoalProgress.mapValues { it.value.toLong() },
            "divisionsData" to divsList,
            "productionLinesData" to prodList,
            "constructionQueueData" to constList,
            "trainingQueueData" to trainList,
            "status" to state.status,
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }

    private fun stateToCreateMap(uid: String, state: CampaignState): Map<String, Any> {
        return stateToCommonFields(state) + mapOf(
            "userId" to uid,
            "playerNationId" to state.playerNationId,
            "difficulty" to state.difficulty.name,
            "status" to "active",
            "createdAt" to FieldValue.serverTimestamp()
        )
    }

    private fun stateToUpdateMap(state: CampaignState): Map<String, Any> {
        return stateToCommonFields(state)
    }
}
