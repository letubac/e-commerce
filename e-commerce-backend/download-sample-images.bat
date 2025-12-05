@echo off
echo Downloading sample placeholder images...
echo.

cd uploads\images\products

echo Downloading iPhone 15 Pro Max...
curl -o iphone-15-pro-max-titanium-1.jpg "https://via.placeholder.com/800x600/667eea/ffffff?text=iPhone+15+Pro+Max" 2>nul
if exist iphone-15-pro-max-titanium-1.jpg (echo   [OK] iphone-15-pro-max-titanium-1.jpg) else (echo   [FAIL] Could not download)

echo Downloading Samsung S24 Ultra...
curl -o samsung-s24-ultra-black-1.jpg "https://via.placeholder.com/800x600/764ba2/ffffff?text=Samsung+S24+Ultra" 2>nul
if exist samsung-s24-ultra-black-1.jpg (echo   [OK] samsung-s24-ultra-black-1.jpg) else (echo   [FAIL] Could not download)

echo Downloading Xiaomi 14 Ultra...
curl -o xiaomi-14-ultra-black-1.jpg "https://via.placeholder.com/800x600/667eea/ffffff?text=Xiaomi+14+Ultra" 2>nul
if exist xiaomi-14-ultra-black-1.jpg (echo   [OK] xiaomi-14-ultra-black-1.jpg) else (echo   [FAIL] Could not download)

echo Downloading MacBook Pro...
curl -o macbook-pro-14-space-gray-1.jpg "https://via.placeholder.com/800x600/764ba2/ffffff?text=MacBook+Pro+14" 2>nul
if exist macbook-pro-14-space-gray-1.jpg (echo   [OK] macbook-pro-14-space-gray-1.jpg) else (echo   [FAIL] Could not download)

echo Downloading Dell XPS 13...
curl -o dell-xps-13-plus-platinum-1.jpg "https://via.placeholder.com/800x600/667eea/ffffff?text=Dell+XPS+13+Plus" 2>nul
if exist dell-xps-13-plus-platinum-1.jpg (echo   [OK] dell-xps-13-plus-platinum-1.jpg) else (echo   [FAIL] Could not download)

echo Downloading ASUS ROG...
curl -o asus-rog-g16-gray-1.jpg "https://via.placeholder.com/800x600/764ba2/ffffff?text=ASUS+ROG+G16" 2>nul
if exist asus-rog-g16-gray-1.jpg (echo   [OK] asus-rog-g16-gray-1.jpg) else (echo   [FAIL] Could not download)

cd ..\..\..

echo.
echo Done! Sample images created in uploads\images\products\
echo.
echo Test URL: http://localhost:8080/api/v1/files/images/products/iphone-15-pro-max-titanium-1.jpg
echo.
pause
