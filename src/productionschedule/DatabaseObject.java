/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package productionschedule;

/**
 *
 * @author dlaub
 */
    public class DatabaseObject {
        public String address; // Databases address
        public String userName; // Database username
        public String password; // Database password
        public DatabaseObject(String a, String u, String p) { // Constructor
            address = a;
            userName = u;
            password = p;
        }
    }