# Enhanced script to find missing SQL files for repository methods
$repositoryDir = "f:\NhaSachTinHoc\CDTC\e-commerce\e-commerce-backend\src\main\java\com\ecommerce\repository"
$sqlDir = "f:\NhaSachTinHoc\CDTC\e-commerce\e-commerce-backend\src\main\resources\com\ecommerce\repository"

Write-Host "Repository directory: $repositoryDir" -ForegroundColor Yellow
Write-Host "SQL directory: $sqlDir" -ForegroundColor Yellow

$repoFiles = Get-ChildItem "$repositoryDir\*.java"
Write-Host "Found $($repoFiles.Count) repository files`n" -ForegroundColor Green

$missingFiles = @()

foreach ($repoFile in $repoFiles) {
    $repoName = $repoFile.BaseName
    Write-Host "Processing $repoName..." -ForegroundColor Cyan
    
    $content = Get-Content $repoFile.FullName -Raw
    
    # Remove comments to avoid false matches
    $content = $content -replace '//.*$', '' -replace '/\*[\s\S]*?\*/', ''
    
    # Find all method signatures that have @Param annotations or look like repository methods
    # This handles multiline method signatures better
    $methodPattern = '(?s)(?:^\s*(?:@\w+\s*)*\s*)?(?:public\s+|protected\s+)?(?:abstract\s+)?(?:(?:List|Optional|Page|Boolean|Long|Integer|BigDecimal|void|[A-Z]\w*)\s*(?:<[^>]*>)?\s+)(\w+)\s*\([^)]*\)\s*;'
    
    # Also look for methods with @Param annotations specifically
    $paramPattern = '(?s)(@Param[^;]+?)(\w+)\s*\([^)]*\)\s*;'
    
    $allMatches = @()
    $allMatches += [regex]::Matches($content, $methodPattern)
    
    # Extract method names from lines containing method signatures
    $lines = $content -split "`n"
    for ($i = 0; $i -lt $lines.Length; $i++) {
        $line = $lines[$i].Trim()
        
        # Skip comments and annotations
        if ($line -match '^\s*//' -or $line -match '^\s*/\*' -or $line -match '^\s*\*' -or $line -match '^\s*@(?!Param)') {
            continue
        }
        
        # Look for method signatures - they typically return something and have parentheses
        if ($line -match '\s+(\w+)\s*\([^)]*\)\s*;?\s*$' -and 
            ($line -match '\b(?:List|Optional|Page|Boolean|Long|Integer|BigDecimal|void|[A-Z]\w*)\b' -or 
             $i -gt 0 -and $lines[$i-1] -match '@Param')) {
            
            $methodName = $Matches[1]
            
            # Skip standard CRUD methods and common JPA methods
            if ($methodName -in @('save', 'delete', 'deleteById', 'count', 'findAll', 'saveAll', 'deleteAll', 'exists', 'existsById', 'flush', 'saveAndFlush', 'deleteInBatch', 'getOne', 'getReferenceById', 'findById')) {
                continue
            }
            
            $expectedSqlFile = "$sqlDir\${repoName}_${methodName}.sql"
            if (!(Test-Path $expectedSqlFile)) {
                $missingFile = "${repoName}_${methodName}.sql"
                if ($missingFiles -notcontains $missingFile) {
                    $missingFiles += $missingFile
                    Write-Host "  Missing: $missingFile" -ForegroundColor Red
                }
            } else {
                Write-Host "  Found: ${repoName}_${methodName}.sql" -ForegroundColor Green
            }
        }
    }
}

Write-Host "`n=== SUMMARY ===" -ForegroundColor Yellow
Write-Host "Total missing SQL files: $($missingFiles.Count)" -ForegroundColor Red
if ($missingFiles.Count -gt 0) {
    Write-Host "`nMissing files:" -ForegroundColor Red
    $missingFiles | Sort-Object | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
} else {
    Write-Host "All SQL files are present!" -ForegroundColor Green
}