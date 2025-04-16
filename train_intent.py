import json
from datasets import load_dataset, Dataset
from transformers import AutoTokenizer, AutoModelForSequenceClassification, Trainer, TrainingArguments
import numpy as np
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics import accuracy_score

# Step 1: 读取数据
with open("finance_utterances_intent_classification.jsonl", "r", encoding="utf-8") as f:
    lines = [json.loads(l) for l in f]

texts = [l["text"] for l in lines]
labels = [l["labels"] for l in lines]

# 标签编码器
le = LabelEncoder()
encoded_labels = le.fit_transform(labels)

# 创建 HuggingFace 数据集
data = Dataset.from_dict({"text": texts, "label": encoded_labels})
dataset = data.train_test_split(test_size=0.2, seed=42)

# Step 2: 分词器与模型
model_name = "distilbert-base-uncased"
tokenizer = AutoTokenizer.from_pretrained(model_name)
model = AutoModelForSequenceClassification.from_pretrained(model_name, num_labels=len(le.classes_))

# Step 3: Tokenize 数据
def preprocess(example):
    return tokenizer(example["text"], truncation=True, padding="max_length", max_length=128)

tokenized_dataset = dataset.map(preprocess, batched=True)

# Step 4: 指定 Trainer
args = TrainingArguments(
    output_dir="./intent_model",
    evaluation_strategy="epoch",
    per_device_train_batch_size=8,
    per_device_eval_batch_size=8,
    num_train_epochs=5,
    logging_dir="./logs",
)

def compute_metrics(p):
    preds = np.argmax(p.predictions, axis=1)
    return {"accuracy": accuracy_score(p.label_ids, preds)}

trainer = Trainer(
    model=model,
    args=args,
    train_dataset=tokenized_dataset["train"],
    eval_dataset=tokenized_dataset["test"],
    tokenizer=tokenizer,
    compute_metrics=compute_metrics,
)

# Step 5: 开始训练
trainer.train()

# Step 6: 保存模型 + 标签映射
trainer.save_model("intent_model")
tokenizer.save_pretrained("intent_model")
with open("intent_model/label_encoder.json", "w") as f:
    json.dump(le.classes_.tolist(), f)
