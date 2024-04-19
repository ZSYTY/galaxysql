package com.alibaba.polardbx.gms.engine;

import com.alibaba.polardbx.common.Engine;
import com.alibaba.polardbx.common.oss.filesystem.FileSystemRateLimiter;
import com.alibaba.polardbx.common.oss.filesystem.GuavaFileSystemRateLimiter;
import com.alibaba.polardbx.common.oss.filesystem.OSSFileSystem;
import com.alibaba.polardbx.common.oss.filesystem.cache.CachingFileSystem;
import com.alibaba.polardbx.common.properties.ConnectionParams;
import com.alibaba.polardbx.common.properties.ConnectionProperties;
import com.alibaba.polardbx.common.properties.DynamicConfig;
import com.alibaba.polardbx.common.utils.GeneralUtil;
import com.alibaba.polardbx.common.utils.time.parser.StringNumericParser;
import com.alibaba.polardbx.gms.config.impl.InstConfUtil;
import com.alibaba.polardbx.gms.config.impl.MetaDbInstConfigManager;
import com.alibaba.polardbx.gms.topology.ServerInstIdManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.Seekable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;

public class FileSystemUtils {

    public static final String DELIMITER = "/";
    private static final File LOCAL_DIRECTORY = new File("/tmp/");

    public static String buildUri(FileSystem fileSystem, String fileName) {
        return String.join(DELIMITER, fileSystem.getWorkingDirectory().toString(), fileName);
    }

    public static Path buildPath(FileSystem fileSystem, String fileName) {
        return new Path(buildUri(fileSystem, fileName));
    }

    public static void writeFile(File localFile, String fileName, Engine engine)
        throws IOException {
        FileSystemGroup fileSystemGroup = FileSystemManager.getFileSystemGroup(engine);
        FileSystem fileSystem = fileSystemGroup.getMaster();
        fileSystemGroup.writeFile(localFile, buildPath(fileSystem, fileName));
    }

    public static boolean deleteIfExistsFile(String fileName, Engine engine) {
        try {
            FileSystemGroup fileSystemGroup = FileSystemManager.getFileSystemGroup(engine);
            return fileSystemGroup.delete(fileName, false);
        } catch (IOException e) {
            throw GeneralUtil.nestedException(e);
        }
    }

    public static void readFile(String fileName, OutputStream outputStream, Engine engine) {
        FileSystem fileSystem = FileSystemManager.getFileSystemGroup(engine).getMaster();
        try (InputStream in = fileSystem.open(buildPath(fileSystem, fileName))) {
            IOUtils.copy(in, outputStream);
        } catch (IOException e) {
            throw GeneralUtil.nestedException(e);
        }
    }

    public static boolean fileExists(String fileName, Engine engine) {
        try {
            FileSystem fileSystem = FileSystemManager.getFileSystemGroup(engine).getMaster();
            return fileSystem.exists(buildPath(fileSystem, fileName));
        } catch (IOException e) {
            throw GeneralUtil.nestedException(e);
        }
    }

    /**
     * Read fully file from engine.
     */
    public static byte[] readFullyFile(String fileName, Engine engine) {
        FileSystem fileSystem = FileSystemManager.getFileSystemGroup(engine).getMaster();
        try (InputStream in = fileSystem.open(buildPath(fileSystem, fileName))) {
            return IOUtils.toByteArray(in);
        } catch (IOException e) {
            throw GeneralUtil.nestedException(e);
        }
    }

    /**
     * Read file from the offset
     */
    public static void readFile(String fileName, int offset, int length, byte[] output, Engine engine) {
        FileSystem fileSystem = FileSystemManager.getFileSystemGroup(engine).getMaster();
        try (InputStream in = fileSystem.open(buildPath(fileSystem, fileName))) {
            ((Seekable) in).seek(offset);
            IOUtils.read(in, output, 0, length);
        } catch (IOException e) {
            throw GeneralUtil.nestedException(e);
        }
    }

    public static File createLocalFile(String fileName) {
        File localFile = new File(fileName);
        if (localFile.exists()) {
            localFile.delete();
        }
        return localFile;
    }

    public static GuavaFileSystemRateLimiter newRateLimiter() {
        // fetch rate params
        Map<String, Long> globalVariables = InstConfUtil.fetchLongConfigs(
            ConnectionParams.OSS_FS_MAX_READ_RATE,
            ConnectionParams.OSS_FS_MAX_WRITE_RATE
        );
        Long maxReadRate = Optional.ofNullable(globalVariables.get(ConnectionProperties.OSS_FS_MAX_READ_RATE))
            .orElse(StringNumericParser.simplyParseLong(ConnectionParams.OSS_FS_MAX_READ_RATE.getDefault()));
        Long maxWriteRate = Optional.ofNullable(globalVariables.get(ConnectionProperties.OSS_FS_MAX_WRITE_RATE))
            .orElse(StringNumericParser.simplyParseLong(ConnectionParams.OSS_FS_MAX_WRITE_RATE.getDefault()));

        return new GuavaFileSystemRateLimiter(
            maxReadRate == null ? -1 : maxReadRate,
            maxWriteRate == null ? -1 : maxWriteRate
        );
    }
}
