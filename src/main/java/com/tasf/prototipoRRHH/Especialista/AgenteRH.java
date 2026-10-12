package com.tasf.prototipoRRHH.Especialista;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface AgenteRH {
	String evaluar(@UserMessage String peticion);
}