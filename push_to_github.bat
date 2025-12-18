@echo off
git init
git remote remove origin
git remote add origin https://github.com/helloworldG97/StaySmart.git
git add .
git commit -m "Implement Messaging, Application Status, and Global Logout"
git branch -M main
git push -u origin main
