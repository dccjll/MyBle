package com.paiai.mble.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.blankj.utilcode.util.CloseUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <h3>File工具类</h3>
 * <p>主要封装了一些对文件读写的操作
 *
 */
public class FileUtils {
    
    private FileUtils() {
        throw new Error("￣﹏￣");
    }

    /** 分隔符. */
    private final static String FILE_EXTENSION_SEPARATOR = ".";

    /**"/"*/
    public final static String SEP = File.separator;

    /** SD卡根目录 */
    public static final String SDPATH = Environment
            .getExternalStorageDirectory() + File.separator;

    /**
     * 判断SD卡是否可用
     * @return SD卡可用返回true
     */
    public static boolean hasSdcard() {
        String status = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(status);
    }

    /**
     * 读取文件的内容
     * <br>
     * 默认utf-8编码
     * @param filePath 文件路径
     * @return 字符串
     * @throws IOException
     */
    public static String readFile(String filePath) throws IOException {
        return readFile(filePath, "utf-8");
    }

    /**
     * 读取文件的内容
     * @param filePath 文件目录
     * @param charsetName 字符编码
     * @return String字符串
     */
    private static String readFile(String filePath, String charsetName)
            throws IOException {
        if (TextUtils.isEmpty(filePath))
            return null;
        if (TextUtils.isEmpty(charsetName))
            charsetName = "utf-8";
        File file = new File(filePath);
        StringBuilder fileContent = new StringBuilder("");
        if (!file.isFile())
            return null;
        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(
                    file), charsetName);
            reader = new BufferedReader(is);
            String line;
            while ((line = reader.readLine()) != null) {
                if (!fileContent.toString().equals("")) {
                    fileContent.append("\r\n");
                }
                fileContent.append(line);
            }
            return fileContent.toString();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取文本文件到List字符串集合中(默认utf-8编码)
     * @param filePath 文件目录
     * @return 文件不存在返回null，否则返回字符串集合
     * @throws IOException
     */
    public static List<String> readFileToList(String filePath)
            throws IOException {
        return readFileToList(filePath, "utf-8");
    }

    /**
     * 读取文本文件到List字符串集合中
     * @param filePath 文件目录
     * @param charsetName 字符编码
     * @return 文件不存在返回null，否则返回字符串集合
     */
    private static List<String> readFileToList(String filePath, String charsetName) throws IOException {
        if (TextUtils.isEmpty(filePath))
            return null;
        if (TextUtils.isEmpty(charsetName))
            charsetName = "utf-8";
        File file = new File(filePath);
        List<String> fileContent = new ArrayList<>();
        if (!file.isFile()) {
            return null;
        }
        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(
                    file), charsetName);
            reader = new BufferedReader(is);
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.add(line);
            }
            return fileContent;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 向文件中写入数据
     * @param filePath 文件目录
     * @param content 要写入的内容
     * @param append 如果为 true，则将数据写入文件末尾处，而不是写入文件开始处
     * @return 写入成功返回true， 写入失败返回false
     * @throws IOException
     */
    public static boolean writeFile(String filePath, String content,
                                    boolean append) throws IOException {
        if (TextUtils.isEmpty(filePath))
            return false;
        if (TextUtils.isEmpty(content))
            return false;
        FileWriter fileWriter = null;
        try {
            createFile(filePath);
            File tempFile = new File(filePath);
            if(!tempFile.exists()){
                if(!tempFile.mkdirs()){
                    return false;
                }
            }
            fileWriter = new FileWriter(filePath, append);
            fileWriter.write(content);
            fileWriter.flush();
            return true;
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 用nio的方式写文件
     * @param filePath
     * @param content
     * @param append
     * @return
     * @throws IOException
     */
    public static boolean writeFileByNio(String filePath, String content,
                                         boolean append) throws IOException {
        if (TextUtils.isEmpty(filePath))
            return false;
        if (TextUtils.isEmpty(content))
            return false;
        
        FileOutputStream outputStream = null;
        FileChannel channel = null;
        try {
            createFile(filePath);
            outputStream = new FileOutputStream(filePath, append);
            channel = outputStream.getChannel();
            ByteBuffer buffer = ByteBuffer.wrap(content.getBytes());
            channel.write(buffer);
            outputStream.flush();
            buffer.clear();
            return true;
        } finally {
            IOUtils.close(channel);
            IOUtils.close(outputStream);
        }
    }

    /**
     * 向文件中写入数据<br>
     * 默认在文件开始处重新写入数据
     * @param filePath 文件目录
     * @param stream 字节输入流
     * @return 写入成功返回true，否则返回false
     * @throws IOException
     */
    private static boolean writeFile(String filePath, InputStream stream)
            throws IOException {
        return writeFile(filePath, stream, false);
    }

    /**
     * 向文件中写入数据
     * @param filePath 文件目录
     * @param stream 字节输入流
     * @param append 如果为 true，则将数据写入文件末尾处；
     *              为false时，清空原来的数据，从头开始写
     * @return 写入成功返回true，否则返回false
     * @throws IOException
     */
    private static boolean writeFile(String filePath, InputStream stream, boolean append) throws IOException {
        if (TextUtils.isEmpty(filePath))
            throw new NullPointerException("filePath is Empty");
        if (stream == null)
            throw new NullPointerException("InputStream is null");
        return writeFile(new File(filePath), stream,
                append);
    }

    /**
     * 向文件中写入数据
     * 默认在文件开始处重新写入数据
     * @param file 指定文件
     * @param stream 字节输入流
     * @return 写入成功返回true，否则返回false
     * @throws IOException
     */
    public static boolean writeFile(File file, InputStream stream)
            throws IOException {
        return writeFile(file, stream, false);
    }

    /**
     * 向文件中写入数据
     * @param file 指定文件
     * @param stream 字节输入流
     * @param append 为true时，在文件开始处重新写入数据；
     *              为false时，清空原来的数据，从头开始写
     * @return 写入成功返回true，否则返回false
     * @throws IOException
     */
    public static boolean writeFile(File file, InputStream stream,
                                    boolean append) throws IOException {
        if (file == null)
            throw new NullPointerException("file = null");
        OutputStream out = null;
        try {
            createFile(file.getAbsolutePath());
            out = new FileOutputStream(file, append);
            byte data[] = new byte[1024];
            int length;
            while ((length = stream.read(data)) != -1) {
                out.write(data, 0, length);
            }
            out.flush();
            return true;
        } finally {
            if (out != null) {
                try {
                    out.close();
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 复制文件
     * @param sourceFilePath 源文件目录（要复制的文件目录）
     * @param destFilePath 目标文件目录（复制后的文件目录）
     * @return 复制文件成功返回true，否则返回false
     * @throws IOException
     */
    public static boolean copyFile(String sourceFilePath, String destFilePath)
            throws IOException {
        InputStream inputStream = new FileInputStream(sourceFilePath);
        return writeFile(destFilePath, inputStream);
    }

    /**
     * 采用nio快速拷贝文件
     * @param inPath
     * @param outPath
     * @throws IOException
     */
    public static void copyFileNio(String inPath, String outPath) throws IOException {
        copyFileNio(new File(inPath), new File(outPath));
    }

    /**
     * 采用nio快速拷贝文件
     * @param in
     * @param out
     * @throws IOException
     */
    @SuppressWarnings("resource")
    private static void copyFileNio(File in, File out) throws IOException {
        FileChannel inChannel = new FileInputStream(in).getChannel();
        FileChannel outChannel = new FileOutputStream(out).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
            int maxCount = (64 * 1024 * 1024) - (32 * 1024);
            long size = inChannel.size();
            long position = 0;
            while (position < size) {
                position += inChannel
                        .transferTo(position, maxCount, outChannel);
            }
        } finally {
            IOUtils.close(inChannel);
            IOUtils.close(outChannel);
        }
    }

    /**
     * 获取某个目录下的文件名
     * @param dirPath 目录
     * @param fileFilter 过滤器
     * @return 某个目录下的所有文件名
     */
    public static List<String> getFileNameList(String dirPath,
                                               FilenameFilter fileFilter) {
        if (fileFilter == null)
            return getFileNameList(dirPath);
        if (TextUtils.isEmpty(dirPath))
            return Collections.emptyList();
        File dir = new File(dirPath);

        File[] files = dir.listFiles(fileFilter);
        if (files == null)
            return Collections.emptyList();

        List<String> conList = new ArrayList<>();
        for (File file : files) {
            if (file.isFile())
                conList.add(file.getName());
        }
        return conList;
    }

    /**
     * 获取某个目录下的文件名
     * @param dirPath 目录
     * @return 某个目录下的所有文件名
     */
    public static List<String> getFileNameList(String dirPath) {
        if (TextUtils.isEmpty(dirPath))
            return Collections.emptyList();
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null)
            return Collections.emptyList();
        List<String> conList = new ArrayList<>();
        for (File file : files) {
            if (file.isFile())
                conList.add(file.getName());
        }
        return conList;
    }
    /**
     * 获取某个目录下的文件的文件名
     * @param dirPath 目录
     * @return 某个目录下的所有文件名
     */
    public static List<String> getDirectoryListName(String dirPath) {
        if (TextUtils.isEmpty(dirPath))
            return Collections.emptyList();
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null)
            return Collections.emptyList();
        List<String> conList = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory())
                conList.add(file.getName());
        }
        return conList;
    }
    /**
     * 获取某个目录下的指定扩展名的文件名称
     * @param dirPath 目录
     * @return 某个目录下的所有文件名
     */
    public static List<String> getFileNameList(String dirPath,
                                               final String extension) {
        if (TextUtils.isEmpty(dirPath))
            return Collections.emptyList();
        File dir = new File(dirPath);
        File[] files = dir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                return filename.indexOf("." + extension) > 0;
            }
        });
        if (files == null)
            return Collections.emptyList();
        List<String> conList = new ArrayList<>();
        for (File file : files) {
            if (file.isFile())
                conList.add(file.getName());
        }
        return conList;
    }

    /**
     * 获得文件的扩展名
     * @param filePath 文件路径
     * @return 如果没有扩展名，返回""
     */
    public static String getFileExtension(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }
        int extenPosi = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        int filePosi = filePath.lastIndexOf(File.separator);
        if (extenPosi == -1) {
            return "";
        }
        return (filePosi >= extenPosi) ? "" : filePath.substring(extenPosi + 1);
    }

    /**
     * 创建文件
     * @param path 文件的绝对路径
     * @return
     */
    public static boolean createFile(String path) {
        return !TextUtils.isEmpty(path) && createFile(new File(path));
    }

    /**
     * 创建文件
     * @param file
     * @return 创建成功返回true
     */
    private static boolean createFile(File file) {
        if (file == null || !makeDirs(getFolderName(file.getAbsolutePath())))
            return false;
        if (!file.exists())
            try {
                return file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        return false;
    }

    /**
     * 创建目录（可以是多个）
     * @param filePath 目录路径
     * @return  如果路径为空时，返回false；如果目录创建成功，则返回true，否则返回false
     */
    private static boolean makeDirs(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File folder = new File(filePath);
        return (folder.exists() && folder.isDirectory()) || folder
                .mkdirs();
    }

    /**
     * 创建目录（可以是多个）
     * @param dir 目录
     * @return 如果目录创建成功，则返回true，否则返回false
     */
    public static boolean makeDirs(File dir) {
        return dir != null && ((dir.exists() && dir.isDirectory()) || dir.mkdirs());
    }

    /**
     * 判断文件是否存在
     * @param filePath 文件路径
     * @return 如果路径为空或者为空白字符串，就返回false；如果文件存在，且是文件，
     *          就返回true；如果不是文件或者不存在，则返回false
     */
    private static boolean isFileExist(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        return (file.exists() && file.isFile());
    }

    /**
     * 获得不带扩展名的文件名称
     * @param filePath 文件路径
     * @return
     */
    public static String getFileNameWithoutExtension(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }
        int extenPosi = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        int filePosi = filePath.lastIndexOf(File.separator);
        if (filePosi == -1) {
            return (extenPosi == -1 ? filePath : filePath.substring(0,
                    extenPosi));
        }
        if (extenPosi == -1) {
            return filePath.substring(filePosi + 1);
        }
        return (filePosi < extenPosi ? filePath.substring(filePosi + 1,
                extenPosi) : filePath.substring(filePosi + 1));
    }

    /**
     * 获得所在目录名称
     * @param filePath 文件的绝对路径
     * @return 如果路径为空或空串，返回路径名；不为空时，如果为根目录，返回"";
     *          如果不是根目录，返回所在目录名称，格式如：C:/Windows/Boot
     */
    private static String getFolderName(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }
        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? "" : filePath.substring(0, filePosi);
    }

    /**
     * 判断目录是否存在
     * @param directoryPath 目录路径
     * @return 如果路径为空或空白字符串，返回false；如果目录存在且，确实是目录文件夹，
     *          返回true；如果不是文件夹或者不存在，则返回false
     */
    public static boolean isFolderExist(String directoryPath) {
        if (TextUtils.isEmpty(directoryPath)) {
            return false;
        }
        File dire = new File(directoryPath);
        return (dire.exists() && dire.isDirectory());
    }

    /**
     * 删除指定文件或指定目录内的所有文件
     * @param path 文件或目录的绝对路径
     * @return 路径为空或空白字符串，返回true；文件不存在，返回true；文件删除返回true；
     *          文件删除异常返回false
     */
    public static boolean deleteFile(String path) {
        return TextUtils.isEmpty(path) || deleteFile(new File(path));
    }

    /**
     * 删除指定文件或指定目录内的所有文件
     * @param file
     * @return 路径为空或空白字符串，返回true；文件不存在，返回true；文件删除返回true；
     *          文件删除异常返回false
     */
    public static boolean deleteFile(File file) {
        if (file == null)
            throw new NullPointerException("file is null");
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        if (!file.isDirectory()) {
            return false;
        }

        File[] files = file.listFiles();
        if (files == null)
            return true;
        for (File f : files) {
            if (f.isFile()) {
                f.delete();
            } else if (f.isDirectory()) {
                deleteFile(f.getAbsolutePath());
            }
        }
        return file.delete();
    }

    /**
     * 删除指定目录中特定的文件
     * @param dir
     * @param filter
     */
    public static void delete(String dir, FilenameFilter filter) {
        if (TextUtils.isEmpty(dir))
            return;
        File file = new File(dir);
        if (!file.exists())
            return;
        if (file.isFile())
            file.delete();
        if (!file.isDirectory())
            return;

        File[] lists;
        if (filter != null)
            lists = file.listFiles(filter);
        else
            lists = file.listFiles();

        if (lists == null)
            return;
        for (File f : lists) {
            if (f.isFile()) {
                f.delete();
            }
        }
    }

    /**
     * 获得文件或文件夹的大小
     * @param path 文件或目录的绝对路径
     * @return 返回当前目录的大小 ，注：当文件不存在，为空，或者为空白字符串，返回 -1
     */
    public static long getFileSize(String path) {
        if (TextUtils.isEmpty(path)) {
            return -1;
        }
        File file = new File(path);
        return (file.exists() && file.isFile() ? file.length() : -1);
    }

    /**
     * 压缩文件
     * @param sourceFile 源文件路径(绝对路径)
     * @param destFile 目标文件路径(绝对路径)
     * @return 压缩成功返回true
     * @throws IOException
     */
    public static boolean zipFile(String sourceFile, String destFile) throws IOException {
        if (!isFileExist(sourceFile))
            return false;
        File zip = new File(destFile);
        if (zip.exists())
            zip.delete();

        FileInputStream fis = null;
        FileOutputStream outputStream = null;
        ZipOutputStream zipOut = null;
        try {
            File srcFile = new File(sourceFile);
            fis = new FileInputStream(srcFile);
            outputStream = new FileOutputStream(zip);
            zipOut = new ZipOutputStream(outputStream);

            ZipEntry ze = new ZipEntry(srcFile.getName());
            ze.setSize(srcFile.length());
            ze.setTime(srcFile.lastModified());
            zipOut.putNextEntry(ze);

            byte data[] = new byte[1024];
            int length;
            while ((length = fis.read(data)) != -1) {
                zipOut.write(data, 0, length);
            }
            zipOut.flush();
            return true;
        } finally {
            IOUtils.close(zipOut);
            IOUtils.close(outputStream);
            IOUtils.close(fis);
        }
    }
    public static boolean copyAssetsFile(Context context, String sourceFile, String destFile) throws IOException {
        InputStream inputStream=null;
        OutputStream outputStream=null;
        try {
            outputStream = new FileOutputStream(destFile);
            inputStream = context.getAssets().open(sourceFile);
            byte[] buffer = new byte[1024];
            int length = inputStream.read(buffer);
            while (length > 0) {
                outputStream.write(buffer, 0, length);
                length = inputStream.read(buffer);
            }
            return true;
        }finally {
            if(outputStream!=null) {
                outputStream.flush();
            }
            IOUtils.close(inputStream);
            IOUtils.close(outputStream);
        }
    }

    private static final String LINE_SEP = System.getProperty("line.separator");

    /**
     * 根据文件路径获取文件
     *
     * @param filePath The path of file.
     * @return 文件
     */
    public static File getFileByPath(final String filePath) {
        return isSpace(filePath) ? null : new File(filePath);
    }

    /**
     * 判断文件是否存在
     *
     * @param filePath The path of file.
     * @return {@code true}: 存在<br>{@code false}: 不存在
     */
    public static boolean isFileExists(final String filePath) {
        return isFileExists(getFileByPath(filePath));
    }

    /**
     * 判断文件是否存在
     *
     * @param file The file.
     * @return {@code true}: 存在<br>{@code false}: 不存在
     */
    public static boolean isFileExists(final File file) {
        return file != null && file.exists();
    }

    /**
     * 重命名文件
     *
     * @param filePath The path of file.
     * @param newName  新名称
     * @return {@code true}: 重命名成功<br>{@code false}: 重命名失败
     */
    public static boolean rename(final String filePath, final String newName) {
        return rename(getFileByPath(filePath), newName);
    }

    /**
     * 重命名文件
     *
     * @param file The file.
     * @param newName The new name of file.
     * @return {@code true}: 重命名成功<br>{@code false}: 重命名失败
     */
    public static boolean rename(final File file, final String newName) {
        // 文件为空返回 false
        if (file == null) return false;
        // 文件不存在返回 false
        if (!file.exists()) return false;
        // 新的文件名为空返回 false
        if (isSpace(newName)) return false;
        // 如果文件名没有改变返回 true
        if (newName.equals(file.getName())) return true;
        File newFile = new File(file.getParent() + File.separator + newName);
        // 如果重命名的文件已存在返回 false
        return !newFile.exists()
                && file.renameTo(newFile);
    }

    /**
     * 判断是否是目录
     *
     * @param dirPath 目录路径
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isDir(final String dirPath) {
        return isDir(getFileByPath(dirPath));
    }

    /**
     * 判断是否是目录
     *
     * @param file The file.
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isDir(final File file) {
        return file != null && file.exists() && file.isDirectory();
    }

    /**
     * 判断是否是文件
     *
     * @param filePath The path of file.
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isFile(final String filePath) {
        return isFile(getFileByPath(filePath));
    }

    /**
     * 判断是否是文件
     *
     * @param file The file.
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isFile(final File file) {
        return file != null && file.exists() && file.isFile();
    }

    /**
     * 判断目录是否存在，不存在则判断是否创建成功
     *
     * @param dirPath 目录路径
     * @return {@code true}: 存在或创建成功<br>{@code false}: 不存在或创建失败
     */
    public static boolean createOrExistsDir(final String dirPath) {
        return createOrExistsDir(getFileByPath(dirPath));
    }

    /**
     * 判断目录是否存在，不存在则判断是否创建成功
     *
     * @param file The file.
     * @return {@code true}: 存在或创建成功<br>{@code false}: 不存在或创建失败
     */
    public static boolean createOrExistsDir(final File file) {
        // 如果存在，是目录则返回 true，是文件则返回 false，不存在则返回是否创建成功
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * 判断文件是否存在，不存在则判断是否创建成功
     *
     * @param filePath The path of file.
     * @return {@code true}: 存在或创建成功<br>{@code false}: 不存在或创建失败
     */
    public static boolean createOrExistsFile(final String filePath) {
        return createOrExistsFile(getFileByPath(filePath));
    }

    /**
     * 判断文件是否存在，不存在则判断是否创建成功
     *
     * @param file The file.
     * @return {@code true}: 存在或创建成功<br>{@code false}: 不存在或创建失败
     */
    public static boolean createOrExistsFile(final File file) {
        if (file == null) return false;
        // 如果存在，是文件则返回 true，是目录则返回 false
        if (file.exists()) return file.isFile();
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断文件是否存在，存在则在创建之前删除
     *
     * @param filePath The path of file.
     * @return {@code true}: 创建成功<br>{@code false}: 创建失败
     */
    public static boolean createFileByDeleteOldFile(final String filePath) {
        return createFileByDeleteOldFile(getFileByPath(filePath));
    }

    /**
     * 判断文件是否存在，存在则在创建之前删除
     *
     * @param file The file.
     * @return {@code true}: 创建成功<br>{@code false}: 创建失败
     */
    public static boolean createFileByDeleteOldFile(final File file) {
        if (file == null) return false;
        // 文件存在并且删除失败返回 false
        if (file.exists() && !file.delete()) return false;
        // 创建目录失败返回 false
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 复制或移动目录
     *
     * @param srcDirPath  源目录路径
     * @param destDirPath 目标目录路径
     * @param listener    是否覆盖监听器
     * @param isMove      是否移动
     * @return {@code true}: 复制或移动成功<br>{@code false}: 复制或移动失败
     */
    private static boolean copyOrMoveDir(final String srcDirPath,
                                         final String destDirPath,
                                         final FileUtils.OnReplaceListener listener,
                                         final boolean isMove) {
        return copyOrMoveDir(getFileByPath(srcDirPath),
                getFileByPath(destDirPath),
                listener,
                isMove
        );
    }

    /**
     * 复制或移动目录
     *
     * @param srcDir   源目录
     * @param destDir  目标目录
     * @param listener 是否覆盖监听器
     * @param isMove   是否移动
     * @return {@code true}: 复制或移动成功<br>{@code false}: 复制或移动失败
     */
    private static boolean copyOrMoveDir(final File srcDir,
                                         final File destDir,
                                         final FileUtils.OnReplaceListener listener,
                                         final boolean isMove) {
        if (srcDir == null || destDir == null) return false;
        // 如果目标目录在源目录中则返回 false，看不懂的话好好想想递归怎么结束
        // srcPath : F:\\MyGithub\\AndroidUtilCode\\utilcode\\src\\test\\res
        // destPath: F:\\MyGithub\\AndroidUtilCode\\utilcode\\src\\test\\res1
        // 为防止以上这种情况出现出现误判，须分别在后面加个路径分隔符
        String srcPath = srcDir.getPath() + File.separator;
        String destPath = destDir.getPath() + File.separator;
        if (destPath.contains(srcPath)) return false;
        // 源文件不存在或者不是目录则返回 false
        if (!srcDir.exists() || !srcDir.isDirectory()) return false;
        if (destDir.exists()) {
            if (listener.onReplace()) {// 需要覆盖则删除旧目录
                if (!deleteAllInDir(destDir)) {// 删除文件失败的话返回 false
                    return false;
                }
            } else {// 不需要覆盖直接返回即可 true
                return true;
            }
        }
        // 目标目录不存在返回 false
        if (!createOrExistsDir(destDir)) return false;
        File[] files = srcDir.listFiles();
        for (File file : files) {
            File oneDestFile = new File(destPath + file.getName());
            if (file.isFile()) {
                // 如果操作失败返回 false
                if (!copyOrMoveFile(file, oneDestFile, listener, isMove)) return false;
            } else if (file.isDirectory()) {
                // 如果操作失败返回 false
                if (!copyOrMoveDir(file, oneDestFile, listener, isMove)) return false;
            }
        }
        return !isMove || deleteDir(srcDir);
    }

    /**
     * 复制或移动文件
     *
     * @param srcFilePath  源文件路径
     * @param destFilePath 目标文件路径
     * @param listener     是否覆盖监听器
     * @param isMove       是否移动
     * @return {@code true}: 复制或移动成功<br>{@code false}: 复制或移动失败
     */
    private static boolean copyOrMoveFile(final String srcFilePath,
                                          final String destFilePath,
                                          final FileUtils.OnReplaceListener listener,
                                          final boolean isMove) {
        return copyOrMoveFile(getFileByPath(srcFilePath),
                getFileByPath(destFilePath),
                listener,
                isMove
        );
    }

    /**
     * 复制或移动文件
     *
     * @param srcFile  源文件
     * @param destFile 目标文件
     * @param listener 是否覆盖监听器
     * @param isMove   是否移动
     * @return {@code true}: 复制或移动成功<br>{@code false}: 复制或移动失败
     */
    private static boolean copyOrMoveFile(final File srcFile,
                                          final File destFile,
                                          final FileUtils.OnReplaceListener listener,
                                          final boolean isMove) {
        if (srcFile == null || destFile == null) return false;
        // 如果源文件和目标文件相同则返回 false
        if (srcFile.equals(destFile)) return false;
        // 源文件不存在或者不是文件则返回 false
        if (!srcFile.exists() || !srcFile.isFile()) return false;
        if (destFile.exists()) {// 目标文件存在
            if (listener.onReplace()) {// 需要覆盖则删除旧文件
                if (!destFile.delete()) {// 删除文件失败的话返回 false
                    return false;
                }
            } else {// 不需要覆盖直接返回即可 true
                return true;
            }
        }
        // 目标目录不存在返回 false
        if (!createOrExistsDir(destFile.getParentFile())) return false;
        try {
            return IOUtils.writeFileFromIS(destFile, new FileInputStream(srcFile), false)
                    && !(isMove && !deleteFile(srcFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 复制目录
     *
     * @param srcDirPath  源目录路径
     * @param destDirPath 目标目录路径
     * @param listener    是否覆盖监听器
     * @return {@code true}: 复制成功<br>{@code false}: 复制失败
     */
    public static boolean copyDir(final String srcDirPath,
                                  final String destDirPath,
                                  final FileUtils.OnReplaceListener listener) {
        return copyDir(getFileByPath(srcDirPath), getFileByPath(destDirPath), listener);
    }

    /**
     * 复制目录
     *
     * @param srcDir   源目录
     * @param destDir  目标目录
     * @param listener 是否覆盖监听器
     * @return {@code true}: 复制成功<br>{@code false}: 复制失败
     */
    public static boolean copyDir(final File srcDir,
                                  final File destDir,
                                  final FileUtils.OnReplaceListener listener) {
        return copyOrMoveDir(srcDir, destDir, listener, false);
    }

    /**
     * 复制文件
     *
     * @param srcFilePath  源文件路径
     * @param destFilePath 目标文件路径
     * @param listener     是否覆盖监听器
     * @return {@code true}: 复制成功<br>{@code false}: 复制失败
     */
    public static boolean copyFile(final String srcFilePath,
                                   final String destFilePath,
                                   final FileUtils.OnReplaceListener listener) {
        return copyFile(getFileByPath(srcFilePath), getFileByPath(destFilePath), listener);
    }

    /**
     * 复制文件
     *
     * @param srcFile  源文件
     * @param destFile 目标文件
     * @param listener 是否覆盖监听器
     * @return {@code true}: 复制成功<br>{@code false}: 复制失败
     */
    public static boolean copyFile(final File srcFile,
                                   final File destFile,
                                   final FileUtils.OnReplaceListener listener) {
        return copyOrMoveFile(srcFile, destFile, listener, false);
    }

    /**
     * 移动目录
     *
     * @param srcDirPath  源目录路径
     * @param destDirPath 目标目录路径
     * @param listener    是否覆盖监听器
     * @return {@code true}: 移动成功<br>{@code false}: 移动失败
     */
    public static boolean moveDir(final String srcDirPath,
                                  final String destDirPath,
                                  final FileUtils.OnReplaceListener listener) {
        return moveDir(getFileByPath(srcDirPath), getFileByPath(destDirPath), listener);
    }

    /**
     * 移动目录
     *
     * @param srcDir   源目录
     * @param destDir  目标目录
     * @param listener 是否覆盖监听器
     * @return {@code true}: 移动成功<br>{@code false}: 移动失败
     */
    public static boolean moveDir(final File srcDir,
                                  final File destDir,
                                  final FileUtils.OnReplaceListener listener) {
        return copyOrMoveDir(srcDir, destDir, listener, true);
    }

    /**
     * 移动文件
     *
     * @param srcFilePath  源文件路径
     * @param destFilePath 目标文件路径
     * @param listener     是否覆盖监听器
     * @return {@code true}: 移动成功<br>{@code false}: 移动失败
     */
    public static boolean moveFile(final String srcFilePath,
                                   final String destFilePath,
                                   final FileUtils.OnReplaceListener listener) {
        return moveFile(getFileByPath(srcFilePath), getFileByPath(destFilePath), listener);
    }

    /**
     * 移动文件
     *
     * @param srcFile  源文件
     * @param destFile 目标文件
     * @param listener 是否覆盖监听器
     * @return {@code true}: 移动成功<br>{@code false}: 移动失败
     */
    public static boolean moveFile(final File srcFile,
                                   final File destFile,
                                   final FileUtils.OnReplaceListener listener) {
        return copyOrMoveFile(srcFile, destFile, listener, true);
    }

    /**
     * 删除目录
     *
     * @param dirPath 目录路径
     * @return {@code true}: 删除成功<br>{@code false}: 删除失败
     */
    public static boolean deleteDir(final String dirPath) {
        return deleteDir(getFileByPath(dirPath));
    }

    /**
     * 删除目录
     *
     * @param dir 目录
     * @return {@code true}: 删除成功<br>{@code false}: 删除失败
     */
    public static boolean deleteDir(final File dir) {
        if (dir == null) return false;
        // dir doesn't exist then return true
        if (!dir.exists()) return true;
        // dir isn't a directory then return false
        if (!dir.isDirectory()) return false;
        File[] files = dir.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                if (file.isFile()) {
                    if (!file.delete()) return false;
                } else if (file.isDirectory()) {
                    if (!deleteDir(file)) return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * 删除目录下所有东西
     *
     * @param dirPath 目录路径
     * @return {@code true}: 删除成功<br>{@code false}: 删除失败
     */
    public static boolean deleteAllInDir(final String dirPath) {
        return deleteAllInDir(getFileByPath(dirPath));
    }

    /**
     * 删除目录下所有东西
     *
     * @param dir 目录
     * @return {@code true}: 删除成功<br>{@code false}: 删除失败
     */
    public static boolean deleteAllInDir(final File dir) {
        return deleteFilesInDirWithFilter(dir, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return true;
            }
        });
    }

    /**
     * 删除目录下所有文件
     *
     * @param dirPath 目录路径
     * @return {@code true}: 删除成功<br>{@code false}: 删除失败
     */
    public static boolean deleteFilesInDir(final String dirPath) {
        return deleteFilesInDir(getFileByPath(dirPath));
    }

    /**
     * 删除目录下所有文件
     *
     * @param dir 目录
     * @return {@code true}: 删除成功<br>{@code false}: 删除失败
     */
    public static boolean deleteFilesInDir(final File dir) {
        return deleteFilesInDirWithFilter(dir, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
    }

    /**
     * 删除目录下所有过滤的文件
     *
     * @param dirPath 目录路径
     * @param filter  过滤器
     * @return {@code true}: 删除成功<br>{@code false}: 删除失败
     */
    public static boolean deleteFilesInDirWithFilter(final String dirPath,
                                                     final FileFilter filter) {
        return deleteFilesInDirWithFilter(getFileByPath(dirPath), filter);
    }

    /**
     * 删除目录下所有过滤的文件
     *
     * @param dir    目录
     * @param filter 过滤器
     * @return {@code true}: 删除成功<br>{@code false}: 删除失败
     */
    public static boolean deleteFilesInDirWithFilter(final File dir, final FileFilter filter) {
        if (dir == null) return false;
        // dir doesn't exist then return true
        if (!dir.exists()) return true;
        // dir isn't a directory then return false
        if (!dir.isDirectory()) return false;
        File[] files = dir.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                if (filter.accept(file)) {
                    if (file.isFile()) {
                        if (!file.delete()) return false;
                    } else if (file.isDirectory()) {
                        if (!deleteDir(file)) return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 获取目录下所有文件
     * <p>不递归进子目录</p>
     *
     * @param dirPath 目录路径
     * @return 文件链表
     */
    public static List<File> listFilesInDir(final String dirPath) {
        return listFilesInDir(dirPath, false);
    }

    /**
     * 获取目录下所有文件
     * <p>不递归进子目录</p>
     *
     * @param dir 目录
     * @return 文件链表
     */
    public static List<File> listFilesInDir(final File dir) {
        return listFilesInDir(dir, false);
    }

    /**
     * 获取目录下所有文件
     *
     * @param dirPath     目录路径
     * @param isRecursive 是否递归进子目录
     * @return 文件链表
     */
    public static List<File> listFilesInDir(final String dirPath, final boolean isRecursive) {
        return listFilesInDir(getFileByPath(dirPath), isRecursive);
    }

    /**
     * 获取目录下所有文件
     *
     * @param dir         目录
     * @param isRecursive 是否递归进子目录
     * @return 文件链表
     */
    public static List<File> listFilesInDir(final File dir, final boolean isRecursive) {
        return listFilesInDirWithFilter(dir, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return true;
            }
        }, isRecursive);
    }

    /**
     * 获取目录下所有过滤的文件
     * <p>不递归进子目录</p>
     *
     * @param dirPath 目录路径
     * @param filter  过滤器
     * @return 文件链表
     */
    public static List<File> listFilesInDirWithFilter(final String dirPath,
                                                      final FileFilter filter) {
        return listFilesInDirWithFilter(getFileByPath(dirPath), filter, false);
    }

    /**
     * 获取目录下所有过滤的文件
     * <p>不递归进子目录</p>
     *
     * @param dir    目录
     * @param filter 过滤器
     * @return 文件链表
     */
    public static List<File> listFilesInDirWithFilter(final File dir,
                                                      final FileFilter filter) {
        return listFilesInDirWithFilter(dir, filter, false);
    }

    /**
     * 获取目录下所有过滤的文件
     *
     * @param dirPath     目录路径
     * @param filter      过滤器
     * @param isRecursive 是否递归进子目录
     * @return 文件链表
     */
    public static List<File> listFilesInDirWithFilter(final String dirPath,
                                                      final FileFilter filter,
                                                      final boolean isRecursive) {
        return listFilesInDirWithFilter(getFileByPath(dirPath), filter, isRecursive);
    }

    /**
     * 获取目录下所有过滤的文件
     *
     * @param dir         目录
     * @param filter      过滤器
     * @param isRecursive 是否递归进子目录
     * @return 文件链表
     */
    public static List<File> listFilesInDirWithFilter(final File dir,
                                                      final FileFilter filter,
                                                      final boolean isRecursive) {
        if (!isDir(dir)) return null;
        List<File> list = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                if (filter.accept(file)) {
                    list.add(file);
                }
                if (isRecursive && file.isDirectory()) {
                    //noinspection ConstantConditions
                    list.addAll(listFilesInDirWithFilter(file, filter, true));
                }
            }
        }
        return list;
    }

    /**
     * 获取文件最后修改的毫秒时间戳
     *
     * @param filePath The path of file.
     * @return 文件最后修改的毫秒时间戳
     */

    public static long getFileLastModified(final String filePath) {
        return getFileLastModified(getFileByPath(filePath));
    }

    /**
     * 获取文件最后修改的毫秒时间戳
     *
     * @param file The file.
     * @return 文件最后修改的毫秒时间戳
     */
    public static long getFileLastModified(final File file) {
        if (file == null) return -1;
        return file.lastModified();
    }

    /**
     * 简单获取文件编码格式
     *
     * @param filePath The path of file.
     * @return 文件编码
     */
    public static String getFileCharsetSimple(final String filePath) {
        return getFileCharsetSimple(getFileByPath(filePath));
    }

    /**
     * 简单获取文件编码格式
     *
     * @param file The file.
     * @return 文件编码
     */
    public static String getFileCharsetSimple(final File file) {
        int p = 0;
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            p = (is.read() << 8) + is.read();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtils.closeIO(is);
        }
        switch (p) {
            case 0xefbb:
                return "UTF-8";
            case 0xfffe:
                return "Unicode";
            case 0xfeff:
                return "UTF-16BE";
            default:
                return "GBK";
        }
    }

    /**
     * 获取文件行数
     *
     * @param filePath The path of file.
     * @return 文件行数
     */
    public static int getFileLines(final String filePath) {
        return getFileLines(getFileByPath(filePath));
    }

    /**
     * 获取文件行数
     * <p>比 readLine 要快很多</p>
     *
     * @param file The file.
     * @return 文件行数
     */
    public static int getFileLines(final File file) {
        int count = 1;
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[1024];
            int readChars;
            if (LINE_SEP.endsWith("\n")) {
                while ((readChars = is.read(buffer, 0, 1024)) != -1) {
                    for (int i = 0; i < readChars; ++i) {
                        if (buffer[i] == '\n') ++count;
                    }
                }
            } else {
                while ((readChars = is.read(buffer, 0, 1024)) != -1) {
                    for (int i = 0; i < readChars; ++i) {
                        if (buffer[i] == '\r') ++count;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtils.closeIO(is);
        }
        return count;
    }

    /**
     * 获取目录大小
     *
     * @param dirPath 目录路径
     * @return 文件大小
     */
    public static String getDirSize(final String dirPath) {
        return getDirSize(getFileByPath(dirPath));
    }

    /**
     * 获取目录大小
     *
     * @param dir 目录
     * @return 文件大小
     */
    public static String getDirSize(final File dir) {
        long len = getDirLength(dir);
        return len == -1 ? "" : byte2FitMemorySize(len);
    }

    /**
     * 获取目录长度
     *
     * @param dirPath 目录路径
     * @return 目录长度
     */
    public static long getDirLength(final String dirPath) {
        return getDirLength(getFileByPath(dirPath));
    }

    /**
     * 获取目录长度
     *
     * @param dir 目录
     * @return 目录长度
     */
    public static long getDirLength(final File dir) {
        if (!isDir(dir)) return -1;
        long len = 0;
        File[] files = dir.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                if (file.isDirectory()) {
                    len += getDirLength(file);
                } else {
                    len += file.length();
                }
            }
        }
        return len;
    }

    /**
     * 获取文件长度
     *
     * @param filePath The path of file.
     * @return 文件长度
     */
    public static long getFileLength(final String filePath) {
        boolean isURL = filePath.matches("[a-zA-z]+://[^\\s]*");
        if (isURL) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(filePath).openConnection();
                conn.setRequestProperty("Accept-Encoding", "identity");
                conn.connect();
                if (conn.getResponseCode() == 200) {
                    return conn.getContentLength();
                }
                return -1;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return getFileLength(getFileByPath(filePath));
    }

    /**
     * 获取文件长度
     *
     * @param file The file.
     * @return 文件长度
     */
    public static long getFileLength(final File file) {
        if (!isFile(file)) return -1;
        return file.length();
    }

    /**
     * 获取文件的 MD5 校验码
     *
     * @param filePath The path of file.
     * @return 文件的 MD5 校验码
     */
    public static String getFileMD5ToString(final String filePath) {
        File file = isSpace(filePath) ? null : new File(filePath);
        return getFileMD5ToString(file);
    }

    /**
     * 获取文件的 MD5 校验码
     *
     * @param file The file.
     * @return 文件的 MD5 校验码
     */
    public static String getFileMD5ToString(final File file) {
        return bytes2HexString(getFileMD5(file));
    }

    /**
     * 获取文件的 MD5 校验码
     *
     * @param filePath The path of file.
     * @return 文件的 MD5 校验码
     */
    public static byte[] getFileMD5(final String filePath) {
        return getFileMD5(getFileByPath(filePath));
    }

    /**
     * 获取文件的 MD5 校验码
     *
     * @param file The file.
     * @return 文件的 MD5 校验码
     */
    public static byte[] getFileMD5(final File file) {
        if (file == null) return null;
        DigestInputStream dis = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            MessageDigest md = MessageDigest.getInstance("MD5");
            dis = new DigestInputStream(fis, md);
            byte[] buffer = new byte[1024 * 256];
            while (true) {
                if (!(dis.read(buffer) > 0)) break;
            }
            md = dis.getMessageDigest();
            return md.digest();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtils.closeIO(dis);
        }
        return null;
    }

    /**
     * 获取全路径中的最长目录
     *
     * @param file The file.
     * @return filePath 最长目录
     */
    public static String getDirName(final File file) {
        if (file == null) return null;
        return getDirName(file.getPath());
    }

    /**
     * 获取全路径中的最长目录
     *
     * @param filePath The path of file.
     * @return filePath 最长目录
     */
    public static String getDirName(final String filePath) {
        if (isSpace(filePath)) return filePath;
        int lastSep = filePath.lastIndexOf(File.separator);
        return lastSep == -1 ? "" : filePath.substring(0, lastSep + 1);
    }

    /**
     * 获取全路径中的文件名
     *
     * @param file The file.
     * @return 文件名
     */
    public static String getFileName(final File file) {
        if (file == null) return null;
        return getFileName(file.getPath());
    }

    /**
     * 获取全路径中的文件名
     *
     * @param filePath The path of file.
     * @return 文件名
     */
    public static String getFileName(final String filePath) {
        if (isSpace(filePath)) return filePath;
        int lastSep = filePath.lastIndexOf(File.separator);
        return lastSep == -1 ? filePath : filePath.substring(lastSep + 1);
    }

    /**
     * 获取全路径中的不带拓展名的文件名
     *
     * @param file The file.
     * @return 不带拓展名的文件名
     */
    public static String getFileNameNoExtension(final File file) {
        if (file == null) return null;
        return getFileNameNoExtension(file.getPath());
    }

    /**
     * 获取全路径中的不带拓展名的文件名
     *
     * @param filePath The path of file.
     * @return 不带拓展名的文件名
     */
    public static String getFileNameNoExtension(final String filePath) {
        if (isSpace(filePath)) return filePath;
        int lastPoi = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);
        if (lastSep == -1) {
            return (lastPoi == -1 ? filePath : filePath.substring(0, lastPoi));
        }
        if (lastPoi == -1 || lastSep > lastPoi) {
            return filePath.substring(lastSep + 1);
        }
        return filePath.substring(lastSep + 1, lastPoi);
    }

    ///////////////////////////////////////////////////////////////////////////
    // copy from ConvertUtils
    ///////////////////////////////////////////////////////////////////////////

    private static final char hexDigits[] =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static String bytes2HexString(final byte[] bytes) {
        if (bytes == null) return null;
        int len = bytes.length;
        if (len <= 0) return null;
        char[] ret = new char[len << 1];
        for (int i = 0, j = 0; i < len; i++) {
            ret[j++] = hexDigits[bytes[i] >>> 4 & 0x0f];
            ret[j++] = hexDigits[bytes[i] & 0x0f];
        }
        return new String(ret);
    }

    @SuppressLint("DefaultLocale")
    private static String byte2FitMemorySize(final long byteNum) {
        if (byteNum < 0) {
            return "shouldn't be less than zero!";
        } else if (byteNum < 1024) {
            return String.format("%.3fB", (double) byteNum);
        } else if (byteNum < 1048576) {
            return String.format("%.3fKB", (double) byteNum / 1024);
        } else if (byteNum < 1073741824) {
            return String.format("%.3fMB", (double) byteNum / 1048576);
        } else {
            return String.format("%.3fGB", (double) byteNum / 1073741824);
        }
    }

    private static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public interface OnReplaceListener {
        boolean onReplace();
    }
}
