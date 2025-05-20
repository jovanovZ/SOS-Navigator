const mongoose = require("mongoose");
const Schema = mongoose.Schema;

const PathSchema = new Schema({
  accidentId: { type: Schema.Types.ObjectId, ref: "accident" },
  locationPoints: [
    {
      lat: { type: Number, required: true },
      lng: { type: Number, required: true }
    }
  ]
});

module.exports = mongoose.model("path", PathSchema);
