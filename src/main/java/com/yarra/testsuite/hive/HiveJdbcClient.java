package com.yarra.testsuite.hive;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

public class HiveJdbcClient implements Callable<Long> {
	final static Logger log = Logger.getLogger(HiveJdbcClient.class);
	private String query;

	private HiveJdbcClient () {
	}
	public HiveJdbcClient (Properties hiveConfigProp,String query) {
		log.info(" HiveJdbcClient constructor .....!");
		this.query = query;
	}
	
	@Override
	public Long call() throws Exception {
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		long start = System.currentTimeMillis(); 
		try {
			 Class.forName("org.apache.hive.jdbc.HiveDriver");
		}catch(Exception e) {
			log.error("Exception @ class loader"+e);
		}
		try {
			if (HiveHelper.hasPoolInitialized()) {
				con = HiveHelper.getDataSource().getConnection();
				log.info("connection  >>>>>>>>>>>>>>>>>> " + con);
				log.info(" query :  ****************** "+ query);
				stmt = con.createStatement();
				rs = stmt.executeQuery(query);
				log.info("Resultset No. of Rows :::::::::::::::::>>>>>>>>>>>>>>>>> "+rs.getRow());
			} else {
				throw new Exception("Unable to get Hive Connection object ");
			}

		}
		catch(Exception ex) {
			log.error("Exception @ run method,"+ex);
		}
		finally {
			log.info(" finally block call before clean the resources................!");
			try {
				if (rs != null && !rs.isClosed()) {
					rs.close();
				}
			}
			catch(Exception e) {
				
			}
			rs = null;
			
			try {
				if (stmt != null && !stmt.isClosed()) {
					stmt.close();
				}
			}
			catch(Exception e) {
				
			}
			stmt = null;
			
			try {
				if (con != null && !con.isClosed()) {
					con.close();
				}
			}
			catch(Exception e) {
				
			}
			con = null;
		}
		
		 return System.currentTimeMillis() - start;  
	}
	
}


