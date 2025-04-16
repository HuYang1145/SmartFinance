import sys
import os
import json
import torch
from transformers import AutoTokenizer, AutoModelForTokenClassification, AutoModelForSequenceClassification

# ============ 处理输入 ============
input_text = " ".join(sys.argv[1:]) if len(sys.argv) > 1 else ""
if not input_text:
    print(json.dumps({"error": "No input text received"}))
    exit()

base_dir = os.path.dirname(__file__)

# ============ 意图识别 ============
intent_model_path = os.path.join(base_dir, "intent_model")
intent_tokenizer = AutoTokenizer.from_pretrained(intent_model_path, local_files_only=True)
intent_model = AutoModelForSequenceClassification.from_pretrained(intent_model_path, local_files_only=True)

intent_inputs = intent_tokenizer(input_text, return_tensors="pt", truncation=True)
intent_outputs = intent_model(**intent_inputs)
intent_id = torch.argmax(intent_outputs.logits, dim=1).item()

# 加载标签映射
intent_label_path = os.path.join(intent_model_path, "label_encoder.json")
if os.path.exists(intent_label_path):
    with open(intent_label_path, "r", encoding="utf-8") as f:
        intent_labels = json.load(f)
    intent_label = intent_labels[intent_id]
else:
    intent_label = str(intent_id)

# ============ 实体识别（NER） ============
ner_model_path = os.path.join(base_dir, "ner_model")
ner_tokenizer = AutoTokenizer.from_pretrained(ner_model_path, local_files_only=True)
ner_model = AutoModelForTokenClassification.from_pretrained(ner_model_path, local_files_only=True)

ner_label_list = ["O", "B-amount", "I-amount", "B-category", "I-category", "B-date", "I-date"]

ner_inputs = ner_tokenizer(input_text, return_tensors="pt", truncation=True, is_split_into_words=False)
with torch.no_grad():
    ner_outputs = ner_model(**ner_inputs)

ner_preds = torch.argmax(ner_outputs.logits, dim=2)[0].tolist()
tokens = ner_tokenizer.convert_ids_to_tokens(ner_inputs["input_ids"][0])

# 提取实体
entities = []
current_entity = ""
current_label = ""

for token, pred_id in zip(tokens, ner_preds):
    label = ner_label_list[pred_id]

    if label == "O" or token in ner_tokenizer.all_special_tokens:
        if current_entity:
            entities.append({"entity": current_label, "value": current_entity})
            current_entity = ""
            current_label = ""
        continue

    if label.startswith("B-"):
        if current_entity:
            entities.append({"entity": current_label, "value": current_entity})
        current_entity = token.lstrip("##")
        current_label = label[2:]
    elif label.startswith("I-") and current_label == label[2:]:
        current_entity += token.lstrip("##")
    else:
        if current_entity:
            entities.append({"entity": current_label, "value": current_entity})
        current_entity = ""
        current_label = ""

if current_entity:
    entities.append({"entity": current_label, "value": current_entity})

# ============ 输出 JSON ============
result = {
    "intent": intent_label,
    "entities": entities
}
print(json.dumps(result, ensure_ascii=False))
