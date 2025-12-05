@echo off
echo Creating upload directories...

cd /d "%~dp0"

if not exist "uploads" mkdir uploads
if not exist "uploads\images" mkdir uploads\images
if not exist "uploads\images\products" mkdir uploads\images\products
if not exist "uploads\images\categories" mkdir uploads\images\categories
if not exist "uploads\images\brands" mkdir uploads\images\brands
if not exist "uploads\images\users" mkdir uploads\images\users

echo.
echo Directory structure created:
echo   uploads\
echo   └── images\
echo       ├── products\
echo       ├── categories\
echo       ├── brands\
echo       └── users\
echo.
echo Done!
pause
