/**
 *
 */
package jp.freenet.FNFI.client
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

/**
 * @author qwilas
 *
 */
class FNFIMessage(private val header:String, private val message:String) {
  val head = header
  var items = Map.empty[String, String]
  message.lines.foreach((s:String) => items = items.updated(s.split("=")(0), s.split("=")(1)))
  	
def toString(insertEndMessage:Boolean = true):String = {
      var s:String = head + "\n"
      items foreach ((i) => if (i._2 == "") {s = s + i._1 + "\n"} else {s = s + i._1 + "=" + i._2 + "\n"})
      if (insertEndMessage) {return s + "EndMessage\n"} else {return s}
    }
}

class FNFIMessageWithData(header:String, message:String, datablock:Array[Byte])
extends FNFIMessage(header, message) {
  val data:Array[Byte] = datablock
  override def toString():String = {
    var s:String = head + "\n"
      items foreach ((i) => if (i._2 == "") {s = s + i._1 + "\n"} else {s = s + i._1 + "=" + i._2 + "\n"})
      s + data
}
}

class FNFIController(C:FNFIConnect) {
   def makeMessagefromMap(name:String, m:Map[String, String]):FNFIMessage = {
     var s:String = ""
     m foreach ((mi) => if (mi._2 == "") {s = s + mi._1 + "\n"} else {s = s + mi._1 + "=" + mi._2 + "\n"})
     new FNFIMessage(name, s.init)
  }
   def sendMessagefromMap(title:String, m:Map[String,String], insertEndMessage:Boolean = true) = {
     C.sendMessage(makeMessagefromMap(title, m), insertEndMessage)
   }
   def exchangeMessagefromMap(title:String, m:Map[String,String], insertEndMessage:Boolean = true):FNFIMessage = {
     C.sendMessage(makeMessagefromMap(title, m), insertEndMessage)
     
     C.recvMessage()
   }
   def exchangeMessagefromMaptoData(title:String, m:Map[String,String]): FNFIMessageWithData = {
     C.sendMessage(makeMessagefromMap(title, m), true)
     C.recvMessagewithData()
   }
	def ClientHello(name:String, expectedversion:String) = {
	  val a = Map("Name"->name, "ExpectedVersion"->expectedversion)
	  exchangeMessagefromMap("ClientHello", a, true)
	}
	def ClientPut(data:ByteArrayInputStream, identifier:String, key:String = "CHK@",
	    mime:String = "application/octet-stream") = {
	  val a = Map("URI" -> key, "Metadata.ContentType" -> mime, "Identifier" -> identifier,
	      "Data" -> "", data.toString() -> "")
	      exchangeMessagefromMap("ClientPut", a, false)
	  
	}
	def ClientGet(URI:String, Identifier:String, Priority:Short = 4):FNFIMessageWithData = {
	  val a = Map("URI" -> URI, "Identifier" -> Identifier, "PriorityClass" -> Priority.toString())
	  exchangeMessagefromMap("ClientGet", a)
	  C.recvMessagewithData()
	}
	def GetNode(GiveOpennetRef:Boolean = false, WithPrivate:Boolean = false, WithVolatile:Boolean = false): FNFIMessage = {
	  val a = Map("GiveOpennetRef" -> GiveOpennetRef.toString(),
	      "WithPrivate" -> WithPrivate.toString(),
	      "WithVolatile" -> WithVolatile.toString())
	  exchangeMessagefromMap("GetNode", a, true)
	}
}
final class FNFIConnect(address:String, port:Integer=9481){
import java.io._
import java.net.{InetAddress,ServerSocket,Socket,SocketException}
import java.util.Random


	var socket = new Socket(address, port)
	socket.setTcpNoDelay(true)
	var out = socket.getOutputStream

	var in = new DataInputStream(socket.getInputStream())

	System.err.println("ATTEMPTING TO CONNECT:" + address)
	//System.err.println("SOCK:" + socket.toString() + " STAT:" + socket.isConnected())

def sendMessage(msg:FNFIMessage, insertEndMessage:Boolean = true) = {
	System.err.println(msg.toString(true))
	val code = msg.toString(true)
	
    out.write(code.getBytes())
    out.flush()
}
def recvMessage():FNFIMessage = {
  System.err.println("Receiving")
  var strTmp: String = ""
  var aline:String = ""
  while (in.available() == 0){/*System.err.print(".")*/}//Wait for reply
  while (in.available() != 0 && aline != "EndMessage"){
	aline = in.readLine()
	strTmp = strTmp + aline + "\n"
	
  }
  val head:String = strTmp.split("\n")(0)
  
  val body:String = strTmp.substring(strTmp.indexOf("\n") + 1, strTmp.indexOf("EndMessage") - 1)
  if (head == "ProtocolError") throw new Exception("ProtocolError:\n" + body)
  System.err.println("head: " + head)
  System.err.println("body: " + body)

  new FNFIMessage(head, body)
}
def recvMessagewithData():FNFIMessageWithData = {
  System.err.println("Receiving data")
  var strTmp: String = ""
  while (in.available() == 0){/*System.err.print(".")*/}//Wait for reply
  while (in.available() != 0){
	
	strTmp = strTmp + in.readLine() + "\n"
  }
  val head:String = strTmp.split("\n")(0)
  System.err.println("Head: " + head)
  if (head == "ProtocolError") throw new Exception("ProtocolError:\n" + strTmp.substring(strTmp.indexOf("\n") + 1, strTmp.indexOf("EndMessage") - 1))
  val body:String = strTmp.substring(strTmp.indexOf("\n") + 1, strTmp.indexOf("\nData\n") + 1)
  val datastartindex = strTmp.indexOf("\nData\n") + 7
  System.err.println("Body:\n" + body)
  System.err.println("DataStart: " + datastartindex)
  System.err.println("Datalength:" + (strTmp.length() - datastartindex + 1).toString())
  val data:Array[Byte] = strTmp.substring(datastartindex - 1, strTmp.length()).getBytes()
  System.err.println("Data:\n" + strTmp.substring(datastartindex - 1, strTmp.length()))
  
  new FNFIMessageWithData(head, body, data)
}
def exchangeMessage(msg:FNFIMessage):FNFIMessage = {
  sendMessage(msg)
  recvMessage()
}
private def close(){
  try {
	  out.close()
	  in.close()
	  socket.close()
  } catch {
    case e: Exception => e.printStackTrace()
  }
}
override protected def finalize() {
    try {
        super.finalize();
    } finally {
    	 out.close()
        in.close()
        socket.close()
    }
}


}
