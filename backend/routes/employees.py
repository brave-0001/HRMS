from flask import Blueprint, jsonify, request
from config import get_db

emp_bp = Blueprint("employees", __name__)

@emp_bp.route("/", methods=["GET"])
def get_employees():
    db = get_db()
    cursor = db.cursor(dictionary=True)
    cursor.execute("""
        SELECT e.*, d.name AS department, j.title AS job_title
        FROM employees e
        JOIN departments d ON e.department_id = d.id
        JOIN job_titles j  ON e.job_title_id  = j.id
    """)
    result = cursor.fetchall()
    cursor.close()
    db.close()
    return jsonify(result)

@emp_bp.route("/", methods=["POST"])
def create_employee():
    data = request.get_json()
    required = ["first_name", "last_name", "email", "hire_date", "department_id", "job_title_id"]
    for field in required:
        if not data.get(field):
            return jsonify({"error": f"{field} is required"}), 400
    db = get_db()
    cursor = db.cursor()
    cursor.execute("""
        INSERT INTO employees (first_name, last_name, email, phone, hire_date, department_id, job_title_id)
        VALUES (%s, %s, %s, %s, %s, %s, %s)
    """, (
        data["first_name"], data["last_name"], data["email"],
        data.get("phone"), data["hire_date"],
        data["department_id"], data["job_title_id"]
    ))
    db.commit()
    new_id = cursor.lastrowid
    cursor.close()
    db.close()
    return jsonify({"id": new_id}), 201