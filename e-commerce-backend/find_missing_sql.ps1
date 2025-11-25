# Script to find missing SQL files for repository methods
$repositoryDir = "f:\NhaSachTinHoc\CDTC\e-commerce\e-commerce-backend\src\main\java\com\ecommerce\repository"
$sqlDir = "f:\NhaSachTinHoc\CDTC\e-commerce\e-commerce-backend\src\main\resources\com\ecommerce\repository"

Write-Host "Repository directory: $repositoryDir" -ForegroundColor Yellow
Write-Host "SQL directory: $sqlDir" -ForegroundColor Yellow

# Get all repository files
$repoFiles = Get-ChildItem "$repositoryDir\*.java"
Write-Host "Found $($repoFiles.Count) repository files" -ForegroundColor Green

$missingFiles = @()

foreach ($repoFile in $repoFiles) {
    $repoName = $repoFile.BaseName
    Write-Host "`nProcessing $repoName..." -ForegroundColor Cyan
    
    $content = Get-Content $repoFile.FullName -Raw
    
    # Extract method signatures - improved regex for repository methods
    $methodPattern = '(?m)^\s*(?:@\w+\s*)*\s*(?:public\s+|protected\s+)?(?:abstract\s+)?(?:(?:List|Optional|Page|Boolean|Long|Integer|BigDecimal|void|[A-Z]\w*)\s*(?:<[^>]*>)?\s+)(\w+)\s*\([^)]*\)\s*;'
    $matches = [regex]::Matches($content, $methodPattern)
    
    Write-Host "  Found $($matches.Count) method signatures" -ForegroundColor Gray
    
    foreach ($match in $matches) {
        $methodName = $match.Groups[1].Value
        
        # Skip standard CRUD methods that are inherited and common JPA methods
        if ($methodName -in @('save', 'delete', 'deleteById', 'count', 'findAll', 'saveAll', 'deleteAll', 'exists', 'existsById', 'flush', 'saveAndFlush', 'deleteInBatch', 'getOne', 'getReferenceById')) {
            continue
        }
        
        $expectedSqlFile = "$sqlDir\${repoName}_${methodName}.sql"
        if (!(Test-Path $expectedSqlFile)) {
            $missingFile = "${repoName}_${methodName}.sql"
            $missingFiles += $missingFile
            Write-Host "  Missing: $missingFile" -ForegroundColor Red
        } else {
            Write-Host "  Found: ${repoName}_${methodName}.sql" -ForegroundColor Green
        }
    }
}

Write-Host "`n=== SUMMARY ===" -ForegroundColor Yellow
Write-Host "Total missing SQL files: $($missingFiles.Count)" -ForegroundColor Red
if ($missingFiles.Count -gt 0) {
    Write-Host "`nMissing files:" -ForegroundColor Red
    $missingFiles | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
}