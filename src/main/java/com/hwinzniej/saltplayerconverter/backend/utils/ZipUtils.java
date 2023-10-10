package com.hwinzniej.saltplayerconverter.backend.utils;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.Collection;

/**
 * @description: 将指定目录压缩为zip文件
 * @author: HWinZnieJ
 * @create: 2023-10-10 20:59
 **/

public class ZipUtils {
    public static void zip(File dir, OutputStream outputStream) throws IOException, ArchiveException {
        ZipArchiveOutputStream zipOutput = null;
        try {
            zipOutput = (ZipArchiveOutputStream) new ArchiveStreamFactory()
                    .createArchiveOutputStream(ArchiveStreamFactory.ZIP, outputStream);
            zipOutput.setEncoding("utf-8");
            zipOutput.setUseZip64(Zip64Mode.AsNeeded);
            Collection<File> files = FileUtils.listFilesAndDirs(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            for (File file : files) {
                InputStream in = null;
                try {
                    if (file.getName().contains(".zip")) {
                        continue;
                    }
                    if (file.getPath().equals(dir.getPath())) {
                        continue;
                    }
                    String relativePath = StringUtils.replace(file.getPath(), dir.getPath() + File.separator, "");
                    ZipArchiveEntry entry = new ZipArchiveEntry(file, relativePath);
                    zipOutput.putArchiveEntry(entry);
                    if (file.isDirectory()) {
                        continue;
                    }
                    in = new FileInputStream(file);
                    IOUtils.copy(in, zipOutput);
                    zipOutput.closeArchiveEntry();
                } finally {
                    if (in != null) {
                        IOUtils.closeQuietly(in);
                    }
                }
            }
            zipOutput.finish();
        } finally {
            IOUtils.closeQuietly(zipOutput);
        }
    }
}
