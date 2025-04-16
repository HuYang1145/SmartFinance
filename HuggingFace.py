import json
from datasets import Dataset, ClassLabel, Sequence

class JsonlToHFDataset:
    def __init__(self, jsonl_data):
        self.data = jsonl_data
        # 从所有记录中提取唯一标签，确保统一为字符串
        self.labels_to_ids, self.ids_to_labels = self.__get_unique_labels()

    def __get_unique_labels(self):
        label_unique = set()
        # 遍历每个记录，从 "label" 字段提取第三个值（实体标签）
        for record in self.data:
            for lbl in record.get("label", []):
                # 要求 lbl 为 [start, end, tag]，且 tag 转为字符串
                if isinstance(lbl, list) and len(lbl) >= 3:
                    label_unique.add(str(lbl[2]))
        label_unique = list(label_unique)
        # 构造 IOB 标签列表：先添加 "0"（表示O），再为每个唯一标签添加 "B-" 与 "I-" 前缀
        iob_labels = ["0"]
        for lab in sorted(label_unique):
            iob_labels.append("B-" + lab)
            iob_labels.append("I-" + lab)
        labels_to_ids = {label: idx for idx, label in enumerate(iob_labels)}
        ids_to_labels = {idx: label for label, idx in labels_to_ids.items()}
        return labels_to_ids, ids_to_labels

    def convert_to_hf_dataset(self):
        tokens_list = []
        ner_tags_list = []
        ids = []

        for idx, record in enumerate(self.data):
            record_id = record.get("id", idx)
            ids.append(record_id)
            text = record.get("text", "")
            # 使用空格简单分词
            tokens = text.split()
            ner_labels = ["0"] * len(tokens)

            # 提取字符级实体标注为字典，key 为实体开始位置，value 为 (结束位置, tag)
            entities = {}
            for lbl in record.get("label", []):
                if isinstance(lbl, list) and len(lbl) >= 3:
                    try:
                        start = int(lbl[0])
                        end = int(lbl[1])
                        tag = str(lbl[2])
                        entities[start] = (end, tag)
                    except Exception as e:
                        continue

            # 调整对齐逻辑：如果 token 的起始位置位于 [entity_start, entity_end) 内，则认为 token 属于该实体。
            char_offset = 0
            for i, token in enumerate(tokens):
                token_start = char_offset
                token_end = char_offset + len(token)
                assigned = False
                for start, (end, tag) in entities.items():
                    if token_start >= start and token_start < end:
                        if token_start == start:
                            ner_labels[i] = "B-" + tag
                        else:
                            ner_labels[i] = "I-" + tag
                        assigned = True
                        break
                char_offset = token_end + 1  # 假设 token 之间由单个空格隔开

            ner_tag_ids = [self.labels_to_ids.get(label, 0) for label in ner_labels]
            tokens_list.append(tokens)
            ner_tags_list.append(ner_tag_ids)

        data_dict = {"id": ids, "tokens": tokens_list, "ner_tags": ner_tags_list}
        ds = Dataset.from_dict(data_dict)
        ds.features["ner_tags"] = Sequence(ClassLabel(names=list(self.labels_to_ids.keys())))
        return ds

if __name__ == "__main__":
    input_path = r"D:\下载\ai_question_dataset_cleaned.jsonl"
    with open(input_path, "r", encoding="utf-8") as f:
        lines = f.readlines()
    jsonl_data = [json.loads(line) for line in lines]
    converter = JsonlToHFDataset(jsonl_data)
    dataset = converter.convert_to_hf_dataset()
    dataset.save_to_disk(r"D:\下载/dataset")

    print("第一条记录 tokens:", dataset[0]['tokens'])
    print("第一条记录 ner_tags:", dataset[0]['ner_tags'])
    print("Mapping:", converter.labels_to_ids)
