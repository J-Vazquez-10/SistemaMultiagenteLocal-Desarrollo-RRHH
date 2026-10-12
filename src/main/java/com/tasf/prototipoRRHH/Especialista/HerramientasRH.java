package com.tasf.prototipoRRHH.Especialista;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class HerramientasRH {
	
	// ==========================================
    // HERRAMIENTAS EXPUESTAS AL AGENTE (LLM)
    // ==========================================

    // 1. RUTAS DINÁMICAS Y SEGURAS
    private static final String DIRECTORIO_RAIZ = System.getProperty("user.dir");
    private static final String CARPETA_POLITICAS = "Politicas";
    private static final String CARPETA_CANDIDATOS = "candidatos";


    @Tool("Consulta las políticas internas, manuales o normativas de la empresa. Úsalo SIEMPRE que te pregunten sobre reglas.")
    public String consultarPoliticaInterna(
            @P("El nombre exacto del archivo de política a leer, ej: vacaciones.txt") String documento) {
        
        System.out.println("⚡ [TOOL CALLING RRHH] El agente solicitó leer la política: " + documento);
        return leerArchivoSeguro(CARPETA_POLITICAS, documento);
    }
    
    @Tool("Lee el currículum de un candidato. Úsalo cuando te pidan evaluar el perfil, las habilidades o la experiencia de un postulante.")
    public String leerCurriculumCandidato(
            @P("El nombre exacto del archivo del currículum, ej: cv_pedro_perez.txt") String nombreArchivo) {
        
        System.out.println("⚡ [TOOL CALLING RRHH] El agente solicitó leer el CV: " + nombreArchivo);
        return leerArchivoSeguro(CARPETA_CANDIDATOS, nombreArchivo);
    }

    @Tool("Escanea y lee TODOS los currículums en la carpeta de candidatos. Úsalo para comparar múltiples perfiles y decidir cuál es el mejor.")
    public String escanearTodosLosCandidatos(
            @P("Escribe exactamente la palabra 'confirmar' para ejecutar el escaneo") String parametroObligatorio) {
        
        System.out.println("⚡ [TOOL CALLING RRHH] El agente está escaneando toda la carpeta de candidatos...");
        StringBuilder todosLosCVs = new StringBuilder();
        
        try {
            Path rutaDirectorio = Paths.get(DIRECTORIO_RAIZ, CARPETA_CANDIDATOS).normalize().toAbsolutePath();
            
            if (!Files.exists(rutaDirectorio)) {
                return "[Error] La carpeta de candidatos no existe.";
            }

            // Recorremos la carpeta y delegamos la lectura de CADA archivo al método maestro
            Files.list(rutaDirectorio)
                 .filter(Files::isRegularFile)
                 .forEach(archivo -> {
                     String nombreArchivo = archivo.getFileName().toString();
                     String contenido = leerArchivoSeguro(CARPETA_CANDIDATOS, nombreArchivo);
                     
                     todosLosCVs.append("=== Archivo: ").append(nombreArchivo).append(" ===\n");
                     todosLosCVs.append(contenido).append("\n\n");
                 });

            if (todosLosCVs.length() == 0) {
                return "La carpeta de candidatos está vacía.";
            }
            return todosLosCVs.toString();

        } catch (Exception e) {
            return "[Error al escanear directorio] " + e.getMessage();
        }
    }

    // ==========================================
    // MÉTODOS INTERNOS MAESTROS (Ocultos al LLM)
    // ==========================================

    /**
     * ÚNICO método encargado de abrir archivos y leer su texto en todo el microservicio.
     */
    private String leerArchivoSeguro(String carpetaBase, String nombreArchivo) {
        try {
            Path ruta = validarRutaSegura(carpetaBase, nombreArchivo);
            
            if (Files.exists(ruta) && Files.isRegularFile(ruta)) {
                return Files.readString(ruta);
            }
            return "[Error] No se encontró el documento llamado '" + nombreArchivo + "' en la carpeta '" + carpetaBase + "'.";
        } catch (SecurityException e) {
            return "[Acceso Denegado] " + e.getMessage();
        } catch (Exception e) {
            return "[Error de Lectura Interna] " + e.getMessage();
        }
    }

    /**
     * Valida matemáticamente que nadie intente leer archivos fuera de las carpetas permitidas.
     */
    private Path validarRutaSegura(String carpetaBase, String nombreArchivo) throws SecurityException {
        Path pathBase = Paths.get(DIRECTORIO_RAIZ, carpetaBase).normalize().toAbsolutePath();
        Path pathDestino = pathBase.resolve(nombreArchivo).normalize().toAbsolutePath();

        // Evita el path traversal (ej. nombreArchivo = "../../../Windows/System32/config")
        if (!pathDestino.startsWith(pathBase)) {
            throw new SecurityException("Intento de manipulación de rutas detectado.");
        }
        return pathDestino;
    }
}