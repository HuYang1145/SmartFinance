package Model;
public class EntityResultModel {
    // 根据你的 Python 脚本输出自由定义字段
    // 如果输出类似 {"amount":"30 dollars","category":"repast","time":"yesterday"}
    // 那么就写这三项：
    private String amount;
    private String category;
    private String time;

    // 当然，你也可以用一个 Map<String,String> 承载任意实体：
    // private Map<String,String> entities;

    // 下面是标准的 getter/setter
    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
