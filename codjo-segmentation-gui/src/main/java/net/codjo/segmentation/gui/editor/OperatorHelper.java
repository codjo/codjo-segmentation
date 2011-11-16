package net.codjo.segmentation.gui.editor;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
/**
 *
 */
public class OperatorHelper {
    private List<OperatorHelp> operatorMap;

    public OperatorHelper() {
        operatorMap = new ArrayList<OperatorHelp>();
        operatorMap.add(new OperatorHelp("+", "Description : plus\nExemple : (Cours d'ouverture + Cours de clôture) / 2"));
        operatorMap
              .add(new OperatorHelp("-", "Description : moins\nExemple : (Cours de clôture - Cours d'ouverture) / Cours d'ouverture"));
        operatorMap.add(new OperatorHelp("/", "Description : diviser\nExemple : (Cours d'ouverture + Cours de clôture) / 2"));
        operatorMap.add(new OperatorHelp("*",
                        "Description : multiplier\nExemple : (Cours de clôture - Cours d'ouverture) * (1 / Cours d'ouverture)"));
        operatorMap.add(new OperatorHelp("||", "Description : ou\nExemple : Pays ==\"FRA\" || Pays ==\"ESP\""));
        operatorMap.add(new OperatorHelp("&&", "Description : et\nExemple : Pays ==\"FRA\" && Devise ==\"EUR\""));
        operatorMap.add(new OperatorHelp("==", "Description : égal\nExemple : Pays ==\"FRA\""));
        operatorMap.add(new OperatorHelp("!=", "Description : différent\nExemple : Pays !=\"FRA\""));
        operatorMap.add(new OperatorHelp(">", "Description : strictement supérieur\nExemple : Cours de clôture > Cours d'ouverture"));
        operatorMap.add(new OperatorHelp("<", "Description : strictement inférieur\nExemple : Cours d'ouverture < Cours de clôture"));
        operatorMap.add(new OperatorHelp(">=", "Description : supérieur ou égal\nExemple : Cours de clôture >= Cours d'ouverture"));
        operatorMap.add(new OperatorHelp("<=", "Description : inférieur ou égal\nExemple : Cours d'ouverture <= Cours de clôture"));
        operatorMap.add(new OperatorHelp("(",
                        "Description : ouverture d'un bloc d'opérations arithmétiques ou logiques\nExemple : (Pays ==\"FRA\" || Pays == \"ESP\") && Devise ==\"EUR\""));
        operatorMap.add(new OperatorHelp(")",
                        "Description : fermeture d'un bloc d'opérations arithmétiques ou logiques\nExemple : (Cours d'ouverture + Cours de clôture) / 2"));
        operatorMap.add(new OperatorHelp(".", "Description : séparateur de décimales\nExemple : Cours d'ouverture <= 15.24"));
        operatorMap.add(new OperatorHelp(",",
                        "Description : séparateur de paramètres lors de l'utilisation de fonctions\nExemple : Math.min(Cours d'ouverture, Cours de clôture)"));
    }


    public List<OperatorHelp> getAllOperators() {
        return operatorMap;
    }

    public class OperatorHelp{
        private String operator;
        private String help;


        public String getHelp() {
            return help;
        }

        @Override
        public String toString(){
            return operator;
        }

        OperatorHelp(String operator, String help){
            this.operator = operator;
            this.help = help;
        }

    }
}
