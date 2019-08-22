package edu.hawaii.its.filedrop.repository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class FileSetRepositoryTest {

    @Autowired
    private FileDropRepository fileDropRepository;

    @Autowired
    private FileSetRepository fileSetRepository;

    @Test
    public void findFileSetTest() {
        LocalDate created = LocalDate.now();
        LocalDate expiration = created.plus(10, ChronoUnit.DAYS);
        FileDrop fileDrop = new FileDrop();
        fileDrop.setId(1);
        fileDrop.setUploader("test");
        fileDrop.setUploaderFullName("Test 123");
        fileDrop.setRecipient("tester");
        fileDrop.setCreated(created);
        fileDrop.setExpiration(expiration);
        fileDrop.setDownloadKey("download-key");
        fileDrop.setUploadKey("upload-key");
        fileDrop.setEncryptionKey("enc-key");
        fileDrop.setAuthenticationRequired(false);
        fileDrop.setValid(false);

        fileDropRepository.save(fileDrop);

        FileSet fileSet = new FileSet();
        fileSet.setId(1);
        fileSet.setFileName("Test image.png");
        fileSet.setFileDrop(fileDrop);
        fileSet.setComment("test fileset");
        fileSet.setType("image/png");

        fileSetRepository.save(fileSet);

        FileSet foundFileSet = fileSetRepository.findById(fileSet.getId()).get();

        assertNotNull(foundFileSet);
        assertEquals(foundFileSet.getFileName(), "Test image.png");

        List<FileSet> fileSets = fileSetRepository.findAllByFileDrop(fileDrop);

        assertFalse(fileSets.isEmpty());
        assertEquals(1, fileSets.size());
        assertEquals(1, fileSets.get(0).getId().intValue());

        assertEquals(fileSet.toString(), foundFileSet.toString());

        fileSet.setFileName("test.txt");

        assertNotEquals(fileSet.toString(), foundFileSet.toString());

        fileSetRepository.save(fileSet);

        assertNotEquals(fileSet.toString(), foundFileSet.toString());

        FileSet foundFileSet2 = fileSetRepository.findById(fileSet.getId()).get();

        assertEquals(fileSet.toString(), foundFileSet2.toString());
    }

}
