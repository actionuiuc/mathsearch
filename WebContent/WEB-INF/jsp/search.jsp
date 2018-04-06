<%-- 	Brian Jackson			
		CS5012 Project 3			
		12/13/2015	
		
		Filename: search.jsp
		
		Usage:  View page that displays a search box and search results (selectable word count along with problem and concept questions).   
				A search box is provided to allow the user to submit either a single term or multiple terms delimited by a comma.  
		        For a single term, search results are provided immediately in the form of up to two expandable lists containing problem and/or concept questions. 
		        When multiple terms are provided, a count of found questions is provided for each term along with a checkbox next to each term count. 
		        Javascript functions allow the user to select a term count that will return and display all questions found for that term.  
		        Multiple selected search terms will return questions that contain all the selected search terms.  
 
		        Validations are performed on the submitted search terms when performing a search.  
		        Error messages are displayed when applicable above the input box used to search for a term(s).  			
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Search Math Problems</title>

<style type="text/css">
body {
	font-family: tahoma, helvetica, arial, sans-serif;
}

table {
	font-size: .9em;
	border: 1px solid;
	padding: 3px;
	border-collapse: collapse; 
	border-spacing: 0;
}

th {
	background-color: #F5DEB3;
}

td,tr {
	font-size: .9em;
	border: 1px solid;
		padding: 3px;
		background-color: #FFFFFF;
}

table {
	margin: auto;
	width: 80%;
}

td.title { 
	white-space: nowrap;
}

.error {
	color: #ff0000;
	font-weight: bold;
	font-size: 16px;
}

</style>


<script type="text/javascript">
	window.MathJax = {
		tex2jax : {
			inlineMath : [ [ '$', '$' ], [ "\\(", "\\)" ] ],
			processEscapes : true
		}
	};
</script>

<script type="text/javascript">

	//Javascript function used to expand/collapse the list of found questions.  Also switches the displayed button between +/-
    function expandCollapse(a) {
		var e=document.getElementById(a);
		
		if(a == "contSection") {
			var btn = "expandConcept";
		} else {
			var btn = "expandProblem";
		}
		
		if(!e) {
			return true;
		}
		if(e.style.display=="none") {
			e.style.display="block";
			document.getElementById(btn).value = "-";
		} else {
			e.style.display="none";
			document.getElementById(btn).value = "+";
		}
		
		return true;
    }
</script>


<script type="text/javascript"
	src="mathjax/MathJax.js?config=TeX-AMS_HTML">
</script>

<script type="text/javascript">
	//Javascript function that allows the user to select a checkbox next to a term count that will return and display all questions found for that term. 
    function selectTerm() {	
    		document.forms["selectCheckBox"].setAttribute("action", "selectSearch.do");
    		document.forms["selectCheckBox"].submit();
    }
</script>

<script type="text/javascript">
	//Javascript function that sends a request to the SearchController with the appropriate url mapping depending on if a single term or multiple terms are 
	//being searched. 
    function submitTerms() {	
    	if(document.getElementById("termBox").value.search(",") > 1) {
    		document.forms["searchQuestion"].setAttribute("action", "searchCount.do"); //search with multiple terms
    	} else {
    		document.forms["searchQuestion"].setAttribute("action", "searchSingle.do");  //search with one term
    	}
    	document.forms["searchQuestion"].submit();
    }
	
</script>

</head>

<body>
	<div style="text-align:center">
	<p style="font-size:40px;font-weight: bold;">Search Math Problems & Concepts</p>
	<p style="font-size: 18px; font-weight: italic;">Search for math problems and concepts based on the provided keyword(s).</p>
	</div>


	<%-- Display the search box either empty or containing currently searched term(s). --%>
	<div style="text-align:center;font-size:12px;font-weight:normal;">
	<form:form id="searchQuestion" method="POST" commandName="term">
				<form:errors path="userTerm" cssClass="error"/><BR>
				
				<% if(!(session.getAttribute("searchText") == null)) { %>
				<form:input path="userTerm" id="termBox" size="65" value="${sessionScope.searchText}" />
				<% } else { %>
				<form:input path="userTerm" id="termBox" size="65" />
				<% } %>
	
				<input type="submit" value="Search" onclick="submitTerms();" />
	</form:form>


	<%-- When multiple search terms are provided, display the found question count for each individual term along with a checkbox next to each term count. --%>
	<% if(!(session.getAttribute("countExist") == null) && ((Boolean) session.getAttribute("countExist"))) { %>
	<form:form id="selectCheckBox" method="POST" commandName="problem">
		<c:forEach items="${sessionScope.resultlist}" var="rest">
			(<c:out value="${rest.term}" />-
			<c:out value="${rest.resultCount}" />-
			<input type="checkbox" name="checkTerm" id="checkTerm" value="${rest.term}" onclick="selectTerm();" ${rest.isChecked}/>)
		</c:forEach>
	</form:form>
	<% } %>
	</div>
	
	<BR><BR>

<%-- Allows a user to expand/collapse a list of search results. --%>
<% if(!(session.getAttribute("resultsExist") == null) && ((Boolean) session.getAttribute("resultsExist"))) { %>
<BR><BR>
	<table>
	<tr>
	<th COLSPAN="3"><div style="text-align:center"><p style="font-size:20px;font-weight: bold;">Search Results</p></div></th>
	</tr>
	<%-- Display expandable problem button only if problems are found. --%>
	<c:if test="${model.problist.size() > 0}">
	<tr>
	<td style="width:10%"><form><input type="button" id="expandProblem" class="button" onclick="return expandCollapse('probSection');" value="+"></form></td>
	<td style="width:10%">${model.problist.size()}</td>
	<td><p style="font-size:20px;font-weight: bold;">Problems</p></td>
	</tr>
	</c:if>
	</table>
	
	<div id="probSection" style="display:none">
	<table>	
		<c:forEach items="${model.problist}" var="prob">
			<tr>
			<td><b><c:out value="${prob.pid}" /></b></td> 
			<td><b><c:out value="${prob.title}" /></b></td>
			</tr>
			<tr>
			<td COLSPAN="2"><c:out value="${prob.content}" /></td>
			</tr>	
		</c:forEach>
	</table>
	</div>
			
<%-- Display expandable concept button only if concepts are found. --%>
<c:if test="${model.contlist.size() > 0}">
	<table>
	<tr>
	<td style="width:10%"><form><input type="button" id="expandConcept" class="button" onclick="return expandCollapse('contSection');" value="+"></form></td>
	<td style="width:10%">${model.contlist.size()}</td>
	<td><p style="font-size:20px;font-weight: bold;">Concepts</p></td>
	</tr>
	</table>
</c:if>

	<div id="contSection" style="display:none">
		<table>
		<c:forEach items="${model.contlist}" var="cont">
			<tr>
			<td><b><c:out value="${cont.cid}" /></b></td> 
			<td><b><c:out value="${cont.title}" /></b></td>
			</tr>
			<tr>
			<td COLSPAN="2"><c:out value="${cont.content}" /></td>
			</tr>
		</c:forEach>
		</table>
	</div>	
	
	<table>
	<tr>
	<th COLSPAN="3"><div style="text-align:center"><p style="font-size:20px;font-weight: bold;">The End</p></div></th>
	</tr>
	
	</table>
	<% } %>

</body>
</html>