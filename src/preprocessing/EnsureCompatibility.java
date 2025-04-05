package preprocessing;

import java.io.File;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.FixedDictionaryStringToWordVector;
import weka.filters.unsupervised.attribute.Reorder;


public class EnsureCompatibility {

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("Uso: java -jar EnsureCompatibility.jar input.arff dictionary.txt output.arff");
            return;
        }

        String inputArff = args[0];
        String dictionaryFile = args[1];
        String outputArff = args[2];

        // Datuak kargatu
        DataSource source = new DataSource(inputArff);
        Instances data = source.getDataSet();

        // Klasearen indizea ezarri (azken atributua)
        data.setClassIndex(data.numAttributes() - 1);

        // FixedDictionaryStringToWordVector aplikatu
        FixedDictionaryStringToWordVector fdsv = new FixedDictionaryStringToWordVector();
        fdsv.setDictionaryFile(new File(dictionaryFile));
        fdsv.setInputFormat(data);
        Instances filteredData = Filter.useFilter(data, fdsv);

        // Atributuak berrantolatu klasea amaieran kokatzeko
        if (filteredData.classIndex() >= 0) {
            Reorder reorder = new Reorder();

            // Atributuen ordena definitu
            String indices = "";

            if (filteredData.classIndex() > 0) {
                indices += "1-" + filteredData.classIndex();
            }
            if (filteredData.classIndex() < filteredData.numAttributes() - 1) {
                if (!indices.isEmpty()) indices += ",";
                indices += (filteredData.classIndex() + 2) + "-" + filteredData.numAttributes();
            }
            if (!indices.isEmpty()) indices += ",";
            indices += (filteredData.classIndex() + 1);

            reorder.setAttributeIndices(indices);
            reorder.setInputFormat(filteredData);
            filteredData = Filter.useFilter(filteredData, reorder);
        }

        // Irteerako fitxategia gorde
        ArffSaver saver = new ArffSaver();
        saver.setInstances(filteredData);
        saver.setFile(new File(outputArff));
        saver.writeBatch();

        System.out.println("Transformación completada. Archivo guardado en: " + outputArff);
    }
}
