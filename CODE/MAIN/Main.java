package Main;

public class Main {
    public static void main(String[] args) {
        //args[0] pourrait être null ou que l'index 0 n'est pas valide si args est vide.
        if (args.length > 0 && args[0] != null) {
            DBParams.DBPath = args[0];
        } else {
            // Définissez une valeur par défaut ou traitez l'absence de chemin de base de données
            System.err.println("Avertissement : Aucun chemin de base de données fourni. Utilisation d'un chemin par défaut.");
            DBParams.DBPath = "chemin_par_défaut";
        }

        DBParams.SGBDPageSize = 4096;
        DBParams.DMFileCount = 4;
        DBParams.frameCount = 2;
    }
}
