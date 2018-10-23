import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.structure.DatabaseObject;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainApp {
    public static void main(String[] args) {

        String dbReferencePath = "jdbc:mysql://[ip]:3306/[DB_NAME]";
        String dbReferenceUserName = "userName";
        String dbReferencePassword = "password";

        String dbTargetPath = "jdbc:mysql://[ip]:3306/[DB_NAME]";
        String dbTargetUserName = "userName";
        String dbTargetPassword = "password";

        Connection conReference;
        Connection conTarget;
        try {
            conReference = DriverManager.getConnection(dbReferencePath, dbReferenceUserName, dbReferencePassword);
            conTarget = DriverManager.getConnection(dbTargetPath, dbTargetUserName, dbTargetPassword);

            diff(conReference, conTarget);

            conReference.close();
            conTarget.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }


    }


    static void diff(Connection referenceConnection, Connection targetConnection) throws LiquibaseException, IOException, ParserConfigurationException, SQLException {

        Liquibase liquibase = null;

        try {

            Database referenceDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(referenceConnection));
            Database targetDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(targetConnection));

            liquibase = new Liquibase("", new FileSystemResourceAccessor(), referenceDatabase);
            DiffResult diffResult = liquibase.diff(referenceDatabase, targetDatabase, new CompareControl());
            new DiffToChangeLog(diffResult, new DiffOutputControl()).print(System.out);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (liquibase != null) {
                liquibase.forceReleaseLocks();
            }
        }
    }

    private static DiffResult diff2(Connection referenceConnection, Connection targetConnection) throws SQLException, LiquibaseException, IOException, ParserConfigurationException {
        try {
            Database referenceDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(referenceConnection));
            Database targetDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(targetConnection));

            final DiffGeneratorFactory generatorFactory = DiffGeneratorFactory.getInstance();
            final CompareControl compareControl = new CompareControl();
            final DiffResult diffResult = generatorFactory.compare(referenceDatabase, targetDatabase, compareControl);
            boolean ignoreDefaultValueDifference = true;
            if (ignoreDefaultValueDifference) {
                Map<DatabaseObject, ObjectDifferences> changedObjects = diffResult.getChangedObjects();
                for (DatabaseObject changedDbObject : changedObjects.keySet()) {
                    ObjectDifferences objectDifferences = changedObjects.get(changedDbObject);
                    if (objectDifferences.removeDifference("defaultValue")) {
                    }
                    if (!objectDifferences.hasDifferences()) {
                        changedObjects.remove(objectDifferences);
                    }
                }
            }
            return diffResult;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void getSchemaList(String dbKeyWord, List<String> dbList, ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            String db = resultSet.getString("TABLE_CAT");
            if (db.contains(dbKeyWord)) {
                dbList.add(db);
            }
        }
    }


}
