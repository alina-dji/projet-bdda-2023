package Main;

import java.util.ArrayList;
import java.util.List;

public class TableInfo {

    String tableName;
    int nbrColumns;
    List<ColInfo> columnInfo;

    public TableInfo(String tableName, int nbrColumns, List<String> colNames, List<String> colTypes) {
        this.tableName = tableName;
        this.nbrColumns = nbrColumns;
        this.columnInfo = new ArrayList<>();


        // Vérification que le nombre de colonnes et la longueur des listes correspondent
        if (nbrColumns == colNames.size() && nbrColumns == colTypes.size()) {
            for (int i = 0; i < nbrColumns; i++) {
                ColInfo colInfo = new ColInfo(colNames.get(i), colTypes.get(i));
                this.columnInfo.add(colInfo);
            }
        } else {
            throw new IllegalArgumentException("Le nombre de colonnes ne correspond pas à la longueur des listes.");
        }
    }


    public List<ColInfo> getColumnInfo() {
        return columnInfo;
    }
    
}


