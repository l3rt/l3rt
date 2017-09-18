package lert.core.rule

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase

import scala.collection.mutable.ListBuffer

object ThreadAppender extends AppenderBase[ILoggingEvent]{
  private val store: ThreadLocal[ListBuffer[ILoggingEvent]] = new ThreadLocal[ListBuffer[ILoggingEvent]]

  def startCapturing() = {
    store.set(ListBuffer())
  }

  def getAndClear(): ListBuffer[ILoggingEvent] = {
    val storeBuffer = store.get()
    store.remove()
    storeBuffer
  }

  override def append(eventObject: ILoggingEvent) = {
    Option(ThreadAppender.store.get()).foreach(_.append(eventObject))
  }
}