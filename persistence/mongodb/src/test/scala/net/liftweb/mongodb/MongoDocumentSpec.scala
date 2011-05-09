/*
 * Copyright 2010 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.liftweb
package mongodb

import org.bson.types.ObjectId
import org.specs.Specification

import common._
import json.ext.JsonBoxSerializer


package mongodocumentspecs {
  case class Primitives(
    _id: ObjectId,
    s: String,
    i: Int,
    l: Long,
    d: Double,
    f: Float,
    b: Byte,
    bi: BigInt,
    bool: Boolean,
    sh: Short,
    jli: java.lang.Integer,
    jll: java.lang.Long,
    jld: java.lang.Double,
    jlf: java.lang.Float,
    jlb: java.lang.Byte,
    jlbool: java.lang.Boolean,
    jlsh: java.lang.Short
  ) extends MongoDocument[Primitives] {

    def meta = Primitives
  }
  object Primitives extends MongoDocumentMeta[Primitives] {
    override def formats = super.formats + new ObjectIdSerializer
  }

  case class NullTestEmbed(nul: String)
  case class NullTestDoc(_id: ObjectId, nul: String, ent: NullTestEmbed)
  extends MongoDocument[NullTestDoc] {
    def meta = NullTestDoc
  }
  object NullTestDoc extends MongoDocumentMeta[NullTestDoc] {
    override def formats = super.formats + new ObjectIdSerializer
  }

  case class OptionTestDoc(_id: ObjectId, optNone: Option[String],
    optSome: Option[String])
  extends MongoDocument[OptionTestDoc] {
    def meta = OptionTestDoc
  }
  object OptionTestDoc extends MongoDocumentMeta[OptionTestDoc] {
    override def formats = super.formats + new ObjectIdSerializer
  }

  case class BoxTestDoc(_id: ObjectId, boxEmpty: Box[String],
    boxFull: Box[String], boxFail: Box[String])
  extends MongoDocument[BoxTestDoc] {
    def meta = BoxTestDoc
  }
  object BoxTestDoc extends MongoDocumentMeta[BoxTestDoc] {
    override def formats = super.formats + new JsonBoxSerializer + new ObjectIdSerializer
  }

  case class MapTestDoc(_id: ObjectId, aMap: Map[String, String])
  extends MongoDocument[MapTestDoc] {
    def meta = MapTestDoc
  }
  object MapTestDoc extends MongoDocumentMeta[MapTestDoc] {
    override def formats = super.formats + new ObjectIdSerializer
  }
}

/**
 * System specification for MongoDocument
 */
object MongoDocumentSpec extends Specification("MongoDocument Specification") with MongoTestKit {

  def passSaveAndRetrieveTests(obj: MongoDocument[_], meta: MongoDocumentMeta[_]): Unit = {
    obj.save
    val objFromDb = meta.find(obj._id.asInstanceOf[ObjectId])
    objFromDb.isDefined must_== true
    objFromDb.get must_== obj
  }

  "MongoDocument" should {

    "handle primitives" in {
      checkMongoIsRunning
      import mongodocumentspecs._

      val primitives = Primitives(
        ObjectId.get,
        "This is a String",
        123,
        124L,
        (125.5).toDouble,
        (126.5).toFloat,
        (127).toByte,
        BigInt(128999),
        true,
        (129).toShort,
        new java.lang.Integer(130),
        new java.lang.Long(131L),
        new java.lang.Double(132.5),
        new java.lang.Float(133.5),
        new java.lang.Byte("12"),
        java.lang.Boolean.TRUE,
        new java.lang.Short("135")
      )
      passSaveAndRetrieveTests(primitives, Primitives)
    }

    "handle null" in {
      checkMongoIsRunning
      import mongodocumentspecs._

      val ntd = NullTestDoc(ObjectId.get, null, NullTestEmbed(null))
      passSaveAndRetrieveTests(ntd, NullTestDoc)
    }

    "handle Option" in {
      checkMongoIsRunning
      import mongodocumentspecs._

      val otd = OptionTestDoc(ObjectId.get, None, Some("Some String"))
      passSaveAndRetrieveTests(otd, OptionTestDoc)
    }

    "handle Box using JsonBoxSerializer" in {
      checkMongoIsRunning
      import mongodocumentspecs._

      val btd = BoxTestDoc(ObjectId.get, Empty, Full("Full String"), Failure("This is a failure"))
      passSaveAndRetrieveTests(btd, BoxTestDoc)
    }

    "handle Maps properly" in {
      checkMongoIsRunning
      import mongodocumentspecs._

      val mtd = MapTestDoc(ObjectId.get, Map("x" -> "1"))
      passSaveAndRetrieveTests(mtd, MapTestDoc)

      // empty map
      val mtd2 = MapTestDoc(ObjectId.get, Map[String, String]())
      passSaveAndRetrieveTests(mtd2, MapTestDoc)
    }
  }
}
