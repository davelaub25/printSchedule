/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package productionschedule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.csvreader.CsvReader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;

/**
 *
 * @author dlaub
 */
public class ProductionSchedule {

    public static final String address = "jdbc:mysql://davelaub.com:3306/dlaub25_lasersched";
    public static final String userName = "dlaub25_fmi";
    public static final String password = "admin";
    public static DatabaseObject dbo = new DatabaseObject(address, userName, password);
    public static final int lastJobFieldIndex = 2;
    public static final int firstPkgFieldIndex = lastJobFieldIndex + 1;
    public static ArrayList successfulEntries = new ArrayList();

    ////////////////////////////////////////////////////////////////////////////
    public static void csvToJob(File f) throws FileNotFoundException, IOException, ClassNotFoundException, ParseException, SQLException {
        CsvReader reader = new CsvReader(new FileReader(f));
        int jobId;
        reader.readHeaders();
        reader.readRecord();
        String programmer = Files.getOwner(f.toPath(), LinkOption.NOFOLLOW_LINKS).getName();
        String[] name = programmer.split("\\\\");
        programmer = name[name.length - 1];

        String jobQuery = "INSERT INTO `dlaub25_lasersched`.`jobs` (`jobNum`, `client`, `jobName`, `programmer`) VALUES (?, ?, ?, '" + programmer + "');";
        System.out.println(jobQuery);
        ArrayList jobValues = new ArrayList();
        for (int column = 0; column <= lastJobFieldIndex; column++) {
            jobValues.add(reader.get(column));
        }
        jobId = DatabaseTools.updateDatabase(dbo, jobQuery, jobValues);
        String pkgQuery = "INSERT INTO `dlaub25_lasersched`.`packages` (`pkgName`, `size`, `nUp`, `ERT`, `mailDate`, `id`) VALUES (?, ?, ?, ?, ?, '" + jobId + "');";
        ArrayList packages = new ArrayList();
        while (reader.readRecord()) {
            ArrayList packageValues = new ArrayList();
            for (int column = firstPkgFieldIndex; column < reader.getColumnCount(); column++) {
                try {
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    Date result = df.parse(reader.get(column));
                    packageValues.add(result);
                } catch (ParseException pex) {
                    try {
                        if (Integer.parseInt(reader.get(column)) != Double.parseDouble(reader.get(column))) {
                            double d = Double.parseDouble(reader.get(column));
                            packageValues.add(d);
                        } else {
                            int i = Integer.parseInt(reader.get(column));
                            packageValues.add(i);
                        }
                    } catch (NumberFormatException nfex) {
                        String s = reader.get(column);
                        packageValues.add(s);
                    }
                }
            }
            packages.add(packageValues);
        }
        try {
            DatabaseTools.multiUpdateDatabase(dbo, pkgQuery, packages);
            successfulEntries.add(jobValues.get(0));
        } catch (SQLException ex) {
            Logger.getLogger(ProductionSchedule.class.getName()).log(Level.SEVERE, "Problem inserting job into SQL database", ex);
        }
    }

    public static void csvToJob(File[] f) throws FileNotFoundException, IOException, ClassNotFoundException, ParseException, SQLException {
        for (File files : f) {
            CsvReader reader = new CsvReader(new FileReader(files));
            int jobId;
            reader.readHeaders();
            reader.readRecord();
            String programmer = Files.getOwner(files.toPath(), LinkOption.NOFOLLOW_LINKS).getName();
            String[] name = programmer.split("\\\\");
            programmer = name[name.length - 1];

            String jobQuery = "INSERT INTO `dlaub25_lasersched`.`jobs` (`jobNum`, `client`, `jobName`, `programmer`) VALUES (?, ?, ?, '" + programmer + "');";
            System.out.println(jobQuery);
            ArrayList jobValues = new ArrayList();
            for (int column = 0; column <= lastJobFieldIndex; column++) {
                jobValues.add(reader.get(column));
            }
            jobId = DatabaseTools.updateDatabase(dbo, jobQuery, jobValues);
            String pkgQuery = "INSERT INTO `dlaub25_lasersched`.`packages` (`pkgName`, `size`, `nUp`, `ERT`, `mailDate`, `id`) VALUES (?, ?, ?, ?, ?, '" + jobId + "');";
            ArrayList packages = new ArrayList();
            while (reader.readRecord()) {
                ArrayList packageValues = new ArrayList();
                for (int column = firstPkgFieldIndex; column < reader.getColumnCount(); column++) {
                    try {
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        Date result = df.parse(reader.get(column));
                        packageValues.add(result);
                    } catch (ParseException pex) {
                        try {
                            if (Integer.parseInt(reader.get(column)) != Double.parseDouble(reader.get(column))) {
                                double d = Double.parseDouble(reader.get(column));
                                packageValues.add(d);
                            } else {
                                int i = Integer.parseInt(reader.get(column));
                                packageValues.add(i);
                            }
                        } catch (NumberFormatException nfex) {
                            String s = reader.get(column);
                            packageValues.add(s);
                        }
                    }
                }
                packages.add(packageValues);
            }
            try {
                DatabaseTools.multiUpdateDatabase(dbo, pkgQuery, packages);
                successfulEntries.add(jobValues.get(0));
            } catch (SQLException ex) {
                Logger.getLogger(ProductionSchedule.class.getName()).log(Level.SEVERE, "Problem inserting job into SQL database", ex);
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////

    public static ArrayList exportHandler(Job j) throws ClassNotFoundException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        //Crazy ass code to emulate a for each loop
        Class cls = Class.forName("productionschedule.Job");
        Field fieldlist[] = cls.getFields();
        ArrayList queryValues = new ArrayList();
        for (int i = 0; i < fieldlist.length; i++) {
            if (!fieldlist[i].toString().equals("public productionschedule.Package[] productionschedule.Job.packages")) {
                String names = fieldlist[i].toString();
                String[] fieldName = names.split("\\.");    // Splits the object name string on periods
                String lastName = fieldName[fieldName.length - 1];    // Pulls the position of the string which contains the property name
                Field propertyField = j.getClass().getDeclaredField(lastName);
                String propertyValue = propertyField.get(j).toString();
                queryValues.add(propertyValue);
            }
        }
        return queryValues;
        //End crazy ass code

    }
    /////////////////////////////////////////////////////////////////////////////

    public static ArrayList[] exportHandler(JobPackage jp) throws ClassNotFoundException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        //Crazy ass code to emulate a for each loop
        Class cls = Class.forName("productionschedule.JobPackage");
        Field fieldlist[] = cls.getFields();
        ArrayList<Field> jobFields = new ArrayList<Field>(Arrays.asList(Job.class.getFields()));
        ArrayList<String> jobFieldNames = new ArrayList<String>();
        ArrayList<Field> pkgFields = new ArrayList<Field>(Arrays.asList(Package.class.getFields()));
        ArrayList<String> pkgFieldNames = new ArrayList<String>();
        int j = 0;
        for (Field f : jobFields) {
            jobFieldNames.add(f.getName());
            j++;
        }
        j = 0;
        for (Field f : pkgFields) {
            pkgFieldNames.add(f.getName());
            j++;
        }
        ArrayList jobValues = new ArrayList();
        ArrayList pkgValues = new ArrayList();
        for (int i = 0; i < fieldlist.length; i++) {
            if (pkgFieldNames.contains(fieldlist[i].getName()) && !fieldlist[i].getName().contains("pages")) {
                System.out.println("Pkg Tested True");
                String fieldName = fieldlist[i].getName();
                String propertyValue;
                Field propertyField;
                try {
                    propertyField = jp.getClass().getField(fieldName);
                    pkgValues.add(propertyField.get(jp));
                } catch (NullPointerException e) {
                    propertyValue = "";
                    pkgValues.add(propertyValue);
                }

            }
            if (jobFieldNames.contains(fieldlist[i].getName()) && !fieldlist[i].getName().contains("status")) {
                System.out.println("Job Tested True");
                String fieldName = fieldlist[i].getName();
                Field propertyField = jp.getClass().getField(fieldName);

                jobValues.add(propertyField.get(jp));
            }
        }
        String fieldName = fieldlist[3].getName();                  //
        Field propertyField = jp.getClass().getField(fieldName);    // Adding extra pkgName to the end for unique identifier
        pkgValues.add(propertyField.get(jp));                       //
        ArrayList[] al = new ArrayList[2];
        al[0] = jobValues;
        al[1] = pkgValues;
        return al;
        //End crazy ass code

    }
    ////////////////////////////////////////////////////////////////////////////

    public static ArrayList importHandler(DatabaseOutputObject exDBOO) throws ClassNotFoundException, SQLException, IllegalArgumentException, IllegalAccessException {
        double d = 100.0;
        double e = (double)exDBOO.rowSet.size();
        int increment = (int)Math.ceil(d/e);
        ArrayList jobs = new ArrayList();
        //splashScreen.splashBar.setValue(0);
        int progress = 0;
        while (exDBOO.rowSet.next()) {
            int numberOfColumns = exDBOO.rowSet.getMetaData().getColumnCount();
            Class cls = Class.forName("productionschedule.Job");
            Field fieldlist[] = cls.getFields();
            Map<String, Object> map = new HashMap<String, Object>();
            for (int i = 0; i < fieldlist.length; i++) {
                if (!fieldlist[i].getName().equals("packages")) {
                    String fieldName = fieldlist[i].getName();
                    System.out.println(fieldName);
                    System.out.println(exDBOO.rowSet.getObject(fieldName));
                    map.put(fieldName, exDBOO.rowSet.getObject(fieldName));
                }
            }
            Job j = new Job(map);
            jobs.add(j);
            progress = progress + increment;
            UI.splashProgress1(progress);
        }
        return jobs;
    }
    ////////////////////////////////////////////////////////////////////////////

    public static ArrayList pkgImportHandler(DatabaseOutputObject exDBOO, Class c) throws ClassNotFoundException, SQLException, IllegalArgumentException, IllegalAccessException {
        ArrayList pkgs = new ArrayList();
        while (exDBOO.rowSet.next()) {
            Field fieldlist[] = c.getFields();
            Map<String, Object> map = new HashMap<String, Object>();
            for (int i = 0; i < fieldlist.length; i++) {
                String fieldName = fieldlist[i].getName();
                if (!fieldName.contains("pages")) {
                    System.out.println(fieldName);
                    System.out.println(exDBOO.rowSet.getObject(fieldName));
                    map.put(fieldName, exDBOO.rowSet.getObject(fieldName));
                }
            }
            Package p = new Package(map);
            pkgs.add(p);
        }
        return pkgs;
    }
    ////////////////////////////////////////////////////////////////////////////

    public static void test() throws FileNotFoundException, IOException, ClassNotFoundException, ParseException, SQLException {
        File f = new File("C:\\LASER\\csv Reports\\65395 Laser Production Count Sheet.csv");
        CsvReader newReader = new CsvReader("C:\\LASER\\csv Reports\\65268 Laser Production Count Sheet.csv");
        csvToJob(f);
    }
    ////////////////////////////////////////////////////////////////////////////
    public static void unsched(ArrayList<JobPackage> p) throws ClassNotFoundException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, SQLException{
        for (int i = 0; i < p.size(); i++) {
            p.get(i).queuePos = null;
            ArrayList[] al = exportHandler(p.get(i));
            String pkgQuery = "UPDATE packages SET pkgName =?, mailDate=?, status=?, size=?, nUp=?, printer=?, ert=?, queuePos=? WHERE id = ? AND pkgName = ?";  //Added pkg name due to the fact packages lack a unique identifier
            DatabaseTools.updateDatabase(dbo, pkgQuery, al[1]);
        }
        
    }    
    ////////////////////////////////////////////////////////////////////////////
    public static void commitTables() {
        String jobQuery = "UPDATE jobs SET jobNum=?, client=?, jobName=?, programmer=? WHERE id=?";
        String pkgQuery = "UPDATE packages SET pkgName =?, mailDate=?, status=?, size=?, nUp=?, printer=?, ert=?, queuePos=? WHERE id = ? AND pkgName = ?";  //Added pkg name due to the fact packages lack a unique identifier
        ArrayList jobValues = new ArrayList();
        ArrayList pkgValues = new ArrayList();
        ArrayList <JTable>tables = new ArrayList();
        tables.add(UI.bonniePool);
        tables.add(UI.clydePool);
        tables.add(UI.ocePool);
        for (JTable jt : tables) {
            PrinterTableModel ptm = (PrinterTableModel) jt.getModel();
            try {
                for (int i = 0; i < ptm.getRowCount(); i++) {

                    JobPackage jp = (JobPackage) ptm.dataVector.elementAt(i);
                    jobValues.add(ProductionSchedule.exportHandler(jp)[0]);
                    pkgValues.add(ProductionSchedule.exportHandler(jp)[1]);
                }
                System.out.println("Job Values:\n");
                for (int i = 0; i < jobValues.size(); i++) {
                    System.out.println(jobValues.get(i));
                }
                System.out.println("Pkg Values:\n");
                for (int i = 0; i < pkgValues.size(); i++) {
                    System.out.println(pkgValues.get(i));
                }
                DatabaseTools.multiUpdateDatabase(dbo, pkgQuery, pkgValues);
                DatabaseTools.multiUpdateDatabase(dbo, jobQuery, jobValues);
            } catch (SQLException | ClassNotFoundException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(UI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////
//    public static JTable buildJobTable(ArrayList jobs){
//        for (int curJob = 0; curJob < jobs.size(); curJob++) {
//            JTable table = new JTable;
//
//        }
//    }
    ////////////////////////////////////////////////////////////////////////////
}
