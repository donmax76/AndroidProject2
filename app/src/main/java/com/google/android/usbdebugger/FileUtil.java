package com.google.android.usbdebugger;

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Вспомогательный класс для работы с файлами, в частности, для создания ZIP-архивов.
 */
public class FileUtil {

    private static final String TAG = "FileUtil";

    /**
     * Создает ZIP-архив из всех файлов в указанной директории.
     * @param sourceDirPath Путь к директории, которую нужно заархивировать.
     * @param zipFilePath Путь, по которому будет создан ZIP-файл.
     * @return true в случае успеха, false в случае ошибки.
     */
    public boolean createZipArchive(String sourceDirPath, String zipFilePath) {
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            Log.e(TAG, "Исходная директория не существует или не является директорией: " + sourceDirPath);
            return false;
        }

        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            Log.d(TAG, "Создание ZIP-архива: " + zipFilePath);
            addDirectoryToZip(sourceDir, sourceDir.getName(), zos);
            Log.d(TAG, "ZIP-архив успешно создан.");
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Ошибка при создании ZIP-архива", e);
            return false;
        }
    }

    /**
     * Рекурсивный метод для добавления файлов и папок в ZIP-архив.
     * @param source Файл или директория для добавления.
     * @param parentPath Путь внутри архива.
     * @param zos Поток для записи в ZIP.
     */
    private void addDirectoryToZip(File source, String parentPath, ZipOutputStream zos) throws IOException {
        File[] files = source.listFiles();
        if (files == null) {
            Log.w(TAG, "Не удалось получить список файлов из директории: " + source.getAbsolutePath());
            return;
        }

        for (File file : files) {
            String entryPath = parentPath + File.separator + file.getName();
            if (file.isDirectory()) {
                Log.d(TAG, "Добавление директории в архив: " + entryPath);
                addDirectoryToZip(file, entryPath, zos);
            } else {
                Log.d(TAG, "Добавление файла в архив: " + entryPath);
                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(entryPath);
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }
        }
    }
}
