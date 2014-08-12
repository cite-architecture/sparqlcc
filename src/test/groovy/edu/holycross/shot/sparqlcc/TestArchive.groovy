package edu.holycross.shot.sparqlcc

import edu.harvard.chs.cite.CiteUrn
import edu.holycross.shot.prestochango.CollectionArchive

import static org.junit.Assert.*
import org.junit.Test

class TestArchive extends GroovyTestCase {
    
    File caps = new File("testdata/testinventory.xml")
    String schemaFileName = "schemas/CiteCollectionInventory.rng"

    @Test void testArchive() {
      CollectionArchive archive = new CollectionArchive(caps, schemaFileName)
      assert archive
    }
}
