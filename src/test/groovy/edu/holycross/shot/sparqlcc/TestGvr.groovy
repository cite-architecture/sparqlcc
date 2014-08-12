package edu.holycross.shot.sparqlcc

import edu.harvard.chs.cite.CiteUrn

import static org.junit.Assert.*
import org.junit.Test

class TestGvr extends GroovyTestCase {
    // use default fuseki settings to test
    String serverUrl = "http://localhost:3030/ds/"
    groovy.xml.Namespace ccns = new groovy.xml.Namespace("http://chs.harvard.edu/xmlns/cite")

    CiteUrn venAimgs = new CiteUrn("urn:cite:hmt:vaimg")
    Integer expectedNumber = 966

    void testGVR() {
        CiteCollectionSvc cc = new CiteCollectionSvc(serverUrl)
        String gvrReply = cc.getValidReffReply(venAimgs)
        def root = new XmlParser().parseText(gvrReply)
	Integer urnsFound =  root[ccns.reply][ccns.urn].size() 
        assert urnsFound == expectedNumber
    }


}
