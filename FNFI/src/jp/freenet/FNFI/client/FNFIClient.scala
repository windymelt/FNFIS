package jp.freenet.FNFI.client
import sun.org.mozilla.javascript.debug.Debugger

object FNFIClient extends App {
  System.err.println("DEBUG START")
  Console.println("FNFI Client")
  val F = new FNFIController(new FNFIConnect("cirno"))
  val hello = F.ClientHello("FNFIS", "2.0")
  Console.println("Supported Language is " + hello.items("NodeLanguage"))
  //Test: get FNFI.dtd
  val fnfidtd = F.ClientGet("CHK@3tKHs~gb-PgmhXnfuJ00PD5RuIAykBzisNM9x542nqc,36eqIi3A7a-kWqPg2lVtdKCsi5J51K~sUniGkiXf154,AAIC--8/FNFI01.dtd",
		  "brobtextzeromoebius")
		  Console.println("FNFI01.dtd:\n" + fnfidtd.data.toString())
true
}