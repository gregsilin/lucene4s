package com.outr.lucene4s.document

import com.outr.lucene4s._
import com.outr.lucene4s.facet.FacetValue
import com.outr.lucene4s.field.value.FieldAndValue
import com.outr.lucene4s.query.SearchTerm
import org.apache.lucene.document.Document
import org.apache.lucene.index.Term

class DocumentBuilder(lucene: Lucene, update: Option[SearchTerm], val document: Document = new Document) {
  var fullText: List[String] = List.empty[String]

  def fields(fieldAndValues: FieldAndValue[_]*): DocumentBuilder = {
    fieldAndValues.foreach { fv =>
      fv.write(document)
      if (fv.field.fullTextSearchable) {
        synchronized {
          fullText = fv.value.toString :: fullText
        }
      }
    }
    this
  }

  def facets(facetValues: FacetValue*): DocumentBuilder = {
    facetValues.foreach(_.write(document))
    this
  }

  def index(): Unit = {
    if (fullText.nonEmpty) {
      val fullTextString: String = fullText.mkString("\n")
      fields(lucene.fullText(fullTextString))
    }
    val doc = lucene.facetsConfig.build(lucene.taxonomyWriter, document)

    update.foreach { searchTerm =>
      // We need to do an update, so delete based on the criteria
      lucene.delete(searchTerm)
    }
    lucene.indexWriter.addDocument(doc)
    lucene.indexed(this)
  }
}