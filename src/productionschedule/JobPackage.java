/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package productionschedule;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author dlaub
 */
public class JobPackage implements Iterable<Field> {

    private List<Field> m_JobPackage;
    public String jobNum;       //Contained in csv
    public String client;       //Contained in csv
    public String jobName;      //Contained in csv
    public String pkgName;   // Contained in CSV
    public Date mailDate; // Contained in CSV
    public String status;
    public int size;      // Contained in CSV
    public int pages;
    public int nUp;       // Contained in CSV
    public String printer;
    public Double ert;    // Contained in CSV
    public String programmer;
    public String queuePos;
    public int id;

    public JobPackage(String jNum, String cl, String jName, String prog, String n, Date m, String st, int si, int u, Double e, String qp, int i) {

        jobNum = jNum;
        client = cl;
        jobName = jName;
        pkgName = n;
        mailDate = m;
        status = st;
        size = si;
        nUp = u;
        ert = e;
        pages = numberOfPages();
        printer = "None";
        programmer = prog;
        id = i;
        queuePos = qp;
    }

    JobPackage(Package p, DatabaseObject dO) throws ClassNotFoundException, SQLException {
        String query = "SELECT * FROM `jobs` WHERE `id` = " + p.id;
        DatabaseOutputObject dBoo = DatabaseTools.queryDatabase(dO, query);
        dBoo.rowSet.first();
        jobNum = dBoo.rowSet.getString("jobNum");
        client = dBoo.rowSet.getString("client");
        jobName = dBoo.rowSet.getString("jobName");
        pkgName = p.pkgName;
        mailDate = p.mailDate;
        status = p.status;
        size = p.size;
        nUp = p.nUp;
        ert = p.ert;
        pages = numberOfPages();
        printer = p.printer;
        programmer = dBoo.rowSet.getString("programmer");
        id = dBoo.rowSet.getInt("id");
        queuePos = p.queuePos;

        Field fieldlist[] = this.getClass().getFields();
        m_JobPackage = Arrays.asList(fieldlist);

    }
    ////////////////////////////////////////////////////////////////////////////

    JobPackage(Map properties) throws ClassNotFoundException, SQLException, IllegalArgumentException, IllegalAccessException {
        Field fieldlist[] = this.getClass().getDeclaredFields();
        for (int i = 0; i < fieldlist.length; i++) {
            fieldlist[i].set(this, properties.get(fieldlist[i].getName()));
        }
    }
    ////////////////////////////////////////////////////////////////////////////

    public int numberOfPages() {
        double pageCount = size / nUp;
        pageCount = Math.ceil(pageCount);
        return (int) pageCount;
    }
    ////////////////////////////////////////////////////////////////////////////

    public void setDate(Date d) {
        mailDate = d;
    }

    @Override
    public Iterator<Field> iterator() {
        Iterator<Field> iprof = m_JobPackage.iterator();
        return iprof;
    }
}