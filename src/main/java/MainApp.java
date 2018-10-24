import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.configuration.LiquibaseConfiguration;
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
import liquibase.parser.LiquibaseParser;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.sdk.Context;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.util.SqlParser;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainApp {
    public static void main(String[] args) {
        String dbReferencePath = "jdbc:mysql://[ip]:[port]";
        String dbReferenceUserName = "userName";
        String dbReferencePassword = "pass";

        String dbTargetPath = "jdbc:mysql://[ip]:[port]";
        String dbTargetUserName = "userName";
        String dbTargetPassword = "pass";

        ArrayList<String> dbNames = new ArrayList<>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(dbReferencePath, dbReferenceUserName, dbReferencePassword);
            ResultSet rs = con.getMetaData().getCatalogs();
            //schema list that name contains "keyword"
            getSchemaList("keyword", dbNames, rs);
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        try {
            Connection conReference = null;
            Connection conTarget = null;
            for (String dbName : dbNames) {
                conReference = DriverManager.getConnection(dbReferencePath + "/" + dbName, dbReferenceUserName, dbReferencePassword);
                conTarget = DriverManager.getConnection(dbTargetPath + "/" + dbName, dbTargetUserName, dbTargetPassword);
                File targetFile = new File("./results/" + dbName + ".xml");
                PrintStream stream = new PrintStream(targetFile);
                diff(conReference, conTarget, stream);

                try {
                    Runtime.getRuntime().exec("java -jar liquibase.jar " +
                            "--driver=com.mysql.cj.jdbc.Driver " +
                            " --classpath=mysql-connector.jar " +
                            "--changeLogFile=results/" + dbName + ".xml " +
                            "--url=jdbc:mysql://localhost:3306/?serverTimezone=UTC " +
                            "--username=userName " +
                            "--password=password " +
                            "updateSQL > sqlScripts/" + dbName + ".sql");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            conReference.close();
            conTarget.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }


    static void diff(Connection referenceConnection, Connection targetConnection, PrintStream stream) {

        Liquibase liquibase = null;
        try {

            Database referenceDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(referenceConnection));
            Database targetDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(targetConnection));

            liquibase = new Liquibase("", new FileSystemResourceAccessor(), referenceDatabase);
            DiffResult diffResult = liquibase.diff(referenceDatabase, targetDatabase, new CompareControl());
            DiffToChangeLog diffToChangeLog = new DiffToChangeLog(diffResult, new DiffOutputControl());
            diffToChangeLog.print(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static DiffResult diff2(Connection referenceConnection, Connection targetConnection) throws SQLException, LiquibaseException, IOException, ParserConfigurationException {
        try {
            Database referenceDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(referenceConnection));
            Database targetDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(targetConnection));

            final DiffGeneratorFactory generatorFactory = DiffGeneratorFactory.getInstance();
            final CompareControl compareControl = new CompareControl();
            final DiffResult diffResult = generatorFactory.compare(referenceDatabase, targetDatabase, compareControl);
            boolean ignoreDefaultValueDifference = false;
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
