package Main;

/*import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;*/

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class DatabaseInfo {


    // Nouveau nom de la constante pour le fichier de sauvegarde
    public static final String Sauvegarde = "DBInfo.save";

    private static DatabaseInfo instance;
    private List<TableInfo> tables;
    private int tableCounter;

    private DatabaseInfo() {
        // Constructeur privé pour s'assurer qu'il n'y a qu'une seule instance de la classe
        tables = new ArrayList<>();
        tableCounter = 0;
    }

    public static DatabaseInfo getInstance() {
        if (instance == null) {
            instance = new DatabaseInfo();
        }
        return instance;
    }

    
    // Méthode d'initialisation pour lire les informations depuis le fichier de sauvegarde
    public void Init() {  
        // Récupération du fichier de sauvegarde
        File file = getSaveFile();
    
        // Vérification si le fichier de sauvegarde existe
        if (!file.exists()) {
            System.out.println("Le fichier de sauvegarde n'existe pas. Aucune opération d'initialisation nécessaire.");
            return;
        }
    
        // Utilisation du try-with-resources pour simplifier la gestion des ressources
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
    
            // Lecture des objets sérialisés depuis le fichier de sauvegarde
            Object[] objects = (Object[]) ois.readObject();
            TableInfo temp;
    
            // Ajout des objets TableInfo à la liste des tables
            for (Object obj : objects) {
                if (obj instanceof TableInfo) {
                    temp = (TableInfo) obj;
                    tables.add(temp);
                    tableCounter++;
                } else {
                    throw new ClassNotFoundException("Le fichier de sauvegarde ne contient pas d'objets TableInfo !");
                }
            }
    
            System.out.println("Initialisation réussie depuis le fichier de sauvegarde.");
    
        } catch (IOException | ClassNotFoundException e) {
            // Gestion des exceptions en cas d'erreur lors de la lecture du fichier
            e.printStackTrace();
        }
    }
    


    public void Finish() {
        try {
            // Récupération du fichier de sauvegarde
            File file = getSaveFile();

            // Création d'un flux de sortie pour écrire dans le fichier
            FileOutputStream fos = new FileOutputStream(file);

            // Création d'un flux d'objets pour sérialiser les objets
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            // Écriture de la liste d'objets TableInfo dans le fichier
            oos.writeObject(tables.toArray());

            // Fermeture du flux d'objets
            oos.close();

            System.out.println("Sauvegarde réussie dans le fichier.");
        } catch (IOException e) {
            // Gestion des exceptions en cas d'erreur lors de l'écriture dans le fichier
            e.printStackTrace();
        }
    }


    public void AddTableInfo(TableInfo tableInfo) {
        tables.add(tableInfo);
        tableCounter++;
        System.out.println("Table ajoutée : " + tableInfo.tableName);
        System.out.println("Nombre total de tables : " + tableCounter);
    }

    public TableInfo GetTableInfo(String tableName) {
        for (TableInfo table : tables) {
            if (table.tableName.equals(tableName)) {
                //vérifie si le nom de la table actuelle correspond au nom de la table recherchée 
                System.out.println("Table trouvée : " + table.tableName);
                return table;
            }
        }
        System.out.println("Table non trouvée : " + tableName);
        return null; // Retourne null si la relation n'est pas trouvée
    }

    private File getSaveFile() {
        return new File(Paths.get(DBParams.DBPath, Sauvegarde).toAbsolutePath().toString());
    }

   
    /*Methode supp
    public void DeleteDatabase() {
        File file = getSaveFile();
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("Fichier de sauvegarde supprimé avec succès.");
            } else {
                System.out.println("Erreur lors de la suppression du fichier de sauvegarde.");
            }
        }
    }*/

    
}
