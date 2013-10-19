/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package productionschedule;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TooManyListenersException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListSelectionModel;
import javax.swing.DropMode;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author dlaub
 */
public class UI extends javax.swing.JFrame {

    private DefaultTableModel tableModel;
    private JTable table;
    public ArrayList<Job> jobs;
    public ArrayList<Package> pkgs;
    public static JTable jobPoolTable;
    public static JTable packagePoolTable;
    public static JTable bonniePool;
    public static JTable clydePool;
    public static JTable ocePool;
    public TransferHandler jtmHandler = new TableRowTransferHandler();
    public DatabaseObject dbo = new DatabaseObject("jdbc:mysql://davelaub.com:3306/dlaub25_lasersched", "dlaub25_fmi", "admin");
    static SplashScreen mySplash; 
    static Graphics2D splashGraphics;               // graphics context for overlay of the splash image
    static Rectangle2D.Double splashTextArea1;       // area where we draw the text
    static Rectangle2D.Double splashProgressArea1;   // area where we draw the progress bar
    static Rectangle2D.Double splashTextArea2;       // area where we draw the text
    static Rectangle2D.Double splashProgressArea2;   // area where we draw the progress bar
    static Font font; 
    public static int height;
    public static int width;
    public static TableRowSorter bonnieSorter;
    public static TableRowSorter clydeSorter;
    public static TableRowSorter oceSorter;
    public static Preferences prefs;

    //public static Job j = new Job(1, "A", "B", "C", "D", "E", 2);
    /**
     * Creates new form UI
     */
    public UI() throws TooManyListenersException, ClassNotFoundException, SQLException, IllegalArgumentException, IllegalAccessException, MalformedURLException, NullPointerException, IOException {
        
        mySplash = SplashScreen.getSplashScreen();
        
        prefs = Preferences.userRoot().node(this.getClass().getName());
        
        
        
        Dimension ssDim = mySplash.getSize();
        height = ssDim.height;
        width = ssDim.width;
        if (mySplash != null)
        {
            // get the size of the image now being displayed


            
            // stake out some area for our status information
            splashTextArea2 = new Rectangle2D.Double(width * .30, height*0.48, width * .45, 32.);
            splashProgressArea2 = new Rectangle2D.Double(width * .30, height*.52, width*.4, 24 );
            splashTextArea1 = new Rectangle2D.Double(width * .30, height*0.38, width * .45, 32.);
            splashProgressArea1 = new Rectangle2D.Double(width * .30, height*.42, width*.4, 24 );

            // create the Graphics environment for drawing status info
            splashGraphics = mySplash.createGraphics();
            font = new Font("Arial", Font.BOLD, 24);
            splashGraphics.setFont(font);

            // initialize the status info
            splashText1("Loading Jobs...");
            splashProgress1(0);
        }
        Graphics2D g = mySplash.createGraphics();
        
        initComponents();
        
        String jobQuery = "SELECT * FROM jobs";
        DatabaseOutputObject jobDboo = DatabaseTools.queryDatabase(dbo, jobQuery);
        jobs = ProductionSchedule.importHandler(jobDboo);
        String pkgQuery = "SELECT * FROM packages ORDER BY packages.queuePos ASC ";
        DatabaseOutputObject pkgDboo = DatabaseTools.queryDatabase(dbo, pkgQuery);
        pkgs = ProductionSchedule.pkgImportHandler(pkgDboo, Package.class);

        ArrayList bonnie = new ArrayList();
        ArrayList clyde = new ArrayList();
        ArrayList oce = new ArrayList();
        
        double d = 100.0;
        double e = (double)pkgs.size();
        int increment = (int)Math.ceil(d/e);
        UI.splashProgress2(0);
        UI.splashText2("Loading Packages...");
        int progress = 0;
        for (int i = 0; i < pkgs.size(); i++) {
            System.out.println("Adding Package");
            try {
                if (!pkgs.get(i).queuePos.isEmpty()) {
                    if (pkgs.get(i).queuePos.startsWith("B")) {
                        JobPackage jP = new JobPackage(pkgs.get(i), dbo);
                        bonnie.add(jP);
                    } else if (pkgs.get(i).queuePos.startsWith("C")) {
                        JobPackage jP = new JobPackage(pkgs.get(i), dbo);
                        clyde.add(jP);
                    } else if (pkgs.get(i).queuePos.startsWith("O")) {
                        JobPackage jP = new JobPackage(pkgs.get(i), dbo);
                        oce.add(jP);
                    }
                }
            } catch (NullPointerException ex) {
            }
            progress = progress + increment;
            UI.splashProgress2(progress);
        }
        int numJobs = jobs.size();
        ArrayList<Job> jl = new ArrayList();
        jl.addAll(jobs);
        for (int i = 0; i < numJobs; i++) {
            System.out.println("Adding Jobs");
            Job j = jobs.get(i);
            int numQueued = 0;
            for (Package p : j.packages) {
                try {
                    if (!p.queuePos.isEmpty()) {
                        numQueued++;
                    }
                } catch (NullPointerException ex) {
                }
            }
            if (numQueued == j.packages.size()) {
                jl.remove(j);
            }
        }
        
        AbstractTableModel jtm = new JobTableModel(jl);
        AbstractTableModel ptm = new PoolTableModel();
        AbstractTableModel btm = new PrinterTableModel(bonnie);
        AbstractTableModel ctm = new PrinterTableModel(clyde);
        AbstractTableModel otm = new PrinterTableModel(oce);


        jobPoolTable = new JTable(jtm);
        packagePoolTable = new JTable(ptm);
        bonniePool = new JTable(btm);
        clydePool = new JTable(ctm);
        ocePool = new JTable(otm);
        
        TableColumn tc = bonniePool.getColumnModel().getColumn(5);
        JComboBox cb = new JComboBox();
        cb.addItem("Queued");
        cb.addItem("Printed");
        DefaultCellEditor dce = new DefaultCellEditor(cb);
        tc.setCellEditor(dce);

        jobPoolTable.getSelectionModel().addListSelectionListener(new RowSelectedListener());
        jobPoolTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        TableFocusListener tfl = new TableFocusListener();
        bonniePool.addFocusListener(tfl);
        clydePool.addFocusListener(tfl);
        ocePool.addFocusListener(tfl);

        jobPoolTable.setDragEnabled(false);
        jobPoolTable.setDropMode(DropMode.INSERT_ROWS);
        jobPoolTable.setTransferHandler(jtmHandler);
        packagePoolTable.setDragEnabled(true);
        packagePoolTable.setDropMode(DropMode.INSERT_ROWS);
        packagePoolTable.setTransferHandler(jtmHandler);
        bonniePool.setDragEnabled(true);
        bonniePool.setDropMode(DropMode.INSERT_ROWS);
        bonniePool.setTransferHandler(jtmHandler);
        BonnieModelListener bml = new BonnieModelListener();
        bonniePool.getModel().addTableModelListener(bml);
        clydePool.setDragEnabled(true);
        clydePool.setDropMode(DropMode.INSERT_ROWS);
        clydePool.setTransferHandler(jtmHandler);
        ClydeModelListener cml = new ClydeModelListener();
        clydePool.getModel().addTableModelListener(cml);
        ocePool.setDragEnabled(true);
        ocePool.setDropMode(DropMode.INSERT_ROWS);
        ocePool.setTransferHandler(jtmHandler);
        OceModelListener oml = new OceModelListener();
        ocePool.getModel().addTableModelListener(oml);

        jobPoolPane.setViewportView(jobPoolTable);
        pkgPoolPane.setViewportView(packagePoolTable);
        bonniePane.setViewportView(bonniePool);
        clydePane.setViewportView(clydePool);
        ocePane.setViewportView(ocePool);

        jobPoolTable.setFillsViewportHeight(true);
        packagePoolTable.setFillsViewportHeight(true);
        bonniePool.setFillsViewportHeight(true);
        clydePool.setFillsViewportHeight(true);
        ocePool.setFillsViewportHeight(true);
        //splash.close();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        pkgPoolPane = new javax.swing.JScrollPane();
        jobPoolPane = new javax.swing.JScrollPane();
        bonniePane = new javax.swing.JScrollPane();
        clydePane = new javax.swing.JScrollPane();
        ocePane = new javax.swing.JScrollPane();
        jSeparator1 = new javax.swing.JSeparator();
        refreshButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        Test = new javax.swing.JButton();
        jToolBar1 = new javax.swing.JToolBar();
        unschedButton = new javax.swing.JButton();
        commitButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        testButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pkgPoolPane.setPreferredSize(new java.awt.Dimension(100, 200));

        jobPoolPane.setToolTipText("");
        jobPoolPane.setPreferredSize(new java.awt.Dimension(100, 200));

        bonniePane.setPreferredSize(new java.awt.Dimension(100, 200));

        clydePane.setPreferredSize(new java.awt.Dimension(100, 200));

        ocePane.setPreferredSize(new java.awt.Dimension(100, 200));

        refreshButton.setText("Refresh List");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel1.setText("Clyde");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel2.setText("Bonnie");

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel3.setText("Oce");

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel4.setText("Jobs");

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel5.setText("Packages");

        Test.setText("Test");
        Test.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TestActionPerformed(evt);
            }
        });

        jToolBar1.setRollover(true);

        unschedButton.setText("UnSchedule");
        unschedButton.setToolTipText("Clicking this button with a package in a printer pool selected will un-schedule the job and put it back into the package pool.");
        unschedButton.setBorder(null);
        unschedButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        unschedButton.setFocusable(false);
        unschedButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        unschedButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        unschedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unschedButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(unschedButton);

        commitButton.setText("Commit");
        commitButton.setFocusable(false);
        commitButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        commitButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        commitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commitButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(commitButton);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addComponent(clydePane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ocePane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bonniePane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jobPoolPane, javax.swing.GroupLayout.PREFERRED_SIZE, 483, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(pkgPoolPane, javax.swing.GroupLayout.DEFAULT_SIZE, 908, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1)
                            .addComponent(refreshButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(Test)
                        .addGap(73, 73, 73)))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 626, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jobPoolPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pkgPoolPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bonniePane, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clydePane, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ocePane, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Test)
                    .addComponent(refreshButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("tab1", jPanel1);

        jPanel3.setMinimumSize(new java.awt.Dimension(50, 1200));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(158, 158, 158)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(715, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(848, Short.MAX_VALUE))
        );

        jScrollPane2.setViewportView(jPanel3);

        jButton1.setText("jButton1");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        testButton.setText("Test");
        testButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(jButton1))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(88, 88, 88)
                        .addComponent(testButton)))
                .addGap(191, 191, 191)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1079, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 966, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(jButton1)
                .addGap(271, 271, 271)
                .addComponent(testButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("tab2", jPanel2);

        jMenu1.setText("File");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem1.setText("Preferences");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
//        try {
//            ProductionSchedule.exportHandler(j);
//        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (NoSuchFieldException ex) {
//            Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IllegalArgumentException ex) {
//            Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//                        Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
        //            }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void testButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testButtonActionPerformed
    }//GEN-LAST:event_testButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void commitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_commitButtonActionPerformed
        ProductionSchedule.commitTables();
    }//GEN-LAST:event_commitButtonActionPerformed

    private void TestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TestActionPerformed
        try {
            String query = "SELECT * FROM jobs LIMIT 1";
            DatabaseOutputObject dboo = DatabaseTools.queryDatabase(dbo, query);
            int columnCount = dboo.rowSet.getMetaData().getColumnCount();
            jobs = ProductionSchedule.importHandler(dboo);
            Job j = (Job) jobs.get(0);
            ArrayList packList = j.packages;
            ArrayList bonnie = new ArrayList();
            Package p = (Package) j.packages.get(0);
            JobPackage jP = new JobPackage(p, dbo);
            ArrayList[] al = ProductionSchedule.exportHandler(jP);

            for (ArrayList a : al) {
                for (Object o : a) {
                    System.out.println(o.toString());
                }
            }

        } catch (ClassNotFoundException | SQLException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException ex) {
            Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
        }


    }//GEN-LAST:event_TestActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        String[] args = null;
        PrefWindow.main(args);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void unschedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unschedButtonActionPerformed
        ArrayList<JobPackage> removalList = new ArrayList<>();
        boolean jobNotInPool = true;
        ArrayList jobsToAdd = new ArrayList();
        JobTableModel jtm = (JobTableModel)jobPoolTable.getModel();
        if(bonniePool.isFocusOwner()){
            System.out.println("Bonnie Has Focus");
            int rowsSelected[] = bonniePool.getSelectedRows();
            PrinterTableModel tempModel = (PrinterTableModel)bonniePool.getModel();
            for (int i = 0; i < rowsSelected.length; i++) {
                JobPackage jp = (JobPackage)tempModel.dataVector.get(rowsSelected[i]);
                removalList.add((JobPackage)tempModel.dataVector.get(rowsSelected[i]));
                for (int j = 0; j < jtm.getRowCount(); j++) {
                    Job job = (Job)jtm.dataVector.get(j);
                    System.out.println("Checking if " + jp.id + " is equal to " + job.id);
                    if(jp.id == job.id){
                        System.out.println("Job In Job Pool");
                        jobNotInPool = false;
                    }
                }
                if(jobNotInPool){
                    try {
                        jobsToAdd.add(new Job(jp));
                    } catch ( ClassNotFoundException | SQLException | IllegalArgumentException | IllegalAccessException ex) {
                        Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                tempModel.removeRow(bonniePool.getSelectedRow() + i);
            }
            for (int i = 0; i < jobsToAdd.size(); i++) {
                if(jobNotInPool){
                    System.out.println("Adding to model");
                    jtm.insertRow(0, jobsToAdd.get(i));
                }
            }
        }
        else if(clydePool.isFocusOwner()){
            System.out.println("Clyde Has Focus");
            int rowsSelected[] = clydePool.getSelectedRows();
            PrinterTableModel tempModel = (PrinterTableModel)clydePool.getModel();
            for (int i = 0; i < rowsSelected.length; i++) {
                JobPackage jp = (JobPackage)tempModel.dataVector.get(rowsSelected[i]);
                removalList.add((JobPackage)tempModel.dataVector.get(rowsSelected[i]));
                for (int j = 0; j < jtm.getRowCount(); j++) {
                    Job job = (Job)jtm.dataVector.get(j);
                    System.out.println("Checking if " + jp.id + " is equal to " + job.id);
                    if(jp.id == job.id){
                        System.out.println("Job In Job Pool");
                        jobNotInPool = false;
                    }
                }
                if(jobNotInPool){
                    try {
                        jobsToAdd.add(new Job(jp));
                    } catch ( ClassNotFoundException | SQLException | IllegalArgumentException | IllegalAccessException ex) {
                        Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                tempModel.removeRow(clydePool.getSelectedRow() + i);
            }
            for (int i = 0; i < jobsToAdd.size(); i++) {
                if(jobNotInPool){
                    System.out.println("Adding to model");
                    jtm.insertRow(0, jobsToAdd.get(i));
                }
            }
        }
        else if (ocePool.isFocusOwner()){
            System.out.println("Oce Has Focus");
            int rowsSelected[] = ocePool.getSelectedRows();
            PrinterTableModel tempModel = (PrinterTableModel)ocePool.getModel();
            for (int i = 0; i < rowsSelected.length; i++) {
                JobPackage jp = (JobPackage)tempModel.dataVector.get(rowsSelected[i]);
                removalList.add((JobPackage)tempModel.dataVector.get(rowsSelected[i]));
                for (int j = 0; j < jtm.getRowCount(); j++) {
                    Job job = (Job)jtm.dataVector.get(j);
                    System.out.println("Checking if " + jp.id + " is equal to " + job.id);
                    if(jp.id == job.id){
                        System.out.println("Job In Job Pool");
                        jobNotInPool = false;
                    }
                }
                if(jobNotInPool){
                    try {
                        jobsToAdd.add(new Job(jp));
                    } catch ( ClassNotFoundException | SQLException | IllegalArgumentException | IllegalAccessException ex) {
                        Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                tempModel.removeRow(ocePool.getSelectedRow() + i);
            }
            for (int i = 0; i < jobsToAdd.size(); i++) {
                if(jobNotInPool){
                    System.out.println("Adding to model");
                    jtm.insertRow(0, jobsToAdd.get(i));
                }
            }
        }
        try {
            ProductionSchedule.unsched(removalList);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException | SQLException ex) {
            Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
        }
        int selectedJob = jobPoolTable.getSelectedRow();
        updatePkgPool(selectedJob);
    }//GEN-LAST:event_unschedButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new UI().setVisible(true);
                } catch (TooManyListenersException | ClassNotFoundException | SQLException | IllegalArgumentException | IllegalAccessException | NullPointerException ex) {
                    Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Test;
    private javax.swing.JScrollPane bonniePane;
    private javax.swing.JScrollPane clydePane;
    private javax.swing.JButton commitButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JScrollPane jobPoolPane;
    private javax.swing.JScrollPane ocePane;
    private javax.swing.JScrollPane pkgPoolPane;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton testButton;
    private javax.swing.JButton unschedButton;
    // End of variables declaration//GEN-END:variables

    public static void errorWindow(String errorMessage) {
        Frame f = new Frame();
        JOptionPane.showMessageDialog(f, errorMessage);
    }

    protected static Vector convertToVector(Object[] anArray, int i) {
        if (anArray == null) {
            return null;
        }
        Vector<Object> v = new Vector<Object>(anArray.length);
        v.add(anArray[i]);

        return v;
    }

    class TargetListener implements DropTargetListener {

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            System.out.println("DragEnter");
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            System.out.println("DragOver");
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
            System.out.println("DropActionChanged");
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            System.out.println("DragExit");
        }

        @Override
        public void drop(DropTargetDropEvent dtde) {
            System.out.println("Drop");
        }
    }
    
    class TableFocusListener implements FocusListener{

        @Override
        public void focusGained(FocusEvent e) {
            
        }

        @Override
        public void focusLost(FocusEvent e) {
            JTable tempTable = (JTable)e.getSource();
            tempTable.clearSelection();
        }
        
    }

    class RowSelectedListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            DefaultListSelectionModel dlsm = (DefaultListSelectionModel) e.getSource();
            int selectedRow = dlsm.getLeadSelectionIndex();
            updatePkgPool(selectedRow);
        }
    }

    class BonnieModelListener implements TableModelListener {

        @Override
        public void tableChanged(TableModelEvent e) {
            PrinterTableModel ptm = (PrinterTableModel) e.getSource();
            int firstRow = e.getFirstRow();
            int lastRow = e.getLastRow();
            int index = e.getColumn();
            System.out.println("PrinterTableModelHasChanged!");
            switch (e.getType()) {
                case TableModelEvent.INSERT:
                    for (int i = 0; i <= ptm.getRowCount() - 1; i++) {
                        System.out.println("Number Of Rows: " + ptm.getRowCount());
                        String s = "B-" + (i + 1);
                        System.out.println("Row Inserted");
                        System.out.println("First Row: " + firstRow + " Last Row: " + lastRow);
                        ptm.setValueAt(s, i, 12);
                    }
                    break;
                case TableModelEvent.UPDATE:
                    System.out.println("Row Updated");
                    break;
                case TableModelEvent.DELETE:
                    for (int i = 0; i <= ptm.getRowCount() - 1; i++) {
                        System.out.println("Number Of Rows: " + ptm.getRowCount());
                        String s = "B-" + (i + 1);
                        System.out.println("Row Deleted");
                        System.out.println("First Row: " + firstRow + " Last Row: " + lastRow);
                        ptm.setValueAt(s, i, 12);
                    }
                    break;
            }
            if(prefs.getBoolean("AutoCommit", false)){
                ProductionSchedule.commitTables();
            }
        }
    }

    class ClydeModelListener implements TableModelListener {

        @Override
        public void tableChanged(TableModelEvent e) {
            PrinterTableModel ptm = (PrinterTableModel) e.getSource();
            int firstRow = e.getFirstRow();
            int lastRow = e.getLastRow();
            int index = e.getColumn();
            System.out.println("PrinterTableModelHasChanged!");
            switch (e.getType()) {
                case TableModelEvent.INSERT:
                    for (int i = 0; i <= ptm.getRowCount() - 1; i++) {
                        System.out.println("Number Of Rows: " + ptm.getRowCount());
                        String s = "C-" + (i + 1);
                        System.out.println("Row Inserted");
                        System.out.println("First Row: " + firstRow + " Last Row: " + lastRow);
                        ptm.setValueAt(s, i, 12);
                    }
                    break;
                case TableModelEvent.UPDATE:
                    System.out.println("Row Updated");
                    break;
                case TableModelEvent.DELETE:
                    for (int i = 0; i <= ptm.getRowCount() - 1; i++) {
                        System.out.println("Number Of Rows: " + ptm.getRowCount());
                        String s = "C-" + (i + 1);
                        System.out.println("Row Deleted");
                        System.out.println("First Row: " + firstRow + " Last Row: " + lastRow);
                        ptm.setValueAt(s, i, 12);
                    }
                    break;
            }
        }
    }

    class OceModelListener implements TableModelListener {

        @Override
        public void tableChanged(TableModelEvent e) {
            PrinterTableModel ptm = (PrinterTableModel) e.getSource();
            int firstRow = e.getFirstRow();
            int lastRow = e.getLastRow();
            int index = e.getColumn();
            System.out.println("PrinterTableModelHasChanged!");
            switch (e.getType()) {
                case TableModelEvent.INSERT:
                    for (int i = 0; i <= ptm.getRowCount() - 1; i++) {
                        System.out.println("Number Of Rows: " + ptm.getRowCount());
                        String s = "O-" + (i + 1);
                        System.out.println("Row Inserted");
                        System.out.println("First Row: " + firstRow + " Last Row: " + lastRow);
                        ptm.setValueAt(s, i, 12);
                    }
                    break;
                case TableModelEvent.UPDATE:
                    System.out.println("Row Updated");
                    break;
                case TableModelEvent.DELETE:
                    for (int i = 0; i <= ptm.getRowCount() - 1; i++) {
                        System.out.println("Number Of Rows: " + ptm.getRowCount());
                        String s = "O-" + (i + 1);
                        System.out.println("Row Deleted");
                        System.out.println("First Row: " + firstRow + " Last Row: " + lastRow);
                        ptm.setValueAt(s, i, 12);
                    }
                    break;
            }
        }
    }
    class ColumnRenderer extends JComboBox implements TableCellRenderer{
        public void updateUI(){
            super.updateUI();
        }
        public void revalidate() {}
        public Component getTableCellRendererComponent(
                     JTable table, Object value,
                     boolean isSelected, boolean hasFocus,
                     int row, int column)
        {
            if (value != null) {
                //System.out.println(value.toString());
                removeAllItems();
                addItem(value);
            }
            return this;
        }
    }
    
     public static void splashText1(String str)
    {
        if (mySplash != null && mySplash.isVisible())
        {   // important to check here so no other methods need to know if there
            // really is a Splash being displayed

            // erase the last status text
            splashGraphics.setBackground(splashGraphics.getBackground());
//            Double textX = width * .55;
//            Double textY = height*0.88;
//            Double textW = width*.45;
//            Double textH = 32.;
//            splashGraphics.clearRect(textX.intValue(), textY.intValue(), textW.intValue(), textH.intValue());
//            splashGraphics.setPaint(Color.LIGHT_GRAY);
//            splashGraphics.fill(splashTextArea);

            // draw the text
            splashGraphics.setPaint(Color.BLACK);
            splashGraphics.drawString(str, (int)(splashTextArea1.getX() + 10),(int)(splashTextArea1.getY() + 15));

            // make sure it's displayed
            mySplash.update();
        }
    }
    /**
     * Display a (very) basic progress bar
     * @param pct how much of the progress bar to display 0-100
     */
    public static void splashProgress1(int pct)
    {
        if (mySplash != null && mySplash.isVisible())
        {

            // Note: 3 colors are used here to demonstrate steps
            // erase the old one
//            splashGraphics.setPaint(Color.GREEN);
//            splashGraphics.fill(splashProgressArea);
//
//            // draw an outline
            splashGraphics.setPaint(Color.CYAN);
            splashGraphics.draw(splashProgressArea1);

            // Calculate the width corresponding to the correct percentage
            int x = (int) splashProgressArea1.getMinX();
            int y = (int) splashProgressArea1.getMinY();
            int wid = (int) splashProgressArea1.getWidth();
            int hgt = (int) splashProgressArea1.getHeight();

            int doneWidth = Math.round(pct*wid/100.f);
            doneWidth = Math.max(0, Math.min(doneWidth, wid-1));  // limit 0-width

            // fill the done part one pixel smaller than the outline
            splashGraphics.setPaint(Color.CYAN);
            splashGraphics.fillRect(x, y+1, doneWidth, hgt-1);

            // make sure it's displayed
            mySplash.update();
        }
    }
    public static void splashText2(String str)
    {
        if (mySplash != null && mySplash.isVisible())
        {   // important to check here so no other methods need to know if there
            // really is a Splash being displayed

            // erase the last status text
            splashGraphics.setBackground(splashGraphics.getBackground());
//            Double textX = width * .55;
//            Double textY = height*0.88;
//            Double textW = width*.45;
//            Double textH = 32.;
//            splashGraphics.clearRect(textX.intValue(), textY.intValue(), textW.intValue(), textH.intValue());
//            splashGraphics.setPaint(Color.LIGHT_GRAY);
//            splashGraphics.fill(splashTextArea);

            // draw the text
            splashGraphics.setPaint(Color.BLACK);
            splashGraphics.drawString(str, (int)(splashTextArea2.getX() + 10),(int)(splashTextArea2.getY() + 15));

            // make sure it's displayed
            mySplash.update();
        }
    }
    /**
     * Display a (very) basic progress bar
     * @param pct how much of the progress bar to display 0-100
     */
    public static void splashProgress2(int pct)
    {
        if (mySplash != null && mySplash.isVisible())
        {

            // Note: 3 colors are used here to demonstrate steps
            // erase the old one
//            splashGraphics.setPaint(Color.GREEN);
//            splashGraphics.fill(splashProgressArea);
//
//            // draw an outline
            splashGraphics.setPaint(Color.CYAN);
            splashGraphics.draw(splashProgressArea2);

            // Calculate the width corresponding to the correct percentage
            int x = (int) splashProgressArea2.getMinX();
            int y = (int) splashProgressArea2.getMinY();
            int wid = (int) splashProgressArea2.getWidth();
            int hgt = (int) splashProgressArea2.getHeight();

            int doneWidth = Math.round(pct*wid/100.f);
            doneWidth = Math.max(0, Math.min(doneWidth, wid-1));  // limit 0-width

            // fill the done part one pixel smaller than the outline
            splashGraphics.setPaint(Color.CYAN);
            splashGraphics.fillRect(x, y+1, doneWidth, hgt-1);

            // make sure it's displayed
            mySplash.update();
        }
    }
    public void clearEmptyJobs(){
        
    }
    
    public void updatePkgPool(int selected){
        PrinterTableModel btm = (PrinterTableModel)bonniePool.getModel();
            PrinterTableModel ctm = (PrinterTableModel)clydePool.getModel();
            PrinterTableModel otm = (PrinterTableModel)ocePool.getModel();
            JobTableModel jtm = (JobTableModel) jobPoolTable.getModel();
            Job tempJob = (Job) jtm.dataVector.get(selected);
            ArrayList<Package> pkgList = new ArrayList<>();
            pkgList.addAll(tempJob.packages);
            ArrayList<Package> loopList = new ArrayList<>();
            loopList.addAll(pkgList);

            for (Package pkg : loopList) {
                
                try {
                    for (int j = 0; j < btm.getRowCount(); j++) {
                        JobPackage btjp = (JobPackage)btm.dataVector.get(j);
                        if(pkg.pkgName.equals(btjp.pkgName) && pkg.id == btjp.id){
                            System.out.println("Package Found in Bonnie");
                            pkgList.remove(pkg);
                            break;
                        }
                    }
                } catch (NullPointerException | IndexOutOfBoundsException  ex) {
                }
                try {                
                    for (int j = 0; j < ctm.getRowCount(); j++) {
                        JobPackage ctjp = (JobPackage)ctm.dataVector.get(j);
                        if(pkg.pkgName.equals(ctjp.pkgName) && pkg.id == ctjp.id){
                            pkgList.remove(pkg);
                            break;
                        }
                    }
                } catch (NullPointerException | IndexOutOfBoundsException  ex) {
                }
                try {                
                    for (int j = 0; j < otm.getRowCount(); j++) {
                        JobPackage otjp = (JobPackage)otm.dataVector.get(j);
                        if(pkg.pkgName.equals(otjp.pkgName) && pkg.id == otjp.id){
                            pkgList.remove(pkg);
                            break;
                        }
                    }
                } catch (NullPointerException | IndexOutOfBoundsException  ex) {
                }
                
                try {
                    if (!pkg.queuePos.isEmpty()) {
                        pkgList.remove(pkg);
                    }
                } catch (NullPointerException ex) {
                    
                }
            }
            AbstractTableModel tempPoolModel = new PoolTableModel(pkgList);
            packagePoolTable = new JTable(tempPoolModel);
            pkgPoolPane.setViewportView(packagePoolTable);
            packagePoolTable.setFillsViewportHeight(true);
            packagePoolTable.setDragEnabled(true);
            packagePoolTable.setDropMode(DropMode.INSERT_ROWS);
            packagePoolTable.setTransferHandler(jtmHandler);
        }
    
}
