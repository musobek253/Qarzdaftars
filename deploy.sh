#!/bin/bash

# Deployment Script for DebtBook
# Usage: ./deploy.sh

echo "🚀 Starting deployment..."

# 1. Check if git is installed
if ! command -v git &> /dev/null; then
    echo "❌ Git is not installed. Please install git first."
    exit 1
fi

# 2. Check if docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install docker first."
    exit 1
fi

# 3. Pull latest changes
echo "📥 Pulling latest changes from Git..."
git pull origin main

# 4. Check for .env file
if [ ! -f .env ]; then
    echo "⚠️ .env file not found!"
    if [ -f .env.example ]; then
        echo "Example detected. Creating .env from .env.example..."
        cp .env.example .env
        echo "❗ PLEASE EDIT .env FILE WITH YOUR SECRETS BEFORE CONTINUING!"
        echo "Nano .env to edit."
        exit 1
    else
        echo "❌ No .env or .env.example found. Aborting."
        exit 1
    fi
fi

# 5. Build and Run with Docker Compose
echo "whale: Rebuilding and starting containers..."
docker compose down
docker compose up -d --build

echo "✅ Deployment successful! App should be running on port 8080."
