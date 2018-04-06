/*	Brian Jackson						*/
/*  CS 5012 Project 3					*/
/*	12/13/2015							*/
/*										*/
/*  Filename: FormValidator.java		*/
/*																												*/
/*	Usage - Used to validate the search term(s) form field value submitted by the user when performing a keyword search.    		*/

package edu.umsl.java.validator;



import edu.umsl.java.bean.SearchTerms;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class FormValidator implements Validator {
	
	@Override
	public boolean supports(Class clazz) {
        return SearchTerms.class.isAssignableFrom(clazz);
    }
	
	//######################################################################################################
    // Method: validate()
    // Input: Object target, Errors errors
    // Output: none
    // Description:  Validates the supplied target object.  
	//				 Supplied errors instance used to report any resulting validation errors that will be sent back to the form view where error messages
	//				 will be displayed.	 
	//				 Fallback default error messages are provided.
	//				 Error code is provided to map to error message in messages.properties file.
    //######################################################################################################
	@Override
	public void validate(Object target, Errors errors) {
		
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userTerm",
                "required.userTerm", "Search term is required.");  //Check if a search term is not provided.  

        SearchTerms tm = (SearchTerms) target;
        
        if(!(tm.getUserTerm().trim().isEmpty())) {  //perform is search term is provided

        	String terms = tm.getUserTerm();
        	String[] searchTerms = terms.trim().split(",", -1);  //split the provided search terms by comma and load individual terms into an array
        
        	int len = searchTerms.length;
        
        	//check if a provided search term is null when multiple search terms are provided by the user
        	for(int i = 0; i < len; i++) {   
        		//System.out.println(searchTerms[i]);
        		if(searchTerms[i].trim() == null || searchTerms[i].trim().isEmpty()) { //perform if a provided search term is blank or null
        			errors.rejectValue("userTerm", "empty.userTerm", "Empty search term is not allowed.");  //register error for the userTerm field with BindingResult
        			break;
        		}
        	}
        
        }
	}

}
