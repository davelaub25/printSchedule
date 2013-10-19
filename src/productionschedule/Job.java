/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package productionschedule;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author dlaub
 */
public class Job {

    public String jobNum;       //Contained in csv
    public String client;       //Contained in csv
    public String jobName;      //Contained in csv
    public ArrayList<Package> packages;
    public String status;
    public String programmer;
    public int id;

    Job(String n, String c, String j, String s, String pro, String pri, int i) throws ClassNotFoundException, SQLException, IllegalArgumentException, IllegalAccessException {
        jobNum = n;
        client = c;
        jobName = j;
        status = s;
        programmer = pro;
        id = i;
        packages = buildPackageArray();
    }

    Job(Map properties, boolean csvSourced) throws ClassNotFoundException, SQLException, IllegalArgumentException, IllegalAccessException {
        Field fieldlist[] = this.getClass().getDeclaredFields();
        for (int i = 0; i < fieldlist.length; i++) {
            if (!fieldlist[i].getName().equals("packages")) {
                fieldlist[i].set(this, properties.get(fieldlist[i].getName()));
            }
        }

        packages = buildPackageArray();
    }

    Job(Map properties) throws ClassNotFoundException, SQLException, IllegalArgumentException, IllegalAccessException {
        Field fieldlist[] = this.getClass().getDeclaredFields();
        for (int i = 0; i < fieldlist.length; i++) {
            if (!fieldlist[i].getName().equals("packages")) {
                fieldlist[i].set(this, properties.get(fieldlist[i].getName()));
            }

        }
        //jobNum = jobNum.getClass().getName();
        packages = buildPackageArray();
    }
    Job(JobPackage jp) throws ClassNotFoundException, SQLException, IllegalArgumentException, IllegalAccessException{
        jobNum = jp.jobNum;
        client = jp.client;
        jobName = jp.jobName;
        status = jp.status;
        programmer = jp.programmer;
        id = jp.id;
        packages = buildPackageArray();
    }

    private ArrayList buildPackageArray() throws ClassNotFoundException, SQLException, IllegalArgumentException, IllegalAccessException {
        System.out.println("BuildPackageArray started");
        DatabaseObject dbo = new DatabaseObject("jdbc:mysql://davelaub.com:3306/dlaub25_lasersched", "dlaub25_fmi", "admin");
        String query = "SELECT `pkgName`, `mailDate`, `status`, `size`, `nUp`, `printer`, `queuePos` ,`ert`, `id`, x_cast_to_int(size/nUp) AS 'pages' FROM `packages` WHERE `id` =" + this.id;
        DatabaseOutputObject dboo = DatabaseTools.queryDatabase(dbo, query);
        ArrayList packagesOut = new ArrayList();
        Class cls = Class.forName("productionschedule.Package");
        Field fieldlist[] = cls.getDeclaredFields();
        Map<String, Object> map = new HashMap<String, Object>();

        while (dboo.rowSet.next()) {
            for (int i = 0; i < fieldlist.length; i++) {
                String fieldName = fieldlist[i].getName();
                map.put(fieldName, dboo.rowSet.getObject(fieldName));
            }
            Package p = new Package(map);
            packagesOut.add(p);
        }

        System.out.println("BuildPackageArrayFinished");
        return packagesOut;
    }

    public void setClient(String s) {
        client = s;
    }

    public void setName(String s) {
        jobName = s;
    }

    public void setNumber(String i) {
        jobNum = i;
    }

    public void setProgrammer(String s) {
        programmer = s;
    }

    public void setId(int i) {
        id = i;
    }

    public void addPackage(Package p) {
        packages.add(p);
    }
//    public void addPackage(Package p){
//        int newSize = packages.length + 1;
//        Package[] copy = null;
//        System.arraycopy(packages, 0, copy, 0, newSize);
//        packages = copy;
//    }
}
