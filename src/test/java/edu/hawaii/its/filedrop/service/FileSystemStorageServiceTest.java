package edu.hawaii.its.filedrop.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import edu.hawaii.its.filedrop.configuration.SpringBootWebApplication;
import edu.hawaii.its.filedrop.configuration.StorageProperties;
import edu.hawaii.its.filedrop.exception.StorageException;
import edu.hawaii.its.filedrop.exception.StorageFileNotFoundException;
import edu.hawaii.its.filedrop.type.FileDrop;
import edu.hawaii.its.filedrop.type.FileSet;
import edu.hawaii.its.filedrop.util.Strings;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FileSystemStorageServiceTest {

    @Autowired
    private StorageProperties storageProperties;

    @Autowired
    private FileSystemStorageService storageService;

    @Autowired
    private FileDropService fileDropService;

    @Test
    public void construct() {
        assertNotNull(storageService);
    }

    @Test
    public void store() throws Exception {
        boolean finished = false;

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
            storageService.store(firstFile);
            finished = true;
        } catch (Exception e) {
            fail("Unexpected error: " + e);
        } finally {
            if (Files.exists(absPath)) {
                Files.delete(absPath);
            }
        }

        assertTrue("Test did not finish properly.", finished);
    }

    @Test
    public void rootLocation() {
        assertNotNull(storageService.getRootLocation());
        assertEquals("StorageProperties [location=" + storageProperties.getLocation() + "]", storageProperties.toString());
    }

    @Test
    public void createDirectories() throws Exception {
        Integer applicationId = 19900000;
        String dirname = applicationId + "/outsider";

        Path dirPath = Paths.get(dirname);
        Path rootPath = storageService.getRootLocation();
        Path path = Paths.get(rootPath.toString(), dirname);
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

        deleteDirectory(path);

        assertFalse(storageService.exists(dirname));
        assertFalse(Files.exists(path));

    }

    private void deleteDirectory(Path path) throws IOException {
        File dir = path.toFile();
        if (dir.exists()) {
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteDirectory(files[i].toPath());
                }
            }
            dir.delete();
        }
    }

    @Test
    public void storeFileSet() throws Exception {
        FileDrop fileDrop = new FileDrop();
        fileDrop.setDownloadKey(Strings.generateRandomString());
        fileDrop = fileDropService.saveFileDrop(fileDrop);
        final String downloadKey = fileDrop.getDownloadKey();
        assertNotNull(downloadKey);
        String fileName = "test.txt";
        Path expectedPath = Paths.get(storageService.getRootLocation().toString(), downloadKey, fileName);

        // Make sure the file is not there
        // before performing the main test.
        assertEquals(downloadKey, expectedPath.getParent().getFileName().toString());
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
        fileSet = fileDropService.saveFileSet(fileSet);
        fileName = fileSet.getId() + ".txt";
        assertFalse(storageService.exists(multipartFile, downloadKey));
        storageService.storeFileSet(multipartFile, Paths.get(downloadKey, fileName));

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
    }

    @Test
    public void storeFileSetEmpty() throws Exception {
        FileDrop fileDrop = new FileDrop();
        fileDrop.setDownloadKey(Strings.generateRandomString());
        fileDrop = fileDropService.saveFileDrop(fileDrop);
        final String downloadKey = fileDrop.getDownloadKey();
        assertNotNull(downloadKey);
        String fileName = "test.txt";
        Path expectedPath = Paths.get(storageService.getRootLocation().toString(), downloadKey, fileName);

        // Make sure the file is not there
        // before performing the main test.
        assertEquals(downloadKey, expectedPath.getParent().getFileName().toString());
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
        fileSet = fileDropService.saveFileSet(fileSet);
        fileName = fileSet.getId() + ".txt";
        assertFalse(storageService.exists(multipartFile, downloadKey));
        storageService.storeFileSet(multipartFile, Paths.get(downloadKey, fileName));

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
    }

    @Test
    public void storeAndLoadResource() throws Exception {
        String tmpdir = System.getProperty("java.io.tmpdir");
        Path path = Paths.get(tmpdir, "~filedrop.store.txt");

        File file = new File(path.toAbsolutePath().toString());
        file.deleteOnExit();
        assertFalse(file.exists());

        MockMultipartFile multipartFile =
                new MockMultipartFile("file",
                        file.getAbsolutePath(),
                        "text/plain",
                        "Test MockMultipartFile".getBytes());

        // Store the file.
        storageService.store(multipartFile);

        // Now load that same file.
        Resource r = storageService.loadAsResource(file.getAbsolutePath());
        assertThat(r.contentLength(), equalTo(22L));

        // Test a broken file path.
        try {
            storageService.loadAsResource(file.getAbsolutePath() + "no");
            fail("Should not have reached here.");
        } catch (Exception e) {
            assertEquals(e.getClass(), StorageFileNotFoundException.class);
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
            storageService.store(multipartFile);
            fail("Should not have reached here.");
        } catch (Exception e) {
            assertEquals(e.getClass(), StorageException.class);
            assertThat(e.getMessage(), containsString("Failed to store empty file"));
        }

        try {
            storageService.storeFileSet(multipartFile, storageService.getRootLocation());
            fail("Should not have reached here.");
        } catch (Exception e) {
            assertEquals(e.getClass(), StorageException.class);
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
            storageService.store(multipartFile);
            fail("Should not have reached here.");
        } catch (Exception e) {
            assertEquals(e.getClass(), StorageException.class);
            assertThat(e.getMessage(), containsString("FileAlreadyExistsException"));
        }
    }

    @Test
    public void deleteNullFile() {
        try {
            storageService.delete(Paths.get(storageService.getRootLocation().toString(), "test"));
        } catch(Exception e) {
            assertEquals(e.getClass(), StorageException.class);
            assertThat(e.getMessage(), containsString("IOException"));
        }

        try {
            storageService.delete("test", storageService.getRootLocation().toString());
        } catch(Exception e) {
            assertEquals(e.getClass(), StorageException.class);
            assertThat(e.getMessage(), containsString("IOException"));
        }
    }

    @Test
    public void testToString() {
        String s = storageService.toString();
        assertThat(s, containsString("FileSystemStorageService ["));

        Integer applicationId = 19999999;
        String committeRecommendationFile = "somefile";

        String pathStr = applicationId + File.separator
                + "recommendations"
                + File.separator
                + committeRecommendationFile;

        Path path = Paths.get(applicationId.toString(), "recommendations", committeRecommendationFile);
        assertEquals(pathStr, path.toString());
    }
}
