/*
 * File Management class.  Used for finding and reading files, for deleting used files and filtering files.
 */
package productionschedule;

import com.sun.rowset.CachedRowSetImpl;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import javax.sql.rowset.CachedRowSet;

/**
 *
 * @author Dave
 */
public class fileManagement {

    public static final String address = "jdbc:mysql://davelaub.com:3306/dlaub25_lasersched";
    public static final String userName = "dlaub25_fmi";
    public static final String password = "admin";
    public static DatabaseObject dbo = new DatabaseObject(address, userName, password);

    public static File[] getFileList(String path) {
        FileFilter filter1 = new csvFileFilter();
        File folder = new File(path.toString());
        File[] files = folder.listFiles(filter1);
        return files;
    }
    ////////////////////////////////////////////////////////////////////////////

    public static void safeDelete(String path) throws SQLException, IOException {
        for (int i = 0; i < ProductionSchedule.successfulEntries.size(); i++) {
            if (path.contains(ProductionSchedule.successfulEntries.get(i).toString())) {
                Path p = Paths.get(path);
                Files.delete(p);
                ProductionSchedule.successfulEntries.remove(i);
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////

    public static class csvFileFilter implements FileFilter {

        String[] extensions = {"csv", "txt"};

        @Override
        public boolean accept(File pathname) {
            for (String currentExtension : extensions) {
                if (pathname.getName().toLowerCase().endsWith(currentExtension)) {
                    return true;
                }
            }
            return false;
        }
    }
    ////////////////////////////////////////////////////////////////////////////

    public static void main(String args[]) {  // Main class strictly for test purposes
        File[] fileList = getFileList("C:\\LASER\\csv Reports");
        for (File file : fileList) {
            System.out.println(file.getName());
        }
    }
}
