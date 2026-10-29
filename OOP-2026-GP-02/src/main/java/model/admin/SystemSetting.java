package main.java.model.admin;

public class SystemSetting {
    private String key;
    private String value;
    private String category;
    private String description;

    public SystemSetting(String key, String value, String category, String description) {
        this.key = key;
        this.value = value;
        this.category = category;
        this.description = description;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
