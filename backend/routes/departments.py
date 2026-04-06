from flask import Blueprint, jsonify, request
from config import get_db

dept_bp = Blueprint("departments", __name__)

@dept_bp.route("/", methods=["GET"])
def get_departments():
    db = get_db()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT * FROM departments")
    result = cursor.fetchall()
    cursor.close()
    db.close()
    return jsonify(result)

@dept_bp.route("/", methods=["POST"])
def create_department():
    data = request.get_json()
    name = data.get("name", "").strip()
    if not name:
        return jsonify({"error": "Department name is required"}), 400
    db = get_db()
    cursor = db.cursor()
    cursor.execute("INSERT INTO departments (name) VALUES (%s)", (name,))
    db.commit()
    new_id = cursor.lastrowid
    cursor.close()
    db.close()
    return jsonify({"id": new_id, "name": name}), 201