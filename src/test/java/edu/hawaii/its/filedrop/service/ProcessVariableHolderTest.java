package edu.hawaii.its.filedrop.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ProcessVariableHolderTest {

    @Test
    public void constructors() {
        ProcessVariableHolder processVariableHolder = new ProcessVariableHolder();
        assertThat(processVariableHolder.size(), equalTo(0));

        Map<String, Object> variables = null;
        processVariableHolder = new ProcessVariableHolder(variables);
        assertThat(processVariableHolder.size(), equalTo(0));

        variables = new HashMap<>();
        processVariableHolder = new ProcessVariableHolder(variables);
        assertThat(processVariableHolder.size(), equalTo(0));

        variables = new HashMap<>();
        variables.put("Test", 2);
        processVariableHolder = new ProcessVariableHolder(variables);
        assertThat(processVariableHolder.size(), equalTo(1));
    }

    @Test
    public void basics() {
        ProcessVariableHolder processVariableHolder = new ProcessVariableHolder();

        assertFalse(processVariableHolder.containsKey("Test"));
        assertFalse(processVariableHolder.containsValue(2));

        processVariableHolder.add("Test", 2);
        assertTrue(processVariableHolder.containsKey("Test"));
        assertTrue(processVariableHolder.containsValue(2));

        assertFalse(processVariableHolder.containsKey("The Beatles"));
        assertFalse(processVariableHolder.containsValue("White Album"));

        processVariableHolder.add("The Beatles", "White Album");
        assertTrue(processVariableHolder.containsKey("The Beatles"));
        assertTrue(processVariableHolder.containsValue("White Album"));

        assertThat(processVariableHolder.toString(), containsString("Test"));
        assertThat(processVariableHolder.toString(), containsString("2"));
        assertThat(processVariableHolder.toString(), containsString("The Beatles"));
        assertThat(processVariableHolder.toString(), containsString("Test"));

        assertThat(processVariableHolder.size(), equalTo(2));
        assertThat(processVariableHolder.getMap().size(), equalTo(2));

        Map<String, Object> variables = new HashMap<>();
        variables.put("Tame Impala", "The Slow Rush");
        variables.put("The Black Keys", "El Camino");

        processVariableHolder.addAll(variables);

        assertThat(processVariableHolder.toString(), containsString("Tame Impala"));
        assertThat(processVariableHolder.toString(), containsString("The Slow Rush"));
        assertThat(processVariableHolder.toString(), containsString("The Black Keys"));
        assertThat(processVariableHolder.toString(), containsString("El Camino"));

        assertThat(processVariableHolder.size(), equalTo(4));
        assertThat(processVariableHolder.getMap().size(), equalTo(4));

        assertThat(processVariableHolder.getString("The Black Keys"), equalTo("El Camino"));
    }
}
