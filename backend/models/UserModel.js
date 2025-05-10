// ne pozabi se tega, da bo uporabnik imel profilno sliko, ki jo bo shranil na nek cloud
// tako bo en element modela tudi URL (imageUrl), ki kaze na cloud
var mongoose = require("mongoose");
var Schema = mongoose.Schema;

var UserSchema = new Schema({
  name: String,
  email: String,
  password: String,
  imageUrl: String,
  historySimulations: [{ type: Schema.Types.ObjectId, ref: "simulation" }],
});

module.exports = mongoose.model("user", UserSchema);
