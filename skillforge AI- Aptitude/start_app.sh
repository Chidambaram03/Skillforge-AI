#!/bin/bash
# Simple auto-runner for SkillForge AI on Mac

echo "🚀 Starting SkillForge AI..."

# Kill any existing ports
lsof -ti:8080 | xargs kill -9 2>/dev/null
lsof -ti:3000 | xargs kill -9 2>/dev/null

echo "1) Starting the Backend API (Spring Boot & H2 DB) on Port 8080..."
cd backend
./mvnw spring-boot:run &
BACKEND_PID=$!

echo "2) Starting the Frontend Website on Port 3000..."
cd ../frontend
python3 -m http.server 3000 &
FRONTEND_PID=$!

echo ""
echo "✅ Everything is running successfully!"
echo "👉 Open Safari or Chrome and go to: http://localhost:3000"
echo "   (Or directly to: http://localhost:3000/login.html)"
echo "Press [CTRL + C] here to stop the app."

wait
