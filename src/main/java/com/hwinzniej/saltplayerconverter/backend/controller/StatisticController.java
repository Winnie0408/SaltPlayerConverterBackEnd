package com.hwinzniej.saltplayerconverter.backend.controller;

import com.alibaba.fastjson.JSONObject;
import com.hwinzniej.saltplayerconverter.backend.utils.Database;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: 处理前端请求，返回数据给前端，实现前后端交互的功能
 * @author: HWinZnieJ
 * @create: 2023-09-20 15:01
 **/

@RestController
@RequestMapping("/statistic")
public class StatisticController {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticController.class);

    @PostMapping("/save")
    public Object save(@RequestBody String frontEnd, HttpServletResponse response) {
        JSONObject data = JSONObject.parseObject(frontEnd);
        Map<String, Object> result = new HashMap<>();

        if (data.isEmpty()) {
            response.setStatus(400);
            result.put("result", "0");
            result.put("msg", "参数错误");
            return new JSONObject(result);
        }

        String sessionId = data.getString("uuid");
        String sourceEng = data.getString("sourceEng");
        String sourceChn = data.getString("sourceChn");
        int totalCount = data.getIntValue("totalCount");
        int saveCount = data.getIntValue("successCount");
        boolean enableParenthesesRemoval = data.getBooleanValue("enableParenthesesRemoval");
        boolean enableAlbumNameMatch = data.getBooleanValue("enableAlbumNameMatch");
        boolean enableArtistNameMatch = data.getBooleanValue("enableArtistNameMatch");
        boolean mode = data.getBooleanValue("mode");
        double similarity = data.getDoubleValue("similarity");
        int autoSuccessCount = data.getIntValue("autoSuccessCount");
        String time = data.getString("startTime");
        String endTime = data.getString("endTime");
        String tool = data.getString("tool");

        try {
            Database db = new Database();
            Connection conn = db.getMySQLConnection();

            Statement stmt = conn.createStatement();
            int saveResult = stmt.executeUpdate("INSERT INTO info (sourceEng, sourceChn, enableParenthesesRemoval, enableArtistNameMatch, enableAlbumNameMatch, mode, totalCount, autoSuccessCount, similarity, startTime, sessionId, tool, saveCount, endTime) VALUES ('" + sourceEng + "', " + "'" + sourceChn + "', " + enableParenthesesRemoval + ", " + enableArtistNameMatch + ", " + enableAlbumNameMatch + ", " + mode + ", " + totalCount + ", " + autoSuccessCount + ", " + similarity + ", '" + time + "', '" + sessionId + "', '" + tool + "' , " + saveCount + " , '" + endTime + "')");
            stmt.close();
            db.closeConnection(conn);

            if (saveResult == 1) {
                response.setStatus(200);
                result.put("result", 0);
                result.put("msg", "上传成功");
            } else {
                response.setStatus(500);
                result.put("result", 1);
                result.put("msg", "上传失败");
            }

        } catch (Exception e) {
            response.setStatus(500);
            result.put("result", 1);
            result.put("msg", "上传失败" + e.getMessage());
            LOGGER.error(e.toString(), e);
        }

        return result;
    }

    @PostMapping("/usage")
    public Object usage(@RequestBody String frontEnd, HttpServletResponse response) {
        JSONObject data = JSONObject.parseObject(frontEnd);
        Map<String, Object> result = new HashMap<>();

        if (data.isEmpty()) {
            response.setStatus(400);
            result.put("result", "0");
            result.put("msg", "参数错误");
            return new JSONObject(result);
        }

        String sessionId = data.getString("sessionId");
        String time = data.getString("time");
        String tool = data.getString("tool");
        String type = data.getString("type");

        try {
            Database db = new Database();
            Connection conn = db.getMySQLConnection();

            Statement stmt = conn.createStatement();
            int saveResult = stmt.executeUpdate("INSERT INTO `usage` (time,sessionId,type,tool) VALUES ('" + time + "', '" + sessionId + "', '" + type + "', '" + tool + "')");

            stmt.close();
            db.closeConnection(conn);

            if (saveResult == 1) {
                response.setStatus(200);
                result.put("result", 0);
                result.put("msg", "上传成功");
            } else {
                response.setStatus(500);
                result.put("result", 1);
                result.put("msg", "上传失败");
            }

        } catch (Exception e) {
            response.setStatus(500);
            result.put("result", 1);
            result.put("msg", "上传失败" + e.getMessage());
            LOGGER.error(e.toString(), e);
        }
        return result;
    }
}
