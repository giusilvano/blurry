@ECHO OFF &SETLOCAL

SET BIN_DIR_PATH=%~dp0

%BIN_DIR_PATH%blurry.bat -r %*
pause