import java.io.*;
import java.util.*;

public class MainModel {
    public static void main(String[] args) {
        List<String[]> commands = readConfigFile("src/ConfigFile.txt");

        if (commands.size() < 4) {  // Cambiado a 4 para incluir NaiveBayes
            System.err.println("Error: El archivo de configuración no contiene suficientes parámetros.");
            return;
        }

        try {
            // Primero compilamos todas las clases
            compileAllJavaFiles();

            System.out.println("Ejecutando GetArff...");
            executeJavaClass("preprocessing.GetArff", commands.get(0));

            System.out.println("Ejecutando GetArffBOW...");
            executeJavaClass("preprocessing.GetArffBOW", commands.get(1));

            System.out.println("Ejecutando FSS...");
            executeJavaClass("preprocessing.FSS", commands.get(2));

            System.out.println("Ejecutando NaiveBayes...");
            executeJavaClass("Sailkatzailea.NaiveBayes", commands.get(3));

            System.out.println("Ejecución completada correctamente.");
        } catch (Exception e) {
            System.err.println("Error durante la ejecución:");
            e.printStackTrace();
        }
    }

    private static void compileAllJavaFiles() throws IOException, InterruptedException {
        List<String> compileCommand = new ArrayList<>();
        compileCommand.add("javac");
        compileCommand.add("-cp");
        compileCommand.add("lib/weka.jar");
        compileCommand.add("-d");
        compileCommand.add("out");
        compileCommand.add("src/MainModel.java");
        compileCommand.add("src/preprocessing/GetArff.java");
        compileCommand.add("src/preprocessing/GetArffBOW.java");
        compileCommand.add("src/preprocessing/FSS.java");
        compileCommand.add("src/Sailkatzailea/NaiveBayes.java"); // Añadido

        System.out.println("Compilando con: " + String.join(" ", compileCommand));

        ProcessBuilder compileBuilder = new ProcessBuilder(compileCommand);
        compileBuilder.inheritIO();
        Process compileProcess = compileBuilder.start();
        int compileExitCode = compileProcess.waitFor();

        if (compileExitCode != 0) {
            throw new RuntimeException("Error al compilar los archivos Java");
        }
    }

    private static List<String[]> readConfigFile(String filePath) {
        List<String[]> commands = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    commands.add(line.split("\\s+"));
                }
            }
        } catch (IOException e) {
            System.err.println("Error leyendo el archivo de configuración:");
            e.printStackTrace();
        }
        return commands;
    }

    private static void executeJavaClass(String className, String[] args) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-cp");
        command.add("lib/weka.jar;out"); // Incluye weka.jar y el directorio de clases compiladas
        command.add(className);
        command.addAll(Arrays.asList(args));

        System.out.println("Ejecutando: " + String.join(" ", command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true); // Combina stdout y stderr

        // Capturamos la salida del proceso
        Process process = processBuilder.start();

        // Leemos la salida línea por línea
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Error al ejecutar " + className +
                    " (Código de salida: " + exitCode + ")");
                    }
        }
}