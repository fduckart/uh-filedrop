package edu.hawaii.its.filedrop.configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RuntimeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProcessEngineConfig {

    @Bean
    public RuntimeService runtimeService() {
        ProcessEngine processEngine = configure();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        Path processDefPath = Paths.get("processes", "filedrop-workflow.bpmn20.xml");
        processEngine.getRepositoryService()
                .createDeployment()
                .addClasspathResource(processDefPath.toString())
                .deploy();
        return runtimeService;
    }

    @Bean
    public ProcessEngine configure() {
        ProcessEngine engine = ProcessEngineConfiguration
                .createStandaloneInMemProcessEngineConfiguration()
                .buildProcessEngine();
        return engine;
    }

}
