package com.hwinzniej.saltplayerconverter.backend.utils;

import com.hwinzniej.saltplayerconverter.backend.controller.ConverterController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @description: 自定义定时任务
 * @author: HWinZnieJ
 * @create: 2023-10-20 13:26
 **/

public class Cron {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterController.class);

    public static void startJob() {
        try (ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor()) {
            long initialDelay = 3; // 延迟3天
            long period = 3; // 每隔3天执行一次
            service.scheduleAtFixedRate(() -> {
                deleteOutdatedFile(new File("sqliteUpload" + File.separator), 259200000);
                deleteOutdatedFile(new File("musicListUpload" + File.separator), 259200000);
                deleteOutdatedFile(new File("convertResult" + File.separator), 259200000);

                LOGGER.info("已删除过时的文件");
            }, initialDelay, period, TimeUnit.DAYS);
        }
    }

    private static void deleteOutdatedFile(File folder, int milliseconds) {
        File[] files = folder.listFiles();
        Date DATE = new Date();

        // 删除指定过期时间前上传的文件
        for (File file : files) {
            if (DATE.getTime() - file.lastModified() > milliseconds) {
                if (file.isDirectory()) {
                    // 如果是子文件夹，递归地处理它
                    deleteOutdatedFile(file, milliseconds);
                } else {
                    // 如果是文件，直接删除它
                    try {
                        file.delete();
                    } catch (Exception e) {
                        LOGGER.error("删除文件失败" + e);
                    }
                }
            }
        }
    }
}
