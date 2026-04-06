from flask import Blueprint, jsonify, request
from config import get_db
import hashlib
import secrets

auth_bp = Blueprint("auth", __name__)

sessions = {}

@auth_bp.route("/login", methods=["POST"])
def login():
    data = request.get_json()
    email    = data.get("email", "").strip()
    password = data.get("password", "").strip()

    if not email or not password:
        return jsonify({"error": "Email and password required"}), 400

    pw_hash = hashlib.sha256(password.encode()).hexdigest()

    db = get_db()
    cursor = db.cursor(dictionary=True)
    cursor.execute("""
        SELECT u.*, e.first_name, e.last_name
        FROM users u
        JOIN employees e ON u.employee_id = e.id
        WHERE u.email = %s AND u.password_hash = %s
    """, (email, pw_hash))
    user = cursor.fetchone()
    cursor.close()
    db.close()

    if not user:
        return jsonify({"error": "Invalid email or password"}), 401

    token = secrets.token_hex(32)
    sessions[token] = {
        "user_id":    user["id"],
        "employee_id": user["employee_id"],
        "name":       f"{user['first_name']} {user['last_name']}",
        "role":       user["role"]
    }

    return jsonify({
        "token": token,
        "name":  f"{user['first_name']} {user['last_name']}",
        "role":  user["role"]
    })

@auth_bp.route("/logout", methods=["POST"])
def logout():
    token = request.headers.get("Authorization", "").replace("Bearer ", "")
    sessions.pop(token, None)
    return jsonify({"message": "Logged out"})

def get_session(request):
    token = request.headers.get("Authorization", "").replace("Bearer ", "")
    return sessions.get(token)
@auth_bp.route("/reset-password", methods=["POST"])
def reset_password():
    data = request.get_json()
    email    = data.get("email", "").strip()
    new_pass = data.get("new_password", "").strip()

    if not email or not new_pass:
        return jsonify({"error": "Email and new password required"}), 400

    if len(new_pass) < 6:
        return jsonify({"error": "Password must be at least 6 characters"}), 400

    pw_hash = hashlib.sha256(new_pass.encode()).hexdigest()

    db = get_db()
    cursor = db.cursor()
    cursor.execute("""
        UPDATE users SET password_hash = %s WHERE email = %s
    """, (pw_hash, email))
    db.commit()
    affected = cursor.rowcount
    cursor.close()
    db.close()

    if affected == 0:
        return jsonify({"error": "Email not found"}), 404

    return jsonify({"message": "Password reset successfully"})