/*
const mqtt = require("mqtt");


const MQTT_URL = process.env.MQTT_URL 

const clientId = `backend-subscriber-${Math.random().toString(16).slice(2)}`;

const client = mqtt.connect(MQTT_URL, {
  clientId,
  clean: true,
});

client.on("connect", () => {
  console.log(" MQTT connected:", MQTT_URL);

  client.subscribe("device/+/command/#", (err) => {
    if (err) {
      console.error("MQTT subscribe error:", err);
    } else {
      console.log("Subscribed to device/+/command/#");
    }
  });
});

client.on("message", (topic, message) => {
  try {
    const payload = JSON.parse(message.toString());
    console.log("MQTT message received");
    console.log("Topic:", topic);
    console.log("Payload:", payload);

    handleMessage(topic, payload);
  } catch (err) {
    console.error("Invalid JSON:", err.message);
  }
});

client.on("error", (err) => {
  console.error("MQTT error:", err);
});

function handleMessage(topic, payload) {
  const parts = topic.split("/");
  // device/1/command/message
  const deviceId = parts[1];
  const commandType = parts[3];

  switch (commandType) {
    case "message":
      // tu bo logika za create message
      console.log(`Message from device ${deviceId}:`, payload.message);
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
*/
