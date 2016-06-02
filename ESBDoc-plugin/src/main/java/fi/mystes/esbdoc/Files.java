package fi.mystes.esbdoc;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;

import java.io.*;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static fi.mystes.esbdoc.Constants.FILE_SEPARATOR;

/**
 * Created by mystes-am on 29.5.2015.
 */
public class Files {
    private static Log log = LogFactory.getLog(Files.class);

    private enum Type {
        CAR_FILE, SOAPUI_FILE;
    }

    public static File[] convertToFileHandles(String commaSeparatedListOfFilenames){
        String[] filenames = StringUtils.split(commaSeparatedListOfFilenames, FILE_SEPARATOR);

        List<File> fileList = new ArrayList<File>();
        for(String filename : filenames){
            fileList.add(new File(filename));
        }

        return fileList.toArray(new File[]{});
    }

    public static List<FileObject> getCarFileObjects(File[] files) throws FileSystemException {
        return getFileObjects(files, Type.CAR_FILE);
    }

    public static List<FileObject> getTestFileObjects(File[] folders) throws FileSystemException {
        File[] files = new File[0];
        for(File folder : folders){
            files = ArrayUtils.addAll(files, folder.listFiles());
        }
        return getFileObjects(files, Type.SOAPUI_FILE);
    }

    private static List<FileObject> getFileObjects(File[] files, Type type) throws FileSystemException {
        List<FileObject> fileObjects = new ArrayList<FileObject>(files.length);
        for (File file : files) {
            fileObjects.add(getFileObject(file.getAbsolutePath(), type));
        }
        return fileObjects;
    }

    private static FileObject getFileObject(String filename, Type type) throws FileSystemException {
        File file = new File(filename);
        if (file.exists()) {
            return getFileObject(file, type);
        }
        log.warn(MessageFormat.format("The specified file [{0}] does not exist.", filename));
        return null;
    }

    private static FileObject getFileObject(File file, Type type) throws FileSystemException {
        switch (type){
            case CAR_FILE: return resolveZipFile(file);
            default: return resolveNormalFile(file);
        }
    }

    private static FileObject resolveZipFile(File file) throws FileSystemException {
        return VFS.getManager().resolveFile("zip:" + file.getAbsolutePath());
    }

    private static FileObject resolveNormalFile(File file) throws FileSystemException {
        return VFS.getManager().resolveFile(file.getAbsolutePath());
    }

    public static boolean buildDirectoryPathFor(String filename){
        return new File(filename).getParentFile().mkdirs();
    }

    private static FileOutputStream textOutputFor(String filename) throws FileNotFoundException{
        return outputStreamFor(filename);
    }

    //TODO should be private
    public static FileOutputStream jsonOutputFor(String filename) throws FileNotFoundException{
        return outputStreamFor(filename);
    }

    private static FileOutputStream outputStreamFor(String filename) throws FileNotFoundException{
        return new FileOutputStream(new File(filename));
    }

    private static OutputStreamWriter utf8WriterFor(OutputStream stream){
        return new OutputStreamWriter(stream, Charset.forName("UTF-8"));
    }

    public static void writeTextTo(String filename, List<String> values) throws IOException {
        FileOutputStream textStream = textOutputFor(filename);
        writeTo(textStream, values);
        textStream.close();
    }

    public static void writeJsonTo(String filename, List<String> values) throws IOException {
        FileOutputStream jsonStream = jsonOutputFor(filename);
        writeTo(jsonStream, values);
        jsonStream.close();
    }

    private static void writeTo(OutputStream outputStream, List<String> values) throws IOException {
        OutputStreamWriter outputStreamWriter = utf8WriterFor(outputStream);
        writeTo(outputStreamWriter, values);
        outputStreamWriter.close();
    }

    private static void writeTo(OutputStreamWriter outputStreamWriter, List<String> values) throws IOException {
        for (String value : values) {
            outputStreamWriter.write(value);
            outputStreamWriter.write('\n');
        }
    }
}