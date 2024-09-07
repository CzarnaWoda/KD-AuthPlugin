package pl.krayday.auth.store;

public enum StoreMode
{
    MYSQL("mysql");
    
    private String name;
    
    StoreMode(String name) {
        this.name = name;
    }
    
    public static StoreMode getByName(String name) {
        for (StoreMode sm : values()) {
            if (sm.getName().equalsIgnoreCase(name)) {
                return sm;
            }
        }
        return null;
    }
    
    public String getName() {
        return this.name;
    }
}
