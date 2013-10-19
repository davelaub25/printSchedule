/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package productionschedule;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dlaub
 */
class PoolTableModel extends AbstractTableModel {

    private ArrayList datalist = new ArrayList();
    protected Vector dataVector;
    protected Vector columnIdentifiers;
    public static Boolean stop = false;
    private boolean stopper = stop;

    private static Vector newVector(int size) {
        Vector v = new Vector(size);
        v.setSize(size);
        return v;
    }

    private static Vector nonNullVector(Vector v) {
        return (v != null) ? v : new Vector();
    }
    ////////////////////////////////////////////////////////////////////////////

    public PoolTableModel() {
    }
    ////////////////////////////////////////////////////////////////////////////

    public PoolTableModel(ArrayList l) {
        Object[] a;
        a = l.toArray();
        datalist.addAll(l);
        setDataVector(convertToVector(a), convertToVector(l));

    }
    ////////////////////////////////////////////////////////////////////////////

    public PoolTableModel(Object o) {
        Object[] a = {o};
        datalist.add(o);
        setDataVector(convertToVector(a), convertToVector(o));

    }
    ////////////////////////////////////////////////////////////////////////////

    public PoolTableModel(ArrayList l, ArrayList l2) {
        Object[] a;
        a = l.toArray();
        datalist.addAll(l);
        setDataVector(convertToVector(a), convertToVector(l));

    }
    ////////////////////////////////////////////////////////////////////////

    public int getColumnCount() {
        Field[] fieldList = Package.class.getFields();
        return fieldList.length;
    }
    ////////////////////////////////////////////////////////////////////////

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
    ////////////////////////////////////////////////////////////////////////

    public String getColumnName(int col) {
        Field[] fieldList = Package.class.getFields();
        return fieldList[col].getName();
    }
    ////////////////////////////////////////////////////////////////////////

    public Object getValueAt(int row, int col) {

        Object job;
        Vector v = new Vector();
        //v.add(stop);
        if (datalist.get(row).getClass().getSimpleName() == "Vector") {
            v = (Vector) datalist.get(row);
        }
        if ("Vector".equals(datalist.get(row).getClass().getSimpleName()) && v.get(0).getClass().getSimpleName() == "Vector") {
            job = (Object) datalist.get(row);
            Vector vec = (Vector) job;
            job = (Object) vec.get(0);
        } else if ("Vector".equals(datalist.get(row).getClass().getSimpleName())) {
            job = (Object) datalist.get(row);
            Vector vec = (Vector) job;
            job = (Object) vec.get(0);
        } else {
            job = (Object) datalist.get(row);
        }

        Field[] fieldList = job.getClass().getFields();
        try {
            return fieldList[col].get(job);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(PoolTableModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    ////////////////////////////////////////////////////////////////////////

    public boolean isCellEditable(int row, int col) {
        return true;
    }
    ////////////////////////////////////////////////////////////////////////

    public void insertRow(int row, Object rowData) {

        dataVector.insertElementAt(rowData, row);
        datalist.add(row, rowData);
        //justifyRows(row, row+1);
        fireTableRowsInserted(row, row);
    }
    ////////////////////////////////////////////////////////////////////////

    private void justifyRows(int from, int to) {
        // Sometimes the DefaultTableModel is subclassed
        // instead of the AbstractTableModel by mistake.
        // Set the number of rows for the case when getRowCount
        // is overridden.
        dataVector.setSize(getRowCount());

        for (int i = from; i < to; i++) {
            if (dataVector.elementAt(i) == null) {
                dataVector.setElementAt(new Vector(), i);
            }
            ((Vector) dataVector.elementAt(i)).setSize(getColumnCount());
        }
    }
    ////////////////////////////////////////////////////////////////////////

    public Vector getDataVector() {
        return dataVector;
    }
    ////////////////////////////////////////////////////////////////////////

    public void setDataVector(Vector dataVector, Vector columnIdentifiers) {
        this.dataVector = nonNullVector(dataVector);
        this.columnIdentifiers = nonNullVector(columnIdentifiers);
        //justifyRows(0, getRowCount());
        fireTableStructureChanged();
    }
    ////////////////////////////////////////////////////////////////////////

    public void setDataVector(Object[][] dataVector, Object[] columnIdentifiers) {
        setDataVector(convertToVector(dataVector), convertToVector(columnIdentifiers));
    }
    ////////////////////////////////////////////////////////////////////////

    protected static Vector convertToVector(Object[] anArray) {
        if (anArray == null) {
            return null;
        }
        Vector<Object> v = new Vector<Object>(anArray.length);
        for (Object o : anArray) {
            v.addElement(o);
        }
        return v;
    }
    ////////////////////////////////////////////////////////////////////////

    protected static Vector convertToVector(Object anObject) {
        if (anObject == null) {
            return null;
        }
        Vector<Object> v = new Vector<Object>(1);
        v.addElement(anObject);
        return v;
    }
    ////////////////////////////////////////////////////////////////////////

    static Vector convertToVector(ArrayList al) {
        return new Vector(al);
    }

    static Vector convertColumnNamesToVector(ArrayList al) {
        return new Vector(al);
    }
    ////////////////////////////////////////////////////////////////////////

    protected static Vector convertToVector(Object[][] anArray) {
        if (anArray == null) {
            return null;
        }
        Vector<Vector> v = new Vector<Vector>(anArray.length);
        for (Object[] o : anArray) {
            v.addElement(convertToVector(o));
        }
        return v;
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object aValue, int row, int column) {
        Vector rowVector = (Vector) dataVector.elementAt(row);
        rowVector.setElementAt(aValue, column);
        fireTableCellUpdated(row, column);
    }

    public void removeRow(int row) {
        dataVector.removeElementAt(row);
        datalist.remove(row);
        fireTableRowsDeleted(row, row);
    }
}
