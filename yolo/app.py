from flask import Flask, request, jsonify
from ultralytics import YOLO
from PIL import Image
import io
from pymongo import MongoClient
from dotenv import load_dotenv
import os
from pymongo.errors import ServerSelectionTimeoutError
import base64
import gridfs
from datetime import datetime
import numpy as np
import cv2



load_dotenv()

app = Flask(__name__)

try:
    client = MongoClient(
        os.getenv("MONGO_URI"),
        serverSelectionTimeoutMS=3000  
    )

    client.admin.command("ping")
    print("MongoDB connection OK")

except ServerSelectionTimeoutError as e:
    print("MongoDB connection FAILED")
    print(e)
    exit(1)


db = client["SOS-Navigator"]
fs = gridfs.GridFS(db)
collection = db["traffic_data"]

model = YOLO("runs/detect/train2/weights/best.pt")

VEHICLE_CLASSES = {"car", "bus", "truck"}
CONF_THRESHOLD = 0.5
CONGESTION_THRESHOLD = 20


@app.route("/analyze", methods=["POST"])
def analyze():
    if "image" not in request.files:
        return jsonify({
            "error": "Missing image field"
        }), 400
    
    if "lat" not in request.form or "lon" not in request.form:
        return jsonify({
            "error": "Missing lat or lon field"
        }), 400
    
    lat = float(request.form["lat"])
    lon = float(request.form["lon"])

    image_file = request.files["image"]

    if image_file.filename == "":
        return jsonify({
            "error": "Empty filename"
        }), 400

    try:
        image_bytes = image_file.read()
        image = Image.open(io.BytesIO(image_bytes)).convert("RGB")

        results = model(image, conf=CONF_THRESHOLD)

        annotaded_image = results[0].plot()
        annotaded_image = cv2.cvtColor(annotaded_image, cv2.COLOR_BGR2RGB)
        annotated_pil = Image.fromarray(annotaded_image)
        buff = io.BytesIO()
        annotated_pil.save(buff, format="JPEG",quality=95 )
        annotaded_bytes = buff.getvalue()

        vehicle_count = 0
        for r in results:
            for box in r.boxes:
                cls_name = model.names[int(box.cls)]
                if cls_name in VEHICLE_CLASSES:
                    vehicle_count += 1

        traffic_status = (
            "congested"
            if vehicle_count >= CONGESTION_THRESHOLD
            else "normal"
        )

        encoded_image = base64.b64encode(annotaded_bytes).decode("utf-8")

        if traffic_status == "congested":
            doc = {
                "timestamp": datetime.utcnow(),
                "status": "congested",
                "vehicle_count": vehicle_count,
                "geometry": {
                    "type": "Point",
                    "coordinates": [lat, lon]
                },
                "image_base64": encoded_image   
            }

            collection.insert_one(doc)


        return jsonify({
            "vehicle_count": vehicle_count,
            "traffic_status": traffic_status,
            "image_base64": encoded_image

        })

    except Exception as e:
        return jsonify({
            "error": str(e)
        }), 500


if __name__ == "__main__":
    print("Flask ML server running on http://0.0.0.0:5000")
    app.run(host="0.0.0.0", port=5000, debug=False)
