var mongoose = require("mongoose");
var Schema = mongoose.Schema;

var accidentSchema = new Schema({
  locationId: { type: Schema.Types.ObjectId, ref: "location" },
  typeOfAccident: {
    type: String,
    enum: [
      "Prometna",
      "Požar",
      "Naravna nesreča",
      "Onesnaženje",
      "Zdravstveni nujni primer",
      "Eksplozija",
      "Napad", // assult je mišljeno
      "Drugo",
    ],
  },
});

module.exports = mongoose.model("accident", accidentSchema);
