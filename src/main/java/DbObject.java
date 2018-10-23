import java.util.List;

public class DbObject {
    String dbName;
    List<String> tableNames;

    @Override
    public String toString() {
        return "DbObject{" +
                "dbName='" + dbName + '\'' +
                ", tableNames=" + tableNames +
                '}';
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public List<String> getTableNames() {
        return tableNames;
    }

    public void setTableNames(List<String> tableNames) {
        this.tableNames = tableNames;
    }
}
