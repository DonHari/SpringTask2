package createData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;

public class CreateData {
    private static final int RECORD_COUNT = 1000000;
    private static final int BATCH_SIZE = 10000;
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost/";
    private static final String DB_URL_PARAMS = "?useSSL=false";
    private static final String DB_TABLE = "test";
    private static final String USER = "root";
    private static final String PASS = "root";

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        System.out.println("creating schema and table");
        createDatabaseAndTable();

        System.out.println("connecting");
        Class.forName(JDBC_DRIVER);
        Connection con = DriverManager.getConnection(DB_URL + DB_TABLE + DB_URL_PARAMS, USER, PASS);

        System.out.println("deleting");
        con.createStatement().execute("delete from contacts where id>0;");

        System.out.println("creating records");
        con.setAutoCommit(false);
        PreparedStatement ps = con.prepareStatement("insert into contacts(name) VALUE (?);");
        for(int i=0; i<RECORD_COUNT; i++){
            ps.setString(1, "contact" + (i + 1));
            ps.addBatch();
            if((i + 1) % BATCH_SIZE == 0) {
                System.out.println("executing batch\t" + ((RECORD_COUNT-(i + 1))/BATCH_SIZE) + " left");
                ps.executeBatch();
//                con.commit();
            }
        }

        System.out.println("committing changes");
        ps.executeBatch();
        con.commit();
        con.close();
    }

    private static BufferedReader openConnectionToResFile(String path){
        InputStream is = CreateData.class.getResourceAsStream(path);
        return new BufferedReader(new InputStreamReader(is));
    }

    private static void createDatabaseAndTable(){
        Connection con = null;
        Statement statement = null;
        try {
            //подключение через чистый jdbc, потому что BasicDataSource не пропускает подключение без схемы
            Class.forName(JDBC_DRIVER);
            con = DriverManager.getConnection(DB_URL + DB_URL_PARAMS, USER, PASS);

            BufferedReader in = openConnectionToResFile("/sql/schema.sql");
            String buffer;
            StringBuilder sqlScript = new StringBuilder();
            while ((buffer = in.readLine()) != null){
                sqlScript.append(buffer);
            }
            statement = con.createStatement();
            statement.addBatch(sqlScript.toString());

            in = openConnectionToResFile("/sql/table.sql");
            sqlScript = new StringBuilder();
            while ((buffer = in.readLine()) != null){
                sqlScript.append(buffer);
            }
            statement.addBatch(sqlScript.toString());

            statement.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(con != null){
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(statement != null){
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}