package edu.hawaii.its.filedrop.repository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileSet;
import edu.hawaii.its.filedrop.util.Strings;

import static edu.hawaii.its.filedrop.repository.specification.FileDropSpecification.withDownloadKey;
import static edu.hawaii.its.filedrop.repository.specification.FileDropSpecification.withEncryptionKey;
import static edu.hawaii.its.filedrop.repository.specification.FileDropSpecification.withFileSetId;
import static edu.hawaii.its.filedrop.repository.specification.FileDropSpecification.withId;
import static edu.hawaii.its.filedrop.repository.specification.FileDropSpecification.withUploadKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class FileDropRepositoryTest {

    @Autowired
    private FileDropRepository fileDropRepository;

    @Autowired
    private FileSetRepository fileSetRepository;

    @Test
    public void specificationWithIdTest() {
        LocalDateTime created = LocalDateTime.now();
        LocalDateTime expiration = created.plus(10, ChronoUnit.DAYS);
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

        Optional<FileDrop> foundFileDrop = fileDropRepository.findOne(withId(1));

        assertTrue(foundFileDrop.isPresent());
        assertEquals("test", foundFileDrop.get().getUploader());
        assertEquals(1L, (long) foundFileDrop.get().getId());
        assertEquals("Test 123", foundFileDrop.get().getUploaderFullName());
        assertEquals("tester", foundFileDrop.get().getRecipient());
        assertEquals("download-key", foundFileDrop.get().getDownloadKey());
        assertEquals("upload-key", foundFileDrop.get().getUploadKey());
        assertEquals("enc-key", foundFileDrop.get().getEncryptionKey());
        assertFalse(foundFileDrop.get().isAuthenticationRequired());
        assertFalse(foundFileDrop.get().isValid());
        assertEquals(created, foundFileDrop.get().getCreated());
        assertEquals(expiration, foundFileDrop.get().getExpiration());
    }

    @Test
    public void testRandomDownloadKey() {
        String downloadKey = Strings.generateRandomString();
        FileDrop fileDrop = new FileDrop();
        fileDrop.setId(1);
        fileDrop.setUploader("test");
        fileDrop.setUploaderFullName("Test 123");
        fileDrop.setCreated(LocalDateTime.now());
        fileDrop.setExpiration(LocalDateTime.now().plus(10, ChronoUnit.DAYS));
        fileDrop.setDownloadKey(downloadKey);
        fileDrop.setUploadKey("upload-key");
        fileDrop.setEncryptionKey("encrypted");
        fileDrop.setAuthenticationRequired(false);
        fileDrop.setValid(false);

        fileDropRepository.save(fileDrop);

        Optional<FileDrop> foundFileDrop = fileDropRepository.findOne(withDownloadKey(downloadKey));

        assertNotNull(foundFileDrop);
    }

    @Test
    public void specificationWithKeyTest() {
        FileDrop fileDrop = new FileDrop();
        fileDrop.setId(1);
        fileDrop.setUploader("test");
        fileDrop.setUploaderFullName("Test 123");
        fileDrop.setCreated(LocalDateTime.now());
        fileDrop.setExpiration(LocalDateTime.now().plus(10, ChronoUnit.DAYS));
        fileDrop.setDownloadKey("download-key");
        fileDrop.setUploadKey("upload-key");
        fileDrop.setEncryptionKey("encrypted");
        fileDrop.setAuthenticationRequired(false);
        fileDrop.setValid(false);

        fileDropRepository.save(fileDrop);

        FileDrop fileDrop2 = new FileDrop();
        fileDrop2.setId(2);
        fileDrop2.setUploader("test");
        fileDrop2.setUploaderFullName("Test 123");
        fileDrop2.setCreated(LocalDateTime.now());
        fileDrop2.setExpiration(LocalDateTime.now().plus(10, ChronoUnit.DAYS));
        fileDrop2.setDownloadKey("download-key2");
        fileDrop2.setUploadKey("upload-key2");
        fileDrop2.setEncryptionKey("encrypted2");
        fileDrop2.setAuthenticationRequired(false);
        fileDrop2.setValid(false);

        fileDropRepository.save(fileDrop2);

        Optional<FileDrop> foundFileDrop = fileDropRepository.findOne(withDownloadKey("download-key"));

        assertTrue(foundFileDrop.isPresent());
        assertEquals("test", fileDrop.getUploader());

        foundFileDrop = fileDropRepository.findOne(withUploadKey("upload-key"));

        assertTrue(foundFileDrop.isPresent());
        assertEquals("test", fileDrop.getUploader());

        foundFileDrop = fileDropRepository.findOne(withEncryptionKey("encrypted"));

        assertTrue(foundFileDrop.isPresent());
        assertEquals("test", fileDrop.getUploader());

        List<FileDrop> foundFileDrops = fileDropRepository.findAll(withDownloadKey("download-key2"));

        assertEquals(1, foundFileDrops.size());
    }

    @Test
    public void fileSetTest() {
        FileDrop fileDrop = new FileDrop();
        fileDrop.setId(1);
        fileDrop.setUploader("test");
        fileDrop.setUploaderFullName("Test 123");
        fileDrop.setCreated(LocalDateTime.now());
        fileDrop.setExpiration(LocalDateTime.now().plus(10, ChronoUnit.DAYS));
        fileDrop.setDownloadKey("download-key");
        fileDrop.setUploadKey("upload-key");
        fileDrop.setEncryptionKey("encrypted");
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

        assertEquals(fileDrop, fileSet.getFileDrop());
        assertEquals("image/png", fileSet.getType());
        assertEquals("test fileset", fileSet.getComment());

        Set<FileSet> fileSets = new HashSet<>();
        fileSets.add(fileSet);

        fileDrop.setFileSet(fileSets);

        fileDropRepository.save(fileDrop);

        assertEquals(1, fileDrop.getFileSet().size());

        assertTrue(fileDrop.getFileSet().stream().anyMatch(f -> f.getFileName().equalsIgnoreCase("Test image.png")));

        fileSet.setFileName("another image.png");

        fileSetRepository.save(fileSet);

        assertFalse(fileDrop.getFileSet().stream().anyMatch(f -> f.getFileName().equalsIgnoreCase("Test image.png")));

        assertTrue(fileDrop.getFileSet().stream().anyMatch(f -> f.getFileName().equalsIgnoreCase("another image.png")));
    }

    @Test
    public void findFileDropFromFileSet() {
        FileDrop fileDrop = new FileDrop();
        fileDrop.setId(1);
        fileDrop.setUploader("test");
        fileDrop.setUploaderFullName("Test 123");
        fileDrop.setCreated(LocalDateTime.now());
        fileDrop.setExpiration(LocalDateTime.now().plus(10, ChronoUnit.DAYS));
        fileDrop.setDownloadKey("download-key");
        fileDrop.setUploadKey("upload-key");
        fileDrop.setEncryptionKey("encrypted");
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

        Set<FileSet> fileSets = new HashSet<>();
        fileSets.add(fileSet);

        fileDrop.setFileSet(fileSets);

        fileDropRepository.save(fileDrop);

        List<FileDrop> fileDrops = fileDropRepository.findAll(withFileSetId(fileSet.getId()));

        assertEquals(1, fileDrops.size());
        assertEquals("test", fileDrops.get(0).getUploader());
    }

}
