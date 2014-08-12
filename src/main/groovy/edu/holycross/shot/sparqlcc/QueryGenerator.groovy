package edu.holycross.shot.sparqlcc


import edu.harvard.chs.cite.CiteUrn


/** A class using knowledge of the CHS Image extension to generate appropriate
* SPARQL queries.
*/
class QueryGenerator {

    String prefix = """prefix cite:        <http://www.homermultitext.org/cite/rdf/>
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
prefix olo:     <http://purl.org/ontology/olo/core#> 
prefix hmt:        <http://www.homermultitext.org/hmt/rdf/>
"""

    /** Empty constructor.*/
    QueryGenerator() {
    }



    String getCollUrnsQuery() {
      SELECT DISTINCT ?coll  where {
    ?coll rdf:type cite:CiteCollection  .
    }


    String getNextUrnQuery(CiteUrn urn) {
       return """${prefix}
SELECT ?curr ?next
           WHERE {
           <${urn}> olo:next ?next .
           BIND ( <${urn}> as ?curr ) 
           }
"""
    }

    String getPrevUrnQuery(CiteUrn urn) {
       return """${prefix}
SELECT ?curr ?prev
           WHERE {
           <${urn}> olo:previous ?prev .
           BIND ( <${urn}> as ?curr ) 
           }
"""
   }

    String getPrevObjQuery(CiteUrn urn) {
return """${prefix}

	SELECT ?prev ?pval ?p ?l ?t WHERE {
			{SELECT ?prev
					WHERE {
							<${urn}> olo:previous ?prev .
						   BIND ( <${urn}> as ?curr ) 
					}
			}
			?prev ?p ?pval .
			?p cite:propLabel ?l .
			?p cite:propType ?t .

         }
"""
    }


    String getNextObjQuery(CiteUrn urn) {

return """${prefix}

	SELECT ?nxt ?pval ?p ?l ?t WHERE {
			{SELECT ?nxt
					WHERE {
							<${urn}> olo:previous ?nxt .
						   BIND ( <${urn}> as ?curr ) 
					}
			}
			?nxt ?p ?pval .
			?p cite:propLabel ?l .
			?p cite:propType ?t .


	}
        
"""
    }
        

   /* Using ?urn at the top and in the nested SELECT cuts 30% off execution time, for some reason */ 
    String getLastQuery(CiteUrn collUrn) {
        return """${prefix}
        SELECT ?urn WHERE {
			?urn cite:belongsTo <${collUrn}> .
		    ?urn olo:item ?seq .
			{ SELECT (MAX (?s) as ?max )
				WHERE {
					?urn olo:item ?s .
					?urn cite:belongsTo <${collUrn}> .
				}
		}
		FILTER (?seq = ?max).
			}
       """
    }



   /* Using ?urn at the top and in the nested SELECT cuts 30% off execution time, for some reason */ 

    String getFirstQuery(CiteUrn collUrn) {
        return """${prefix}
        SELECT ?urn WHERE {
			?urn cite:belongsTo <${collUrn}> .
         ?urn olo:item ?seq .
         { SELECT  (MIN (?s) as ?min)
           WHERE {
           ?urn olo:item ?s .
           ?urn cite:belongsTo <${collUrn}> .
           }
          }
        FILTER (?seq = ?min) .
        }
       """
        
    }



    String getCollSizeQuery(CiteUrn collUrn) {
        return """${prefix}
        SELECT (COUNT(?urn) AS ?size) WHERE {
          ?urn cite:belongsTo <${collUrn}> .
        }
"""
    }

    String getValidReffQuery(CiteUrn collUrn) {
        return """${prefix}
        SELECT ?urn WHERE {
          ?urn cite:belongsTo <${collUrn}> .
        }     
        """
    }

    String propsForCollection(CiteUrn collUrn) {
        return """${prefix}
        SELECT ?p ?l ?t WHERE {
        <${collUrn}> cite:collProperty ?p .
        ?p cite:propLabel ?l .
        ?p cite:propType ?t .
        }
        """
    }

    String getObjectQuery(CiteUrn urn) {
        return """${prefix}
        SELECT ?pval ?p ?l ?t WHERE {
        <${urn}> cite:belongsTo ?coll  .
        ?coll cite:collProperty ?p .
        <${urn}> ?p ?pval .
        ?p cite:propLabel ?l .
        ?p cite:propType ?t .
        }
       """
     }

String getPagedQuery(CiteUrn urn, int offset, int limit) {
return """${prefix}
select ?s  ?o WHERE {
?s  cite:belongsTo <${urn}> .
?s olo:item  ?seq .
BIND ( <${urn}> as ?o )
}
ORDER BY ?seq
LIMIT ${limit}
OFFSET ${offset}
"""
}


String getPagedIllQuery(CiteUrn urn, int offset, int limit) {
return """${prefix}

select ?s ?img WHERE {
?s  cite:belongsTo <${urn}> .
?s olo:item  ?seq .
?s hmt:hasDefaultImage ?img .
}
ORDER BY ?seq
LIMIT ${limit}
OFFSET ${offset}
"""
}
}
