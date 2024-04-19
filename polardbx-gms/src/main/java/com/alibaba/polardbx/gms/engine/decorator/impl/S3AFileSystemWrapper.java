package com.alibaba.polardbx.gms.engine.decorator.impl;

import com.alibaba.polardbx.gms.engine.decorator.FileSystemWrapper;
import org.apache.hadoop.fs.s3a.S3AFileSystem;

public class S3AFileSystemWrapper extends S3AFileSystem implements FileSystemWrapper {
    @Override
    public Statistics getInnerStatistics() {
        return statistics;
    }
}
