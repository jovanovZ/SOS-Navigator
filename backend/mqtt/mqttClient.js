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
      // tu bo logika za update Vehicle in Accident
      console.log(`Update from device ${deviceId}`, payload);
      break;

    default:
      console.log("Unknown command:", commandType);
  }
}

module.exports = client;