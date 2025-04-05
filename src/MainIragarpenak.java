import java.io.*;
import java.util.*;

public class MainIragarpenak {
    public static void main(String[] args) {
        List<String[]> commands = readConfigFile("src/ConfigFile.txt");

        if (commands.size() < 6) {
            System.err.println("Error: El archivo de configuración no contiene suficientes parámetros.");
            return;
        }

        try {
            compileAllJavaFiles();

            System.out.println("Ejecutando testBlindEguneratu...");
            executeJavaClass("Iragarpenak.testBlindEguneratu", commands.get(4), true);

            System.out.println("Ejecutando Iragarpenak...");
            executeJavaClass("Iragarpenak.Iragarpenak", commands.get(5), false);

            System.out.println("Predicciones completadas correctamente.");
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
        compileCommand.add("src/Iragarpenak/testBlindEguneratu.java");
        compileCommand.add("src/Iragarpenak/Iragarpenak.java");

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

    private static void executeJavaClass(String className, String[] args, boolean needsOpens)
            throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("java");

        if (needsOpens) {
            command.add("--add-opens");
            command.add("java.base/java.lang=ALL-UNNAMED");
            command.add("--add-opens");
            command.add("java.base/java.util=ALL-UNNAMED");
        }

        command.add("-cp");
        command.add("lib/weka.jar;out");
        command.add(className);
        command.addAll(Arrays.asList(args));

        System.out.println("Ejecutando: " + String.join(" ", command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

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