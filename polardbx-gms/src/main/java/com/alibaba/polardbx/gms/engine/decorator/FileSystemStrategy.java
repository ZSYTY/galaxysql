package com.alibaba.polardbx.gms.engine.decorator;

import com.alibaba.polardbx.common.oss.filesystem.FileSystemRateLimiter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CreateFlag;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.util.Progressable;

import java.io.IOException;
import java.net.URI;
import java.util.EnumSet;

public interface FileSystemStrategy {
    FileSystemRateLimiter getRateLimiter();

    FSDataInputStream open(FileSystem fs, Path f, int bufferSize) throws IOException;

    FSDataOutputStream create(FileSystem fs, Path f, FsPermission permission, boolean overwrite, int bufferSize,
                              short replication, long blockSize, Progressable progress) throws IOException;

    FSDataOutputStream append(FileSystem fs, Path f, int bufferSize, Progressable progress) throws IOException;

    FSDataOutputStream createNonRecursive(FileSystem fs, Path p, FsPermission permission, EnumSet<CreateFlag> flags,
                                          int bufferSize, short replication, long blockSize,
                                          Progressable progress) throws IOException;

    FileStatus getFileStatus(FileSystem fs, Path path) throws IOException;
}
