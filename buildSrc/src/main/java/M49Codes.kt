// source: https://unstats.un.org/unsd/methodology/m49/
val M49Codes = mapOf(
    // africa
    "002" to listOf("015", "202"),
    // northern africa
    "015" to listOf("DZ", "EG", "LY", "MA", "SD", "TN", "EH"),
    // sub-saharan africa
    "202" to listOf("014", "017", "018", "011"),
    // eastern africa
    "014" to listOf("IO", "BI", "KM", "DJ", "ER", "ET", "TF", "KE", "MG", "MW", "MU", "YT", "MZ", "RE", "RW", "SC", "SO", "SS", "UG", "TZ", "ZM", "ZW"),
    // middle africa
    "017" to listOf("AO", "CM", "CF", "TD", "CG", "CD", "GQ", "GA", "ST"),
    // southern africa
    "018" to listOf("BW", "SZ", "LS", "NA", "ZA"),
    // western africa
    "011" to listOf("BJ", "BF", "CV", "CI", "GM", "GH", "GN", "GW", "LR", "ML", "MR", "NE", "NG", "SN", "SL", "TG", "SH"),

    // americas
    "019" to listOf("419", "021"),
    // north america
    "003" to listOf("021", "029", "013"),
    // northern america
    "021" to listOf("BM", "CA", "GL", "PM", "US"),
    // latin america and the caribbean
    "419" to listOf("029", "013", "005"),
    // caribbean
    "029" to listOf("AI", "AG", "AW", "BS", "BB", "BQ", "VG", "KY", "CU", "CW", "DM", "DO", "GD", "GP", "HT", "JM", "MQ", "MS", "PR", "BL", "KN", "LC", "MF", "VC", "SX", "TT", "TC", "VI"),
    // central america
    "013" to listOf("BZ", "CR", "SV", "GT", "HN", "MX", "NI", "PA"),
    // south america
    "005" to listOf("AR", "BO", "BV", "BR", "CL", "CO", "EC", "FK", "GF", "GY", "PY", "PE", "GS", "SR", "UY", "VE"),

    // antarctica
    "010" to listOf("AQ"),

    // asia
    "142" to listOf("143", "030", "035", "035", "034", "145"),
    // central asia
    "143" to listOf("KZ", "KG", "TJ", "TM", "UZ"),
    // eastern asia
    "030" to listOf("CN", "HK", "MO", "KP", "JP", "MN", "KR"),
    // south-eastern asia
    "035" to listOf("BN", "KH", "ID", "LA", "MY", "MM", "PH", "SG", "TH", "TL", "VN"),
    // south asia
    "034" to listOf("AF", "BD", "BT", "IN", "IR", "MV", "NP", "PK", "LK"),
    // western asia
    "145" to listOf("AM", "AZ", "BH", "CY", "GE", "IQ", "IL", "JO", "KW", "LB", "OM", "QA", "SA", "PS", "SY", "TR", "AE", "YE"),

    // europe
    "150" to listOf("151", "154", "039", "155"),
    // eastern europe
    "151" to listOf("BY", "BG", "CZ", "HU", "PL", "MD", "RO", "RU", "SK", "UA"),
    // northern europe
    "154" to listOf("AX", "830", "DK", "EE", "FO", "FI", "IS", "IE", "IM", "LV", "LT", "NO", "SJ", "SE", "GB"),
    "830" to listOf("GG", "JE", "CQ"),
    // southern europe
    "039" to listOf("AL", "AD", "BA", "HR", "GI", "GR", "VA", "IT", "MT", "ME", "MK", "PT", "SM", "RS", "SI", "ES", "XK"),
    // western europe
    "155" to listOf("AT", "BE", "FR", "DE", "LI", "LU", "MC", "NL", "CH"),

    // oceania
    "009" to listOf("053", "054", "057", "061"),
    // australia and new zealand
    "053" to listOf("AU", "CX", "CC", "HM", "NZ", "NF"),
    // Melanesia
    "054" to listOf("FJ", "NC", "PG", "SB", "VU"),
    // Micronesia
    "057" to listOf("GU", "KI", "MH", "FM", "NR", "MP", "PW", "UM"),
    // Polynesia
    "061" to listOf("AS", "CK", "PF", "NU", "PN", "WS", "TK", "TV", "WF")
)
