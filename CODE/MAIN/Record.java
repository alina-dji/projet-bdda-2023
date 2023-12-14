package Main;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Record {
    private TableInfo tabInfo; // Relation à laquelle appartient le record
    private List<String> recValues; // Valeurs du record (sous forme de chaînes de caractères)
    private RecordId recordId;  // Nouvelle variable membre

    public Record(TableInfo tabInfo) {
        this.tabInfo = tabInfo;
        this.recValues = new ArrayList<>();
    }

     public void addValue(String value) {
        recValues.add(value);
    }

    public void displayRecord() {
        System.out.println("Record de la table " + tabInfo.tableName + ": " + recValues);
    }

    public TableInfo getTableInfo() {
        return tabInfo;
    }

    public List<String> getRecValues() {
        return recValues;
    }

    //ne touche pas a partir de la

    public int writeToBuffer(ByteBuffer buff, int pos) {
        if (buff == null || pos < 0 || pos > buff.capacity()) {
            throw new IllegalArgumentException("Invalide buffer ou position");
        }
    
        int totalBytesWritten = 0;
        List<ColInfo> colInfoList = tabInfo.getColumnInfo();
    
        for (int i = 0; i < recValues.size(); i++) {
            // Récupérer la valeur actuelle du record
            String value = recValues.get(i);
    
            // Récupérer les informations sur la colonne correspondante
            ColInfo colInfo = colInfoList.get(i);
    
            switch (colInfo.getType()) {
                case INT:
                    // Convertir la chaîne en entier et écrire dans le buffer
                    int intValue = Integer.parseInt(value);
                    buff.putInt(pos + totalBytesWritten, intValue);
                    totalBytesWritten += Integer.BYTES;
                    break;
    
                case FLOAT:
                    // Convertir la chaîne en float et écrire dans le buffer
                    float floatValue = Float.parseFloat(value);
                    buff.putFloat(pos + totalBytesWritten, floatValue);
                    totalBytesWritten += Float.BYTES;
                    break;
    
                case STRING_VAR:
                    // Écrire chaque caractère de la chaîne dans le buffer
                    byte[] stringBytes = value.getBytes(StandardCharsets.UTF_8);
                    buff.position(pos + totalBytesWritten);
                    buff.put(stringBytes);
                    totalBytesWritten += stringBytes.length;
                    break;
    
                case VARSTRING_VAR:
                    // Écrire chaque caractère de la chaîne dans le buffer avec une taille variable
                    byte[] varStringBytes = value.getBytes(StandardCharsets.UTF_8);
                    int varStringLength = varStringBytes.length;
                    buff.putInt(pos + totalBytesWritten, varStringLength);
                    totalBytesWritten += Integer.BYTES;
                    buff.position(pos + totalBytesWritten);
                    buff.put(varStringBytes);
                    totalBytesWritten += varStringLength;
                    break;
    
                default:
                    throw new IllegalArgumentException("Type de colonne non géré : " + colInfo.getType());
            }
    
            // Mettre à jour la position pour la prochaine colonne
            pos += colInfo.getSize();
        }
    
        return totalBytesWritten;
    }
    
    
    public int readFromBuffer(ByteBuffer buff, int pos) {
        if (buff == null || pos < 0 || pos > buff.capacity()) {
            throw new IllegalArgumentException("Invalide buffer ou position");
        }

        int totalBytesRead = 0;
        recValues.clear();  // Vider la liste avant de la remplir avec de nouvelles valeurs

        List<ColInfo> colInfoList = tabInfo.getColumnInfo();

        for (ColInfo colInfo : colInfoList) {
            switch (colInfo.getType()) {
                case INT:
                    // Lire un entier depuis le buffer
                    int intValue = buff.getInt(pos + totalBytesRead);
                    recValues.add(Integer.toString(intValue));
                    totalBytesRead += Integer.BYTES;
                    break;

                case FLOAT:
                    // Lire un float depuis le buffer
                    float floatValue = buff.getFloat(pos + totalBytesRead);
                    recValues.add(Float.toString(floatValue));
                    totalBytesRead += Float.BYTES;
                    break;

                case STRING_VAR:
                    // Lire une chaîne variable depuis le buffer
                    int stringLength = buff.getInt(pos + totalBytesRead);
                    byte[] stringBytes = new byte[stringLength];
                    buff.position(pos + totalBytesRead + Integer.BYTES);
                    buff.get(stringBytes);
                    recValues.add(new String(stringBytes, StandardCharsets.UTF_8));
                    totalBytesRead += Integer.BYTES + stringLength;
                    break;

                case VARSTRING_VAR:
                    // Lire une chaîne variable depuis le buffer
                    int varStringLength = buff.getInt(pos + totalBytesRead);
                    byte[] varStringBytes = new byte[varStringLength];
                    buff.position(pos + totalBytesRead + Integer.BYTES);
                    buff.get(varStringBytes);
                    recValues.add(new String(varStringBytes, StandardCharsets.UTF_8));
                    totalBytesRead += Integer.BYTES + varStringLength;
                    break;

                default:
                    throw new IllegalArgumentException("Type de colonne non géré : " + colInfo.getType());
            }
        }

        return totalBytesRead;
    }

    public void setRecordId(RecordId recordId) {
        this.recordId = recordId;
    }

    public RecordId getRecordId() {
        return recordId;
    }
    
    public int recordSizeFromValues() {
        int totalSize = 0;
        List<ColInfo> colInfoList = tabInfo.getColumnInfo();
    
        for (int i = 0; i < recValues.size(); i++) {
            String value = recValues.get(i);
            ColInfo colInfo = colInfoList.get(i);
    
            switch (colInfo.getType()) {
                case INT:
                    totalSize += Integer.BYTES;
                    break;
    
                case FLOAT:
                    totalSize += Float.BYTES;
                    break;
    
                case STRING_VAR:
                    totalSize += Integer.BYTES + value.getBytes(StandardCharsets.UTF_8).length;
                    break;
    
                case VARSTRING_VAR:
                    totalSize += Integer.BYTES + value.getBytes(StandardCharsets.UTF_8).length;
                    break;
    
                default:
                    throw new IllegalArgumentException("Type de colonne non géré : " + colInfo.getType());
            }
        }
    
        return totalSize;
    }
}
