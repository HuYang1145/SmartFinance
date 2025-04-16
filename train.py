from datasets import load_from_disk
from transformers import AutoTokenizer, AutoModelForTokenClassification
from transformers import TrainingArguments, Trainer, DataCollatorForTokenClassification
import sys
print("当前 Python 路径：", sys.executable)

import numpy as np

# ==== 1. 加载原始数据 ====
dataset = load_from_disk(r"dataset")

# ==== 2. 加载 Tokenizer ====
tokenizer = AutoTokenizer.from_pretrained("D:/pythonProject1/distilbert-base-cased/")

# ==== 3. 分词 + 标签对齐 ====
def tokenize_and_align_labels(example):
    tokenized_inputs = tokenizer(
        example["tokens"],
        is_split_into_words=True,
        truncation=True,
        padding="max_length",
        max_length=128
    )
    word_ids = tokenized_inputs.word_ids()
    label_ids = []
    for word_idx in word_ids:
        if word_idx is None:
            label_ids.append(-100)
        else:
            label_ids.append(example["ner_tags"][word_idx])

    return {
        "input_ids": tokenized_inputs["input_ids"],
        "attention_mask": tokenized_inputs["attention_mask"],
        "labels": label_ids
    }

tokenized_dataset = dataset.map(
    tokenize_and_align_labels,
    batched=False,
    remove_columns=["id", "tokens", "ner_tags"]
)

# ==== 4. 加载模型 ====
model = AutoModelForTokenClassification.from_pretrained(
    "D:/pythonProject1/distilbert-base-cased/",
    num_labels=7
)

# ==== 5. 设置训练参数 ====
training_args = TrainingArguments(
    output_dir="./results",
    per_device_train_batch_size=8,
    per_device_eval_batch_size=8,
    num_train_epochs=5,
    evaluation_strategy="no",
    logging_dir="./logs",
    save_strategy="epoch",
    logging_steps=10
)

# ==== 6. 评估函数 ====
label_list = ["O", "B-amount", "I-amount", "B-category", "I-category", "B-time", "I-time"]
def compute_metrics(p):
    predictions, labels = p
    preds = predictions.argmax(axis=-1)

    correct = 0
    total = 0
    for pred_seq, label_seq in zip(preds, labels):
        for pred, label in zip(pred_seq, label_seq):
            if label != -100:
                total += 1
                if pred == label:
                    correct += 1
    acc = correct / total if total > 0 else 0.0
    return {"accuracy": acc}


# ==== 7. 开始训练 ====
trainer = Trainer(
    model=model,
    args=training_args,
    train_dataset=tokenized_dataset,  # 如果你有 eval，可以加上 eval_dataset=
    tokenizer=tokenizer,
    data_collator=DataCollatorForTokenClassification(tokenizer),
    compute_metrics=compute_metrics
)

trainer.train()
trainer.save_model("D:/pythonProject1/ner_model")
