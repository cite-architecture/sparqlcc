package edu.holycross.shot.sparqlcc

import edu.harvard.chs.cite.CiteUrn

import static org.junit.Assert.*
import org.junit.Test

class TestOrdering extends GroovyTestCase {
  // use default fuseki settings to test
  def serverUrl = "http://localhost:3030/ds/"
  groovy.xml.Namespace ccns = new groovy.xml.Namespace("http://chs.harvard.edu/xmlns/cite")

  // an ordered collection:
  CiteUrn ms = new CiteUrn("urn:cite:hmt:u4")
  CiteUrn twentyrecto = new CiteUrn("urn:cite:hmt:u4.20r")

    
  void testGetFirst() {
    String expected = "urn:cite:hmt:u4.1r"
    CiteCollectionSvc cc = new CiteCollectionSvc(serverUrl)
    String getFirst = cc.getFirstReply(ms)
    def root = new XmlParser().parseText(getFirst)
    def firstUrn = root[ccns.reply][ccns.urn][0]
    assert firstUrn.text() == expected
  }
    
  void testGetLast() {
    String expected = "urn:cite:hmt:u4.194v"
    CiteCollectionSvc cc = new CiteCollectionSvc(serverUrl)
    def root = new XmlParser().parseText( cc.getLastReply(ms))
    def lastUrn =  root[ccns.reply][ccns.urn][0]
    assert lastUrn.text() == expected
  }

  
  void testGetNextObj() {
    String expected = "urn:cite:hmt:u4.20v"
    CiteCollectionSvc cc = new CiteCollectionSvc(serverUrl)
    def root = new XmlParser().parseText(cc.getNextReply(twentyrecto))
    def nextObj =  root[ccns.reply][ccns.citeObject][0]
    assert nextObj.'@urn' == expected
  }

  void testGetNextUrn() {
    String expected = "urn:cite:hmt:u4.20v"
    CiteCollectionSvc cc = new CiteCollectionSvc(serverUrl)
    assert cc.getNextUrn(twentyrecto) == expected
  }    


  void testGetPrevObj() {
    String expected = "urn:cite:hmt:u4.19v"
    CiteCollectionSvc cc = new CiteCollectionSvc(serverUrl)
    def root = new XmlParser().parseText(cc.getPrevReply(twentyrecto))
    def prevObj =  root[ccns.reply][ccns.citeObject][0]
    assert prevObj.'@urn' == expected
  }

  void testGetPrevUrn() {
    String expected = "urn:cite:hmt:u4.19v"
    CiteCollectionSvc cc = new CiteCollectionSvc(serverUrl)
    assert cc.getPrevUrn(twentyrecto) == expected
  }


  void testGetPrevNextUrnReply() {
    CiteCollectionSvc cc = new CiteCollectionSvc(serverUrl)
    def root = new XmlParser().parseText(cc.getPrevNextUrnReply(twentyrecto))

    String expectedPrev = "urn:cite:hmt:u4.19v"
    def prevUrn = root[ccns.reply][ccns.prevnext][ccns.prev][ccns.urn][0]
    assert prevUrn.text() == expectedPrev

    String expectedNext = "urn:cite:hmt:u4.20v"
    def nextUrn = root[ccns.reply][ccns.prevnext][ccns.next][ccns.urn][0]
    assert nextUrn.text() == expectedNext
  }

  /*
    void testGetObjectPlusReply() {
        CiteCollectionSvc cc = new CiteCollectionSvc(serverUrl)
        System.err.println cc.getObjectPlusReply(twentyrecto)
        
    }

    void testGroupInfo() {
      // write this test...
      //CiteCollectionSvc cc = new CiteCollectionSvc(serverUrl)
    }

  */


}
