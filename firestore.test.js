const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_commander_01";
const BOB_UID = "bob_commander_02";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function buildValidCampaign(uid, overrides = {}) {
  const past = new Date(Date.now() - 5000);
  return {
    userId: uid,
    campaignName: "Operation Barbarossa 1936",
    playerNationId: "GER",
    difficulty: "REGULAR",
    gameDateEpochDay: -12418,
    gameHour: 12,
    politicalPower: 75.0,
    stability: 0.72,
    warSupport: 0.68,
    manpower: 1250000,
    commandPower: 30.0,
    worldTension: 0.12,
    conscriptionLaw: "LIMITED_CONSCRIPTION",
    economyLaw: "PARTIAL_MOBILIZATION",
    tradeLaw: "EXPORT_FOCUS",
    activeFocusId: "ger_rhineland",
    activeFocusProgressDays: 7,
    completedFocusIds: ["ger_four_year_plan"],
    activeResearchId: "tech_infantry_1",
    activeResearchProgressDays: 14,
    unlockedTechIds: ["tech_basic_eq"],
    provinceOwners: { ber: "GER", war: "POL" },
    provinceCivFactories: { ber: 6, war: 3 },
    provinceMilFactories: { ber: 5, war: 2 },
    provinceForts: { ber: 1, war: 0 },
    provinceInfra: { ber: 8, war: 5 },
    equipmentStockpile: { INFANTRY_EQ: 4200, ARTILLERY: 240, LIGHT_TANK: 80, FIGHTER: 60 },
    diplomacyStates: { POL: "PEACE", FRA: "PEACE" },
    nationRelations: { POL: -30, FRA: -45 },
    warGoalProgress: {},
    divisionsData: [
      {
        id: "div_1",
        name: "1. Infanterie-Division",
        nationId: "GER",
        templateId: "INFANTRY",
        provinceId: "ber",
        targetProvinceId: "",
        moveProgress: 0.0,
        hp: 100.0,
        org: 60.0,
        entrenchment: 5.0,
      },
    ],
    productionLinesData: [
      { equipmentType: "INFANTRY_EQ", assignedFactories: 3, progress: 0.45 },
    ],
    constructionQueueData: [
      { provinceId: "ber", buildingType: "MIL_FACTORY", progressDays: 10, totalDays: 45 },
    ],
    trainingQueueData: [
      { templateId: "ARMOR", deployProvinceId: "ber", progressDays: 20, totalDays: 60 },
    ],
    status: "active",
    createdAt: past,
    updatedAt: past,
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write profiles or campaigns", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("campaigns").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("campaigns").doc("c_1").set(
      buildValidCampaign(ALICE_UID)
    )
  );
});

test("Authenticated user can create, update, and list own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const past = new Date(Date.now() - 5000);
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      commanderName: "Field Marshal Alice",
      totalCampaigns: 1,
      victories: 0,
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      totalCampaigns: 2,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject immutable createdAt modification
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Authenticated owner can create, list, partially update, and lock completed campaigns", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const campRef = aliceDb.collection("users").doc(ALICE_UID).collection("campaigns").doc("camp_1");

  await assertSucceeds(campRef.set(buildValidCampaign(ALICE_UID)));

  // List own subcollection succeeds even without filter
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("campaigns").get()
  );

  // Partial update on active campaign succeeds
  await assertSucceeds(
    campRef.update({
      gameDateEpochDay: -12410,
      gameHour: 18,
      politicalPower: 92.5,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Ghost field 'id' on create is rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("campaigns").doc("camp_ghost").set(
      buildValidCampaign(ALICE_UID, { id: "camp_ghost" })
    )
  );

  // Transition to victory succeeds, then subsequent updates are locked
  await assertSucceeds(
    campRef.update({
      status: "victory",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  await assertFails(
    campRef.update({
      politicalPower: 999.0,
      updatedAt: new Date(Date.now() - 200),
    })
  );
});

test("Cross-user access to profiles and campaigns is strictly denied", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("users").doc(BOB_UID).collection("campaigns").doc("bob_camp").set(
      buildValidCampaign(BOB_UID)
    );
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("campaigns").get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("campaigns").doc("bob_camp").get()
  );
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("campaigns").doc("bob_camp").update({
      politicalPower: 500.0,
    })
  );
});

test("Out-of-bounds values and future timestamps are rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("campaigns").doc("bad_stab").set(
      buildValidCampaign(ALICE_UID, { stability: 1.5 })
    )
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("campaigns").doc("bad_time").set(
      buildValidCampaign(ALICE_UID, { updatedAt: new Date(Date.now() + 60000) })
    )
  );
});
