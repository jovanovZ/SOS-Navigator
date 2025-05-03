var mongoose = require("mongoose");
var Schema = mongoose.Schema;

var StationSchema = new Schema({
  locationId: { type: Schema.Types.ObjectId, ref: "location" },
  typeOfStation: { type: String, enum: ["Policijska", "Bolnica", "Gasilci"] }, // če bomo naredili da lahko dadajaš poljubne postaje potem to spremeni in daj v String
  isPermanent: Boolean, // ali je lokacija postaje stalna
  region: {
    type: String,
    enum: [
      //Slika da vidiš kje so te regije https://sl.wikipedia.org/wiki/Statisti%C4%8Dne_regije_Slovenije#/media/Slika:Statisticne_regije_Slovenije_2015.svg
      "Pomurska",
      "Podravska",
      "Koroška",
      "Savinjska",
      "Zasavska",
      "Posavska",
      "Jugovzhodna Slovenija",
      "Osrednjeslovenska",
      "Gorenjska",
      "Primorsko-notranjska",
      "Goriška",
      "Obalno-kraška",
    ],
  },
});

module.exports = mongoose.model("station", StationSchema);
