package edu.holycross.shot.sparqlcc

import edu.holycross.shot.prestochango.CollectionArchive
import edu.harvard.chs.cite.CiteUrn

import groovyx.net.http.*
import groovyx.net.http.HttpResponseException
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

import groovy.json.JsonSlurper
import groovy.xml.XmlUtil
import groovy.xml.MarkupBuilder


class Query {
    boolean debug = true

    /** RDF namespace statments */
    String prefix = "prefix hmt:        <http://www.homermultitext.org/hmt/rdfverbs/> \nprefix citedata:        <http://www.homermultitext.org/hmt/citedata/> \nprefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n prefix  xsd: <http://www.w3.org/2001/XMLSchema#>\n"

    /** Archive to query  */
    CollectionArchive archive

    /** XML namespace for all CITE Collection replies. */
    static String CITE_NS = "http://chs.harvard.edu/xmlns/cite"

    /** Groovy Namespace object for use in parsing capabilities document. */
    groovy.xml.Namespace citens = new groovy.xml.Namespace(CITE_NS)

    /** Default property name for ordering query results */
    String orderProperty = null

    /** SPARQL query endpoint for HMT graph triples.   */
    String tripletServerUrl

    Query(CollectionArchive archv, String sparqlEndPoint) {
        this.archive = archv
        this.tripletServerUrl = sparqlEndPoint
    }
    
    /** Submits a SPARQL query to the configured endpoint
    * and returns the text of the reply.
    * @param acceptType  Value to use for headers.Accept in 
    * http request.  If the value of acceptType is 'application/json'
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
		/*
        HTTPBuilder http = new HTTPBuilder(q)
		try {
				http.request( Method.GET, ContentType.TEXT ) { req ->
					headers.Accept = acceptType
					response.success = { resp, reader ->
						replyString = reader.text
					}
				}
		} finally {
			http.shutdown()
		}
        return replyString
		*/
        URL queryUrl = new URL(q)
        return queryUrl.getText("UTF-8")

    }
    

    /** 
    * Gets a list of property names, other than canonical identifier,
    * for a collection. Since the CollectionArchive class's config object
    * includes all property names, we have to filter that to eliminate 
    * the canonical ID property from the list.
    * @param collUrn CiteUrn for the collection to query.
    * @throws Exception if no configuration file can be found
    * for requested collection.
    * @returns A list of property names, in the same order
    * as they appear in the archive's inventory.
    */
    ArrayList listPropNames(CiteUrn collUrn) 
    throws Exception {

        String canonical = archive.getCanonicalIdProperty(collUrn)

        System.err.println "\n\nPROPNAMES: CANOICALS: ${canonical}\n\n" 
        String collectionName = collUrn.getCollection()
        def props = []
        def config =  archive.citeConfig[collectionName]
        if (!config) {
            throw new Exception("No configuration found for collection ${collectionName}")
        } else {
            StringBuffer pNames = new StringBuffer()
            config['properties'].each { p ->
                if (p['name'] != canonical) {
                    props.add(p['name'])
                }
            } 
        }
        return props
    }
    

    /* MODIFICATIONS TO MAKE:
    -- CONSULT INFO ON TYPE IN ORDER TO QUOTE VALUE OR NOT APPROPRIATELY
    -- SORT BY DEFAULT IF ORDERED COLLECTION
    -- CHECK FOR OPTIONAL SORT PARAM
    */

    String sparqlForList(CiteUrn collectionUrn, Object triples) {
        StringBuffer qBuff = new StringBuffer("${prefix}\nSELECT ?urn ")
        StringBuffer filterBuff = new StringBuffer()
        def propNames = listPropNames(collectionUrn)        
        propNames.eachWithIndex { p, i ->
            qBuff.append("?p${i} ")
        }
        

        // define collection
        qBuff.append (" WHERE {\n")
        //qBuff.append("<${collectionUrn}> rdf:type hmt:CiteCollection .\n")
        qBuff.append("?urn hmt:belongsTo <${collectionUrn}> .\n")
        // list properties from config:

        def collName = collectionUrn.getCollection()
        propNames.eachWithIndex { prop, i ->
            qBuff.append("?urn citedata:${collName}_${prop} ?p${i} . \n")
        }


        // define additional properties, and build up filter list
        triples.eachWithIndex { t, count  ->
            def propName = t[0]
            def propValue = t[1]
            def propIndex = -1
            propNames.eachWithIndex { prop, i ->
                if (propName == prop) {
                    propIndex = i
                }
            }
            // switch on type of prop....
            filterBuff.append("FILTER (str(?p${propIndex}) = '" + propValue + "') .\n")
        }

        qBuff.append("\n${filterBuff.toString()}")
        qBuff.append("}\n")
        return qBuff.toString() 
    }


    


    /** Implements the 'list' query extension.
    * @param collectionUrn The collection to query.
    * @param List of one or more triplets, composed of a property name,
    * a query value, and an optional operand to use for the query.
    * @returns The XML representation of the matching CITE Objects.
    */
    String listResults(CiteUrn collectionUrn, Object triples) {
        StringBuffer reply = new StringBuffer("<list xmlns='http://shot.holycross.edu/xmlns/citequery' xmlns:cite='http://shot.holycross.edu/xmlns/cite'>\n")
        reply.append("<request>\n<collection>${collectionUrn}</collection>\n<trips>${triples}</trips>\n</request>\n<reply>\n")

        def propNames = listPropNames(collectionUrn)        
        def cfg = this.archive.citeConfig[collectionUrn.getCollection()]                
        def nameProxies = [:]        
        def labelProxies = [:]
        def typeProxies = [:]
        propNames.eachWithIndex { p, i ->
            nameProxies["p${i}"] = p
            cfg['properties'].each { configProp ->
                if (configProp['name'] == p) {
                    labelProxies["p${i}"] = configProp['label']
                    typeProxies["p${i}"] = configProp['type']
                }
            }
        }

        
        String query = sparqlForList(collectionUrn, triples)        

        System.err.println "\n\nQUERYING WITH SPARQL " + query + "\n\n"

        String objReply =  getSparqlReply("application/json", query)
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(objReply)

        parsedReply.results.bindings.each { b ->
            reply.append("<citeObject urn='" + b.urn?.value + "'>\n")
            nameProxies.keySet().each { k ->
  
                reply.append("\t<citeProperty name='" + nameProxies[k] + "'")
                reply.append(" label='" + labelProxies[k] + "' type='" + typeProxies[k] + "'>")
                reply.append(b."${k}"?.value + "</citeProperty>\n")
            }
            reply.append("</citeObject>\n")
        }
        reply.append("</reply>\n</list>\n")
        return reply.toString()
    }

/*

            if (t.size() == 2) {
                op = "="
            } else {
                op = t[2]                
            }
            if (i > 0) {
                qBuff.append(" AND " )
            }
            qBuff.append(" '" + propName + "' ${op} '" + propValue + "'")
*/
/*
        if (orderProperty) {
            qBuff.append(" ORDER BY ${orderProperty}")
        }
        //System.err.println "QUERY :  ${qBuff}" 
*/
//        URL queryUrl = new URL(endPoint + "query?sql=" +  URLEncoder.encode(qBuff.toString()) + "&key=${svc.apiKey}")

        //System.err.println "QUERY URL :  ${queryUrl}" 

//        String rawReply = queryUrl.getText("UTF-8")
//        JsonSlurper slurp = new JsonSlurper()
//        def rows = slurp.parseText(rawReply).rows






/*
    def getCount(CiteUrn collectionUrn, Object triples, String groupBy) {
        return "NOT YET IMPLEMETNED"
    }
    def getAggregate(CiteUrn collectionUrn, Object triples, String countBy) {
        return "NOT YET IMPLEMETNED"
    }
*/

    /** 
    * gets a list of pairs in the form value-imgroi
    */
/*
    def getVisualAggregate(CiteUrn collectionUrn, CiteUrn imgUrn, String mapProperty, String imgProperty ) {
        def results = []

        def triples = []
        def imgTriple = [imgProperty, imgUrn.toString(),  ' STARTS WITH ']
        triples.add(imgTriple)
        //println "Use triples " + triples
        String collName = collectionUrn.getCollection()
        getResults(collectionUrn, triples).each { r ->
            def mapping = [svc.getValue(collName,mapProperty,r),  svc.getValue(collName,imgProperty,r) ]
            results.add(mapping)
        }
        return results
    }
*/


}
