var mongoose = require("mongoose");
var Schema = mongoose.Schema;

var PathSchema = new Schema({
  accidentId: { type: Schema.Types.ObjectId, ref: "accident" },
  locationPoints: [{ type: Schema.Types.ObjectId, ref: "location" }], // to so točke ko jih dobimo iz openStreetMap-a in imajo long in lat. Nisem ziher da bojo ble typeId in ref na location, ali pač bojo samo dejanke vrednoti noter
});

module.exports = mongoose.model("path", PathSchema);
