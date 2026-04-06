from flask import Blueprint, jsonify
from config import get_db

job_bp = Blueprint("job_titles", __name__)

@job_bp.route("/", methods=["GET"])
def get_job_titles():
    db = get_db()
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT * FROM job_titles")
    result = cursor.fetchall()
    cursor.close()
    db.close()
    return jsonify(result)