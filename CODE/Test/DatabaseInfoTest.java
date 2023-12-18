package Test;

import java.util.ArrayList;
import java.util.List;
import Main.DatabaseInfo;
import Main.PageId;
import Main.TableInfo;

public class DatabaseInfoTest {

    public static void main(String[] args) {
        // Test de l'initialisation
        testInit();

        // Test de l'ajout d'une table
        testAddTable();

        // Test de la récupération d'une table
        testGetTable();

        // Test de la sauvegarde
        testFinish();
    }

    private static void testInit() {
        DatabaseInfo databaseInfo = DatabaseInfo.getInstance();
        databaseInfo.Init();
    }

    private static void testAddTable() {
        DatabaseInfo databaseInfo = DatabaseInfo.getInstance();

        // Création des listes de noms et types de colonnes
        List<String> colNames = new ArrayList<>();
        List<String> colTypes = new ArrayList<>();

        colNames.add("ColumnName1");
        colNames.add("ColumnName2");

        colTypes.add("ColumnType1");
        colTypes.add("ColumnType2");

        // Création d'une nouvelle instance de PageId pour simuler le headerPageId
        PageId headerPageId = new PageId(-1, 0);

        // Création d'une nouvelle instance de TableInfo avec le headerPageId
        TableInfo tableInfo = new TableInfo("TableName", 2, colNames, colTypes, headerPageId);

        // Ajout de la table à la base de données
        databaseInfo.AddTableInfo(tableInfo);
    }

    private static void testGetTable() {
        // DatabaseInfo databaseInfo = DatabaseInfo.getInstance();
        // TableInfo tableInfo = databaseInfo.GetTableInfo("TableName");
    }

    private static void testFinish() {
        DatabaseInfo databaseInfo = DatabaseInfo.getInstance();
        databaseInfo.Finish();
    }

}
