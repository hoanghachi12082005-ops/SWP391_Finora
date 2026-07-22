@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

set FILE=src\main\java\dao\system\AuditLogDAO.java
set REPO_DIR=C:\Users\uznpl\Downloads\SWP391_Finora-main

cd /d "%REPO_DIR%"

:: Đếm tổng số dòng
set COUNT=0
for /f %%i in ('type "%FILE%" ^| find /c /v ""') do set COUNT=%%i

echo Total lines: %COUNT%

for /l %%n in (%COUNT%, -1, 1) do (
    :: Đọc tất cả dòng, bỏ qua dòng đầu tiên
    set SKIP=1
    break > "%FILE%.tmp"
    
    for /f "usebackq delims=" %%a in ("%FILE%") do (
        if !SKIP! equ 1 (
            set "SKIP=0"
        ) else (
            echo %%a>> "%FILE%.tmp"
        )
    )
    
    move /y "%FILE%.tmp" "%FILE%" >nul

    git add "%FILE%"
    
    :: Lấy nội dung dòng vừa xoá (dòng thứ %%n từ dưới lên)
    git commit -m "remove line %%n from AuditLogDAO.java"
)

:: Xoá file sau khi hết dòng
del "%FILE%"
git rm "%FILE%"
git commit -m "delete empty AuditLogDAO.java file"

echo Done! All %COUNT% lines removed one by one.
pause
