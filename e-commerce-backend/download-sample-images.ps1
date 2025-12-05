# Download sample placeholder images for testing
Write-Host "Downloading sample images..." -ForegroundColor Cyan

$products = @(
    @{name="iphone-15-pro-max-titanium-1.jpg"; text="iPhone 15 Pro Max"},
    @{name="samsung-s24-ultra-black-1.jpg"; text="Samsung S24 Ultra"},
    @{name="xiaomi-14-ultra-black-1.jpg"; text="Xiaomi 14 Ultra"},
    @{name="macbook-pro-14-space-gray-1.jpg"; text="MacBook Pro 14"},
    @{name="dell-xps-13-plus-platinum-1.jpg"; text="Dell XPS 13 Plus"},
    @{name="asus-rog-g16-gray-1.jpg"; text="ASUS ROG G16"}
)

$baseUrl = "https://via.placeholder.com/800x600/667eea/ffffff?text="

foreach ($product in $products) {
    $url = $baseUrl + [uri]::EscapeDataString($product.text)
    $output = "uploads\images\products\$($product.name)"
    
    Write-Host "Downloading $($product.name)..." -ForegroundColor Yellow
    
    try {
        Invoke-WebRequest -Uri $url -OutFile $output -UseBasicParsing
        Write-Host "  ✓ Downloaded $($product.name)" -ForegroundColor Green
    } catch {
        Write-Host "  ✗ Failed to download $($product.name): $_" -ForegroundColor Red
    }
}

Write-Host "`nAll done! Images saved to uploads\images\products\" -ForegroundColor Cyan
Write-Host "Test URL: http://localhost:8080/api/v1/files/images/products/iphone-15-pro-max-titanium-1.jpg" -ForegroundColor Yellow
