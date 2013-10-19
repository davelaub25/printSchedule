/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package productionschedule;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;
import java.util.ArrayList;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Dave
 */
class TableRowTransferHandler extends TransferHandler {

    public static final String address = "jdbc:mysql://davelaub.com:3306/dlaub25_lasersched";
    public static final String userName = "dlaub25_fmi";
    public static final String password = "admin";
    private int[] rows = null;
    private int addIndex = -1; //Location where items were added
    private int addCount = 0;  //Number of items added.
    private final DataFlavor localObjectFlavor;
    private Object[] transferedObjects = null;
    private JComponent source = null;
    private DatabaseObject dbo = new DatabaseObject(address, userName, password);

    public TableRowTransferHandler() {
        localObjectFlavor = new ActivationDataFlavor(
                Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        source = c;
        JTable table = (JTable) c;
        if (table.getModel().getClass().getSimpleName().equals("PoolTableModel")) {
            System.out.println("CreateTransferable: PoolTableModel Found");
            PoolTableModel model = (PoolTableModel) table.getModel();
            ArrayList<Object> list = new ArrayList<Object>();
            for (int i : rows = table.getSelectedRows()) {
                list.add(model.getDataVector().elementAt(i));
            }
            transferedObjects = list.toArray();
        } else if (table.getModel().getClass().getSimpleName().equals("PrinterTableModel")) {
            System.out.println("CreateTransferable: PrinterTableModel Found");
            PrinterTableModel model = (PrinterTableModel) table.getModel();
            ArrayList<Object> list = new ArrayList<Object>();
            for (int i : rows = table.getSelectedRows()) {
                list.add(model.getDataVector().elementAt(i));
            }
            transferedObjects = list.toArray();
        } else if (table.getModel().getClass().getSimpleName().equals("JobTableModel")) {
            System.out.println("CreateTransferable: JobTableModel Found");
            JobTableModel model = (JobTableModel) table.getModel();
            ArrayList<Object> list = new ArrayList<Object>();
            for (int i : rows = table.getSelectedRows()) {
                list.add(model.getDataVector().elementAt(i));
            }
            transferedObjects = list.toArray();
        }


        return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        JTable t = (JTable) info.getComponent();
        boolean b = info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
        //XXX bug?
        t.setCursor(b ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
        return b;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        JTable target = (JTable) info.getComponent();
        target.getRowCount();
        JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
        //DefaultTableModel tempModel = (DefaultTableModel) target.getModel();


        int index = dl.getRow();
        int max = target.getRowCount();
        if (index < 0 || index > max) {
            index = max;
        }
        addIndex = index;
        PrinterTableModel.stop = true;
        target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        try {
            Object[] values = (Object[]) info.getTransferable().getTransferData(localObjectFlavor);
            //addCount = values.length;
            if (source == target) {
                addCount = values.length;
            }
            for (int i = 0; i < values.length; i++) {
                JTable sourceTable = (JTable) source;
                JTable targetTable = (JTable) info.getComponent();
                if (targetTable.getModel().getClass().getSimpleName().equals("PrinterTableModel") && sourceTable.getModel().getClass().getSimpleName().equals("PoolTableModel")) {
                    System.out.println("ImportData: PoolTableModel to PrinterTableModel Found");
                    PrinterTableModel model = (PrinterTableModel) target.getModel();
                    JobPackage jP = new JobPackage((Package) values[i], dbo);
                    int idx = index++;
                    model.insertRow(idx, jP);
                    target.getSelectionModel().addSelectionInterval(idx, idx);
                } else if (targetTable.getModel().getClass().getSimpleName().equals("PoolTableModel") && sourceTable.getModel().getClass().getSimpleName().equals("PrinterTableModel")) {
                    System.out.println("ImportData: PrinterTableModel to PoolTableModel Found");
                    PoolTableModel model = (PoolTableModel) target.getModel();
                    Package p = new Package((JobPackage) values[i]);
                    int idx = index++;
                    model.insertRow(idx, p);
                    target.getSelectionModel().addSelectionInterval(idx, idx);
                } else if (target.getModel().getClass().getSimpleName().equals("PoolTableModel")) {
                    System.out.println("ImportData: PoolTableModel to PoolTableModel Found");
                    PoolTableModel model = (PoolTableModel) target.getModel();
                    int idx = index++;
                    model.insertRow(idx, values[i]);
                    target.getSelectionModel().addSelectionInterval(idx, idx);
                } else if (target.getModel().getClass().getSimpleName().equals("PrinterTableModel")) {
                    System.out.println("ImportData: PrinterTableModel to PrinterTableModel Found");
                    PrinterTableModel model = (PrinterTableModel) target.getModel();
                    int idx = index++;
                    model.insertRow(idx, values[i]);
                    target.getSelectionModel().addSelectionInterval(idx, idx);
                }

            }

            return true;
        } catch (Exception ufe) {
            ufe.printStackTrace();
        }
        return false;
    }

    @Override
    protected void exportDone(JComponent c, Transferable t, int act) {
        cleanup(c, act == MOVE);
    }

    private void cleanup(JComponent src, boolean remove) {
        if (remove && rows != null) {
            JTable table = (JTable) src;
            src.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            if (table.getModel().getClass().getSimpleName().equals("PoolTableModel")) {
                PoolTableModel model = (PoolTableModel) table.getModel();
                if (addCount > 0) {
                    for (int i = 0; i < rows.length; i++) {
                        if (rows[i] >= addIndex) {
                            rows[i] += addCount;
                        }
                    }
                }
                for (int i = rows.length - 1; i >= 0; i--) {
                    model.removeRow(rows[i]);
                }
            } else if (table.getModel().getClass().getSimpleName().equals("PrinterTableModel")) {
                PrinterTableModel model = (PrinterTableModel) table.getModel();
                if (addCount > 0) {
                    for (int i = 0; i < rows.length; i++) {
                        if (rows[i] >= addIndex) {
                            rows[i] += addCount;
                        }
                    }
                }
                for (int i = rows.length - 1; i >= 0; i--) {
                    model.removeRow(rows[i]);
                }
            } else if (table.getModel().getClass().getSimpleName().equals("JobTableModel")) {
                JobTableModel model = (JobTableModel) table.getModel();
                if (addCount > 0) {
                    for (int i = 0; i < rows.length; i++) {
                        if (rows[i] >= addIndex) {
                            rows[i] += addCount;
                        }
                    }
                }
                for (int i = rows.length - 1; i >= 0; i--) {
                    model.removeRow(rows[i]);
                }
            }

        }
        rows = null;
        addCount = 0;
        addIndex = -1;
    }
}
