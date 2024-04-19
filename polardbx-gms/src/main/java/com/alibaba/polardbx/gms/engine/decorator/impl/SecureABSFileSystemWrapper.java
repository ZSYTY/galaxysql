package com.alibaba.polardbx.gms.engine.decorator.impl;

import com.alibaba.polardbx.gms.engine.decorator.FileSystemWrapper;
import org.apache.hadoop.fs.azure.NativeAzureFileSystem;

public class SecureABSFileSystemWrapper extends NativeAzureFileSystem.Secure implements FileSystemWrapper {
    @Override
    public Statistics getInnerStatistics() {
        return statistics;
    }
}
