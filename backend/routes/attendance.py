from flask import Blueprint, jsonify, request
from config import get_db

att_bp = Blueprint("attendance", __name__)

def serialize(row):
    result = {}
    for key, val in row.items():
        if hasattr(val, 'seconds'):
            total = int(val.total_seconds())
            result[key] = f"{total//3600:02}:{(total%3600)//60:02}:{total%60:02}"
        elif hasattr(val, 'strftime'):
            result[key] = val.strftime('%Y-%m-%d')
        else:
            result[key] = val
    return result

@att_bp.route("/", methods=["GET"])
def get_attendance():
    date = request.args.get("date")
    db = get_db()
    cursor = db.cursor(dictionary=True)
    if date:
        cursor.execute("""
            SELECT a.*, e.first_name, e.last_name
            FROM attendance a
            JOIN employees e ON a.employee_id = e.id
            WHERE a.work_date = %s
        """, (date,))
    else:
        cursor.execute("""
            SELECT a.*, e.first_name, e.last_name
            FROM attendance a
            JOIN employees e ON a.employee_id = e.id
            ORDER BY a.work_date DESC LIMIT 100
        """)
    rows = cursor.fetchall()
    cursor.close()
    db.close()
    return jsonify([serialize(r) for r in rows])

@att_bp.route("/today", methods=["GET"])
def get_today_present():
    db = get_db()
    cursor = db.cursor(dictionary=True)
    cursor.execute("""
        SELECT COUNT(*) as count FROM attendance
        WHERE work_date = CURDATE() AND status = 'present'
    """)
    result = cursor.fetchone()
    cursor.close()
    db.close()
    return jsonify(result)

@att_bp.route("/", methods=["POST"])
def log_attendance():
    data = request.get_json()
    if not data.get("employee_id") or not data.get("work_date"):
        return jsonify({"error": "employee_id and work_date are required"}), 400
    db = get_db()
    cursor = db.cursor()
    cursor.execute("""
        INSERT INTO attendance (employee_id, work_date, clock_in, clock_out, status)
        VALUES (%s, %s, %s, %s, %s)
        ON DUPLICATE KEY UPDATE
            clock_in  = VALUES(clock_in),
            clock_out = VALUES(clock_out),
            status    = VALUES(status)
    """, (
        data["employee_id"], data["work_date"],
        data.get("clock_in"), data.get("clock_out"),
        data.get("status", "present")
    ))
    db.commit()
    cursor.close()
    db.close()
    return jsonify({"message": "Attendance logged"}), 201