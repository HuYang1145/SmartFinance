@echo off
REM —— 配置区 ——
set PYTHON_EXE=D:\python\envs\nlp\python.exe
set MAIN_SCRIPT=src\ai_model\predict.py
set OUTPUT_NAME=predict

REM 模型源目录（会被嵌入 exe）
set MODEL1=src\ai_model\intent_model
set MODEL2=src\ai_model\ner_model


echo 🔧 [1/2] 使用 onefile 模式打包并嵌入模型...
"%PYTHON_EXE%" -m PyInstaller --clean --name %OUTPUT_NAME% ^
  --onefile ^
  --upx-dir "%UPX_DIR%" ^
  --exclude-module torch.cuda ^
  --exclude-module torchvision.ops ^
  --add-data "%MODEL1%;intent_model" ^
  --add-data "%MODEL2%;ner_model" ^
  %MAIN_SCRIPT%

echo 🔄 [2/2] 清理打包中间产物...
rmdir /s /q build
del %OUTPUT_NAME%.spec

echo ✅ 打包完成！
echo 📦 可执行文件: dist\%OUTPUT_NAME%.exe
pause
