package org.zap.framework.test.dao;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by Shin on 2015/11/20.
 */
public class RawTestSqlServer {

    @Test
    public void connect() {

        Statement sql = null;
        ResultSet rs = null;

        String driverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";   //加载JDBC驱动
        String dbURL = "jdbc:sqlserver://192.168.0.221:1433;databaseName=zap";   //连接服务器和数据库sample
        String userName = "sa";   //默认用户名
        String userPwd = "sa";   //密码

        Connection dbConn;

        try {

            Class.forName(driverName);
            dbConn = DriverManager.getConnection(dbURL, userName, userPwd);
            sql = dbConn.createStatement();
            rs = sql.executeQuery("select 1");
            System.out.println("Connection Successful!");   //如果连接成功 控制台输出Connection Successful!
            dbConn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
