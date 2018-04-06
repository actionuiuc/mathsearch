/*	Brian Jackson						*/
/*  CS 5012 Project 3					*/
/*	12/12/2015							*/
/*										*/
/*  Filename: SearchController.java		*/
/*																												*/
/*	Usage - Controller class used to call the DAO to search for keywords in the problem and concept tables.		*/
/*    		Validation is performed on the search terms passed to the controller from the form.    				*/


package edu.umsl.java.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestParam;

import edu.umsl.java.bean.Problem;
import edu.umsl.java.bean.SearchResult;
import edu.umsl.java.bean.Concept;
import edu.umsl.java.bean.SearchTerms;
import edu.umsl.java.dao.QuestionDao;

import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;



@Controller
public class SearchController
{
    
	@Autowired
	@Qualifier("formValidator")  //inject formValidator bean into controller
	private Validator validator;
	
	@InitBinder
	private void initBinder(WebDataBinder binder) {  //apply the validator to all request-handling methods in the controller class
		binder.setValidator(validator);
	}
	
    //######################################################################################################
    // Method: searchSingle()
    // Input: SearchTerms term, BindingResult result, ModelMap model, HttpServletRequest request
    // Output: ModelAndView
    // Description:  Method called to search for a single user submitted term if all validations pass.
	//				 The method is mapped to the URL "/searchSingle.do" for POST requests.
	//				 @Validated triggers validation of the SearchTerms object
	//				 The BindingResult contains the results of the validation process
	//				 
    //######################################################################################################
    @RequestMapping(value="/searchSingle.do", method = RequestMethod.POST)
    public ModelAndView searchSingle(@ModelAttribute("term") @Validated SearchTerms term, BindingResult result, ModelMap model, HttpServletRequest request) throws Exception {
   	 	
    	//If search term not provided during validation, do not perform search.  Error message will be displayed to the user.  
    	if(result.hasErrors()) {
    		request.getSession().setAttribute("resultsExist", false);  //used to determine if the view page needs to display search results problems and concepts
    		request.getSession().setAttribute("countExist", false);  //used to determine if the view page should display term counts for multiple search terms
    		request.getSession().removeAttribute("searchText");  //display user provided search text from the model instead of the session variable
         	return new ModelAndView("search");
	    }
    	else { //SearchTerms object passed validation contained in the validator bean
    		QuestionDao pdao = new QuestionDao();
   
    		List<Problem> problist = pdao.searchProblemsSingle(term);  //Retrieve list of all problems in the DB containing the search term and forward to search view jsp.
    		
    		List<Concept> contlist = pdao.searchConceptsSingle(term);  //Retrieve list of all concepts in the DB containing the search term and forward to search view jsp.
    		
    		model.put("problist", problist);
    		model.put("contlist", contlist);
    		
    		//Create session variables to pass information to the view page
    		request.getSession().setAttribute("resultsExist", true);  //used to determine if the view page needs to display search results problems and concepts
    		request.getSession().setAttribute("countExist", false);  //used to determine if the view page should display term counts for multiple search terms
    		request.getSession().removeAttribute("searchText");  //display user provided search text from the model instead of the session variable
    		
    		return new ModelAndView("search", "model", model);
    		
    	}
    }
    
        //######################################################################################################
        // Method: searchQuestions()
        // Input: HttpServletRequest request, HttpServletResponse response, Model model
        // Output: ModelAndView
        // Description:  Initial search page displayed with input text box displayed.
    	//				 The method is mapped to the URL "/search.do".
    	//				 
        //######################################################################################################
        @RequestMapping(value="/search.do")
        public ModelAndView searchQuestions(HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
        	model.addAttribute("term",new SearchTerms());
        	
    		request.getSession().setAttribute("resultsExist", false);
    		request.getSession().setAttribute("countExist", false);
        	
            return new ModelAndView("search");
        }
    
        //######################################################################################################
        // Method: searchCount()
        // Input: SearchTerms term, BindingResult result, ModelMap model, HttpServletRequest request
        // Output: ModelAndView
        // Description:  Method called to count number of times the search terms passed into the method exist in the problem and concept tables according 
        //				 to the indexing tables. Performed if the provided search terms pass validation.
        //				 Only performed when multiple search terms are provided.
        //				 Calls the DAO methods countProblems and countConcepts to retrieve counts.
    	//				 The method is mapped to the URL "/searchCount.do" for POST requests.
    	//				 @Validated triggers validation of the SearchTerms object
    	//				 The BindingResult contains the results of the validation process
    	//				 
        //######################################################################################################
        @RequestMapping(value="/searchCount.do", method = RequestMethod.POST)
        public ModelAndView searchCount(@ModelAttribute("term") @Validated SearchTerms term, BindingResult result, ModelMap model, HttpServletRequest request) throws Exception {
       	 	
        	//If search term not provided during validation, do not perform search.  Error message will be displayed.  
        	if(result.hasErrors()) {
        		request.getSession().setAttribute("resultsExist", false);
        		request.getSession().setAttribute("countExist", false);
             	return new ModelAndView("search");
    	    }
        	else { //SearchTerms object passed validation contained in the validator bean
        		QuestionDao pdao = new QuestionDao();
        		
        		//Create list of SearchResult objects.  These objects will contain the each search term and respective count.
        		List<SearchResult> resultlist = new ArrayList<SearchResult>();  
        		
        		String terms = term.getUserTerm();
        		
    	        String[] searchTerms = terms.trim().split(",", -1);  //split the provided search terms by comma and load individual terms into an array
        		
    	        int len = searchTerms.length;
    	        int probCount = 0;
    	        
    	        for(int i = 0; i < len; i++) {   
    	        	
    	        	//find total count for term in both the problem and concept tables
    	        	probCount = pdao.countProblems(searchTerms[i].trim());  //trim individual search term from array and call DAO method
    	        	probCount = probCount + pdao.countConcepts(searchTerms[i].trim());  //trim individual search term from array and call DAO method
    	        	
    	        	//Create SearchResult object containing individual search term and total count found.
    	        	SearchResult srchRest = new SearchResult();
    	        	srchRest.setTerm(searchTerms[i].trim());
    	        	srchRest.setResultCount(probCount);
    	        	
    	        	resultlist.add(srchRest);  //add SearchResult object to the resultlist.
    	        }
    	        
    	        //set session variables used for appropriate display in search view jsp
    	        request.getSession().setAttribute("resultlist", resultlist);  //provide list of SearchResult objects to the view page as session variable
    	        request.getSession().setAttribute("searchText", terms);  //store original user provided search terms to display in input text box
        		request.getSession().setAttribute("resultsExist", false);  //actual problems and concept questions have not been found yet
        		request.getSession().setAttribute("countExist", true);  //provide search term counts to the view page to display
    	        
        		return new ModelAndView("search");
        	
        	}
        }

        //######################################################################################################
        // Method: selectSearch()
        // Input: String[] selectedTerms, ModelMap model, HttpServletRequest request, SearchTerms term, BindingResult result
        // Output: ModelAndView
        // Description:  Method called to search for selected search terms checked by the user on the search view page.  
    	//				 The method is mapped to the URL "/selectSearch.do" for POST requests.
    	//				 
        //######################################################################################################
        @RequestMapping(value="/selectSearch.do", method = RequestMethod.POST)
        public ModelAndView selectSearch(@RequestParam(value="checkTerm", required=false) String[] selectedTerms, ModelMap model, HttpServletRequest request, @ModelAttribute("term") SearchTerms term, BindingResult result) throws Exception {
       	 	
        	//Get resultlist of SearchResult objects passed as session variable 
        	List<SearchResult> resultlist =  (List<SearchResult>) request.getSession().getAttribute("resultlist");
        	
        	//If all terms are de-selected on search view page, do not display problem or concept questions.
         	if (selectedTerms == null) {
         		for(int x = 0; x < resultlist.size(); x++) {
         			resultlist.get(x).setIsChecked("");  //set all SearchResult objects in resultlist as being unchecked
         		}
         		request.getSession().setAttribute("resultlist", resultlist);
         		request.getSession().setAttribute("resultsExist", false);

         		return new ModelAndView("search");
         	}
        	
     		//Test code
     		//for(int i =0; i<selectedTerms.length; i++){
     		//	System.out.println(selectedTerms[i] + ",");
     		//}
     		
     		QuestionDao pdao = new QuestionDao();
     	   
    		List<Problem> problist = pdao.searchProblemsMulti(selectedTerms);  //Retrieve list of problems that contain the terms passed in on the array.
    		
    		List<Concept> contlist = pdao.searchConceptsMulti(selectedTerms);  //Retrieve list of concepts that contain the terms passed in on the array.
    		
    		Boolean foundFlag = false; //flag used to identify when a search term from the resultlist is found to be checked by the user
    		
    		for(int i = 0; i < resultlist.size(); i++) {  //loop through SearchResult objects in the resultlist
         		for(int j =0; j<selectedTerms.length; j++) {  //loop through the search terms checked by the user
         			//System.out.println(selectedTerms[j]);
         			//System.out.println(resultlist.get(i).getTerm());
         			if(resultlist.get(i).getTerm().equals(selectedTerms[j])) {  //find SearchResult objects with the terms that are checked by the user 
         				resultlist.get(i).setIsChecked("checked");  //set SearchResult object as being checked
         				foundFlag = true;  
         			}
         		}
         		if(foundFlag == false) {  //if a SearchResult object in the resultlist contains a term that is not currently checked by the user set SearchResult object as not checked
         			resultlist.get(i).setIsChecked("");
         		}
         		else {
         			foundFlag = false;  //reset flag for next pass through loop
         		}
         		
    		}

    		model.put("problist", problist);
    		model.put("contlist", contlist);
    		
    		request.getSession().setAttribute("resultsExist", true);
    		request.getSession().setAttribute("resultlist", resultlist);
    		
    		return new ModelAndView("search", "model", model);
    
        }

}
