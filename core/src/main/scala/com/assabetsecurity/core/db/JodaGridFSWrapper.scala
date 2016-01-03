package com.assabetsecurity.core.db

import com.mongodb.casbah.gridfs.GenericGridFSFile
import com.mongodb.DB
import scala.Some
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.gridfs.Imports._
import com.mongodb.casbah.commons.Logging

import com.mongodb.DBObject
import com.mongodb.gridfs.{ GridFS => MongoGridFS, GridFSDBFile => MongoGridFSDBFile, GridFSFile => MongoGridFSFile, GridFSInputFile => MongoGridFSInputFile }

import java.io._

import org.joda.time.DateTime


/**
 * User: hugh
 * Date: 9/12/12
 * Time: 3:17 PM
 */

/**
 * This cass replaces GridFSDBFile to overwrite the handling of dates
 */
class JodaHackGridFSDBFile extends com.mongodb.gridfs.GridFSDBFile {
  override def put(key: String, v: Any) = {
    v match {
      case d: DateTime => super.put(key, d.toDate)
      case x => super.put(key, x)
    }
  }
}

/**
 * Reset the object class to be the new date-proof one above.
 * @param db the database
 * @param bucket the bucket name
 */
class JodaGridFSWrapper(db: DB, bucket: String) extends com.mongodb.gridfs.GridFS(db, bucket) {
  def this(db: DB) = this(db, com.mongodb.gridfs.GridFS.DEFAULT_BUCKET)
  _filesCollection.setObjectClass(classOf[JodaHackGridFSDBFile])
}


object JodaEnabledGridFS extends Logging {

  def apply(db: MongoDB) = {
    log.debug("Creating a new GridFS Entry against DB '%s', using default bucket ('%s')", db.name, com.mongodb.gridfs.GridFS.DEFAULT_BUCKET)
    new JodaEnabledGridFS(new JodaGridFSWrapper(db.underlying))
  }

  def apply(db: MongoDB, bucket: String) = {
    log.debug("Creating a new GridFS Entry against DB '%s', using specific bucket ('%s')", db.name, bucket)
    new JodaEnabledGridFS(new JodaGridFSWrapper(db.underlying, bucket))
  }

}

class JodaEnabledGridFS(val underlying: MongoGridFS) extends Iterable[JodaGridFSDBFile] with Logging {
  log.info("Instantiated a new GridFS instance against '%s'", underlying)

  type FileOp = JodaGridFSFile => Unit
  type FileWriteOp = JodaGridFSInputFile => Unit
  type FileReadOp = JodaGridFSDBFile => Unit

  implicit val db = underlying.getDB().asScala

  def iterator = new Iterator[JodaGridFSDBFile] {
    val fileSet = files
    def count() = fileSet.count
    override def length() = fileSet.length
    def numGetMores() = fileSet.numGetMores
    def numSeen() = fileSet.numSeen

    def curr() = new JodaGridFSDBFile(fileSet.next().asInstanceOf[MongoGridFSDBFile])
    def explain() = fileSet.explain

    def next() = new JodaGridFSDBFile(fileSet.next.asInstanceOf[MongoGridFSDBFile])
    def hasNext: Boolean = fileSet.hasNext
  }

  /**
   * loan
   *
   * Basic implementation of the Loan pattern -
   * the idea is to pass a Unit returning function
   * and a Mongo file handle, and work on it within
   * a code block.
   *
   */
  def loan[T <: GenericGridFSFile](file: T)(op: T => Option[AnyRef]) = op(file)
  /**
   * Create a new GridFS File from a scala.io.Source
   *
   * Uses a loan pattern, so you need to pass a curried function which expects a GridFSInputFile
   * as a parameter.
   * It AUTOMATICALLY saves the GridFS file at it's end, so throw an exception if you want to fail.
   * If you don't want automatic saving/loaning please see the createFile method instead.
   * @see createFile
   * @return The ID of the created File (Option[AnyRef])
   */
  def apply(data: scala.io.Source)(op: FileWriteOp) = withNewFile(data)(op)

  /**
   * Create a new GridFS File from a Byte Array
   *
   * Uses a loan pattern, so you need to pass a curried function which expects a GridFSInputFile
   * as a parameter.
   * It AUTOMATICALLY saves the GridFS file at it's end, so throw an exception if you want to fail.
   * If you don't want automatic saving/loaning please see the createFile method instead.
   * @see createFile
   * @return The ID of the created File (Option[AnyRef])
   */
  def apply(data: Array[Byte])(op: FileWriteOp) = withNewFile(data)(op)

  /**
   * Create a new GridFS File from a java.io.File
   *
   * Uses a loan pattern, so you need to pass a curried function which expects a GridFSInputFile
   * as a parameter.
   * It AUTOMATICALLY saves the GridFS file at it's end, so throw an exception if you want to fail.
   * If you don't want automatic saving/loaning please see the createFile method instead.
   * @see createFile
   * @return The ID of the created File (Option[AnyRef])
   */
  def apply(f: File)(op: FileWriteOp) = withNewFile(f)(op)

  /**
   * Create a new GridFS File from a java.io.InputStream
   *
   * Uses a loan pattern, so you need to pass a curried function which expects a GridFSInputFile
   * as a parameter.
   * It AUTOMATICALLY saves the GridFS file at it's end, so throw an exception if you want to fail.
   * If you don't want automatic saving/loaning please see the createFile method instead.
   * @see createFile
   * @return The ID of the created File (Option[AnyRef])
   */
  def apply(in: InputStream)(op: FileWriteOp) = withNewFile(in)(op)

  /**
   * Create a new GridFS File from a java.io.InputStream and a specific filename
   *
   * Uses a loan pattern, so you need to pass a curried function which expects a GridFSInputFile
   * as a parameter.
   * It AUTOMATICALLY saves the GridFS file at it's end, so throw an exception if you want to fail.
   * If you don't want automatic saving/loaning please see the createFile method instead.
   * @see createFile
   * @return The ID of the created File (Option[AnyRef])
   */
  def apply(in: InputStream, filename: String)(op: FileWriteOp) = withNewFile(in, filename)(op)

  /**
   * createFile
   *
   * Creates a new file in GridFS
   *
   * TODO - Should the curried versions give the option to not automatically save?
   */
  def createFile(data: scala.io.Source): JodaGridFSInputFile = throw new UnsupportedOperationException("Currently no support for scala.io.Source")
  def withNewFile(data: scala.io.Source)(op: FileWriteOp) = throw new UnsupportedOperationException("Currently no support for scala.io.Source")

  def createFile(data: Array[Byte]): JodaGridFSInputFile = underlying.createFile(data)
  /**
   * Loan pattern style file creation.
   * @return The ID of the created File (Option[AnyRef])
   */
  def withNewFile(data: Array[Byte])(op: FileWriteOp) = loan(createFile(data)) { fh =>
    op(fh)
    fh.save()
    fh.validate()
    Option(fh.id)
  }

  def createFile(f: File): JodaGridFSInputFile = underlying.createFile(f)
  /**
   * Loan pattern style file creation.
   * @return The ID of the created File (Option[AnyRef])
   */
  def withNewFile(f: File)(op: FileWriteOp) = loan(createFile(f)) { fh =>
    op(fh)
    fh.save()
    fh.validate()
    Option(fh.id)
  }

  def createFile(in: InputStream): JodaGridFSInputFile = underlying.createFile(in)

  /**
   * Loan pattern style file creation.
   * @return The ID of the created File (Option[AnyRef])
   */
  def withNewFile(in: InputStream)(op: FileWriteOp) = loan(createFile(in)) { fh =>
    op(fh)
    fh.save()
    fh.validate()
    Option(fh.id)
  }

  def createFile(in: InputStream, filename: String): JodaGridFSInputFile = underlying.createFile(in, filename)
  /**
   * Loan pattern style file creation.
   * @return The ID of the created File (Option[AnyRef])
   */
  def withNewFile(in: InputStream, filename: String)(op: FileWriteOp) = loan(createFile(in, filename)) { fh =>
    op(fh)
    fh.save()
    fh.validate()
    Option(fh.id)
  }



  /** Find by query - returns a list */
  def find[A <% DBObject](query: A) = underlying.find(query)
  /** Find by query - returns a single item */
  def find(id: ObjectId): JodaGridFSDBFile = underlying.find(id)
  /** Find by query - returns a list */
  def find(filename: String) = underlying.find(filename)

  def findOne[A <% DBObject](query: A): Option[JodaGridFSDBFile] = {
    underlying.findOne(query) match {
      case null => None
      case x => Some(x)
    }
  }
  def findOne(id: ObjectId): Option[JodaGridFSDBFile] = {
    underlying.findOne(id) match {
      case null => None
      case x => Some(x)
    }
  }
  def findOne(filename: String): Option[JodaGridFSDBFile] = {
    underlying.findOne(filename) match {
      case null => None
      case x => Some(x)
    }
  }

  def bucketName = underlying.getBucketName

  /**
   * Returns a cursor for this filestore
   * of all of the files...
   */
  def files = { new MongoCursor(underlying.getFileList) }
  def files[A <% DBObject](query: A) = { new MongoCursor(underlying.getFileList(query)) }

  def remove[A <% DBObject](query: A) = underlying.remove(query)
  def remove(id: ObjectId) = underlying.remove(id)
  def remove(filename: String) = underlying.remove(filename)
}