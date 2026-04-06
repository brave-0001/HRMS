# HRMS

A desktop HR management system built from scratch.
Java frontend, Python backend, MySQL database — all talking to each other cleanly.

## Stack

- Java 21 + JavaFX — desktop UI
- Python 3 + Flask — REST API
- MySQL on Aiven — cloud database

## What it does

- Login with role-based access (admin and manager)
- Manage employees, departments, and job titles
- Track daily attendance with clock in and out
- Run payroll and view history
- Live dashboard with today's stats

## Structure

HRMS/
├── backend/      Flask API and database logic
└── frontend/     JavaFX desktop application

## Status

In development. Phase 2 adds leave management, reports, and expanded role permissions.