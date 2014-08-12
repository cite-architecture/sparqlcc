package edu.holycross.shot.sparqlcc

import edu.harvard.chs.cite.CiteUrn


/*
import groovyx.net.http.*
import groovyx.net.http.HttpResponseException
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
*/


/** Implementation of all requests of the CITE Collection service.
*/
class CiteCollectionSvc {

    int debug = 0

    /** SPARQL query endpoint for HMT graph triples.   */
    String tripletServerUrl

    /** QueryGenerator object formulating SPARQL query strings. */
    QueryGenerator qg


    /** Constructor initializing required value for SPARQL endpoint.   */
    CiteCollectionSvc(String serverUrl) {
        this.tripletServerUrl = serverUrl
        this.qg = new QueryGenerator()
    }


    String getNextReply(CiteUrn urn) {
        StringBuffer reply = new StringBuffer("<GetNext xmlns='http://chs.harvard.edu/xmlns/cite'>\n")
        reply.append("<request>\n<urn>${urn}</urn>\n</request>\n<reply>\n")

        String nxtQuery = qg.getNextObjQuery(urn)
        if (debug > 0) { System.err.println "Next query : " + nxtQuery  }
        String objReply =  getSparqlReply("application/json", nxtQuery)
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(objReply)
        boolean objOpened = false

        parsedReply.results.bindings.each { b ->
            if (!objOpened) {
                reply.append("<citeObject urn='" + b.nxt.value + "'>\n" )
                objOpened = true
            }
            String label = b.l.value
            String propType = b.t.value
            String propName = b.p.value
            reply.append("<citeProperty name='" + propName + "' label='" + label + "' type='" + propType + "'>${b.pval.value}</citeProperty>\n")
        }
        if (objOpened) {
            reply.append("</citeObject>\n")
        }
        reply.append("</reply>\n</GetNext>\n")

    }



    String getPrevUrn(CiteUrn urn) {
        String urnStr = ""
        String urnReply =  getSparqlReply("application/json", qg.getPrevUrnQuery(urn))
        if (debug > 0) {
            System.err.println "prev query = " + qg.getPrevUrnQuery(urn)
        }
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(urnReply)
        parsedReply.results.bindings.each { b ->
            if (debug > 0) {
                System.err.println  "prevUrn binds: " + b
            }
            urnStr = b.prev?.value
        }
        return urnStr
    }

    String getNextUrn(CiteUrn urn) {
        String urnStr = ""
        String urnReply =  getSparqlReply("application/json", qg.getNextUrnQuery(urn))
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(urnReply)
        parsedReply.results.bindings.each { b ->
            urnStr = b.next?.value
        }
        return urnStr

    }


    String getPrevNextUrnReply(CiteUrn urn) {
        StringBuffer reply = new StringBuffer("<GetPrevNextUrn xmlns='http://chs.harvard.edu/xmlns/cite'>\n")
        reply.append("<request>\n<urn>${urn}</urn>\n</request>\n<reply>\n<prevnext>\n")
        reply.append("<prev><urn>${getPrevUrn(urn)}</urn></prev>")
        reply.append("<next><urn>${getNextUrn(urn)}</urn></next>")
        reply.append("</prevnext>\n</reply>\n</GetPrevNextUrn>\n")
    }


    String getPrevReply(CiteUrn urn) {
        StringBuffer reply = new StringBuffer("<GetPrev xmlns='http://chs.harvard.edu/xmlns/cite'>\n")
        reply.append("<request>\n<urn>${urn}</urn>\n</request>\n<reply>\n")

        String prevQuery = qg.getPrevObjQuery(urn)
        if (debug > 0) { System.err.println  "Query for prev obj: ${prevQuery}" }
        String objReply =  getSparqlReply("application/json", prevQuery)
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(objReply)

        boolean objOpened = false

        
        parsedReply.results.bindings.each { b ->
            if (!objOpened) {
                reply.append("<citeObject urn='" + b.prev.value + "'>\n" )
                objOpened  = true
            }
            String label = b.l.value
            String propType = b.t.value
            String propName = b.p.value
            reply.append("<citeProperty name='" + propName + "' label='" + label + "' type='" + propType + "'>${b.pval.value}</citeProperty>\n")
        }
        if (objOpened) {
            reply.append("</citeObject>\n")
        }
        reply.append("</reply>\n</GetPrev>\n")

    }


    /** Constructs reply to GetCollectionSize request.
    * @param urn URN of a CITE Collection.
    * @returns A String with the XML text of a valid reply.
    */
    String getCollectionSizeReply(CiteUrn urn) {
        StringBuffer reply = new StringBuffer("<GetCollectionSize xmlns='http://chs.harvard.edu/xmlns/cite'>\n")
        reply.append("<request>\n<urn>${urn}</urn>\n</request>\n<reply>\n")
        String objReply =  getSparqlReply("application/json", qg.getCollSizeQuery(urn))
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(objReply)

        parsedReply.results.bindings.each { b ->
            reply.append("<count>${b.size.value}</count>\n")
        }

        reply.append("</reply>\n</GetCollectionSize>\n")
    }



    String getFirstReply(CiteUrn urn) {
        StringBuffer reply = new StringBuffer("<GetFirst xmlns='http://chs.harvard.edu/xmlns/cite'>\n")
        reply.append("<request>\n<urn>${urn}</urn>\n</request>\n<reply>\n")


        String firstQuery = qg.getFirstQuery(urn)
        if (debug > 0) { System.err.println "Firs query: " + firstQuery }
        String objReply =  getSparqlReply("application/json", firstQuery)
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(objReply)

        parsedReply.results.bindings.each { b ->
            reply.append("<urn>${b.urn.value}</urn>\n")
        }

        reply.append("</reply>\n</GetFirst>\n")
    }

    String getLastReply(CiteUrn urn) {
        StringBuffer reply = new StringBuffer("<GetLast xmlns='http://chs.harvard.edu/xmlns/cite'>\n")
        reply.append("<request>\n<urn>${urn}</urn>\n</request>\n<reply>\n")

        String lastQuery = qg.getLastQuery(urn)
        if (debug  > 0) {System.err.println "LAST QUERY: " + lastQuery }
        String objReply =  getSparqlReply("application/json", lastQuery)
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(objReply)

        parsedReply.results.bindings.each { b ->
            reply.append("<urn>${b.urn.value}</urn>\n")
        }

        reply.append("</reply>\n</GetLast>\n")
    }

    String getValidReffReply(CiteUrn urn) {
        StringBuffer reply = new StringBuffer("<GetValidReff xmlns='http://chs.harvard.edu/xmlns/cite'>\n")
        reply.append("<request>\n<urn>${urn}</urn>\n</request>\n<reply>\n")

        String objReply =  getSparqlReply("application/json", qg.getValidReffQuery(urn))
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(objReply)

        parsedReply.results.bindings.each { b ->
            reply.append("<urn>${b.urn.value}</urn>\n")
        }

        reply.append("</reply>\n</GetValidReff>\n")
    }

    String getObjectReply(CiteUrn urn) {
        StringBuffer reply = new StringBuffer("<GetObject xmlns='http://chs.harvard.edu/xmlns/cite'>\n")
        reply.append("<request>\n<urn>${urn}</urn>\n</request>\n<reply>\n")


System.err.println "GETOBJ: " + qg.getObjectQuery(urn)
        String objReply =  getSparqlReply("application/json", qg.getObjectQuery(urn))
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(objReply)
        reply.append("<citeObject urn='" + urn + "'>\n" )
        
        parsedReply.results.bindings.each { b ->
            
            String label = b.l.value
            String propType = b.t.value
            String propName = b.p.value

            reply.append("<citeProperty name='" + propName + "' label='" + label + "' type='" + propType + "'>${b.pval.value}</citeProperty>\n")
        }
        reply.append("</citeObject>\n")
        reply.append("</reply>\n</GetObject>\n")
    }





    String getObjectPlusReply(CiteUrn urn) {
        StringBuffer reply = new StringBuffer("<GetObjectPlus xmlns='http://chs.harvard.edu/xmlns/cite'>\n")
        reply.append("<request>\n<urn>${urn}</urn>\n</request>\n<reply>\n")

        String objReply =  getSparqlReply("application/json", qg.getObjectQuery(urn))
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(objReply)
        reply.append("<citeObject urn='" + urn + "'>\n" )
        
        parsedReply.results.bindings.each { b ->
            
            String label = b.l.value
            String propType = b.t.value
            String propName = b.p.value

            reply.append("<citeProperty name='" + propName + "' label='" + label + "' type='" + propType + "'>${b.pval.value}</citeProperty>\n")
        }
        reply.append("</citeObject>\n")

        reply.append("<prevnext>\n<prev><urn>${getPrevUrn(urn)}</urn></prev>\n")
        reply.append("<next><urn>${getNextUrn(urn)}</urn></next>\n</prevnext>")

        reply.append("</reply>\n</GetObjectPlus>\n")
    }


    /** Submits a SPARQL query to the configured endpoint
    * and returns the text of the reply.
    * @param acceptType  Value to use for headers.Accept in 
    * http request.  If the value of acceptType is 'applicatoin/json'
    * fuseki's additional 'output' parameter is added to the 
    * http request string so that the string returned for the
    * the request will be in JSON format.  This separates the 
    * concerns of forming SPARQL queries from the decision about
    * how to parse the reply in a given format.
    * @param query Text of SPARQL query to submit.
    * @returns Text content of reply. 
    */
    String getSparqlReply(String acceptType, String query) {
        String replyString
        def encodedQuery = URLEncoder.encode(query)
        def q = "${tripletServerUrl}query?query=${encodedQuery}"
        if (acceptType == "application/json") {
            q +="&output=json"
        }
	URL queryUrl = new URL(q)
        return queryUrl.getText("UTF-8")
    }



    String getPagedIllustration(CiteUrn orderedColl, Integer offset, Integer limit)  {
        StringBuffer reply = new StringBuffer("<GetPagedIllustration xmlns='http://chs.harvard.edu/xmlns/cite'>\n")
        reply.append("<request>\n<urn>${orderedColl}</urn>\n</request>\n<offset>${offset}</offset><reply>\n")

        String pagedIllQuery = qg.getPagedIllQuery(orderedColl, offset, limit)
        if (debug > 0) { System.err.println "PAGED ILL " + pagedIllQuery}
        String objReply =  getSparqlReply("application/json", pagedIllQuery)
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(objReply)
        reply.append('<sequence urn="' + orderedColl + '" offset="' + offset + '" limit="' + limit + '">\n' )
        
        parsedReply.results.bindings.each { b ->
            reply.append( '<urn img="' + b.img.value + '">' +b.s.value + "</urn>\n")
        }
        reply.append("</sequence>\n</reply>\n</GetPagedIllustration>\n")
        return reply.toString()
    }

    String getPagedIllustration(String orderedColl, Integer offset, Integer limit) 
    throws Exception {
        CiteUrn urn 
        try {
            urn = new CiteUrn(orderedColl)
            return getPagedIllustration(urn, offset, limit)
        } catch (Exception e) {
            System.err.println "getPagedIllustration: could not make URN from ${orderedColl}"
            throw e
        }
    }


    String getPagedIllustration(String orderedColl) {
        return getPagedIllustration(orderedColl, 0, 20)
    }


    String getPaged(String orderedColl, Integer offset, Integer limit) 
    throws Exception {
        CiteUrn urn 
        try {
           urn = new CiteUrn(orderedColl)
           return  getPaged(urn, offset, limit)
        } catch (Exception e) {
            System.err.println "getPaged: could not make URN from ${orderedColl}"
            throw e
        }
   }

    String getPaged(CiteUrn urn, Integer offset, Integer limit) {

        StringBuffer reply = new StringBuffer("<GetPaged xmlns='http://chs.harvard.edu/xmlns/cite'>\n")
        reply.append("<request>\n<urn>${urn}</urn>\n<offset>${offset}</offset>\n<limit>${limit}</limit>\n</request>\n<reply>\n")

        String pagedQuery = qg.getPagedQuery(urn, offset, limit)
        if (debug > 0) { System.err.println "PAGED " + pagedQuery}
        String objReply =  getSparqlReply("application/json", pagedQuery)
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(objReply)

        reply.append('<sequence urn="' + urn + '" offset="' + offset + '" limit="' + limit + '">\n' )
        
        parsedReply.results.bindings.each { b ->
            reply.append( "<urn>${b.s.value}</urn>\n")
        }

        reply.append("</sequence>\n</reply>\n</GetPaged>\n")
        return reply.toString()
    }

    String getPaged(String orderedColl) {
        return getPaged(orderedColl, 0, 20)
    }


}




