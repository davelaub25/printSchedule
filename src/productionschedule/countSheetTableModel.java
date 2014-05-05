package productionschedule;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;
import static productionschedule.JobTableModel.convertToVector;
import static productionschedule.JobTableModel.stop;

/**
 *
 * @author dlaub
 */
public class countSheetTableModel extends AbstractTableModel {

    private ArrayList datalist = new ArrayList();
    protected Vector dataVector;
    protected Vector columnIdentifiers;
    public static Boolean stop = false;
    private boolean stopper = stop;

    
    public countSheetTableModel(ArrayList l) {
        Object[] a;
        a = l.toArray();
        datalist.addAll(l);
        
    }
    public int getColumnCount() {
        Field[] fieldList = datalist.get(0).getClass().getFields();
        return fieldList.length;
    }
    ////////////////////////////////////////////////////////////////////////

    public Object getValueAt(int row, int col) {

        Object job;
        Vector v = new Vector();
        job = (Object) datalist.get(row);
        Field[] fieldList = job.getClass().getFields();
        try {
            return fieldList[col].get(job);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(JobTableModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Class getColumnClass(int c) {
        try {
            return getValueAt(0, c).getClass();
        } catch (NullPointerException e) {
            //System.out.println("INFO: Column class of type null found.  Defaulting to String class.\n");
            return String.class;
        }
    }
    ////////////////////////////////////////////////////////////////////////

    public int getRowCount() {
        return datalist.size();
    }
    public String getColumnName(int col) {
        Field[] fieldList = datalist.get(0).getClass().getFields();
        return fieldList[col].getName();
    }
}
