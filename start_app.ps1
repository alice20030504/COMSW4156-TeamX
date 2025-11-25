# Start app_mealplan.py with proper encoding
$env:PYTHONIOENCODING = "utf-8"
chcp 65001 | Out-Null

Write-Host "Starting app_mealplan.py..." -ForegroundColor Green
Write-Host "This will start the Gradio server on http://127.0.0.1:7860" -ForegroundColor Yellow
Write-Host "Press Ctrl+C to stop the application`n" -ForegroundColor Yellow

python app_mealplan.py

