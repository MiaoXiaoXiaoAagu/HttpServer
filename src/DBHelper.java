package src;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by aagui on 2019/2/16.
 */
public class DBHelper {
    // JDBC driver name and database URL
    private final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private final String DB_URL = "jdbc:mysql://111.231.54.85/ball_game"+"?serverTimezone=GMT%2B8";

    //  Database credentials
    private static final String USER = "root";
    private static final String PASS = "admin@1234";

    private Connection conn = null;
    private Statement stmt = null;

    public DBHelper() {
        //STEP 2: Register JDBC driver
        try {
            Class.forName(JDBC_DRIVER);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void open() {
        try {
            //System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, USER,PASS);
            //System.out.println("Creating statement...");
            stmt = conn.createStatement();
        } catch (SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void close() {
        try {
            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<String> query(String tableName) {
        List<String> rows = new ArrayList<>();
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM "+tableName);
            ResultSet rs = stmt.executeQuery(sql.toString()); // DML
            ResultSetMetaData metaData = rs.getMetaData();

            //STEP 5: Extract data from result set
            while (rs.next()) {
                StringBuilder line = new StringBuilder();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    line.append(rs.getString(i)+",");
                }
                line.deleteCharAt(line.length()-1);
                rows.add(line.toString());
            }
            //STEP 6: Clean-up environment
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rows;
    }
    public void insertUser(String[] args) {
        try {
            StringBuffer sql = new StringBuffer();
            sql.append("insert into user values(");
            for(String arg : args) {
                sql.append("\""+arg+"\",");
            }
            sql.deleteCharAt(sql.length()-1);////移除最后一个,号
            sql.append(");");
            stmt.executeUpdate(sql.toString());
        } catch (SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void insertScore(String username,int score) {
    	try {
    		PreparedStatement psql = conn.prepareStatement("insert into score (user,highScore) "
			        + "values(?,?)");
			         
	    	psql.setString(1,username);      
	    	psql.setInt(2,score);
	    	psql.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    }

    public void updateScore(String username,int socre) {
        try {
            StringBuffer sql = new StringBuffer();
            String[] columns = getTableColumns("score");
            sql.append("update score set ");
            //假定主键下标为0
            sql.append(columns[0]+"=\""+username+"\","+columns[1]+"=\""+socre+"\"");
            sql.append(" where "+columns[0]+"=\""+username+"\";");
            stmt.executeUpdate(sql.toString());
        } catch (SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public List<String> getRank()
    {
        List<String> scores = new ArrayList<>();
        try {
            ResultSet rs = stmt.executeQuery("select * from score order by highScore desc");
            int count = 0;
            while (rs.next() && count < 5) {
                scores.add(rs.getString(1)+","+rs.getInt(2));
                count += 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scores;
    }

    private String[] getTableColumns(String tableName) {
        String[] columns = null;
        try {
            StringBuffer sql = new StringBuffer();
            sql.append("select * from "+tableName+";");
            ResultSet rs = stmt.executeQuery(sql.toString());
            ResultSetMetaData metaData = rs.getMetaData();
            columns = new String[metaData.getColumnCount()];
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                columns[i-1] = metaData.getColumnLabel(i);
            }
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return columns;
    }
}
