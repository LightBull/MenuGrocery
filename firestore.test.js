const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

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

test("Unauthenticated user: cannot read meal plans", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("meal_plans").get());
});

test("Unauthenticated user: cannot read shopping items", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("shopping_items").get());
});

test("Authenticated user: can create and read own meal plan", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const planRef = aliceDb.collection("users").doc(ALICE_UID).collection("meal_plans").doc("plan_1");

  await assertSucceeds(planRef.set({
    id: "plan_1",
    userId: ALICE_UID,
    title: "Piano Benessere",
    notes: "Note piano",
    isActive: true,
    mealsJson: "[]"
  }));

  await assertSucceeds(planRef.get());
});

test("Authenticated user: cannot read another user's meal plan", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("meal_plans").doc("bob_plan").set({
      id: "bob_plan",
      userId: BOB_UID,
      title: "Bob's Plan",
      notes: "Note Bob",
      isActive: true,
      mealsJson: "[]"
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("meal_plans").doc("bob_plan").get());
});

test("Authenticated user: can create and read own shopping item", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const itemRef = aliceDb.collection("users").doc(ALICE_UID).collection("shopping_items").doc("item_1");

  await assertSucceeds(itemRef.set({
    id: "item_1",
    userId: ALICE_UID,
    foodName: "Petto di Pollo",
    category: "Carni e Pollame",
    quantity: 500.0,
    unit: "g",
    isPurchased: false,
    isManualExtra: false
  }));

  await assertSucceeds(itemRef.get());
});

test("Authenticated user: cannot write another user's shopping item", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobItemRef = aliceDb.collection("users").doc(BOB_UID).collection("shopping_items").doc("item_bob");

  await assertFails(bobItemRef.set({
    id: "item_bob",
    userId: BOB_UID,
    foodName: "Mele",
    category: "Frutta e Verdura",
    quantity: 1.0,
    unit: "kg",
    isPurchased: false,
    isManualExtra: false
  }));
});
