package com.alibaba.polardbx.common.oss.filesystem;

public interface RateLimitable {
    FileSystemRateLimiter getRateLimiter();
}
