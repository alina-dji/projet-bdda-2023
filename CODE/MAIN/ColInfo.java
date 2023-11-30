package Main;

public class ColInfo {
    
    String name;
    ColType colType;
    private static final int T = 100; // Constante pour la taille maximale

    public enum ColType {
        INT,
        FLOAT,
        STRING_VAR,
        VARSTRING_VAR
    }

    public ColInfo(String name, String colType) {
        this.name = name;

        if (!isValidColType(colType)) {
            throw new IllegalArgumentException("Type de colonne invalide : " + colType);
        }

        this.colType = ColType.valueOf(colType);
    }

    public ColType getType() {
        return colType;
    }

    // Méthode pour valider le type de colonne
    private boolean isValidColType(String type) {
        return type.equals("INT") || type.equals("FLOAT") || type.equals("STRING_VAR") || type.equals("VARSTRING_VAR");
    }

    public int getSize() {
        switch (colType) {
            case INT:
                return Integer.BYTES;
            case FLOAT:
                return Float.BYTES;
            case STRING_VAR:
            case VARSTRING_VAR:
            // Taille de la chaîne variable (taille stockée en tant qu'entier) + taille de la chaîne
            // Vous devez remplacer T par la valeur appropriée pour votre application
            return Integer.BYTES + T * Character.BYTES;
            default:
                throw new IllegalArgumentException("Type de colonne non géré : " + colType);
        }
    }

}
