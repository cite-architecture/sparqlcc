package edu.holycross.shot.sparqlcc

import edu.harvard.chs.cite.CiteUrn

import static org.junit.Assert.*
import org.junit.Test

class TestSize extends GroovyTestCase {
  // use default fuseki settings to test
  String serverUrl = "http://localhost:3030/ds/"

  groovy.xml.Namespace ccns = new groovy.xml.Namespace("http://chs.harvard.edu/xmlns/cite")

  CiteUrn venAimgs = new CiteUrn("urn:cite:hmt:vaimg")
  Integer expectedCount = 966


  void testSize() {
    CiteCollectionSvc cc = new CiteCollectionSvc(serverUrl)
    String q = cc.qg.getCollSizeQuery(venAimgs)
    println "USED QUERY: " + q
    String sizeReply = cc.getCollectionSizeReply(venAimgs)


    Integer actualCount
    def root = new XmlParser().parseText(sizeReply)
    root[ccns.reply][ccns.count].each { c ->
      actualCount = c.text() as Integer
    }
    assert actualCount == expectedCount
  }

}
