import io
import sys
import os
import pandas as pd
import requests

def load_transactions():
    """
    简单加载交易记录CSV文件
    """
    try:
        csv_paths = [
            "../../../transactions.csv",
            "coursework/transactions.csv",
            "transactions.csv",
            "../../transactions.csv"
        ]

        df = None
        for path in csv_paths:
            if os.path.exists(path):
                print(f"找到 CSV 文件: {path}")
                df = pd.read_csv(path)
                break

        if df is None:
            print("无法找到交易记录文件")
            return None

        # 清理列名中的空格
        df.columns = [col.strip() for col in df.columns]
        print(f"CSV 文件列名: {df.columns.tolist()}")

        # 确保列名正确
        expected_columns = ['user name', 'operation performed', 'amount', 'payment time', 'merchant name']
        if len(df.columns) >= 5 and df.columns.tolist() != expected_columns:
            print("正在重命名列名")
            df.columns = expected_columns

        return df
    except Exception as e:
        print(f"加载交易记录时出错: {str(e)}")
        return None

def call_deepseek_api(current_username, user_query, transactions_df):
    """
    调用 Deepseek API 进行智能回答
    """
    api_key = "sk-7ae19476ebd14de3b1a93c594c267886"
    url = "https://api.deepseek.com/v1/chat/completions"
    headers = {
        "Content-Type": "application/json; charset=UTF-8",
        "Authorization": f"Bearer {api_key}",
        "Accept-Charset": "UTF-8"
    }

    # 根据用户名过滤交易记录
    if transactions_df is not None and current_username != "unknown":
        user_transactions = transactions_df[transactions_df['user name'] == current_username]
        if user_transactions.empty:
            transaction_data = f"No transactions found for user {current_username}"
        #else:
            # 将交易记录转换为 JSON 格式，限制为 30 条记录
        transaction_data = user_transactions.head(50).to_json(orient="records", force_ascii=False)
    else:
        transaction_data = "无法加载交易数据或用户名无效"

    # 设置 system_prompt，包含当前用户名
    system_prompt = f"""你是一个专业的金融助手。当前登录的用户是 {current_username}。你需要根据以下交易记录回答用户的金融问题。
交易记录格式包括：用户名、操作类型、金额、支付时间、商户名称。

以下是用户 {current_username} 的交易记录（JSON 格式）：
{transaction_data}

请分析上述数据并回答用户的提问。你可以计算总收入、总支出、按月或年的统计数据等。
只处理与用户 {current_username} 相关的交易记录。
用英文回答，并提供清晰的数据分析。如果没有相关交易记录，请说明。"""

    # 设置请求数据
    data = {
        "model": "deepseek-reasoner",  # 假设这是正确的模型名称，请根据 Deepseek 文档确认
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_query}
        ],
        "temperature": 0.5,
        "max_tokens": 500
    }

    try:
        response = requests.post(url, headers=headers, json=data)
        response.raise_for_status()
        result = response.json()
        return result["choices"][0]["message"]["content"]
    except Exception as e:
        print(f"调用 Deepseek API 时出错: {str(e)}")
        return f"抱歉，调用 AI 服务时发生错误: {str(e)}"

def main():
    if len(sys.argv) > 2:
        current_username = sys.argv[1]
        user_query = " ".join(sys.argv[2:])
        print(f"当前用户名: {current_username}")
        print(f"用户查询: {user_query}")

        transactions_df = load_transactions()
        response = call_deepseek_api(current_username, user_query, transactions_df)
        sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
        print(response)
    else:
        print("请提供用户名和查询。使用方法: python predict.py <username> <query>")

if __name__ == "__main__":
    main()