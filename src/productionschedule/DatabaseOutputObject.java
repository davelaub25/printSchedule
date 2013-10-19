/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package productionschedule;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import javax.sql.rowset.CachedRowSet;

/**
 *
 * @author dlaub
 */
    public class DatabaseOutputObject {
        public ResultSet resultSet; // The result set
        public CachedRowSet rowSet;
        public ResultSetMetaData metaData; // The metadata
        public DatabaseOutputObject(CachedRowSet r, ResultSetMetaData m) { // Constructor
            rowSet = r;
            metaData = m;
        }
    }
