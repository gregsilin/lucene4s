package com.outr.lucene4s.field.value

import com.outr.lucene4s.field.Field
import org.apache.lucene.document.Document

case class FieldAndValue[T](field: Field[T], value: T) {
  def write(document: Document): Unit = {
    val f = field.support.toLucene(field, value)
    document.add(f)
    field.support.toSortedField(field, value).foreach(document.add)
  }
}