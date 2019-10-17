package edu.hawaii.its.filedrop.service;

import java.io.File;
import java.io.IOException;
import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;


@Service
public class SpaceCheckService {

    private static final Log logger = LogFactory.getLog(SpaceCheckService.class);

    public static final long DEFAULT_RESERVED_SPACE = 1000000000;
    private File root;
    private long bytesFree;
    private long bytesUsed;

    @Value("${app.max.size}")
    private long maxUploadSize;

    // The number of bytes which we require to remain available in the filesystem.
    private long reservedSpace = DEFAULT_RESERVED_SPACE;

    @Value("${app.scheduler.spacecheck.interval}")
    private int interval;

    @PostConstruct
    public void init() {
        logger.info("init; root: " + root);
        Assert.notNull(root, "property 'root' is required");

        logger.info("init; root exists?: " + root.exists());

        logger.info("init; bytesFree: " + bytesFree);
        logger.info("init; bytesUsed: " + bytesUsed);

        logger.info("init; maxUploadSize: " + maxUploadSize);
        Assert.isTrue(maxUploadSize > 0, "property 'maxUploadSize' is required");

        logger.info("init; started.");
    }

    public synchronized void update() {
        logger.debug("starting free space update check");
        updateSpaceValues();
    }

    private void updateSpaceValues() {
        long free = root.getFreeSpace();
        long used = root.getTotalSpace() - root.getFreeSpace();

        if (logger.isDebugEnabled()) {
            logger.debug(">>> used space: " + used + "; free space: " + free);
        }

        if (free > 0) {
            this.bytesFree = free;
            this.bytesUsed = used;
        } else {
            throw new RuntimeException("failed to update free space count");
        }
    }

    public void setMaxUploadSize(long maxUploadSize) {
        if (maxUploadSize <= 0) {
            String msg = "property 'maxUploadSize' must be positive";
            throw new IllegalArgumentException(msg);
        } else if (maxUploadSize <= 1000) {
            this.maxUploadSize = maxUploadSize * 1000000;
        } else {
            this.maxUploadSize = maxUploadSize;
        }
    }

    public void setReservedSpace(long reservedSpace) {
        this.reservedSpace = reservedSpace;
    }

    public long getMaxUploadSize() {
        return maxUploadSize;
    }

    public boolean isFreeSpaceAvailable(long size) {
        long availableSpace = getBytesFree();
        return availableSpace > (size + reservedSpace);
    }

    public boolean isFreeSpaceAvailable() {
        return isFreeSpaceAvailable(maxUploadSize);
    }

    public File getRoot() {
        return root;
    }

    @Value("${app.storage-base}")
    public void setRoot(Resource resource) throws IOException {
        this.root = resource.getFile();
    }

    public synchronized long getBytesFree() {
        return bytesFree;
    }

    public void setBytesFree(long bytesFree) {
        this.bytesFree = bytesFree;
    }

    public synchronized long getBytesUsed() {
        return bytesUsed;
    }

    public void setBytesUsed(long bytesUsed) {
        this.bytesUsed = bytesUsed;
    }
}
