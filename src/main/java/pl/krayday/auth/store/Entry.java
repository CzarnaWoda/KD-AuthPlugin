package pl.krayday.auth.store;

public interface Entry
{
    void insert();
    
    void update(boolean p0);
    
    void delete();
}
