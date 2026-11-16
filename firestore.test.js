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

test("unauthenticated user cannot read students", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthedDb.collection("students").get());
});

test("authenticated user can create valid student", async () => {
  const authedDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    authedDb.collection("students").doc("student_1").set({
      fullName: "Muhammad Usman",
      fatherName: "Tariq Mahmood",
      cnicOrBForm: "35201-8765432-1",
      regNumber: "NNL-101",
      totalFee: 4000,
      feeSubmitted: 4000,
      feeRemaining: 0,
      phone: "0300-1234567",
      address: "Lahore",
      seatNumber: "Desk A-1",
      shift: "Full Day",
      isScholarship: false,
      admissionDate: Date.now(),
      isActive: true
    })
  );
});

test("invalid student fails validation", async () => {
  const authedDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    authedDb.collection("students").doc("student_bad").set({
      fullName: "", // empty name
      fatherName: "Unknown"
    })
  );
});

test("authenticated user can record fee payment", async () => {
  const authedDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    authedDb.collection("fee_payments").doc("payment_1").set({
      studentId: "student_1",
      studentName: "Muhammad Usman",
      amount: 4000,
      receiptNumber: "RCP-101",
      paymentDate: Date.now(),
      paymentMethod: "Cash"
    })
  );
});
