// ne pozabi se tega, da bo uporabnik imel profilno sliko, ki jo bo shranil na nek cloud
// tako bo en element modela tudi URL (imageUrl), ki kaze na cloud
var mongoose = require("mongoose");
var Schema = mongoose.Schema;

var UserSchema = new Schema({
  username: { type: String, required: true },
  email: { type: String, required: true },
  password: { type: String, required: true },
  imageUrl: { type: String, required: true },
  historySimulations: [{ type: Schema.Types.ObjectId, ref: "simulation", default: [] }], // to je zgodovina simulacij, ki jih je uporabnik naredil
});

module.exports = mongoose.model("user", UserSchema);
