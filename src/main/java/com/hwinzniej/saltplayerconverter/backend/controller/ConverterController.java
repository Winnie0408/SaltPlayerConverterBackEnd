package com.hwinzniej.saltplayerconverter.backend.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hwinzniej.saltplayerconverter.backend.utils.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * @description: 歌单转换
 * @author: HWinZnieJ
 * @create: 2023-09-30 09:18
 **/
@RestController
@RequestMapping("/converter")
public class ConverterController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterController.class);

    @GetMapping("/init")
    public Object init(@RequestBody String frontEnd, HttpServletRequest request, HttpServletResponse response) {
        JSONObject front = JSONObject.parseObject(frontEnd);
        UUID uuid = UUID.randomUUID();
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(1800);
        session.setAttribute("uuid", uuid.toString());
        setSessionAttribute(front.getString("source"), session);
        response.setStatus(200);
        return "{\"msg\":\"初始化成功\"}";
    }

    @PostMapping("/uploadMusicList")
    public Object uploadMusicList(@RequestParam("musicList") MultipartFile musicList, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(400);
            return "{\"msg\":\"请先完成初始化\"}";
        }
        Map<String, Object> result = new HashMap<>();

        if (musicList.isEmpty()) {
            response.setStatus(400);
            result.put("msg", "请选择文件");

            return new JSONObject(result);
        }

        Random random = new Random();
        String fileName = System.currentTimeMillis() + random.nextInt(1000) + musicList.getOriginalFilename();
        String filePath = "musicListUpload/";

        File dest = new File(filePath);
        if (!dest.exists()) {
            dest.mkdirs();
        }

//        deleteOutdatedFile(dest, 259200000);

        try {
            File newFile = new File(dest.getAbsolutePath() + File.separator + fileName);
            musicList.transferTo(newFile);
            String[] localMusicFile = Files.readString(Path.of(newFile.getAbsolutePath())).split("\n");
            String[][] localMusic = new String[localMusicFile.length][4];
            int a = 0;
            for (String i : localMusicFile) {
                localMusic[a][0] = i.split("#\\*#")[0];
                localMusic[a][1] = i.split("#\\*#")[1];
                localMusic[a][2] = i.split("#\\*#")[2];
                localMusic[a][3] = i.split("#\\*#")[3];
                a++;
            }
            LOGGER.info("上传成功，共有" + localMusic.length + "首歌曲");
            result.put("msg", "上传成功");
            result.put("count", localMusic.length);
            response.setStatus(200);
            session.setAttribute("musicList", fileName);
            session.setAttribute("localMusic", localMusic);
            return new JSONObject(result);//返回json数据给前端
        } catch (IOException e) {
            LOGGER.error(e.toString(), e);
        }
        result.put("msg", "上传失败");
        response.setStatus(500);
        return new JSONObject(result);//返回json数据给前端
    }

    @PostMapping("/uploadDatabase")
    public Object uploadDatabase(@RequestParam("database") MultipartFile databaseFile, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(400);
            return "{\"msg\":\"请先完成初始化\"}";
        }
        Map<String, Object> result = new HashMap<>();

        if (databaseFile.isEmpty()) {
            response.setStatus(400);
            result.put("msg", "请选择文件");

            return new JSONObject(result);
        }

        Random random = new Random();
        String fileName = System.currentTimeMillis() + random.nextInt(1000) + databaseFile.getOriginalFilename();
        String filePath = "sqliteUpload/";

        File dest = new File(filePath);
        if (!dest.exists()) {
            dest.mkdirs();
        }

//        deleteOutdatedFile(dest, 259200000);

        try {
            File newFile = new File(dest.getAbsolutePath() + File.separator + fileName);
            databaseFile.transferTo(newFile);
            if (testDatabase(newFile.getAbsolutePath())) {
                LOGGER.info("上传成功，数据库读取成功");
                result.put("msg", "上传成功，数据库读取成功");
                response.setStatus(200);
                session.setAttribute("database", fileName);
                return new JSONObject(result);
            } else {
                LOGGER.info("上传成功，数据库读取失败");
                result.put("msg", "上传成功，数据库读取失败");
                response.setStatus(400);
                newFile.delete();
                return new JSONObject(result);
            }
        } catch (IOException e) {
            LOGGER.error(e.toString(), e);
        }
        result.put("msg", "上传失败");
        response.setStatus(500);
        return new JSONObject(result);
    }

    @GetMapping("/databaseSummary")
    public Object databaseSummary(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(400);
            return "{\"msg\":\"请先完成初始化\"}";
        }
        String database = String.valueOf(session.getAttribute("database"));

        Map<String, Object> result = new HashMap<>();

        Map<String, String> sourceAttribute = (Map<String, String>) session.getAttribute("sourceAttribute");
        String sourceEng = sourceAttribute.get("sourceEng");
        String sourceChn = sourceAttribute.get("sourceChn");
        String songListId = sourceAttribute.get("songListId");
        String songListName = sourceAttribute.get("songListName");
        String songListTableName = sourceAttribute.get("songListTableName");
        String songListSongInfoTableName = sourceAttribute.get("songListSongInfoTableName");
        String songListSongInfoPlaylistId = sourceAttribute.get("songListSongInfoPlaylistId");

        if (database == null) {
            response.setStatus(400);
            result.put("msg", "请先上传数据库");
            return new JSONObject(result);
        }
        Database db = new Database();
        Connection conn = db.getConnection("sqliteUpload/" + database);

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs;
            if (sourceEng.equals("KuWoMusic")) {
                rs = stmt.executeQuery("SELECT " + songListId + ", " + songListName + " FROM " + songListTableName + " WHERE uid NOT NULL");
            } else rs = stmt.executeQuery("SELECT " + songListId + ", " + songListName + " FROM " + songListTableName);

            ArrayList<String> playListId = new ArrayList<>();
            ArrayList<String> playListName = new ArrayList<>();

            while (rs.next()) {
                playListId.add(rs.getString(songListId)); // 保存歌单ID
                playListName.add(rs.getString(songListName)); // 保存歌单名
            }

            Map<String, String> songNum = new HashMap<>();
            ArrayList<Integer> listToBeDelete = new ArrayList<>();
            ArrayList<String> warnings = new ArrayList<>();

            for (int i = 0; i < playListId.size(); i++) {
                //检查歌单是否包含歌曲
                if (stmt.executeQuery("SELECT COUNT(*) FROM " + songListSongInfoTableName + " WHERE " + songListSongInfoPlaylistId + "=" + playListId.get(i)).getInt(1) == 0) {
                    warnings.add(playListName.get(i));
                    listToBeDelete.add(i);
                } else {
                    rs.close();
                    rs = stmt.executeQuery("SELECT COUNT(*) FROM " + songListSongInfoTableName + " WHERE " + songListSongInfoPlaylistId + "=" + playListId.get(i));
                    songNum.put(playListId.get(i), String.valueOf(rs.getInt(1)));
                }
            }

            //删除不包含歌曲的歌单
            for (int i = listToBeDelete.size() - 1; i >= 0; i--) {
                playListName.remove(playListName.get(listToBeDelete.get(i)));
                playListId.remove(playListId.get(listToBeDelete.get(i)));
            }

            session.setAttribute("playListId", playListId);
            session.setAttribute("playListName", playListName);
            session.setAttribute("songNum", songNum);

            result.put("msg", "歌单读取成功");
            result.put("warnings", warnings);
            result.put("playListName", playListName);
            result.put("playListId", playListId);
            result.put("songNum", songNum);
            response.setStatus(200);
            db.closeConnection(conn);
            return new JSONObject(result);

        } catch (SQLException e) {
            response.setStatus(500);
            result.put("msg", "歌单读取失败");
            db.closeConnection(conn);
            LOGGER.error(e.toString(), e);
            return new JSONObject(result);
        }
    }

    @GetMapping("/attemptConvert")
    public Object attemptConvert(@RequestBody String frontEnd, HttpServletRequest request, HttpServletResponse response) {
        JSONObject front = JSONObject.parseObject(frontEnd);
        int similarity = (front.getInteger("similiarity") == null) ? 85 : front.getInteger("similiarity");
        int playlistId = (front.getInteger("playlistId")) == null ? -1 : front.getInteger("playlistId");
        boolean enableParenthesesRemoval = (front.getBoolean("enableParenthesesRemoval")) != null && front.getBoolean("enableParenthesesRemoval");
        boolean enableArtistNameMatch = (front.getBoolean("enableArtistNameMatch")) == null || front.getBoolean("enableArtistNameMatch");
        boolean enableAlbumNameMatch = (front.getBoolean("enableAlbumNameMatch")) == null || front.getBoolean("enableAlbumNameMatch");

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(400);
            return "{\"msg\":\"请先完成初始化\"}";
        }
        String database = String.valueOf(session.getAttribute("database"));
        if (database == null) {
            response.setStatus(400);
            return "{\"msg\":\"请先上传数据库\"}";
        }
        String[][] localMusic = (String[][]) session.getAttribute("localMusic");
//        ArrayList<String> playListId = (ArrayList<String>) session.getAttribute("playListId");
        Map<String, String> sourceAttribute = (Map<String, String>) session.getAttribute("sourceAttribute");

        int autoSuccessCount = 0; //自动匹配成功的歌曲数量

        Database db = new Database();
        Connection conn = db.getConnection("sqliteUpload/" + database);

        try {
            Statement stmt = conn.createStatement();
            Statement stmt1 = conn.createStatement();
            ResultSet rs;
            ResultSet rs1;

            String songName; //歌曲名
            String songArtist; //歌手名
            String songAlbum; //专辑名
            int num = 0; //当前第几首歌

            //获取歌单歌曲信息
            String songListSongInfoSongId = sourceAttribute.get("songListSongInfoSongId");
            String songListSongInfoPlaylistId = sourceAttribute.get("songListSongInfoPlaylistId");
            String songListSongInfoTableName = sourceAttribute.get("songListSongInfoTableName");
            String sortField = sourceAttribute.get("sortField");
            String songInfoSongName = sourceAttribute.get("songInfoSongName");
            String songInfoSongArtist = sourceAttribute.get("songInfoSongArtist");
            String songInfoSongAlbum = sourceAttribute.get("songInfoSongAlbum");
            String songInfoTableName = sourceAttribute.get("songInfoTableName");
            String songInfoSongId = sourceAttribute.get("songInfoSongId");
            String sourceEng = sourceAttribute.get("sourceEng");
            String sourceChn = sourceAttribute.get("sourceChn");

            Map<String, Object> result = new HashMap<>();

            rs = stmt.executeQuery("SELECT " + songListSongInfoSongId + " FROM " + songListSongInfoTableName + " WHERE " + songListSongInfoPlaylistId + "='" + playlistId + "'ORDER BY " + sortField);
            while (rs.next()) {
                String trackId = rs.getString(songListSongInfoSongId); //歌曲ID
                rs1 = stmt1.executeQuery("SELECT " + songInfoSongName + ", " + songInfoSongArtist + ", " + songInfoSongAlbum + " FROM " + songInfoTableName + " WHERE " + songInfoSongId + "=" + trackId); //使用歌曲ID查询歌曲信息

                songName = rs1.getString(songInfoSongName);
                if (songName == null) songName = "";

                songArtist = rs1.getString(songInfoSongArtist);
                if (songArtist == null) songArtist = "";
                //网易云音乐歌手名为JSON格式，需要特殊处理
                if (sourceEng.equals("CloudMusic"))
                    songArtist = JSON.parseObject(songArtist.substring(1, songArtist.length() - 1)).getString("name");
                songArtist = songArtist.replaceAll(" ?& ?", "/").replaceAll("、", "/");

                songAlbum = rs1.getString(songInfoSongAlbum);
                if (songAlbum == null) songAlbum = "";

                Map<String, Double> nameSimilarityArray = new HashMap<>(); //歌曲名相似度键值对
                Map<String, Double> artistSimilarityArray = new HashMap<>(); //歌手名相似度键值对
                Map<String, Double> albumSimilarityArray = new HashMap<>(); //专辑名相似度键值对

                //获取歌曲名相似度列表
                if (enableParenthesesRemoval) for (int k = 0; k < localMusic.length; k++) {
                    nameSimilarityArray.put(String.valueOf(k), StringSimilarityCompare.similarityRatio(songName.replaceAll("(?i) ?\\((?!inst|[^()]* ver)[^)]*\\) ?", "").toLowerCase(), localMusic[k][0].replaceAll("(?i) ?\\((?!inst|[^()]* ver)[^)]*\\) ?", "").toLowerCase()));
                }
                else for (int k = 0; k < localMusic.length; k++) {
                    nameSimilarityArray.put(String.valueOf(k), StringSimilarityCompare.similarityRatio(songName.toLowerCase(), localMusic[k][0].toLowerCase()));
                }

                Map.Entry<String, Double> maxValue = MapSort.getMaxValue(nameSimilarityArray); //获取键值对表中相似度的最大值所在的键值对
                double songNameMaxSimilarity = maxValue.getValue(); //获取相似度的最大值
                String songNameMaxKey = maxValue.getKey(); //获取相似度的最大值对应的歌曲在localMusic数组中的位置

                //获取歌手名相似度列表
                double songArtistMaxSimilarity;
                if (enableArtistNameMatch) {
                    if (enableParenthesesRemoval) for (int k = 0; k < localMusic.length; k++) {
                        artistSimilarityArray.put(String.valueOf(k), StringSimilarityCompare.similarityRatio(songArtist.replaceAll("(?i) ?\\((?!inst|[^()]* ver)[^)]*\\) ?", "").toLowerCase(), localMusic[k][1].replaceAll("(?i) ?\\((?!inst|[^()]* ver)[^)]*\\) ?", "").toLowerCase()));
                    }
                    else for (int k = 0; k < localMusic.length; k++) {
                        artistSimilarityArray.put(String.valueOf(k), StringSimilarityCompare.similarityRatio(songArtist.toLowerCase(), localMusic[k][1].toLowerCase()));
                    }
                    maxValue = MapSort.getMaxValue(artistSimilarityArray); //获取键值对表中相似度的最大值所在的键值对
                    songArtistMaxSimilarity = maxValue.getValue(); //获取相似度的最大值
                    String songArtistMaxKey = maxValue.getKey(); //获取相似度的最大值对应的歌手名
                } else {
                    songArtistMaxSimilarity = 1.0;
                }

                //获取专辑名相似度列表
                double songAlbumMaxSimilarity;
                if (enableAlbumNameMatch) {
                    if (enableParenthesesRemoval) for (int k = 0; k < localMusic.length; k++) {
                        albumSimilarityArray.put(String.valueOf(k), StringSimilarityCompare.similarityRatio(songAlbum.replaceAll("(?i) ?\\((?!inst|[^()]* ver)[^)]*\\) ?", "").toLowerCase(), localMusic[k][2].replaceAll("(?i) ?\\((?!inst|[^()]* ver)[^)]*\\) ?", "").toLowerCase()));
                    }
                    else for (int k = 0; k < localMusic.length; k++) {
                        albumSimilarityArray.put(String.valueOf(k), StringSimilarityCompare.similarityRatio(songAlbum.toLowerCase(), localMusic[k][2].toLowerCase()));
                    }
                    maxValue = MapSort.getMaxValue(albumSimilarityArray); //获取键值对表中相似度的最大值所在的键值对
                    songAlbumMaxSimilarity = maxValue.getValue(); //获取相似度的最大值
                    String songAlbumMaxKey = maxValue.getKey(); //获取相似度的最大值对应的专辑名
                } else {
                    songAlbumMaxSimilarity = 1.0;
                }

                if (songNameMaxSimilarity >= similarity / 100.0 && songArtistMaxSimilarity >= similarity / 100.0 && songAlbumMaxSimilarity >= similarity / 100.0) {
                    //歌曲名、歌手名、专辑名均匹配成功
                    String[][] data = {{"true", songNameMaxKey}, {songName, localMusic[Integer.parseInt(songNameMaxKey)][0], String.format("%.1f%%", songNameMaxSimilarity * 100)}, {songArtist, localMusic[Integer.parseInt(songNameMaxKey)][1], String.format("%.1f%%", songArtistMaxSimilarity * 100)}, {songAlbum, localMusic[Integer.parseInt(songNameMaxKey)][2], String.format("%.1f%%", songAlbumMaxSimilarity * 100)}};
                    result.put(String.valueOf(num++), data);
                    autoSuccessCount++;
                } else {
                    String[][] data = {{"false", songNameMaxKey}, {songName, localMusic[Integer.parseInt(songNameMaxKey)][0], String.format("%.1f%%", songNameMaxSimilarity * 100)}, {songArtist, localMusic[Integer.parseInt(songNameMaxKey)][1], String.format("%.1f%%", songArtistMaxSimilarity * 100)}, {songAlbum, localMusic[Integer.parseInt(songNameMaxKey)][2], String.format("%.1f%%", songAlbumMaxSimilarity * 100)}};
                    result.put(String.valueOf(num++), data);
                }
            }
            response.setStatus(200);
            db.closeConnection(conn);
            result.put("total", num);
            return new JSONObject(result);

        } catch (SQLException e) {
            response.setStatus(500);
            db.closeConnection(conn);
            LOGGER.error(e.toString(), e);
            return "{\"msg\":\"系统内部错误！\"}";
        }
    }

    private boolean testDatabase(String filePath) {
        Database db = new Database();
        Connection conn = db.getConnection(filePath);
        try {
            conn.createStatement().execute("SELECT type FROM  sqlite_master LIMIT 1");
            db.closeConnection(conn);
            return true;
        } catch (Exception e) {
            db.closeConnection(conn);
            return false;
        }
    }

    private int readTxtLines(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return -1;
        }
        try {
            LineNumberReader lineNumberReader = new LineNumberReader(new java.io.FileReader(file));
            lineNumberReader.skip(Long.MAX_VALUE);
            int result = lineNumberReader.getLineNumber();
            lineNumberReader.close();
            return result;
        } catch (Exception e) {
            return -1;
        }
    }

    private void setSessionAttribute(String source, HttpSession session) {
        Map<String, String> sourceAttribute = new HashMap<>();
        switch (source) {
            case "QQMusic" -> {
                sourceAttribute.put("sourceEng", "QQMusic");
                sourceAttribute.put("sourceChn", "QQ音乐");
                sourceAttribute.put("DatabaseName", "QQMusic");
                sourceAttribute.put("songListTableName", "User_Folder_table");
                sourceAttribute.put("songListId", "folderid");
                sourceAttribute.put("songListName", "foldername");
                sourceAttribute.put("songListSongInfoTableName", "User_Folder_Song_table");
                sourceAttribute.put("songListSongInfoPlaylistId", "folderid");
                sourceAttribute.put("songListSongInfoSongId", "id");
                sourceAttribute.put("songInfoTableName", "Song_table");
                sourceAttribute.put("sortField", "position");
                sourceAttribute.put("songInfoSongId", "id");
                sourceAttribute.put("songInfoSongName", "name");
                sourceAttribute.put("songInfoSongArtist", "singername");
                sourceAttribute.put("songInfoSongAlbum", "albumname");
            }
            case "CloudMusic" -> {
                sourceAttribute.put("sourceEng", "CloudMusic");
                sourceAttribute.put("sourceChn", "网易云音乐");
                sourceAttribute.put("DatabaseName", "cloudmusic.db");
                sourceAttribute.put("songListTableName", "playlist");
                sourceAttribute.put("songListId", "_id");
                sourceAttribute.put("songListName", "name");
                sourceAttribute.put("songListSongInfoTableName", "playlist_track");
                sourceAttribute.put("songListSongInfoPlaylistId", "playlist_id");
                sourceAttribute.put("songListSongInfoSongId", "track_id");
                sourceAttribute.put("songInfoTableName", "track");
                sourceAttribute.put("sortField", "track_order");
                sourceAttribute.put("songInfoSongId", "id");
                sourceAttribute.put("songInfoSongName", "name");
                sourceAttribute.put("songInfoSongArtist", "artists");
                sourceAttribute.put("songInfoSongAlbum", "album_name");
            }
            case "KugouMusic" -> {
                sourceAttribute.put("sourceEng", "KuGouMusic");
                sourceAttribute.put("sourceChn", "酷狗音乐");
                sourceAttribute.put("DatabaseName", "kugou_music_phone_v7.db");
                sourceAttribute.put("songListTableName", "kugou_playlists");
                sourceAttribute.put("songListId", "_id");
                sourceAttribute.put("songListName", "name");
                sourceAttribute.put("songListSongInfoTableName", "playlistsong");
                sourceAttribute.put("songListSongInfoPlaylistId", "plistid");
                sourceAttribute.put("songListSongInfoSongId", "songid");
                sourceAttribute.put("songInfoTableName", "kugou_songs");
                sourceAttribute.put("sortField", "cloudfileorderweight");
                sourceAttribute.put("songInfoSongId", "_id");
                sourceAttribute.put("songInfoSongName", "trackName");
                sourceAttribute.put("songInfoSongArtist", "artistName");
                sourceAttribute.put("songInfoSongAlbum", "albumName");
            }
            case "KuwoMusic" -> {
                sourceAttribute.put("sourceEng", "KuWoMusic");
                sourceAttribute.put("sourceChn", "酷我音乐");
                sourceAttribute.put("DatabaseName", "kwplayer.db");
                sourceAttribute.put("songListTableName", "v3_list");
                sourceAttribute.put("songListId", "id");
                sourceAttribute.put("songListName", "showname");
                sourceAttribute.put("songListSongInfoTableName", "v3_music");
                sourceAttribute.put("songListSongInfoPlaylistId", "listid");
                sourceAttribute.put("songListSongInfoSongId", "rid");
                sourceAttribute.put("songInfoTableName", "v3_music");
                sourceAttribute.put("sortField", "id");
                sourceAttribute.put("songInfoSongId", "rid");
                sourceAttribute.put("songInfoSongName", "name");
                sourceAttribute.put("songInfoSongArtist", "artist");
                sourceAttribute.put("songInfoSongAlbum", "album");
            }
        }
        session.setAttribute("sourceAttribute", sourceAttribute);
    }

    private void deleteOutdatedFile(File folder, int milisenconds) {
        String[] Name = folder.list();
        Date DATE = new Date();

        //自动删除3天前上传的文件
        for (String i : Name) {
            File temp = new File(folder.getAbsolutePath() + "/" + i);
            if (DATE.getTime() - temp.lastModified() > milisenconds) {
                try {
                    File del = new File(temp.getAbsolutePath());
                    if (del.delete()) {
                        LOGGER.info("旧文件删除成功");
                    }
                } catch (Exception e) {
                    LOGGER.info("旧文件删除失败");
                }
            }
        }
    }

}
