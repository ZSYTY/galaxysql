package com.alibaba.polardbx.gms.engine.decorator;

import org.apache.hadoop.fs.FileSystem;

public interface FileSystemWrapper {
    FileSystem.Statistics getInnerStatistics();
}
