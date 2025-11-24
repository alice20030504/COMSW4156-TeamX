# Check application status
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  7-Day Meal Plan Generator Status" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Check port 7860
$portInfo = netstat -ano | findstr ":7860"
if ($portInfo) {
    Write-Host "[RUNNING] Port 7860 is listening" -ForegroundColor Green
    Write-Host $portInfo -ForegroundColor Gray
    
    # Extract PID
    if ($portInfo -match "LISTENING\s+(\d+)") {
        $processId = $matches[1]
        $proc = Get-Process -Id $processId -ErrorAction SilentlyContinue
        if ($proc) {
            Write-Host "`nProcess Information:" -ForegroundColor Yellow
            Write-Host "  PID: $processId" -ForegroundColor White
            Write-Host "  Name: $($proc.ProcessName)" -ForegroundColor White
            Write-Host "  Started: $($proc.StartTime)" -ForegroundColor White
            Write-Host "  Memory: $([math]::Round($proc.WS/1MB, 2)) MB" -ForegroundColor White
        }
    }
    
    Write-Host "`n[SUCCESS] Application is running!" -ForegroundColor Green
    Write-Host "`nAccess your application at:" -ForegroundColor Yellow
    Write-Host "  http://127.0.0.1:7860" -ForegroundColor Cyan
    Write-Host "`nTo stop the application:" -ForegroundColor Gray
    Write-Host "  Stop-Process -Id $processId" -ForegroundColor Gray
} else {
    Write-Host "[STOPPED] Port 7860 is not listening" -ForegroundColor Red
    Write-Host "Application is not running." -ForegroundColor Yellow
}

Write-Host "`n========================================" -ForegroundColor Cyan

