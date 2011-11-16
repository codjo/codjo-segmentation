/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.preference.family;
import net.codjo.expression.FunctionHolder;
import java.util.ArrayList;
import java.util.List;
/**
 * Méthodes utilitaires pour les calculs.
 */
public class DefaultFunctionHolder implements FunctionHolder {
    public String caseOf(boolean[] sleeve, String[] sleeveId, String sleeveOtherId)
          throws IntersectionException {
        if (sleeve == null || sleeveId == null || sleeveOtherId == null) {
            throw new IllegalArgumentException("L'un des paramètres est null.");
        }

        if (sleeve.length != sleeveId.length) {
            throw new IllegalArgumentException(
                  "Les tableaux sleeve et sleeveId ont des tailles différentes.");
        }

        String result = null;
        List<String> intersectionList = null;

        for (int i = 0; i < sleeve.length; i++) {
            if (sleeve[i]) {
                if (result != null) {
                    // Cas intersection
                    if (intersectionList == null) {
                        intersectionList = new ArrayList<String>();
                        intersectionList.add(result);
                    }

                    intersectionList.add(sleeveId[i]);
                }

                result = sleeveId[i];
            }
        }

        if (intersectionList != null) {
            throw new IntersectionException("Les valeurs possibles sont : "
                                            + intersectionList);
        }

        if (result == null) {
            result = sleeveOtherId;
        }

        return result;
    }


    public String getName() {
        return "utils";
    }


    public List<String> getAllFunctions() {
        List<String> allFunction = new ArrayList<String>();
        allFunction.add("utils.caseOf(boolean[] sleeve, String[] sleeveId, String sleeveOtherId)");
        return allFunction;
    }


    /**
     * Exception levée lorsqu'il y a une intersection.
     */
    public static class IntersectionException extends Exception {
        public IntersectionException(String msg) {
            super(msg);
        }
    }
}
