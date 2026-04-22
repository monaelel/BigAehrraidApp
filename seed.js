const admin = require("firebase-admin");
const serviceAccount = require("./big-aehrraid-firebase-adminsdk-fbsvc-f14d90fb7b.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

const db = admin.firestore();

const restaurants = [
  {
    name: "Schwartz's Deli",
    email: "info@schwartzsdeli.com",
    phone: "514-842-4813",
    mail: "info@schwartzsdeli.com",
    street: "3895 Boul. Saint-Laurent",
    city: "Montreal",
    province: "Quebec",
    postalCode: "H2W 1K4",
    lat: 0.0,
    lng: 0.0,
  },
  {
    name: "Joe Beef",
    email: "info@joebeef.com",
    phone: "514-935-6504",
    mail: "info@joebeef.com",
    street: "2491 R. Notre Dame O",
    city: "Montreal",
    province: "Quebec",
    postalCode: "H3J 1N6",
    lat: 0.0,
    lng: 0.0,
  },
  {
    name: "Au Pied de Cochon",
    email: "reservation@aupieddecochon.ca",
    phone: "514-281-1114",
    mail: "reservation@aupieddecochon.ca",
    street: "536 Av. Duluth E",
    city: "Montreal",
    province: "Quebec",
    postalCode: "H2L 1A9",
    lat: 0.0,
    lng: 0.0,
  },
  {
    name: "La Banquise",
    email: "info@labanquise.com",
    phone: "514-525-2415",
    mail: "info@labanquise.com",
    street: "994 Rue Rachel E",
    city: "Montreal",
    province: "Quebec",
    postalCode: "H2J 2J3",
    lat: 0.0,
    lng: 0.0,
  },
  {
    name: "St-Viateur Bagel",
    email: "info@stviateurbagel.com",
    phone: "514-276-8044",
    mail: "info@stviateurbagel.com",
    street: "263 R. Saint Viateur Ouest",
    city: "Montreal",
    province: "Quebec",
    postalCode: "H2V 1Y1",
    lat: 0.0,
    lng: 0.0,
  },
  {
    name: "Toqué!",
    email: "info@restaurant-toque.com",
    phone: "514-499-2084",
    mail: "info@restaurant-toque.com",
    street: "900 Pl. Jean-Paul-Riopelle",
    city: "Montreal",
    province: "Quebec",
    postalCode: "H2Z 2B2",
    lat: 0.0,
    lng: 0.0,
  },
  {
    name: "L'Express",
    email: "info@restaurantlexpress.com",
    phone: "514-845-5333",
    mail: "info@restaurantlexpress.com",
    street: "3927 R. Saint-Denis",
    city: "Montreal",
    province: "Quebec",
    postalCode: "H2W 2M4",
    lat: 0.0,
    lng: 0.0,
  },
  {
    name: "Damas",
    email: "info@damas.ca",
    phone: "514-439-5435",
    mail: "info@damas.ca",
    street: "1209 Av. Van Horne",
    city: "Montreal",
    province: "Quebec",
    postalCode: "H2V 3S5",
    lat: 0.0,
    lng: 0.0,
  },
  {
    name: "Wilensky's Light Lunch Inc",
    email: "info@wilenskys.com",
    phone: "514-271-0247",
    mail: "info@wilenskys.com",
    street: "34 Av. Fairmount O",
    city: "Montreal",
    province: "Quebec",
    postalCode: "H2T 2M1",
    lat: 0.0,
    lng: 0.0,
  },
  {
    name: "Ma Poule Mouillée",
    email: "info@mapoulemouillee.ca",
    phone: "514-522-5175",
    mail: "info@mapoulemouillee.ca",
    street: "969 Rachel St E",
    city: "Montreal",
    province: "Quebec",
    postalCode: "H2J 2J2",
    lat: 0.0,
    lng: 0.0,
  },
];

async function seed() {
  for (const r of restaurants) {
    const authUser = await admin.auth().createUser({
      email: r.email,
      password: "12345678",
    });
    const uid = authUser.uid;

    const batch = db.batch();
    batch.set(db.collection("users").doc(uid), { email: r.email, role: "restaurant" });
    batch.set(db.collection("restaurants").doc(uid), r);
    await batch.commit();

    console.log(`Created: ${r.name} (${r.email})`);
  }

  console.log(`\nDone. Seeded ${restaurants.length} restaurants.`);
  process.exit(0);
}

seed().catch((err) => {
  console.error(err);
  process.exit(1);
});
