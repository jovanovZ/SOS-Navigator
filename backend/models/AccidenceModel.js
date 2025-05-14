var mongoose = require("mongoose");
var Schema = mongoose.Schema;

var accidentSchema = new Schema({
  locationId: { type: Schema.Types.ObjectId, ref: "location" },
  typeOfAccident: {
    type: String,
    enum: [
      "prometna",
      "požar",
      "naravna nesreča",
      "onesnaženje",
      "zdravstveni nujni primer",
      "eksplozija",
      "napad", // assult je mišljeno
      "drugo",
    ],
  },
});

module.exports = mongoose.model("accident", accidentSchema);
