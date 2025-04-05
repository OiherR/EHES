package preprocessing;

import java.io.*;
import java.util.Scanner;


public class GetArff {

    public static void main(String[] args) throws IOException {
        // Argumentuen balidazioa
        if (args.length != 2) {
            System.out.println("Uso: java GetArff directorio_entrada salida.arff");
            System.out.println("  directorio_entrada: Testu-fitxategiak dituen karpeta (azpikarpetak = klaseak)");
            System.out.println("  salida.arff:        Gorde nahi den ARFF fitxategiaren bidea");
            return;
        }

        String inputPath = args[0];  // @arg1
        String outputPath = args[1]; // @arg2

        File inputDir = new File(inputPath);

        // Sarrerako direktorioa egiaztatu
        if (!inputDir.exists() || !inputDir.isDirectory()) {
            System.err.println("❌ Errorea: '" + inputPath + "' direktorioa ez da existitzen.");
            return;
        }

        // Prozesatu egituraren arabera
        File[] subdirs = inputDir.listFiles(File::isDirectory);
        if (subdirs != null && subdirs.length > 0) {
            convertWithTextDirectoryLoader(inputDir, outputPath);  // Train/dev
        } else {
            convertFlatDirectoryToArff(inputDir, outputPath);      // Test_blind
        }
    }

    /**
     * Testu direktorioa ARFF formatura bihurtzen du, azpikarpeten arabera klaseak esleituz.
     * (WEKAren TextDirectoryLoader erabiliz)
     *
     * @param dir        Sarrerako direktorioa (azpikarpetak = klaseak)
     * @param outputPath Irteerako ARFF fitxategiaren bidea
     * @throws IOException Fitxategiak irakurtzean/idaztean errorea gertatzen bada
     */
    public static void convertWithTextDirectoryLoader(File dir, String outputPath) throws IOException {
        weka.core.converters.TextDirectoryLoader loader = new weka.core.converters.TextDirectoryLoader();
        loader.setDirectory(dir);
        weka.core.Instances data = loader.getDataSet();

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            writer.println(data);
            System.out.println("✅ ARFF generatua (train/dev) → " + outputPath);
        }
    }

    /**
     * Karpeta lauko testu-fitxategiak ARFF formatura bihurtzen du (klaserik gabe).
     * (Test_blind kasurako)
     *
     * @param dir        Sarrerako direktorioa (fitxategi lauak)
     * @param outputPath Irteerako ARFF fitxategiaren bidea
     * @throws IOException Fitxategiak irakurtzean/idaztean errorea gertatzen bada
     */
    public static void convertFlatDirectoryToArff(File dir, String outputPath) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(outputPath));

        // ARFF goiburua (klaserik gabe)
        pw.println("@relation test_blind");
        pw.println("@attribute text string");
        pw.println("@data");

        // Testu-fitxategiak prozesatu
        File[] files = dir.listFiles(File::isFile);
        int count = 0;

        for (File file : files) {
            StringBuilder content = new StringBuilder();
            try (Scanner scanner = new Scanner(file, "UTF-8")) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine()
                            .replace("'", "\\'")  // Komillak escapatu
                            .trim();
                    if (!line.isEmpty()) {
                        content.append(line).append(" ");
                    }
                }
            }

            if (content.length() > 0) {
                pw.println("'" + content.toString().trim() + "'");
                count++;
            }
        }

        pw.close();
        System.out.println("✅ ARFF generatua (test_blind, " + count + " instantziekin) → " + outputPath);
    }
}