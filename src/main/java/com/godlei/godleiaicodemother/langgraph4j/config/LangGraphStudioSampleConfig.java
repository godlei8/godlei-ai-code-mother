package com.godlei.godleiaicodemother.langgraph4j.config;

import com.godlei.godleiaicodemother.langgraph4j.CodeGenWorkflow;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.studio.LangGraphStudioServer;
import org.bsc.langgraph4j.studio.springboot.LangGraphStudioConfig;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class LangGraphStudioSampleConfig extends LangGraphStudioConfig {

    @Override
    public Map<String, LangGraphStudioServer.Instance> instanceMap() {

        var workflow = new CodeGenWorkflow().createWorkflow().stateGraph;

        // define your workflow

        var instance = LangGraphStudioServer.Instance.builder()
                .title("LangGraph Studio")
                .graph(workflow)
                .build();

        return Map.of("default", instance);
    }
}