Brian Jackson											
CS 5012 Project 3										
12/13/2015
											
=====================Keyword-Based Search for Math Problems and Concepts=============================								

This program allows the user to do the following:
	-Search for math problems and concepts based on the keywords provided by the user.
	-In the search field, the user can type one or multiple keywords separated by a comma.
	-When multiple search terms are provided, the user is provided the result count of the individual search terms.  The user can then 
	 select via checkbox which terms they would like to be part of the final search that produces the result questions.
					
Extra Credit:  Early Submission
			   Part 2

Issues:  Some search results return questions that upon inspection do not contain the keyword, BUT the keyword_mapping specifies that the keyword exists in the 
         returned question.  I manually ran the queries in phpMyAdmin and confirmed this behavior.  An example would be keyword=side returning the problem
         with pid=101.  

Spring version - Spring Framework version 4.0.0.RELEASE

Required jars (WebContent\WEB-INF\lib)- commons-logging-1.1.1
					jstl
					mysql-connector-java-5.1.7-bin
					spring-aop
					spring-beans
					spring-context
					spring-core
					spring-expression
					spring-web
					spring-webmvc
					standard
		

Features - When a user comes to the web application, a search field with a search button is displayed.
		   The user can type one keyword or multiple keywords separated by commas.
		   
		   For a single search term:
		   -Search results are provided for questions found in both the Problems and Concepts tables.
		   -Initially, result counts for each table are displayed along with a + button allowing the user to expand the view to display all questions from that 
		   particular table.
		   -The expanded question view is collapsable with the - button.      
		   
		   For multiple search terms:
		   -Before the matching questions are returned, partial search result are returned with each term and the individual total match count for that term in 
		    both the problem and concept tables.  A selectable checkbox is provided next to each term to allow the user to refine the displayed search results.  
		   -When the user checks or unchecks any checkbox, they will get the search result information for the selected keywords.
		   *Note:  If a user selects multiple checkboxes then each term must be present in a question for a question to be displayed in the search results.  
		   
		   -Any leading or trailing spaces are trimmed from the provided search terms.
		   -Blank search terms are not allowed and will cause a validation error to be thrown.
	   	   -Non-empty values that fail validation are kept in the search input boxes to allow the user to edit them.
	   	  
		
Database -	mymathprobs
		  	tables: problem
		  		   concept
		  		   keyword
		  		   keyword_mapping
		  		   		type_id=3 (problem table)
		  		   		type_id=4 (concept table)


JSPs* - "search.jsp"
		Description: View pages returned by the controllers that display forms and results from the database searches.
	

Controllers* (edu.umsl.java.web) - "SearchController.java"
	    						   Description:  Controller classes with methods called via url RequestMapping.  

DAO* (edu.umsl.java.dao) - "QuestionDao.java"
	                       Description:  Data Access Object class which performs transactions on the database tables listed above. 

Validator* (edu.umsl.java.validator) - "FormValidator.java"
	    							    Description:  Validates the supplied target object and returns error messages when appropriate.
										A special check is added when multiple search terms are provided by the user to check if any of the provided search terms
										are blank.
										
Bean - "Problem.java" - JavaBean that represents a problem question with ID, content, and title properties.
	   "Concept.java" - JavaBean that represents a concept question with ID, content, and title properties.
	   "SearchResult.java" - JavaBean that represents a single search term.  Used when multiple search terms are submitted separated by commas.
	   						 Represents an object containing the search term, match count, and whether or not the user has checked the box next to the search term.
	   "SearchTerms.java" - JavaBean that represents the entire search text submitted by the user before any splitting occurs.   
	  
       Description:  JavaBeans that contain the appropriate getters and setters.


Config* - "web.xml"
	       Description:  Contains 'welcome file list', 'servlet mapping', and 'servlet' to configure DispatcherServlet instance.  

Application Context* - math-servlet.xml
	 	               Description:  Configure controllers, validators, message source, and view resolver.

*Please	see inline comments in the specified file for more details.