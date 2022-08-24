package edu.hawaii.its.filedrop.service;

import static edu.hawaii.its.filedrop.util.Files.deleteDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.configuration.StorageProperties;
import edu.hawaii.its.filedrop.exception.StorageException;
import edu.hawaii.its.filedrop.exception.StorageFileNotFoundException;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileSet;
import edu.hawaii.its.filedrop.util.Strings;

@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class FileSystemStorageServiceTest {

    @Autowired
    private StorageProperties storageProperties;

    @Autowired
    private FileSystemStorageService storageService;

    @Autowired
    private FileDropService fileDropService;

    @BeforeAll
    public static void beforeAll() {

    }

    @Test
    public void construct() {
        assertNotNull(storageService);
    }

    @Test
    public void store() throws Exception {
        Path dirPath = storageService.getRootLocation();
        String filename = "~filename.txt~";
        Path absPath = Paths.get(dirPath.toAbsolutePath().toString(), filename);

        if (Files.exists(absPath)) {
            Files.delete(absPath);
        }

        try {
            MockMultipartFile firstFile = new MockMultipartFile("data",
                    filename,
                    "text/plain",
                    "some data".getBytes());
            storageService.store(firstFile.getResource());
        } catch (Exception e) {
            fail("Unexpected error: " + e);
        } finally {
            if (Files.exists(absPath)) {
                Files.delete(absPath);
            }
        }
    }

    @Test
    public void rootLocation() {
        assertNotNull(storageService.getRootLocation());
        assertThat("StorageProperties [location=" + storageProperties.getLocation() + "]",
                equalTo(storageProperties.toString()));
    }

    @Test
    public void createDirectories() throws Exception {
        String dirname = "abcd/outsider";
        final Path dirPath = Paths.get(dirname);
        final Path rootPath = storageService.getRootLocation();
        final Path path = Paths.get(rootPath.toString(), dirname);

        deleteDirectory(path.getParent());
        assertFalse(storageService.exists(dirname));
        assertFalse(Files.exists(path));

        // Make a bunch of files.
        for (int i = 0; i < 5; i++) {
            String d = dirPath + "/dir" + i;
            storageService.createDirectories(d);
            for (int j = 0; j < 10; j++) {
                String f = "file-" + j;
                Path p = Paths.get(rootPath.toString(), d, f);
                p.toFile().createNewFile();
                assertTrue(Files.exists(p));
            }
        }

        assertTrue(storageService.exists(dirname));
        assertTrue(Files.exists(path));

        deleteDirectory(path.getParent());

        assertFalse(storageService.exists(dirname));
        assertFalse(Files.exists(path));
    }

    @Test
    public void storeFileSet() throws Exception {
        FileDrop fileDrop = new FileDrop();
        fileDrop.setDownloadKey(Strings.generateRandomString());
        fileDrop.setUploadKey(Strings.generateRandomString());
        fileDrop.setEncryptionKey(Strings.generateRandomString());
        fileDrop.setCreated(LocalDateTime.now());
        fileDrop.setExpiration(LocalDateTime.now());
        fileDrop.setValid(true);
        fileDrop.setAuthenticationRequired(true);
        fileDrop.setUploader("test");
        fileDrop.setUploaderFullName("Test");
        fileDrop.setRecipients(Collections.emptyList());
        fileDrop = fileDropService.saveFileDrop(fileDrop);
        final String downloadKey = fileDrop.getDownloadKey();
        assertNotNull(downloadKey);
        String fileName = "test.txt";
        Path expectedPath = Paths.get(storageService.getRootLocation().toString(), downloadKey, fileName);

        // Make sure the file is not there
        // before performing the main test.
        assertThat(downloadKey, equalTo(expectedPath.getParent().getFileName().toString()));
        storageService.delete(expectedPath);

        assertFalse(storageService.exists(expectedPath));

        MockMultipartFile multipartFile =
                new MockMultipartFile("file",
                        expectedPath.getParent().toString(),
                        "text/plain",
                        "Test MockMultipartFile".getBytes());

        FileSet fileSet = new FileSet();
        fileSet.setFileName(multipartFile.getName());
        fileSet.setFileDrop(fileDrop);
        fileSet.setType(multipartFile.getContentType());
        fileSet.setSize(multipartFile.getSize());
        fileSet = fileDropService.saveFileSet(fileSet);
        fileName = fileSet.getId() + ".txt";
        assertFalse(storageService.exists(multipartFile.getResource(), downloadKey));
        storageService.storeFileSet(multipartFile.getResource(), Paths.get(downloadKey, fileName));

        expectedPath = Paths.get(storageService.getRootLocation().toString(), downloadKey, fileName);
        String expectedFilename = expectedPath.toString();

        // Now load that same file.
        Resource r = storageService.loadAsResource(expectedFilename);
        assertThat(r.contentLength(), equalTo(22L));

        Path filepath = Paths.get(storageService.getRootLocation().toString(), downloadKey);
        assertTrue(storageService.exists(fileName, filepath.toString()));

        assertTrue(storageService.exists(expectedFilename));
        assertTrue(storageService.exists(expectedPath.getParent().toString()));

        storageService.delete(fileName, filepath.toString());
        storageService.delete(fileName, filepath.getParent().toString());

        assertFalse(storageService.exists(filepath.toString(), fileName));
        assertFalse(storageService.exists(filepath.getParent().toString(), fileName));

        deleteDirectory(filepath);
        assertFalse(Files.exists(filepath));
    }

    @Test
    public void storeFileSetEmpty() throws Exception {
        FileDrop fileDrop = new FileDrop();
        fileDrop.setId(123456789);
        fileDrop.setDownloadKey(Strings.generateRandomString());
        fileDrop.setUploadKey(Strings.generateRandomString());
        fileDrop.setEncryptionKey(Strings.generateRandomString());
        fileDrop.setCreated(LocalDateTime.now());
        fileDrop.setExpiration(LocalDateTime.now());
        fileDrop.setValid(true);
        fileDrop.setAuthenticationRequired(true);
        fileDrop.setUploader("test");
        fileDrop.setUploaderFullName("Test");
        fileDrop.setRecipients(Collections.emptyList());
        fileDrop = fileDropService.saveFileDrop(fileDrop);
        final String downloadKey = fileDrop.getDownloadKey();
        assertNotNull(downloadKey);
        String fileName = "test.txt";
        Path expectedPath = Paths.get(storageService.getRootLocation().toString(), downloadKey, fileName);

        // Make sure the file is not there
        // before performing the main test.
        assertThat(downloadKey, equalTo(expectedPath.getParent().getFileName().toString()));
        storageService.delete(expectedPath);

        assertFalse(storageService.exists(expectedPath));

        MockMultipartFile multipartFile =
                new MockMultipartFile("file",
                        expectedPath.getParent().toString(),
                        "text/plain",
                        "".getBytes());

        FileSet fileSet = new FileSet();
        fileSet.setFileName(multipartFile.getName());
        fileSet.setFileDrop(fileDrop);
        fileSet.setType(multipartFile.getContentType());
        fileSet.setSize(multipartFile.getSize());
        fileSet = fileDropService.saveFileSet(fileSet);
        fileName = fileSet.getId() + ".txt";
        assertFalse(storageService.exists(multipartFile.getResource(), downloadKey));
        storageService.storeFileSet(multipartFile.getResource(), Paths.get(downloadKey, fileName));

        expectedPath = Paths.get(storageService.getRootLocation().toString(), downloadKey, fileName);
        String expectedFilename = expectedPath.toString();
        // Now load that same file.
        Resource r = storageService.loadAsResource(expectedFilename);
        assertThat(r.contentLength(), equalTo(0L));

        Path filepath = Paths.get(storageService.getRootLocation().toString(), downloadKey);
        assertTrue(storageService.exists(fileName, filepath.toString()));

        assertTrue(storageService.exists(expectedFilename));
        assertTrue(storageService.exists(expectedPath.getParent().toString()));

        storageService.delete(fileName, filepath.toString());
        storageService.delete(fileName, filepath.getParent().toString());

        assertFalse(storageService.exists(filepath.toString(), fileName));
        assertFalse(storageService.exists(filepath.getParent().toString(), fileName));

        deleteDirectory(filepath);
        assertFalse(Files.exists(filepath));
    }

    @Test
    public void storeAndLoadResource() throws Exception {
        File file = File.createTempFile("~filedrop.store", "txt");
        file.delete();
        assertFalse(file.exists());

        MockMultipartFile multipartFile =
                new MockMultipartFile("file",
                        file.getAbsolutePath(),
                        "text/plain",
                        "Test MockMultipartFile".getBytes());

        // Store the file.
        storageService.store(multipartFile.getResource());

        // Now load that same file.
        Resource r = storageService.loadAsResource(file.getAbsolutePath());
        assertThat(r.contentLength(), equalTo(22L));

        // Test a broken file path.
        try {
            storageService.loadAsResource(file.getAbsolutePath() + "no");
            fail("Should not have reached here.");
        } catch (Exception e) {
            assertThat(e, instanceOf(StorageFileNotFoundException.class));
            assertThat(e.getMessage(), containsString("Error reading file"));
        }
    }

    @Test
    public void storeFileEmptyException() throws Exception {
        File file = File.createTempFile("~filedrop.store", "txt");
        file.deleteOnExit();
        assertTrue(file.exists());

        // Now load that same file.
        Resource r = storageService.loadAsResource(file.getAbsolutePath());
        assertThat(r.contentLength(), equalTo(0L)); // Should be empty.

        // Try to store the file.
        MockMultipartFile multipartFile =
                new MockMultipartFile("file",
                        file.getAbsolutePath(),
                        "text/plain",
                        "".getBytes());
        try {
            storageService.store(multipartFile.getResource());
            fail("Should not have reached here.");
        } catch (Exception e) {
            assertThat(e, instanceOf(StorageException.class));
            assertThat(e.getMessage(), containsString("Failed to store empty file"));
        }

        try {
            storageService.storeFileSet(multipartFile.getResource(), storageService.getRootLocation());
            fail("Should not have reached here.");
        } catch (Exception e) {
            assertThat(e, instanceOf(StorageException.class));
        }
    }

    @Test
    public void storeFileExistsException() throws Exception {
        File file = File.createTempFile("~filedrop.store", "txt");
        file.deleteOnExit();
        assertTrue(file.exists());

        // Now load that same file.
        Resource r = storageService.loadAsResource(file.getAbsolutePath());
        assertThat(r.contentLength(), equalTo(0L)); // Should be empty.

        // Try to store the file.
        MockMultipartFile multipartFile =
                new MockMultipartFile("file",
                        file.getAbsolutePath(),
                        "text/plain",
                        "Test MockMultipartFile Error".getBytes());
        try {
            storageService.store(multipartFile.getResource());
            fail("Should not have reached here.");
        } catch (Exception e) {
            assertThat(e, instanceOf(StorageException.class));
            assertThat(e.getMessage(), containsString("FileAlreadyExistsException"));
        }
    }

    @Test
    public void testFileContent() {
        File file;

        try {
            file = File.createTempFile("~tmp", ".tmp");
            file.deleteOnExit();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            StringBuilder content = new StringBuilder();

            content.append("I'd like to be");
            content.append("under the sea");
            content.append("in an octopus's garden");
            content.append("in the shade");
            content.append("- Ringo");

            bufferedWriter.write(content.toString());
            bufferedWriter.close();

            assertTrue(Files.exists(file.toPath()));
            assertThat(file.length(), greaterThan(0L));
            assertThat(Files.size(file.toPath()), equalTo(file.length()));

            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            StringBuilder builder = new StringBuilder();

            bufferedReader.lines().forEach(builder::append);
            bufferedReader.lines().forEach(System.out::println);
            bufferedReader.close();

            assertThat(builder.toString(), equalTo(content.toString()));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    @Test
    public void deleteNullFile() {
        try {
            storageService.delete(null);
        } catch (Exception e) {
            assertThat(e, instanceOf(StorageException.class));
        }
    }

    @Test
    public void createNullDirectory() {
        try {
            storageService.createDirectories(null);
        } catch (Exception e) {
            assertThat(e, instanceOf(StorageException.class));
        }
    }

    @Test
    public void existNullFile() {
        assertFalse(storageService.exists((String) null));
    }

    @Test
    public void testToString() {
        String s = storageService.toString();
        assertThat(s, containsString("FileSystemStorageService ["));
    }
}
