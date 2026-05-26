// Creates a dedicated user with access only to unojavagame DB
// Root credentials are only used for admin tasks
db = db.getSiblingDB('unojavagame');

db.createUser({
  user: process.env.MONGO_APP_USER,
  pwd:  process.env.MONGO_APP_PASS,
  roles: [{ role: 'readWrite', db: 'unojavagame' }]
});
