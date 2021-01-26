package edu.hawaii.its.filedrop.util;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.Test;

public class ThreadsTest {

    @Test
    public void sleep() {
        Threads.sleep(-1);
        Threads.sleep(0);
        Threads.sleep(1);
    }

    @Test
    public void testConstructorIsPrivate() throws Exception {
        Constructor<Threads> constructor = Threads.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
