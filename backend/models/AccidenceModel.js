var mongoose = require("mongoose");
var Schema = mongoose.Schema;

var accidentSchema = new Schema({
  locationId: { type: Schema.Types.ObjectId, ref: "location" },
  typeOfAccident: {
    type: String,
    enum: [
      "prometna",
      "naravna nesreča",
      "zdravstveni primer",
      "kriminal",
    ],
  },
  locationFreq: { type: Number, default: 1440 }, // shranjeno je v minutah 1dan = 1440min
  timeStamp: { type: Date, default: Date.now },
});

module.exports = mongoose.model("accident", accidentSchema);
