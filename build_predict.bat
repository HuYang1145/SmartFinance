@echo off
setlocal

REM —— 路径配置 ——
set PYTHON_EXE=D:\python\envs\nlp\python.exe
set SCRIPT=ner\predict.py
set OUTPUT_NAME=predict

echo 🔧 [1/2] 打包中...

"%PYTHON_EXE%" -m PyInstaller ^
  --clean ^
  --onedir ^
  --name "%OUTPUT_NAME%" ^
  --add-data "ner\\intent_model;intent_model" ^
  --add-data "ner\\ner_model;ner_model" ^
  %SCRIPT%

echo 🔄 [2/2] 清理中间文件...
rmdir /s /q build
if exist %OUTPUT_NAME%.spec del /q %OUTPUT_NAME%.spec

echo ✅ 打包完成！可执行目录：dist\%OUTPUT_NAME%\
pause
