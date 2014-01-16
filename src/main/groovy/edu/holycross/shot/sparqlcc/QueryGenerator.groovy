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

    String getNextUrnQuery(CiteUrn urn) {
       return """${prefix}
SELECT ?curr ?next
           WHERE {
           ?curr olo:next ?next .
           FILTER (str(?curr) = "${urn}") .
           }
"""
    }

    String getPrevUrnQuery(CiteUrn urn) {
       return """${prefix}
SELECT ?curr ?prev
           WHERE {
           ?curr olo:previous ?prev .
           FILTER (str(?curr) = "${urn}") .
           }
"""
   }

    String getPrevObjQuery(CiteUrn urn) {
return """${prefix}

SELECT ?prev ?pval ?p ?l ?t WHERE {
?prev ?p ?pval .
?p cite:propLabel ?l .
?p cite:propType ?t .

{SELECT ?prev
           WHERE {
           ?curr olo:previous ?prev .
           FILTER (str(?curr) = "${urn}") .
           }
         }
        
         }
        
"""
    }


    String getNextObjQuery(CiteUrn urn) {

return """${prefix}

SELECT ?nxt ?pval ?p ?l ?t WHERE {
?nxt ?p ?pval .
?p cite:propLabel ?l .
?p cite:propType ?t .

{SELECT ?nxt
           WHERE {
           ?curr olo:next ?nxt .
           FILTER (str(?curr) = "${urn}") .
           }
         }
        
         }
        
"""
    }
        

    
    String getLastQuery(CiteUrn collUrn) {
        return """${prefix}
        SELECT ?urn WHERE {
         ?urn olo:item ?seq .
         ?urn cite:belongsTo ?coll .
         { SELECT  (MAX (?s) as ?max)
           WHERE {
           ?ref olo:item ?s .
           ?ref cite:belongsTo ?coll .
        FILTER (str(?coll) = "${collUrn}") .
           }
          }
        FILTER (?seq = ?max) .
        FILTER (str(?coll) = "${collUrn}") .
        }
       """
        
    }



    String getFirstQuery(CiteUrn collUrn) {
        return """${prefix}
        SELECT ?urn WHERE {
         ?urn olo:item ?seq .
         ?urn cite:belongsTo ?coll .
         { SELECT  (MIN (?s) as ?min)
           WHERE {
           ?ref olo:item ?s .
           ?ref cite:belongsTo ?coll .
        FILTER (str(?coll) = "${collUrn}") .
           }
          }
        FILTER (?seq = ?min) .
        FILTER (str(?coll) = "${collUrn}") .
        }
       """
        
    }



    String getCollSizeQuery(CiteUrn collUrn) {
        return """${prefix}
        SELECT (COUNT(?urn) AS ?size) WHERE {
          ?urn cite:belongsTo ?coll .
          FILTER(str(?coll) = "${collUrn}") .
        }
"""
    }

    String getValidReffQuery(CiteUrn collUrn) {
        return """${prefix}
        SELECT ?urn WHERE {
          ?urn cite:belongsTo ?coll .
          FILTER(str(?coll) = "${collUrn}") .
        }     
        """
    }

    String propsForCollection(CiteUrn collUrn) {
        return """${prefix}
        SELECT ?p ?l ?t WHERE {
        ?s cite:collProperty ?p .
        ?p cite:propLabel ?l .
        ?p cite:propType ?t .
        FILTER(str(?s) = "${collUrn}") . 
        }
        """
    }

    String getObjectQuery(CiteUrn urn) {
        return """${prefix}
        SELECT ?pval ?p ?l ?t WHERE {
        ?obj cite:belongsTo ?coll  .
        ?coll cite:collProperty ?p .
        ?obj ?p ?pval .
        ?p cite:propLabel ?l .
        ?p cite:propType ?t .
        FILTER(str(?obj) = "${urn}") . 
        }
       """
     }

String getPagedQuery(CiteUrn urn, int offset, int limit) {
return """${prefix}
select ?s  ?o WHERE {
?s  cite:belongsTo ?o .
?s olo:item  ?seq .
FILTER (str(?o) = "${urn}") .
}
ORDER BY ?seq
LIMIT ${limit}
OFFSET ${offset}
"""
}


String getPagedIllQuery(CiteUrn urn, int offset, int limit) {
return """${prefix}

select ?s ?img WHERE {
?s  cite:belongsTo ?o .
?s olo:item  ?seq .
?s hmt:hasDefaultImage ?img .
FILTER (str(?o) = "${urn}") .
}
ORDER BY ?seq
LIMIT ${limit}
OFFSET ${offset}
"""
}
}
