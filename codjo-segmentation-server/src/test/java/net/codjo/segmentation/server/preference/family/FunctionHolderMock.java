package net.codjo.segmentation.server.preference.family;
import net.codjo.expression.FunctionHolder;
import java.util.List;
/**
 *
 */
public class FunctionHolderMock implements FunctionHolder {
    public String getName() {
        return "mock";
    }


    public List<String> getAllFunctions() {
        return null;
    }
}
