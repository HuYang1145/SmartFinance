package ai_model;

import Model.UserSession;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

public class AIModel {
    private static final String DEFAULT_PYTHON_PATH = "python";
    
    // 改成本地python路径
    private static final String SCRIPT_PATH = "src/ai_model/predict.py";
    
    // Custom python path if set
    private String pythonPath;
    
    public AIModel() {
        // Use default python path
        this.pythonPath = DEFAULT_PYTHON_PATH;
    }
    
    public AIModel(String pythonPath) {
        this.pythonPath = pythonPath;
    }
    
    /**
     * Get a response from the AI model asynchronously
     * 
     * @param userInput The user's input text
     * @return CompletableFuture containing the model's response
     */
    public CompletableFuture<String> getResponseAsync(String userInput) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String scriptFilePath = getScriptPath();
                // 获取当前用户名
                String currentUsername = UserSession.getCurrentUsername() != null ? UserSession.getCurrentUsername() : "unknown";
                // 传递用户名和用户输入
                ProcessBuilder pb = new ProcessBuilder(pythonPath, scriptFilePath, currentUsername, userInput);
                pb.redirectErrorStream(true);
                Process process = pb.start();

                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line).append("\n");
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    return "错误：Python 脚本返回错误代码 " + exitCode;
                }

                return response.toString().trim();
            } catch (Exception e) {
                return "调用 AI 模型时出错: " + e.getMessage();
            }
        });
    }
    
    /**
     * Get a response from the AI model (synchronous version)
     * 
     * @param userInput The user's input text
     * @return The model's response
     */
    public String getResponse(String userInput) {
        StringBuilder output = new StringBuilder();
        try {
            String scriptFilePath = getScriptPath();
            // 获取当前用户名
            String currentUsername = UserSession.getCurrentUsername() != null ? UserSession.getCurrentUsername() : "unknown";
            // 传递用户名和用户输入
            ProcessBuilder pb = new ProcessBuilder(pythonPath, scriptFilePath, currentUsername, userInput);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("错误：Python 脚本返回错误代码 " + exitCode + "\nPython 输出:\n" + output.toString());
                return "错误：Python 脚本返回错误代码 " + exitCode;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "错误：等待 Python 脚本时被中断。";
        } catch (Exception e) {
            return "调用 AI 模型时出错: " + e.getMessage();
        }
        return output.toString().trim();
    }
    
    /**
     * Get the absolute path to the Python script
     */
    private String getScriptPath() throws Exception {
        File scriptFile = new File(SCRIPT_PATH);
        if (scriptFile.exists()) {
            return scriptFile.getAbsolutePath();
        }
        scriptFile = new File("coursework/" + SCRIPT_PATH);
        if (scriptFile.exists()) {
            return scriptFile.getCanonicalPath();
        }
        throw new Exception("无法找到 Python 脚本: " + SCRIPT_PATH);
    }
    
    /**
     * Set a custom Python executable path
     */
    public void setPythonPath(String pythonPath) {
        this.pythonPath = pythonPath;
    }
}