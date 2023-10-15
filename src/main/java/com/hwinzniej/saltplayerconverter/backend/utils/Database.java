package com.hwinzniej.saltplayerconverter.backend.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @description: 数据库相关操作
 * @author: HWinZnieJ
 * @create: 2023-09-30 10:08
 **/

public class Database {
    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);

    /**
     * 获取数据库的连接
     *
     * @param dbPath 数据库文件的名称或绝对路径
     * @return 数据库连接接口
     */
    public Connection getSQLiteConnection(String dbPath) throws SQLException {
        Connection conn;
        String url = "jdbc:sqlite:" + dbPath;
        conn = DriverManager.getConnection(url);
        return conn;
    }

    /**
     * 关闭到数据库的连接
     *
     * @param conn 数据库连接接口
     */
    public void closeConnection(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            LOGGER.error("数据库断开失败！\n错误详情：" + e);
        }
    }

    public Connection getMySQLConnection() throws SQLException {
        Connection conn = null;
        String url = "jdbc:mysql://127.0.0.1:3306/salt_player_converter_statistic?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf-8&rewriteBatchedStatements=true" ;
        String username = "root" ;
//            String password = "";
        String password = "" ;
        conn = DriverManager.getConnection(url, username, password);

        return conn;
    }
}
