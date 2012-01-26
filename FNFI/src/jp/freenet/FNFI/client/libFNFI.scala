/**
 *
 */
package jp.freenet.FNFI.client

/**
 * @author qwilas
 *
 */
class FNFIMessage(header:String, message:String) {
  val head = header
  var items = Map.empty[String, String]
  message.lines.foreach((s:String) => items = items.updated(s.split("=")(0), s.split("=")(1)))
  	
  override def toString():String = {
      var s:String = head
      items foreach ((i) => s = s + i._1 + "=" + i._2 + "\n")
      s + "EndMessage"
    }
}
class FNFIController(C:FNFIConnect) {
   def makeMessagefromMap(name:String, m:Map[String, String]):FNFIMessage = {
     var s:String = ""
     m foreach ((mi) => s = s + mi._1 + "=" + mi._2 + "\n")
     new FNFIMessage(name, s.init)
  }
   def sendMessagefromMap(title:String, m:Map[String,String]) = {
     C.sendMessage(makeMessagefromMap(title, m))
   }
	def ClientHello(name:String, expectedversion:String) = {
	  val a = Map("Name"->name, "ExpectedVersion"->expectedversion)
	  sendMessagefromMap("ClientHello", a)
	}
}
final class FNFIConnect(address:String, port:Integer=9481){
import java.io._
import java.net.{InetAddress,ServerSocket,Socket,SocketException}
import java.util.Random

private val ia:java.net.InetAddress = null
private val socket:java.net.Socket = null
private val out:java.io.ObjectOutputStream = null
private val in:java.io.ObjectInputStream = null

try {
      val ia = InetAddress.getByName(address)
      val socket = new Socket(ia, port)
      val out = new ObjectOutputStream(new DataOutputStream(socket.getOutputStream()))
      val in = new DataInputStream(socket.getInputStream())

      out.close()
      in.close()
      socket.close()
} catch {
      case e: IOException => e.printStackTrace()
}
def sendMessage(msg:FNFIMessage) = {
    out.writeObject(msg.toString)
    out.flush()
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
