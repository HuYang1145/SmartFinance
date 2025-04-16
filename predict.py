from transformers import AutoTokenizer, AutoModelForTokenClassification
import torch

label_list = ["O", "B-amount", "I-amount", "B-category", "I-category", "B-time", "I-time"]

# 加载模型
model = AutoModelForTokenClassification.from_pretrained("D:/pythonProject1/ner_model")
tokenizer = AutoTokenizer.from_pretrained("D:/pythonProject1/ner_model")


def predict(text):
    inputs = tokenizer(text, return_tensors="pt", truncation=True, padding=True)
    with torch.no_grad():
        outputs = model(**inputs)
    predictions = torch.argmax(outputs.logits, dim=2)

    tokens = tokenizer.convert_ids_to_tokens(inputs["input_ids"][0])
    labels = [label_list[pred] for pred in predictions[0].numpy()]

    print("🧾 输入句子:", text)
    print("🧠 预测结果:")
    for token, label in zip(tokens, labels):
        if token not in tokenizer.all_special_tokens:
            print(f"{token:15s} → {label}")


# 示例
predict("I spent 30 dollars in repast")
