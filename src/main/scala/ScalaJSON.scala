/** High Performance message transformer 
 *  @Athor : Chiradip Mandal
 *  @Email : chiradip@chiradip.com
 */

import scala.util.matching.Regex
//import com.typesafe.config.{Config, ConfigObject}

trait Template {

  // This is kept seperately here to avoid multiple time pattern compilation 
  val tokenmatcher = """\$\{([^{}]*)\}""".r //matches ${token} format 

  /** It takes a template string as input and makes a list out of all the matched tokens
   *  Example: val tmpl = "{name} and {address}". tokenList will convert it to Lis(name, address)
   */
  def tokenList(tmpl: String): List[String] = tokenmatcher.findAllMatchIn(tmpl).toList.map(x => {
    val xs = x.toString //a little bit of compromize on beauty to avoid toString calls twice
    xs.slice(2, xs.length -1)
  })

  /** Tokens in the template gets substituted with the mapping stored in the map.
   *  Example: input String "my is name is {name}" and the map Map("name" -> "Chiradip")
   *  This function will return the resulting String as "My name is Chiradip"
   */
  def substituteTemplate(source: String, tokenmap: Map[String, String]): String = {
    tokenmatcher.replaceSomeIn ( source,  { case Regex.Groups(token) => tokenmap get token } )
  }

  def tmplValueMap(source: String)(tokenList: List[String]): Map[String, String] 
}

trait JSONTemplate /*(sourceJson: String) extends Template*/ {
  def tmplValueMap(sourceJson: String)(tokenList: List[String]): Map[String, String] = {
    import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
    import com.fasterxml.jackson.module.scala.DefaultScalaModule
    val mapper = new ObjectMapper().registerModule(DefaultScalaModule)
    val jsonNode = mapper.readTree(sourceJson)

    //supposed to be an internal method for valMap(...)- kept it out side just to make it more tidy
    // This is used to specially take care of multi-vlued templated for a single JSON key
    def _mapelem(token: String): (String, String) = {
      val jnode = findVal(jsonNode, token.split("\\.").toList)
      if (jnode != null) 
        (token -> jnode.toString)
      else 
        (token -> "")
    }

    //Tail recursive function to build a map to hold all templates and respective values 
    def valMap(tokenList: List[String], map: Map[String, String] = Map.empty[String, String]): Map[String, String] = tokenList match {
      case token :: Nil  => map + _mapelem(token)
      case token :: tail => valMap(tail, map + _mapelem(token))
    }

    //Tail recursive function to reduce JsonNode into smaller ones recursively and find the final nodle value 
    //TODO: Handle when we get multiple results 
    def findVal(node: JsonNode, nodeValues: List[String]): JsonNode = {
      // Abstraction over node.findValue(head) and node.get(0).findValue(token) in a single method calll
      def findRegOrArrNode(token: String): JsonNode = {
        if(node.isArray) {
          if(token == "*") {
            node.get(1)
          } else
            node.get(token.toInt)
        }
        else 
          node.findValue(token)
      }
      nodeValues match {
        case head :: Nil  => findRegOrArrNode(head)
        case head :: tail => findVal(findRegOrArrNode(head), tail)
      }
    }

    valMap(tokenList)
  }
}

object Main extends App {
  val json = """{ "Header" : { "agid" : "Somehting",
      "mid" : "sessionKey=2acc26fc4341cb46cf6e40c1da80f878",
      "ts" : 1359418502892
    },
  "mp" : { "NumberOfEvents" : 2,
      "fullyQualifiedSearch" : "host=\"NGDEV-IDS\" risk_rating=\"31\"",
      "jobResultsInJsonFmt" : { "init_offset" : 0,
          "messages" : [ { "text" : "base lispy: [ AND 31 host::ngdev-ids ]",
                "type" : "DEBUG"
              },
              { "text" : "search context: user=\"admin\", app=\"search\", bs-pathname=\"/opt/splunk/etc\"",
                "type" : "DEBUG"
              }
            ],
          "preview" : false,
          "results" : [ { "_bkt" : "main~0~52303D33-A99E-4523-8309-192FA6F26E83",
                "_cd" : "0:1121660",
                "_indextime" : "1357684446",
                "_kv" : "1",
                "_raw" : "2012-12-18 18:00:52 eventid=\"1297835095427572342\" hostId=\"NGDEV-IDS\" sig_created=\"20010202\" sig_type=\"anomaly\" severity=\"informational\" app_name=\"sensorApp\" appInstanceId=\"416\" signature=\"3030\" subSigid=\"0\" description=\"TCP SYN Host Sweep\" sig_version=\"S2\" attacker=\"209.198.155.3\" attacker_port=\"0\" attacker_locality=\"OUT\"  target=\"212.69.162.172\" target_port=\"0\" target_locality=\"OUT\"  target=\"208.69.153.147\" target_port=\"0\" target_locality=\"OUT\"  target=\"209.198.155.192\" target_port=\"0\" target_locality=\"OUT\"  target=\"192.204.82.161\" target_port=\"0\" target_locality=\"OUT\"  target=\"204.15.82.143\" target_port=\"0\" target_locality=\"OUT\"  target=\"74.201.90.5\" target_port=\"0\" target_locality=\"OUT\"  target=\"192.204.82.154\" target_port=\"0\" target_locality=\"OUT\"  target=\"216.227.195.155\" target_port=\"0\" target_locality=\"OUT\"  target=\"204.15.82.140\" target_port=\"0\" target_locality=\"OUT\"  target=\"74.201.90.8\" target_port=\"0\" target_locality=\"OUT\"  target=\"65.55.13.90\" target_port=\"0\" target_locality=\"OUT\"  target=\"137.254.16.78\" target_port=\"0\" target_locality=\"OUT\"  target=\"216.221.226.40\" target_port=\"0\" target_locality=\"OUT\"  target=\"184.106.174.153\" target_port=\"0\" target_locality=\"OUT\"  target=\"67.192.1.11\" target_port=\"0\" target_locality=\"OUT\"  target=\"216.52.244.111\" target_port=\"0\" target_locality=\"OUT\"  protocol=\"tcp\" attack_relevance_rating=\"relevant\"  risk_rating=\"31\" threat_rating=\"31\" target_value_rating=\"medium\" interface=\"ge0_0\" interface_group=\"vs0\" vlan=\"0\" mars_category=\"Probe/SpecificPorts\" trigger_packet=\"AAJLVbwgABZH62mACABFAAA8 U4RAAD8GrsnRxpsD2DT0b+RnAbvAIKaDAAAAAKACFtDatwAAAgQFZAQCCAp+ glIQAAAAAAEDAwc=\" trigger_packet_details=\"=00=02KU=BC =00=16G=EBi=80=08=00E=00=00<S=84@=00?=06=AE=C9=D1=C6=9B=03=D84= =F4o=E4g=01=BB=C0 =A6=83=00=00=00=00=A0=02=16=D0=DA=B7=00=00=02=04=05d=04= =02=08 ~=82R=10=00=00=00=00=01=03=03=07\"",
                "_serial" : "0",
                "_si" : "gkasbeka-lnx\nmain",
                "_sourcetype" : "ips_sdee.log",
                "_time" : "2012-12-18T18:00:52.000-07:00",
                "host" : "Ironport1",
                "index" : "CPY001504653920",
                "linecount" : "1",
                "risk_rating" : "31",
                "source" : "ips_sdee.log.xxx.xxx.xxx.zip:./ips_sdee.log.xxx.xxx.xxx.xxx",
                "sourcetype" : "ips_sdee.log",
                "xxx_server" : "somehting-lnx"
              },
              { "_bkt" : "main~0~52303D33-A99E-4523-8309-192FA6F26E83",
                "_cd" : "0:914727",
                "_indextime" : "1357684446",
                "_kv" : "1",
                "_raw" : "2012-12-18 17:47:38 eventid=\"1297835095427565801\" hostId=\"NGDEV-IDS\" sig_created=\"20010202\" sig_type=\"anomaly\" severity=\"informational\" app_name=\"sensorApp\" appInstanceId=\"416\" signature=\"3030\" subSigid=\"0\" description=\"TCP SYN Host Sweep\" sig_version=\"S2\" attacker=\"61.139.87.55\" attacker_port=\"0\" attacker_locality=\"OUT\"  target=\"209.198.142.79\" target_port=\"0\" target_locality=\"OUT\"  target=\"209.198.142.75\" target_port=\"0\" target_locality=\"OUT\"  target=\"209.198.142.78\" target_port=\"0\" target_locality=\"OUT\"  target=\"209.198.142.77\" target_port=\"0\" target_locality=\"OUT\"  target=\"209.198.142.72\" target_port=\"0\" target_locality=\"OUT\"  target=\"209.198.142.74\" target_port=\"0\" target_locality=\"OUT\"  target=\"209.198.142.76\" target_port=\"0\" target_locality=\"OUT\"  target=\"209.198.142.73\" target_port=\"0\" target_locality=\"OUT\"  target=\"209.198.155.5\" target_port=\"0\" target_locality=\"OUT\"  target=\"209.198.155.55\" target_port=\"0\" target_locality=\"OUT\"  target=\"209.198.155.63\" target_port=\"0\" target_locality=\"OUT\"  target=\"209.198.155.43\" target_port=\"0\" target_locality=\"OUT\"  target=\"209.198.155.75\" target_port=\"0\" target_locality=\"OUT\"  target=\"209.198.155.36\" target_port=\"0\" target_locality=\"OUT\"  target=\"209.198.155.90\" target_port=\"0\" target_locality=\"OUT\"  target=\"209.198.155.59\" target_port=\"0\" target_locality=\"OUT\"  protocol=\"tcp\" attack_relevance_rating=\"relevant\"  risk_rating=\"31\" threat_rating=\"31\" target_value_rating=\"medium\" interface=\"ge0_0\" interface_group=\"vs0\" vlan=\"0\" mars_category=\"Probe/SpecificPorts\" trigger_packet=\"ABZH62mAAAJLVbwgCABFAAAw dHsAAGoG2og9i1c30cabOy7nABYNVVNKANkW+3AC///Z6gAAAgQFtAEBBAI=\" trigger_packet_details=\"=00=16G=EBi=80=00=02KU=BC =08=00E=00=000t{=00=00j=06=DA=88=3D=8BW7=D1=C6=9B= ;.=E7=00=16\nUSJ=00=D9=16=FBp=02=FF=FF=D9=EA=00=00=02=04=05=B4=01=01=04=02\"",
                "_serial" : "1",
                "_si" : "gkasbeka-lnx\nmain",
                "_sourcetype" : "ips_sdee.log",
                "_time" : "2012-12-18T17:47:38.000-07:00",
                "host" : "Ironport1",
                "index" : "CPY001504653920",
                "linecount" : "2",
                "risk_rating" : "31",
                "source" : "ips_sdee.log.xxx.xxx.xxx.zip:./ips_sdee.log.xxx.xxx.xxx.xxx",
                "sourcetype" : "ips_sdee.log",
                "splunk_server" : "gkasbeka-lnx"
              }
            ]
        },
      "rawResultsfile" : "/opt/splunk/var/run/splunk/dispatch/scheduler__admin__search__RMD5e5f2153ac15c4ad1_at_1359418500_231/results.csv.gz",
      "savedSearchLink" : "http://something-lnx:8000/app/search/@go?sid=scheduler__admin__search__RMD5e5f2153ac15c4ad1_at_1359418500_231",
      "savedSearchName" : "risk31 more than 3",
      "searchTerms" : "host=\"NGDEV-IDS\" risk_rating=\"31\"",
      "splunkAlertReason" : "Saved Search [risk31 more than 3] number of events(2)"
    }
}
}"""


val jsontemplate = """
"tmpl" : {
    
    "agentId"             :   ${Header.agid},
    "msgId"               :   "Header-mid",
    "ts"                  :   "Header-ts",
    "noEvents"            :   "mp-NumberOfEvents",
    "savedSearchName"     :   "mp-savedSearchName",
    "searchTerms"         :   "mp-searchTerms",
    "fullyQualifiedSearch":   "mp-fullyQualifiedSearch",
    "splunkAlertReason"   :   "mp-splunkAlertReason",
    "time"                :   "mp-jobResultsInJsonFmt-results-_time",
    "clear"               :   "mp-jobResultsInJsonFmt-results-_kv",
    "host"                :   "mp-jobResultsInJsonFmt-results-host",
    "raw"                 :   "mp-jobResultsInJsonFmt-results-_raw",
    "alertText"           :   ${mp.savedSearchName}${mp-searchTerms}${mp-splunkAlertReason}${mp-jobResultsInJsonFmt-results-_sourcetype},
    "companyId"           :   "mp-jobResultsInJsonFmt-results-index",
    "savedSearchLink"     :   "mp-savedSearchLink"
    "ImportantHost"       :   ${mp.jobResultsInJsonFmt.results.*.host}
"""

  class JSONTmpl extends JSONTemplate with Template 
  val jsontmpl = new JSONTmpl 

  //val tokensource = "${mp.savedSearchName}${mp.searchTerms}${mp.splunkAlertReason}${mp.jobResultsInJsonFmt.results._sourcetype}hghghghgh"
  //make it a lazy value so that startup becomes fast but once it is processed has to be sored for further use 
  lazy val tokenList = jsontmpl.tokenList(jsontemplate)
  println("tokenList: " + tokenList)
  def tmplValueMap = jsontmpl.tmplValueMap(json)(tokenList)
  //Visually verify the map
  println(tmplValueMap)

  val final_json = jsontmpl.substituteTemplate(jsontemplate, tmplValueMap)
  println(final_json)

  import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
  import com.fasterxml.jackson.module.scala.DefaultScalaModule
  val mapper = new ObjectMapper().registerModule(DefaultScalaModule)
  val jsonNode = mapper.readTree(final_json)

  //Performance test - has to be relocated to testing section 
  val start = System.nanoTime
  /*for(i <- 1 to 10) {
    val jsontmpl = new JSONTemplate(json) with Template
    jsontmpl.tmplValueMap(jsontmpl.tokenList("${mp.savedSearchName}${mp.searchTerms}${mp.splunkAlertReason}${mp.jobResultsInJsonFmt.results._sourcetype}"))
  }*/
  println("total time: " + (System.nanoTime - start))
}
