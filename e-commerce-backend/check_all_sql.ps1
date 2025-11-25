# Simple comprehensive check for missing SQL files
$repositoryDir = "f:\NhaSachTinHoc\CDTC\e-commerce\e-commerce-backend\src\main\java\com\ecommerce\repository"
$sqlDir = "f:\NhaSachTinHoc\CDTC\e-commerce\e-commerce-backend\src\main\resources\com\ecommerce\repository"

# Get all repository files
$repoFiles = Get-ChildItem "$repositoryDir\*.java"

Write-Host "Checking all repositories for missing SQL files..." -ForegroundColor Yellow
$totalMissing = 0

foreach ($repoFile in $repoFiles) {
    $repoName = $repoFile.BaseName
    $content = Get-Content $repoFile.FullName -Raw
    
    Write-Host "`n=== $repoName ===" -ForegroundColor Cyan
    
    # Extract all method names from the file
    $lines = $content -split "`n"
    $methods = @()
    
    foreach ($line in $lines) {
        # Skip comments
        if ($line -match '^\s*//' -or $line -match '^\s*/\*' -or $line -match '^\s*\*') {
            continue
        }
        
        # Look for method signatures ending with semicolon
        if ($line -match '\s+(\w+)\s*\([^)]*\)\s*;') {
            $methodName = $Matches[1]
            # Skip common JPA methods
            if ($methodName -notin @('save', 'delete', 'deleteById', 'count', 'findAll', 'saveAll', 'deleteAll', 'exists', 'existsById', 'flush', 'saveAndFlush', 'deleteInBatch', 'getOne', 'getReferenceById', 'findById')) {
                $methods += $methodName
            }
        }
    }
    
    # Check if SQL files exist for each method
    $missing = @()
    foreach ($method in $methods) {
        $sqlFile = "$sqlDir\${repoName}_${method}.sql"
        if (Test-Path $sqlFile) {
            Write-Host "  ✓ ${method}.sql" -ForegroundColor Green
        } else {
            Write-Host "  ✗ ${method}.sql" -ForegroundColor Red
            $missing += "${repoName}_${method}.sql"
        }
    }
    
    if ($missing.Count -gt 0) {
        $totalMissing += $missing.Count
        Write-Host "  Missing $($missing.Count) files" -ForegroundColor Red
    } else {
        Write-Host "  All SQL files present" -ForegroundColor Green
    }
}

Write-Host "`n=== FINAL SUMMARY ===" -ForegroundColor Yellow
if ($totalMissing -gt 0) {
    Write-Host "Total missing SQL files: $totalMissing" -ForegroundColor Red
} else {
    Write-Host "All repositories have their SQL files!" -ForegroundColor Green
}