from flask import Flask
from routes.departments import dept_bp
from routes.employees import emp_bp
from routes.attendance import att_bp
from routes.payroll import pay_bp
from routes.job_titles import job_bp
from routes.auth import auth_bp

app = Flask(__name__)
app.url_map.strict_slashes = False

app.register_blueprint(dept_bp,  url_prefix="/api/departments")
app.register_blueprint(emp_bp,   url_prefix="/api/employees")
app.register_blueprint(att_bp,   url_prefix="/api/attendance")
app.register_blueprint(pay_bp,   url_prefix="/api/payroll")
app.register_blueprint(job_bp,   url_prefix="/api/job_titles")
app.register_blueprint(auth_bp,  url_prefix="/api/auth")

if __name__ == "__main__":
    app.run(debug=True, port=5000)