package edu.hawaii.its.filedrop.service;

import java.io.File;
import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;


@Service
public class SpaceCheckService {

    private static final Log logger = LogFactory.getLog(SpaceCheckService.class);

    @Value("app.scheduler.spacecheck.default-reserved-space")
    public static long DEFAULT_RESERVED_SPACE = 1000000000;
    private File root;
    private long bytesFree;
    private long bytesUsed;

    @Value("${app.max.size}")
    private long maxUploadSize;

    // The number of bytes which we require to remain available in the filesystem.
    private long reservedSpace = DEFAULT_RESERVED_SPACE;

    @Value("${app.scheduler.spacecheck.interval}")
    private int interval;

    @Autowired
    private StorageService storageService;

    @PostConstruct
    public void init() {
        logger.info("init; root: " + root);
        root = storageService.getRootLocation().toFile();
        Assert.notNull(root, "property 'root' is required");

        logger.info("init; root exists?: " + root.exists());

        logger.info("init; bytesFree: " + bytesFree);
        logger.info("init; bytesUsed: " + bytesUsed);

        logger.info("init; maxUploadSize: " + maxUploadSize);
        Assert.isTrue(maxUploadSize > 0, "property 'maxUploadSize' is required and must be positive");

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
        }
    }

    public void setMaxUploadSize(long maxUploadSize) {
        if (maxUploadSize <= 1000) {
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
