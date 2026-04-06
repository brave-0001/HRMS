from flask import Blueprint, jsonify, request
from config import get_db

pay_bp = Blueprint("payroll", __name__)

@pay_bp.route("/", methods=["GET"])
def get_payroll():
    db = get_db()
    cursor = db.cursor(dictionary=True)
    cursor.execute("""
        SELECT p.*, e.first_name, e.last_name
        FROM payroll_runs p
        JOIN employees e ON p.employee_id = e.id
        ORDER BY p.created_at DESC
    """)
    result = cursor.fetchall()
    cursor.close()
    db.close()
    return jsonify(result)

@pay_bp.route("/run", methods=["POST"])
def run_payroll():
    data = request.get_json()
    if not data.get("employee_id") or not data.get("period_start") or not data.get("period_end"):
        return jsonify({"error": "employee_id, period_start and period_end are required"}), 400
    db = get_db()
    cursor = db.cursor(dictionary=True)
    cursor.execute("""
        SELECT base_salary FROM salaries
        WHERE employee_id = %s
        ORDER BY effective_date DESC LIMIT 1
    """, (data["employee_id"],))
    salary = cursor.fetchone()
    if not salary:
        return jsonify({"error": "No salary record found for this employee"}), 404
    gross = float(salary["base_salary"])
    deductions = round(gross * 0.1, 2)
    net = round(gross - deductions, 2)
    cursor = db.cursor()
    cursor.execute("""
        INSERT INTO payroll_runs (employee_id, period_start, period_end, gross_pay, deductions, net_pay)
        VALUES (%s, %s, %s, %s, %s, %s)
    """, (data["employee_id"], data["period_start"], data["period_end"], gross, deductions, net))
    db.commit()
    cursor.close()
    db.close()
    return jsonify({"gross": gross, "deductions": deductions, "net": net}), 201