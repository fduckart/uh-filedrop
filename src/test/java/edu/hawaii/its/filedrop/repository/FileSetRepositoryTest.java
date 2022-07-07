package edu.hawaii.its.filedrop.repository;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileSet;
import edu.hawaii.its.filedrop.type.Recipient;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class FileSetRepositoryTest {

    @Autowired
    private FileDropRepository fileDropRepository;

    @Autowired
    private FileSetRepository fileSetRepository;

    @Autowired
    private RecipientRepository recipientRepository;

    @Test
    public void findFileSetTest() {
        long countFileDrop0 = fileDropRepository.count();
        long countFileSet0 = fileSetRepository.count();

        LocalDateTime created = LocalDateTime.now();
        LocalDateTime expiration = created.plus(10, ChronoUnit.DAYS);
        FileDrop fileDrop = new FileDrop();
        fileDrop.setUploader("test");
        fileDrop.setUploaderFullName("Test 123");
        fileDrop.setCreated(created);
        fileDrop.setExpiration(expiration);
        fileDrop.setDownloadKey("download-key");
        fileDrop.setUploadKey("upload-key");
        fileDrop.setEncryptionKey("enc-key");
        fileDrop.setAuthenticationRequired(false);
        fileDrop.setValid(false);

        fileDrop = fileDropRepository.save(fileDrop);

        long countFileDrop1 = fileDropRepository.count();
        assertThat(countFileDrop1, equalTo(countFileDrop0 + 1));
        long countFileSet1 = fileSetRepository.count();
        assertThat(countFileSet1, equalTo(countFileSet0));

        Recipient recipient = new Recipient();
        recipient.setName("tester");
        recipient.setFileDrop(fileDrop);

        recipientRepository.save(recipient);

        FileSet fs = fileSetRepository.findById(1).get();
        assertThat(fs, not(equalTo(null)));
        assertThat(fs.getId(), equalTo(1));

        FileSet fileSet = new FileSet();
        fileSet.setFileName("Test image.png");
        fileSet.setFileDrop(fileDrop);
        fileSet.setComment(null);  // Note, null.
        fileSet.setType("image/png");
        fileSet.setSize(0L);

        fileSet = fileSetRepository.save(fileSet);

        long countFileDrop2 = fileDropRepository.count();
        assertThat(countFileDrop2, equalTo(countFileDrop1));
        long countFileSet2 = fileSetRepository.count();
        assertThat(countFileSet2, equalTo(countFileSet1 + 1));

        FileSet foundFileSet = fileSetRepository.findById(fileSet.getId()).get();
        assertNotNull(foundFileSet);
        assertEquals(foundFileSet.getFileName(), "Test image.png");

        List<FileSet> fileSets = fileSetRepository.findAllByFileDrop(fileDrop);
        assertFalse(fileSets.isEmpty());
        assertEquals(1, fileSets.size());

        assertThat(fileSet.toString(), containsString("fileName=Test image.png,"));

        fileSet.setFileName("test.txt");

        assertNotEquals(fileSet.toString(), foundFileSet.toString());

        fileSet = fileSetRepository.save(fileSet);

        assertNotEquals(fileSet.toString(), foundFileSet.toString());

        FileSet foundFileSet2 = fileSetRepository.findById(fileSet.getId()).get();
        assertThat(foundFileSet2.toString(), containsString("fileName=test.txt,"));

        // Clean up.
        fileSetRepository.delete(fileSet);
        long countFileSet9 = fileSetRepository.count();
        assertThat(countFileSet9, equalTo(countFileSet0));

        fileDrop = fileDropRepository.findById(fileDrop.getId()).get();
        assertThat(fileDrop, not(equalTo(null)));
        assertThat(fileDrop.getId(), not(equalTo(null)));
        fileDropRepository.delete(fileDrop);
        long countFileDrop9 = fileDropRepository.count();
        assertThat(countFileDrop9, equalTo(countFileDrop0));
    }

    @Test
    public void acomment() {
        final long countFileDrop0 = fileDropRepository.count();
        final long countFileSet0 = fileSetRepository.count();

        FileDrop fd0 = fileDropRepository.findById(1).get();
        assertThat(fd0, not(equalTo(null)));

        FileSet fs0 = new FileSet();
        fs0.setFileName("test");
        fs0.setComment("test comment");
        fs0.setType("text/plain");
        fs0.setSize(Long.MIN_VALUE);
        fs0.setFileDrop(fd0);

        fs0 = fileSetRepository.save(fs0);

        assertThat(fs0.getId(), notNullValue());
        assertThat(fs0.getId(), greaterThan(1));
        assertThat(fs0.getComment(), notNullValue());

        final long countFileDrop1 = fileDropRepository.count();
        assertThat(countFileDrop1, equalTo(countFileDrop0));
        final long countFileSet1 = fileSetRepository.count();
        assertThat(countFileSet1, equalTo(countFileSet0 + 1));

        fileSetRepository.delete(fs0);

        final long countFileDrop2 = fileDropRepository.count();
        assertThat(countFileDrop2, equalTo(countFileDrop0));
        final long countFileSet2 = fileSetRepository.count();
        assertThat(countFileSet2, equalTo(countFileSet1 - 1));
        assertThat(countFileSet2, equalTo(countFileSet0));
    }
}
