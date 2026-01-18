const mqtt = require("mqtt");
const Message = require("../models/MessageModel");

const MQTT_URL = process.env.MQTT_URL || "mqtt://localhost:1883";

const clientId = `backend-subscriber-${Math.random().toString(16).slice(2)}`;

const client = mqtt.connect(MQTT_URL, {
  clientId,
  clean: true,
});

client.on("connect", () => {
  console.log("MQTT connected:", MQTT_URL);

  client.subscribe("device/+/command/#", (err) => {
    if (err) {
      console.error("MQTT subscribe error:", err);
    } else {
      console.log("Subscribed to device/+/command/#");
    }
  });
});

client.on("message", async (topic, message) => {
  try {
    const payload = JSON.parse(message.toString());
    console.log("MQTT message received");
    console.log("Topic:", topic);
    console.log("Payload:", payload);

    await handleMessage(topic, payload);
  } catch (err) {
    console.error("Invalid JSON or error processing message:", err.message);
  }
});

client.on("error", (err) => {
  console.error("MQTT error:", err);
});

async function handleMessage(topic, payload) {
  const parts = topic.split("/");
  // device/1/command/message
  const deviceId = parts[1];
  const commandType = parts[3];

  switch (commandType) {
    case "message":
      try {
        const messageText = payload.message || payload.messageText || "";
        const newMessage = new Message({
          deviceId,
          message: messageText,
        });
        await newMessage.save();
        console.log(`Message saved from device ${deviceId}:`, messageText);
      } catch (error) {
        console.error("Failed to save message:", error);
      }
      break;

    case "update":
      try {
        const updateType = payload.updateType;
        const vehicleId = payload.vehicleId;
        const accidentId = payload.accidentId;
        
        if (!vehicleId && !accidentId) {
          console.error("Vehicle ID or Accident ID missing in update payload");
          break;
        }
        
        const Vehicle = require("../models/VehicleModel");
        const Location = require("../models/LocationModel");
        const Accident = require("../models/AccidenceModel");
        
        // VEHICLE UPDATES
        if (vehicleId) {
          if (updateType === "location") {
            const newLocation = new Location({
              geometry: {
                type: "Point",
                coordinates: [payload.longitude, payload.latitude],
              },
            });
            await newLocation.save();
            
            await Vehicle.findByIdAndUpdate(
              vehicleId,
              { locationStartId: newLocation._id },
              { new: true }
            );
            
            console.log(`Vehicle ${vehicleId} location updated`);
            
          } else if (updateType === "acceleration") {
            await Vehicle.findByIdAndUpdate(
              vehicleId,
              { acceleration: payload.acceleration },
              { new: true }
            );
            console.log(`Vehicle ${vehicleId} acceleration updated to ${payload.acceleration}`);
          }
        }
        
        // ACCIDENT UPDATES
        else if (accidentId) {
          if (updateType === "accident_location") {
            const newLocation = new Location({
              geometry: {
                type: "Point",
                coordinates: [payload.longitude, payload.latitude],
              },
            });
            await newLocation.save();
            
            const accident = await Accident.findById(accidentId);
            if (accident) {
              await Accident.findByIdAndUpdate(
                accidentId,
                {
                  locationId: newLocation._id,
                  typeOfAccident: accident.typeOfAccident,
                  locationFreq: accident.locationFreq
                },
                { new: true }
              );
              
              console.log(`Accident ${accidentId} location updated to [${payload.longitude}, ${payload.latitude}]`);
            } else {
              console.error(`Accident ${accidentId} not found`);
            }
          }
        }
      } catch (error) {
        console.error("Failed to handle update:", error);
      }
      break;

    default:
      console.log("Unknown command:", commandType);
  }
}

module.exports = client;