package net.codjo.segmentation.server.participant;
import net.codjo.segmentation.server.blackboard.message.Level;
import net.codjo.segmentation.server.blackboard.message.Todo;
import net.codjo.segmentation.server.participant.common.Page;
import net.codjo.segmentation.server.participant.context.TodoContent;
import net.codjo.segmentation.server.preference.family.Row;
import net.codjo.segmentation.server.preference.family.RowFilter;
/**
 *
 */
public class CalculatorParticipantTest extends SegmentationParticipantTestCase<CalculatorParticipant> {
    private static final String[] COLUMN_NAMES = new String[]{"INPUT"};


    public CalculatorParticipantTest() {
        super(JdbcType.JDBC_HSQL);
    }


    public void test_compute() throws Exception {
        Page page = new Page();
        page.addRow(new Row(COLUMN_NAMES, new Object[]{"a"}));
        page.addRow(new Row(COLUMN_NAMES, new Object[]{"b"}));

        segmentationContext.putPage(todoContent.getPageId(), page);

        executeHandleTodo(new Todo<TodoContent>(1, todoContent));

        String expected = "evaluator.evaluate(row{INPUT=a})";
        expected += ", result.add(row{RESULT=a})";
        expected += ", evaluator.evaluate(row{INPUT=b})";
        expected += ", result.add(row{RESULT=b})";
        expected += ", result.close()";
        expected += ", write(Todo{-1}, Level{information}), erase(Todo{1}, Level{to-compute})";
        log.assertContent(expected);
    }


    public void test_compute_filter() throws Exception {
        String[] names = new String[]{"INPUT", "AP_FILTER$MY_VALUE"};

        Page page = new Page();
        page.addRow(new Row(names, new Object[]{"a", false}));
        page.addRow(new Row(names, new Object[]{"b", true}));

        segmentationContext.putPage(todoContent.getPageId(), page);
        segmentationContext.getFamilyPreference().setFilter(new MyRowFilter());

        executeHandleTodo(new Todo<TodoContent>(1, todoContent));

        String expected = "evaluator.evaluate(row{INPUT=a, AP_FILTER$MY_VALUE=false})";
        expected += ", result.add(row{RESULT=a})";
        expected += ", result.close()";
        expected += ", write(Todo{-1}, Level{information}), erase(Todo{1}, Level{to-compute})";
        log.assertContent(expected);
    }


    public void test_compute_evluationError() throws Exception {
        Page page = new Page();
        page.addRow(new Row(COLUMN_NAMES, new Object[]{"failure"}));
        page.addRow(new Row(COLUMN_NAMES, new Object[]{"b"}));

        segmentationContext.putPage(todoContent.getPageId(), page);

        executeHandleTodo(new Todo<TodoContent>(1, todoContent));

        String expected = "evaluator.evaluate(row{INPUT=failure})";
        expected += ", result.addError(row{RESULT=failure})";
        expected += ", evaluator.evaluate(row{INPUT=b})";
        expected += ", result.add(row{RESULT=b})";
        expected += ", result.close()";
        expected += ", write(Todo{-1}, Level{information}), erase(Todo{1}, Level{to-compute})";

        log.assertContent(expected);
    }


    @Override
    protected void doSetup() throws Exception {
        super.doSetup();
        segmentationContext = declareSegmentationContext(todoContent.getSegmentationId());
    }


    @Override
    protected Level getListenedLevel() {
        return new Level("to-compute");
    }


    @Override
    protected CalculatorParticipant createParticipant() {
        return new CalculatorParticipant(contextManager);
    }


    private static class MyRowFilter implements RowFilter<Boolean> {

        public boolean isRowExcluded(int segmentationId, Row row, Boolean filterValue) {
            return filterValue;
        }


        public String getTableName() {
            return "AP_FILTER";
        }


        public String getColumnName() {
            return "MY_VALUE";
        }
    }
}
