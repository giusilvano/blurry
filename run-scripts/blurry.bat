@ECHO OFF &SETLOCAL

REM Inspiration: http://www.devx.com/tips/Tip/42153
REM IMPORTANT: do not put other jars in the bin directory, or this script might not work

SET BIN_DIR_PATH=%~dp0

FOR %%F IN (%BIN_DIR_PATH%\*.jar) DO (
  SET JAR_PATH=%%F
  GOTO RUN
)

:RUN
java -version:"1.8+" -jar "%JAR_PATH%"