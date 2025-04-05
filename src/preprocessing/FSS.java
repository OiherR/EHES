package preprocessing;

import java.io.*;
import weka.attributeSelection.*;
import weka.core.*;
import weka.core.converters.*;
import weka.filters.*;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.core.converters.ConverterUtils.DataSource;


public class FSS {
    public static void main(String[] args) throws Exception {
        // Argumentuak balidatu
        if (args.length < 3 || args.length > 4) {
            System.err.println("Erabilera: java FSS input.arff output.arff diccionario.txt [numAtributos]");
            System.err.println("Adibidea: java FSS train.arff train_fss.arff hiztegia.txt 50");
            System.exit(1);
        }

        try {
            // 1. Datuak kargatu
            System.out.println("Datuak kargatzen...");
            Instances data = new DataSource(args[0]).getDataSet();  // @arg1 input.arff
            data.setClassIndex(data.numAttributes() - 1);
            System.out.printf("Atributu kopurua: %d%n", data.numAttributes());

            // 2. Atributu hautapena konfiguratu (InfoGain + Ranker)
            InfoGainAttributeEval evaluator = new InfoGainAttributeEval();
            Ranker ranker = new Ranker();
            ranker.setThreshold(-1.7976931348623157E308);  // Atributu guztiak hartu

            // 3. Hautatzeko atributu kopurua (aukerakoa) - @opt numAtributos
            if (args.length == 4) {
                int numAttr = Integer.parseInt(args[3]);
                if (numAttr > 0 && numAttr <= data.numAttributes()) {
                    ranker.setNumToSelect(numAttr);
                    System.out.printf("Hautatutako atributu kopurua: %d%n", numAttr);
                } else {
                    System.err.println("Oharra: atributu kopuru baliogabea. Guztiak hautatzen.");
                }
            }

            // 4. Iragazkia aplikatu
            AttributeSelection filter = new AttributeSelection();
            filter.setEvaluator(evaluator);
            filter.setSearch(ranker);
            filter.setInputFormat(data);
            Instances filteredData = Filter.useFilter(data, filter);

            // 5. Datuak gorde - @arg2 output.arff
            ArffSaver saver = new ArffSaver();
            saver.setInstances(filteredData);
            saver.setFile(new File(args[1]));
            saver.writeBatch();

            // 6. Hiztegia gorde - @arg3 diccionario.txt
            try (PrintWriter pw = new PrintWriter(args[2])) {
                pw.println("=== METADATA ===");
                pw.printf("Jatorrizko atributuak: %d%n", data.numAttributes());
                pw.printf("Hautatutako atributuak: %d%n", filteredData.numAttributes() - 1);

                for (int i = 0; i < filteredData.numAttributes() - 1; i++) {
                    String attrName = filteredData.attribute(i).name();
                    int originalIdx = -1;
                    for (int j = 0; j < data.numAttributes(); j++) {
                        if (data.attribute(j).name().equals(attrName)) {
                            originalIdx = j + 1;  // Weka-ren indizeak 1-tik hasten dira
                            break;
                        }
                    }
                    pw.printf("%d|%s%n", originalIdx, attrName);
                }
            }

            System.out.println("✅ Prozesua ondo burutu da!");
            System.out.println("Irteerako ARFF: " + args[1]);
            System.out.println("Hiztegia: " + args[2]);

        } catch (Exception e) {
            System.err.println("❌ Errorea: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}