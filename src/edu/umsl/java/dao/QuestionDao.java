/*	Brian Jackson						*/
/*  CS 5012 Project 3					*/
/*	12/12/2015							*/
/*										*/
/*  Filename: QuestionDao.java			*/
/*																												*/
/*	Usage - Data Access Object class which performs transactions on the mymathrprobs database.  Searches for questions based on user provided search terms. 		*/

package edu.umsl.java.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import edu.umsl.java.bean.Problem;
import edu.umsl.java.bean.Concept;
import edu.umsl.java.bean.SearchTerms;

public class QuestionDao {
	private Connection connection;
	private PreparedStatement searchProblems, searchConcepts, countProblems, countConcepts;
	
	public QuestionDao() throws Exception {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mymathprobs", "root", "");

			searchProblems = connection.prepareStatement("SELECT * FROM problem WHERE EXISTS(SELECT * FROM keyword_mapping WHERE keyword_id = (SELECT kid FROM keyword WHERE word = ?) AND type_id = 3 AND problem.pid = keyword_mapping.item_id)");  //SQL statement to retrieve problems based on single search term.
			
			searchConcepts = connection.prepareStatement("SELECT * FROM concept WHERE EXISTS(SELECT * FROM keyword_mapping WHERE keyword_id = (SELECT kid FROM keyword WHERE word = ?) AND type_id = 4 AND concept.cid = keyword_mapping.item_id)");  //SQL statement to retrieve concepts based on single search term.
			
			countProblems = connection.prepareStatement("SELECT COUNT(*) FROM keyword_mapping WHERE keyword_id = (SELECT kid FROM keyword WHERE word = ?) AND type_id = 3");  //SQL statement to retrieve problem count based on single search term.
			
			countConcepts = connection.prepareStatement("SELECT COUNT(*) FROM keyword_mapping WHERE keyword_id = (SELECT kid FROM keyword WHERE word = ?) AND type_id = 4");  //SQL statement to retrieve concept count based on single search term.
			
			//Alternate search interpretation (slower).
			//countProblems = connection.prepareStatement("SELECT COUNT(*) FROM problem WHERE content LIKE CONCAT('%', ? , '%')"); 
			//searchProblems = connection.prepareStatement("SELECT pid, content, title FROM problem WHERE content LIKE CONCAT('%', ? , '%')"); 
			//searchConcept = connection.prepareStatement("SELECT cid, title, content FROM concept WHERE content LIKE CONCAT('%', ? , '%')"); 
			
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}
	
	
	//######################################################################################################
    // Method: searchProblemsSingle()
    // Input: SearchTerm searchTerm
    // Output: List<Problem>
    // Description:  Retrieves a list of math problem objects from the database based on the provided single search term.  Trims the provided term of any
	//               leading or trailing spaces.  
    //######################################################################################################
	public List<Problem> searchProblemsSingle(SearchTerms searchTerm) throws SQLException {
		List<Problem> problist = new ArrayList<Problem>();
		
		try {
			
			String terms = searchTerm.getUserTerm();
	        String[] searchTerms = terms.trim().split(",", -1);
			
			//problem search
			searchProblems.setString(1, searchTerms[0].trim());
			ResultSet resultsRS = searchProblems.executeQuery();

			while (resultsRS.next()) {
				Problem prob = new Problem();

				prob.setPid(resultsRS.getInt(1));
				prob.setContent(resultsRS.getString(2));
				prob.setTitle(resultsRS.getString(3));

				problist.add(prob);
			}
			
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}

		return problist;
	}

	
	//######################################################################################################
    // Method: searchConceptsSingle()
    // Input: SearchTerm searchTerm
    // Output: List<Concept>
    // Description:  Retrieves a list of math concept objects from the database based on the provided single search term.  Trims the provided term of any
	//               leading or trailing spaces.  
	//######################################################################################################
	public List<Concept> searchConceptsSingle(SearchTerms searchTerm) throws SQLException {
		List<Concept> contlist = new ArrayList<Concept>();
		
		try {
				
			String terms = searchTerm.getUserTerm();
	        String[] searchTerms = terms.trim().split(",", -1);
	        //System.out.println(searchTerms[0]);
			
			//concept search
			searchConcepts.setString(1, searchTerms[0].trim());
			ResultSet resultsRS2 = searchConcepts.executeQuery();

			while (resultsRS2.next()) {
				Concept cont = new Concept();

				cont.setCid(resultsRS2.getInt(1));
				cont.setTitle(resultsRS2.getString(2));
				cont.setContent(resultsRS2.getString(3));

				contlist.add(cont);
			}
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}

		return contlist;
	}
	
	
	protected void finalize() {
		try {
			searchProblems.close();
			searchConcepts.close();
			countProblems.close();
			countConcepts.close();
			connection.close();
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}
	}

	
	
	//######################################################################################################
    // Method: countProblems()
    // Input: String searchTerm
    // Output: int
    // Description:  Retrieves the number of times the search term passed into the method exists in the problem table according to the indexing tables.
    //######################################################################################################
	public int countProblems(String searchTerm) throws SQLException {
		int count = 0;
		
		try {
			
			//System.out.println(searchTerm);
			
			//problem search
			countProblems.setString(1, searchTerm);
			ResultSet resultsRS = countProblems.executeQuery();

			while (resultsRS.next()) {
				count = resultsRS.getInt(1);
				//System.out.println(count);
			}
			
			
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}

		return count;
	}
	
	//######################################################################################################
    // Method: countConcepts()
    // Input: String searchTerm
    // Output: int
    // Description:  Retrieves the number of times the search term passed into the method exists in the concept table according to the indexing tables.
	//######################################################################################################
	public int countConcepts(String searchTerm) throws SQLException {
		int count = 0;
		
		try {
			
			//problem search
			countConcepts.setString(1, searchTerm);
			ResultSet resultsRS = countConcepts.executeQuery();

			while (resultsRS.next()) {
				count = resultsRS.getInt(1);
			}
			
			
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}

		return count;
	}
	
	//######################################################################################################
    // Method: searchProblemsMulti()
    // Input: String[] searchTerm
    // Output: List<Problem>
    // Description:  Retrieves a list of math problem objects from the database that consists of the problem records containing the terms in the array passed in as a method parameter.   
    //######################################################################################################
	public List<Problem> searchProblemsMulti(String[] searchTerms) throws SQLException {
		List<Problem> problist = new ArrayList<Problem>();
		
		PreparedStatement searchProblemsMulti;
		String sqlStatement;
		
		sqlStatement = "SELECT * FROM (SELECT * FROM problem) problem INNER JOIN";
		
		//Dynamically create an SQL statement based on the number of terms that exist in the array parameter.
 		for(int i =0; i<searchTerms.length; i++){
 			sqlStatement = sqlStatement + "(SELECT * FROM keyword_mapping WHERE keyword_id = (SELECT kid FROM keyword WHERE word = '" + searchTerms[i] + "') AND type_id = 3) " + searchTerms[i];
 			sqlStatement = sqlStatement + " ON problem.pid = " + searchTerms[i] + ".item_id";
 			
 			if(i < (searchTerms.length - 1)) {
 				sqlStatement = sqlStatement + " INNER JOIN ";
 			}

 		}

 		//System.out.println(sqlStatement);
 		
		searchProblemsMulti = connection.prepareStatement(sqlStatement);  //SQL statement to retrieve all questions
		
		//Create list of problem objects that were found in the database. 
		try {
			
			ResultSet resultsRS = searchProblemsMulti.executeQuery();

			while (resultsRS.next()) {
				Problem prob = new Problem();

				prob.setPid(resultsRS.getInt(1));
				prob.setContent(resultsRS.getString(2));
				prob.setTitle(resultsRS.getString(3));

				problist.add(prob);
			}
			
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}

		return problist;
	}

	//######################################################################################################
    // Method: searchConceptsMulti()
    // Input: String[] searchTerm
    // Output: List<Problem>
	// Description:  Retrieves a list of math concept objects from the database that consists of the concept records containing the terms in the array passed in as a method parameter.   
    //######################################################################################################
	public List<Concept> searchConceptsMulti(String[] searchTerms) throws SQLException {
		List<Concept> contlist = new ArrayList<Concept>();
		
		PreparedStatement searchConceptsMulti;
		String sqlStatement;
		
		sqlStatement = "SELECT * FROM (SELECT * FROM concept) concept INNER JOIN";
		
		//Dynamically create an SQL statement based on the number of terms that exist in the array parameter.
 		for(int i =0; i<searchTerms.length; i++){
 			sqlStatement = sqlStatement + "(SELECT * FROM keyword_mapping WHERE keyword_id = (SELECT kid FROM keyword WHERE word = '" + searchTerms[i] + "') AND type_id = 4) " + searchTerms[i];
 			sqlStatement = sqlStatement + " ON concept.cid = " + searchTerms[i] + ".item_id";
 			
 			if(i < (searchTerms.length - 1)) {
 				sqlStatement = sqlStatement + " INNER JOIN ";
 			}

 		}

 		//System.out.println(sqlStatement);
		
		searchConceptsMulti = connection.prepareStatement(sqlStatement);  //SQL statement to retrieve all questions
		
		//Create list of concept objects that were found in the database. 
		try {
			
			ResultSet resultsRS = searchConceptsMulti.executeQuery();

			while (resultsRS.next()) {
				Concept cont = new Concept();

				cont.setCid(resultsRS.getInt(1));
				cont.setTitle(resultsRS.getString(2));
				cont.setContent(resultsRS.getString(3));

				contlist.add(cont);
			}
			
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}

		return contlist;
	}
	
}
