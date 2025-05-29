import java.io.FileInputStream
import java.io.InputStream


const val ERROR_STATE = 0

const val EOF_SYMBOL = -1//
const val SKIP_SYMBOL = 0 //done
const val NEWLINE = '\n'.code
const val INT = 1 // done
const val VAR = 2 // done
const val DOUBLE = 3 //done
const val PLUS = 4 //done
const val MINUS = 5//done
const val LPAREN = 6 //done
const val RPAREN = 7 //done
const val LBRACE = 8 //done
const val RBRACE = 9 //done
const val ASSIGN = 10 //done
const val COUNTRY = 11//done
const val REGION = 12//done
const val CITY = 13 //done
const val STREET = 14 //done
const val POINT = 15 // done
const val STATION = 16 //done
const val ACCIDENT = 17 // done
const val PARK = 18 // done
const val FIRE_HYDRANT = 19 //done
const val CIRCLE_HEATMAP = 20 //done
const val STRING = 21 //done
const val BLOCK = 22 //done
const val BEND = 23 //done
const val LINE = 24 //done
const val COMMA = 25//done
const val ADDRESS = 26 //done
const val DEC_STRING = 27//done
const val DEC_INT = 28 //done
const val DEC_COORD = 29//done
const val DEC_DOUBLE = 30 //done
const val AVAILABLE_UNITS = 31 // done
const val STATION_TYPE = 32 // done
const val ACCIDENT_TYPE = 33 //done
const val IF = 34//done
const val ELSE = 35//done
const val EQUALS = 36 //done
const val LESSER = 37 //done
const val GREATER = 38 //done
const val NOT_EQUALS = 39 //done
const val CIRCLE = 40
const val CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_"
const val STRING_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789.,"
const val NUMBER_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

interface DFA {
    val states: Set<Int>
    val alphabet: IntRange
    fun next(state: Int, code: Int): Int
    fun symbol(state: Int): Int
    val startState: Int
    val finalStates: Set<Int>
}

object Lexer: DFA {
    override val states = (1..163).toSet()
    override val alphabet = 0..255
    override val startState = 1
    override val finalStates = setOf(2,3,4,5,6,7,8,9,10,11,13,20,26,29,35,40,45,53,56,68,72,80,85,88,92,93,99,109,112,117,123,137,142,147,149,153,154,155,156,158,159,162,163)

    private val numberOfStates = states.max() + 1 // plus the ERROR_STATE
    private val numberOfCodes = alphabet.max() + 1 // plus the EOF
    private val transitions = Array(numberOfStates) { IntArray(numberOfCodes) }
    private val values = Array(numberOfStates) { SKIP_SYMBOL }

    private fun setTransition(from: Int, chr: Char, to: Int) {
        transitions[from][chr.code + 1] = to // + 1 because EOF is -1 and the array starts at 0
    }

    private fun setTransition(from: Int, code: Int, to: Int) {
        transitions[from][code + 1] = to
    }

    private fun setSymbol(state: Int, symbol: Int) {
        values[state] = symbol
    }

    override fun next(state: Int, code: Int): Int {
        assert(states.contains(state))
        assert(alphabet.contains(code))
        return transitions[state][code + 1]
    }

    override fun symbol(state: Int): Int {
        assert(states.contains(state))
        return values[state]
    }

    //problemi, country, street
    init {
        // INT [0-9]
        for (num in '0'.. '9'){
            setTransition(1,num,2)
            setTransition(2,num,2)
        }
        setSymbol(2,INT)

        // VAR a1 a11 aa1 nesto problemi
        for(char in CHARS){
            setTransition(1,char,3)
            setTransition(3,char,3)
        }
        for(d in '0'..'9'){
            setTransition(3,d,4)
            setTransition(4,d,4)
        }
        setSymbol(3,VAR)
        setSymbol(4,VAR)

        // DOUBLE
        setTransition(2,'.',12)
        for(d in '0'..'9'){
            setTransition(12,d,13)
            setTransition(13,d,13)
        }
        setSymbol(13,DOUBLE)


        // PLUS
        setTransition(1,'+',5)
        setSymbol(5,PLUS)

        //MINUS
        setTransition(1,'-',6)
        setSymbol(6,MINUS)

        //LPAREN
        setTransition(1,'(',7)
        setSymbol(7,LPAREN)

        //RPAREN
        setTransition(1,')',8)
        setSymbol(8,RPAREN)

        //LBRACE
        setTransition(1,'{',9)
        setSymbol(9,LBRACE)

        //RBRACE
        setTransition(1,'}',10)
        setSymbol(10,RBRACE)

        //ASSIGN
        setTransition(1,'=',11)
        setSymbol(11,ASSIGN)

        // COUNTRY
        setTransition(1,'c',14)
        setTransition(14,'o',15)
        setTransition(15,'u',16)
        setTransition(16,'n',17)
        setTransition(17,'t',18)
        setTransition(18,'r',19)
        setTransition(19,'y',20)
        for (c in CHARS) {
            if ((c != 'o') && (c != 'i')) setTransition(14, c, 3)
            if (c != 'u') setTransition(15, c, 3)
            if (c != 'n') setTransition(16, c, 3)
            if (c != 't') setTransition(17, c, 3)
            if (c != 'r') setTransition(18, c, 3)
            if (c != 'y') setTransition(19, c, 3)
            if (c != ' ') setTransition(20, c, 3)
        }
        setSymbol(20,COUNTRY)

        // REGION
        setTransition(1,'r',21)
        setTransition(21,'e',22)
        setTransition(22,'g',23)
        setTransition(23,'i',24)
        setTransition(24,'o',25)
        setTransition(25,'n',26)
        for (c in CHARS) {
            if (c != 'e') setTransition(21, c, 3)
            if (c != 'g') setTransition(22, c, 3)
            if (c != 'i') setTransition(23, c, 3)
            if (c != 'o') setTransition(24, c, 3)
            if (c != 'n') setTransition(25, c, 3)
            if ( c != ' ') setTransition(26, c, 3)
        }
        setSymbol(26,REGION)


        // CITY
        setTransition(14,'i',27)
        setTransition(27,'t',28)
        setTransition(28,'y',29)
        for (c in CHARS) {
            if ((c != 't') && (c != 'r')) setTransition(27, c, 3)
            if (c != 'y') setTransition(28, c, 3)
            if (c != ' ') setTransition(29, c, 3)
        }
        setSymbol(29,CITY)

        // STREET
        setTransition(1,'s',30)
        setTransition(30,'t',31)
        setTransition(31,'r',32)
        setTransition(32,'e',33)
        setTransition(33,'e',34)
        setTransition(34,'t',35)
        for (c in CHARS) {
            if (c != 't') setTransition(30, c, 3)
            if ((c != 'r') && (c != 'a') ) setTransition(31, c, 3)
            if (c != 'e') setTransition(32, c, 3)
            if (c != 'e') setTransition(33, c, 3)
            if (c != 't') setTransition(34, c, 3)
            if (c != ' ') setTransition(35, c, 3)
        }
        setSymbol(35,STREET)

        // POINT
        setTransition(1,'p',36)
        setTransition(36,'o',37)
        setTransition(37,'i',38)
        setTransition(38,'n',39)
        setTransition(39,'t',40)
        for (c in CHARS) {
            if ((c != 'o') && (c != 'a')) setTransition(36, c, 3)
            if (c != 'i') setTransition(37, c, 3)
            if (c != 'n') setTransition(38, c, 3)
            if (c != 't') setTransition(39, c, 3)
            if (c != ' ') setTransition(40, c, 3)
        }
        setSymbol(40,POINT)

        // STATION
        setTransition(31,'a',41)
        setTransition(41,'t',42)
        setTransition(42,'i',43)
        setTransition(43,'o',44)
        setTransition(44,'n',45)
        for (c in CHARS) {
            if (c != 't') setTransition(41, c, 3)
            if (c != 'i') setTransition(42, c, 3)
            if (c != 'o') setTransition(43, c, 3)
            if (c != 'n') setTransition(44, c, 3)
            if ((c != ' ') && (c != '_')) setTransition(45, c, 3)
        }
        setSymbol(45, STATION)

        // ACCIDENT
        setTransition(1,'a',46)
        setTransition(46,'c',47)
        setTransition(47,'c',48)
        setTransition(48,'i',49)
        setTransition(49,'d',50)
        setTransition(50,'e',51)
        setTransition(51,'n',52)
        setTransition(52,'t',53)
        for (c in CHARS) {
            if ((c != 'c') && (c != 'd') && (c != 'v')) setTransition(46, c, 3)
            if (c != 'c') setTransition(47, c, 3)
            if (c != 'i') setTransition(48, c, 3)
            if (c != 'd') setTransition(49, c, 3)
            if (c != 'e') setTransition(50, c, 3)
            if (c != 'n') setTransition(51, c, 3)
            if (c != 't') setTransition(52, c, 3)
            if ((c != ' ') && (c != '_')) setTransition(53, c, 3)
        }
        setSymbol(53, ACCIDENT)

        // PARK
        setTransition(36,'a',54)
        setTransition(54,'r',55)
        setTransition(55,'k',56)
        for (c in CHARS) {
            if (c != 'r') setTransition(54, c, 3)
            if (c != 'k') setTransition(55, c, 3)
            if (c != ' ') setTransition(56, c, 3)
        }
        setSymbol(56, PARK)

        // FIRE_HYDRANT
        setTransition(1,'f',57)
        setTransition(57,'i',58)
        setTransition(58,'r',59)
        setTransition(59,'e',60)
        setTransition(60,'_',61)
        setTransition(61,'h',62)
        setTransition(62,'y',63)
        setTransition(63,'d',64)
        setTransition(64,'r',65)
        setTransition(65,'a',66)
        setTransition(66,'n',67)
        setTransition(67,'t',68)
        for (c in CHARS) {
            if (c != 'i') setTransition(57, c, 3)
            if (c != 'r') setTransition(58, c, 3)
            if (c != 'e') setTransition(59, c, 3)
            if (c != '_') setTransition(60, c, 3)
            if (c != 'h') setTransition(61, c, 3)
            if (c != 'y') setTransition(62, c, 3)
            if (c != 'd') setTransition(63, c, 3)
            if (c != 'r') setTransition(64, c, 3)
            if (c != 'a') setTransition(65, c, 3)
            if (c != 'n') setTransition(66, c, 3)
            if (c != 't') setTransition(67, c, 3)
            if (c != ' ') setTransition(68, c, 3)
        }
        setSymbol(68, FIRE_HYDRANT)




        //CIRCLE
        setTransition(27,'r',69)
        setTransition(69,'c',70)
        setTransition(70,'l',71)
        setTransition(71,'e',72)
        for (c in CHARS) {
            if (c != 'c') setTransition(69, c, 3)
            if (c != 'l') setTransition(70, c, 3)
            if (c != 'e') setTransition(71, c, 3)
            if ((c != ' ') && (c != '_')) setTransition(72, c, 3)
        }
        setSymbol(72, CIRCLE)
        // CIRCLE_HEATMAP

        setTransition(72,'_',73)
        setTransition(73,'h',74)
        setTransition(74,'e',75)
        setTransition(75,'a',76)
        setTransition(76,'t',77)
        setTransition(77,'m',78)
        setTransition(78,'a',79)
        setTransition(79,'p',80)
        for (c in CHARS) {
            if (c != 'c') setTransition(69, c, 3)
            if (c != 'l') setTransition(70, c, 3)
            if (c != 'e') setTransition(71, c, 3)
            if (c != '_') setTransition(72, c, 3)
            if (c != 'h') setTransition(73, c, 3)
            if (c != 'e') setTransition(74, c, 3)
            if (c != 'a') setTransition(75, c, 3)
            if (c != 't') setTransition(76, c, 3)
            if (c != 'm') setTransition(77, c, 3)
            if (c != 'a') setTransition(78, c, 3)
            if (c != 'p') setTransition(79, c, 3)
            if (c != ' ') setTransition(80, c, 3)
        }
        setSymbol(80, CIRCLE_HEATMAP)

        // BLOCK
        setTransition(1,'b',81)
        setTransition(81,'l',82)
        setTransition(82,'o',83)
        setTransition(83,'c',84)
        setTransition(84,'k',85)
        for (c in CHARS) {
            if ((c != 'l') && (c != 'e')) setTransition(81, c, 3)
            if (c != 'o') setTransition(82, c, 3)
            if (c != 'c') setTransition(83, c, 3)
            if (c != 'k') setTransition(84, c, 3)
            if (c != ' ') setTransition(85, c, 3)
        }
        setSymbol(85,BLOCK)

        // BEND
        setTransition(81,'e',86)
        setTransition(86,'n',87)
        setTransition(87,'d',88)
        for(c in CHARS) {
            if (c != 'n') setTransition(86, c, 3)
            if (c != 'd') setTransition(87, c, 3)
            if (c != ' ') setTransition(88, c, 3)
        }
        setSymbol(88,BEND)

        // LINE
        setTransition(1,'l',89)
        setTransition(89,'i',90)
        setTransition(90,'n',91)
        setTransition(91,'e',92)
        for (c in CHARS) {
            if (c != 'i') setTransition(89, c, 3)
            if (c != 'n') setTransition(90, c, 3)
            if (c != 'e') setTransition(91, c, 3)
            if (c != ' ') setTransition(92, c, 3)
        }
        setSymbol(92,LINE)

        // COMMA
        setTransition(1,',',93)
        setSymbol(93,COMMA)

        // ADDRESS
        setTransition(46,'d',94)
        setTransition(94,'d',95)
        setTransition(95,'r',96)
        setTransition(96,'e',97)
        setTransition(97,'s',98)
        setTransition(98,'s',99)
        for (c in CHARS) {
            if (c != 'd') setTransition(94, c, 3)
            if (c != 'r') setTransition(95, c, 3)
            if (c != 'e') setTransition(96, c, 3)
            if (c != 's') setTransition(97, c, 3)
            if (c != 's') setTransition(98, c, 3)
            if (c != ' ') setTransition(99, c, 3)
        }
        setSymbol(99,ADDRESS)

        // DEC_STRING
        setTransition(1, 'd', 100)
        setTransition(100, 'e', 101)
        setTransition(101, 'c', 102)
        setTransition(102, '_', 103)
        setTransition(103, 's', 104)
        setTransition(104, 't', 105)
        setTransition(105, 'r', 106)
        setTransition(106, 'i', 107)
        setTransition(107, 'n', 108)
        setTransition(108, 'g', 109)
        for (c in CHARS) {
            if (c != 'e') setTransition(100, c, 3)
            if (c != 'c') setTransition(101, c, 3)
            if (c != '_') setTransition(102, c, 3)
            if ((c != 's') && (c != 'i') && (c != 'c') && (c != 'd')) setTransition(103, c, 3)
            if (c != 't') setTransition(104, c, 3)
            if (c != 'r') setTransition(105, c, 3)
            if (c != 'i') setTransition(106, c, 3)
            if (c != 'n') setTransition(107, c, 3)
            if (c != 'g') setTransition(108, c, 3)
            if (c != ' ') setTransition(109, c, 3)
        }
        setSymbol(109, DEC_STRING)

        // DEC_INT
        setTransition(103, 'i', 110)
        setTransition(110, 'n', 111)
        setTransition(111, 't', 112)
        for(c in CHARS){
            if (c != 'n') setTransition(110, c, 3)
            if (c != 't') setTransition(111, c, 3)
            if (c != ' ') setTransition(112, c, 3)
        }
        setSymbol(112, DEC_INT)

        //DEC_COORD
        setTransition(103, 'c', 113)
        setTransition(113, 'o', 114)
        setTransition(114, 'o', 115)
        setTransition(115, 'r', 116)
        setTransition(116, 'd', 117)
        for(c in CHARS){
            if (c != 'o') setTransition(113, c, 3)
            if (c != 'o') setTransition(114, c, 3)
            if (c != 'r') setTransition(115, c, 3)
            if (c != 'd') setTransition(116, c, 3)
            if (c != ' ') setTransition(117, c, 3)
        }
        setSymbol(117, DEC_COORD)


        // DEC_DOUBLE
        setTransition(103, 'd', 118)
        setTransition(118, 'o', 119)
        setTransition(119, 'u', 120)
        setTransition(120, 'b', 121)
        setTransition(121, 'l', 122)
        setTransition(122, 'e', 123)
        for(c in CHARS){
            if (c != 'o') setTransition(118, c, 3)
            if (c != 'u') setTransition(119, c, 3)
            if (c != 'b') setTransition(120, c, 3)
            if (c != 'l') setTransition(121, c, 3)
            if (c != 'e') setTransition(122, c, 3)
            if (c != ' ') setTransition(123, c, 3)
        }
        setSymbol(123, DEC_DOUBLE)

        // AVAILABLE_UNITS
        setTransition(46,'v',124)
        setTransition(124,'a',125)
        setTransition(125,'i',126)
        setTransition(126,'l',127)
        setTransition(127,'a',128)
        setTransition(128,'b',129)
        setTransition(129,'l',130)
        setTransition(130,'e',131)
        setTransition(131,'_',132)
        setTransition(132,'u',133)
        setTransition(133,'n',134)
        setTransition(134,'i',135)
        setTransition(135,'t',136)
        setTransition(136,'s',137)
        for (c in CHARS) {
            if (c != 'a') setTransition(124, c, 3)
            if (c != 'i') setTransition(125, c, 3)
            if (c != 'l') setTransition(126, c, 3)
            if (c != 'a') setTransition(127, c, 3)
            if (c != 'b') setTransition(128, c, 3)
            if (c != 'l') setTransition(129, c, 3)
            if (c != 'e') setTransition(130, c, 3)
            if (c != '_') setTransition(131, c, 3)
            if (c != 'u') setTransition(132, c, 3)
            if (c != 'n') setTransition(133, c, 3)
            if (c != 'i') setTransition(134, c, 3)
            if (c != 't') setTransition(135, c, 3)
            if (c != 's') setTransition(136, c, 3)
            if (c != ' ') setTransition(137, c, 3)
        }
        setSymbol(137, AVAILABLE_UNITS)

        // STATION_TYPE
        setTransition(45,'_',138)
        setTransition(138,'t',139)
        setTransition(139,'y',140)
        setTransition(140,'p',141)
        setTransition(141,'e',142)
        for (c in CHARS) {
            if (c != 't') setTransition(138, c, 3)
            if (c != 'y') setTransition(139, c, 3)
            if (c != 'p') setTransition(140, c, 3)
            if (c != 'e') setTransition(141, c, 3)
            if (c != ' ') setTransition(142, c, 3)
        }
        setSymbol(142, STATION_TYPE)

        // ACCIDENT_TYPE
        setTransition(53,'_',143)
        setTransition(143,'t',144)
        setTransition(144,'y',145)
        setTransition(145,'p',146)
        setTransition(146,'e',147)
        for (c in CHARS) {
            if (c != 't') setTransition(143, c, 3)
            if (c != 'y') setTransition(144, c, 3)
            if (c != 'p') setTransition(145, c, 3)
            if (c != 'e') setTransition(146, c, 3)
            if (c != ' ') setTransition(147, c, 3)
        }
        setSymbol(147, ACCIDENT_TYPE)

        // IF
        setTransition(1, 'i', 148)
        setTransition(148, 'f', 149)
        for (c in CHARS) {
            if (c != 'f') setTransition(148, c, 3)
            if (c != ' ') setTransition(149, c, 3)
        }
        setSymbol(149, IF)


        // ELSE
        setTransition(1, 'e', 150)
        setTransition(150, 'l', 151)
        setTransition(151, 's', 152)
        setTransition(152, 'e', 153)
        for (c in CHARS) {
            if (c != 'l') setTransition(150, c, 3)
            if (c != 's') setTransition(151, c, 3)
            if (c != 'e') setTransition(152, c, 3)
            if (c != ' ') setTransition(153, c, 3)
        }
        setSymbol(153, ELSE)

        // EQUALS
        setTransition(11, '=', 154)
        setSymbol(154, EQUALS)

        // LESSER
        setTransition(1, '<', 155)
        setSymbol(155, LESSER)

        // GREATER
        setTransition(1, '>', 156)
        setSymbol(156, GREATER)

        // NOT_EQUALS
        setTransition(1, '!', 157)
        setTransition(157, '=', 158)
        setSymbol(158, NOT_EQUALS)

        //SKIP
        setTransition(1,'\n',159)
        setTransition(1,'\r',159)
        setTransition(1,'\t',159)
        setTransition(1,' ',159)
        setSymbol(159, SKIP_SYMBOL)

        // STRING
        setTransition(1,'"',160)
        for (c in STRING_CHARS) {
            setTransition(160, c, 161)
            setTransition(161, c, 161)

        }
        setTransition(161, '"', 162)
        setSymbol(162,STRING)

        // EOF
        setTransition(1,-1, 163)
        setSymbol(163, EOF_SYMBOL)


    }
}


data class Token(val symbol: Int, val lexeme: String, val startRow: Int, val startColumn: Int)

class Scanner(private val automaton: DFA, private val stream: InputStream) {
    private var last: Int? = null
    private var row = 1
    private var column = 1

    private fun updatePosition(code: Int) {
        if (code == NEWLINE) {
            row += 1
            column = 1
        } else {
            column += 1
        }
    }

    fun getToken(): Token {
        val startRow = row
        val startColumn = column
        val buffer = mutableListOf<Char>()

        var code = last ?: stream.read()
        var state = automaton.startState
        while (true) {
            val nextState = automaton.next(state, code)
            if (nextState == ERROR_STATE) break

            state = nextState
            updatePosition(code)
            buffer.add(code.toChar())
            code = stream.read()
        }
        last = code

        if (automaton.finalStates.contains(state)) {
            val symbol = automaton.symbol(state)
            return if (symbol == SKIP_SYMBOL) {
                getToken()
            } else {
                val lexeme = String(buffer.toCharArray())
                Token(symbol, lexeme, startRow, startColumn)
            }
        } else {
            throw Error("Invalid pattern at ${row}:${column}")
        }
    }
}

fun name(symbol: Int) =
    when (symbol) {
        INT -> "int"
        VAR -> "variable"
        DOUBLE -> "double"
        PLUS -> "plus"
        MINUS -> "minus"
        LPAREN -> "lparen"
        RPAREN -> "rparen"
        LBRACE -> "lbrace"
        RBRACE -> "rbrace"
        ASSIGN -> "assign"
        COUNTRY -> "country"
        REGION -> "region"
        CITY -> "city"
        STREET -> "street"
        POINT -> "point"
        STATION -> "station"
        ACCIDENT -> "accident"
        PARK -> "park"
        FIRE_HYDRANT -> "fire_hydrant"
        CIRCLE_HEATMAP -> "circle_heatmap"
        STRING -> "string"
        BLOCK -> "block"
        BEND -> "bend"
        LINE -> "line"
        COMMA -> "comma"
        ADDRESS -> "address"
        DEC_STRING -> "dec_string"
        DEC_INT -> "dec_integer"
        DEC_COORD -> "dec_coordinate"
        DEC_DOUBLE -> "dec_double"
        AVAILABLE_UNITS -> "available_units"
        STATION_TYPE -> "station_type"
        ACCIDENT_TYPE -> "accident_type"
        IF -> "if"
        ELSE -> "else"
        EQUALS -> "equals"
        LESSER -> "lesser"
        GREATER -> "greater"
        NOT_EQUALS -> "notequals"
        CIRCLE -> "circle"
        else -> throw Error("Invalid symbol")
    }

fun printTokens(scanner: Scanner) {
    val token = scanner.getToken()
    if (token.symbol != EOF_SYMBOL) {
        print("${name(token.symbol)}(\"${token.lexeme}\") ")
        printTokens(scanner)
    }
}
data class Country(val name: String, val regions: List<Region>)

data class Region(val name: String, val block: Block, val cities: List<City>)

data class City(
    val name: String,
    val operations: List<Operation>,
    val mandatory: MandatoryElements,
    val additional: AdditionalElements?
)

data class Block(val corners: List<Coordinate>)

data class Coordinate(val x: Double, val y: Double)

data class Expr(val value: String, val next: Pair<String, Expr>? = null)

fun Expr.evaluate(context: Map<String, Any>): Double {
    val base = value.toDoubleOrNull() ?: context[value]?.let {
        when (it) {
            is Int -> it.toDouble()
            is Double -> it
            is Expr -> it.evaluate(context)
            else -> error("Variable $value is not numeric")
        }
    } ?: error("Unknown identifier: $value")

    return next?.let { (op, rhs) ->
        val right = rhs.evaluate(context)
        when (op) {
            "+" -> base + right
            "-" -> base - right
            else -> error("Unsupported operator in expression: $op")
        }
    } ?: base
}

fun resolveStringValue(raw: String, context: Map<String, Any>): String {
    return if (raw.startsWith("\"")) raw else context[raw]?.toString() ?: error("Unknown string identifier: $raw")
}

fun resolveIntValue(raw: String, context: Map<String, Any>): Int {
    return raw.toIntOrNull() ?: (context[raw] as? Int ?: error("Unknown int identifier: $raw"))
}

fun resolveDoubleValue(raw: String, context: Map<String, Any>): Double {
    return raw.toDoubleOrNull() ?: (context[raw] as? Double ?: error("Unknown double identifier: $raw"))
}


fun buildEvaluationContext(ops: List<Operation>): Map<String, Any> {
    val ctx = mutableMapOf<String, Any>()
    for (op in ops) {
        when (op.value) {
            is Expr -> ctx[op.variable] = op.value
            is Number -> ctx[op.variable] = op.value
            else -> ctx[op.variable] = op.value
        }
    }
    return ctx
}

data class Operation(val type: String, val variable: String, val value: Any)

data class MandatoryElements(
    val streets: List<Street>,
    val stations: List<Station>,
    val accidents: List<Accident>
)

data class AdditionalElements(
    val parks: List<Park>,
    val hydrants: List<FireHydrant>,
    val heatmaps: List<CircleHeatmap>
)

data class Street(
    val name: String,
    val preBendOps: List<Operation>,
    val bend: Bend,
    val postBendOps: List<Operation>,
    val line: Line
)

data class Bend(val start: Coordinate, val end: Coordinate, val angle: Expr)

data class Line(val start: Coordinate, val end: Coordinate)

data class Station(
    val name: String,
    val preAddressOps: List<Operation>,
    val address: String,
    val midOps: List<Operation>,
    val units: Int,
    val postOps: List<Operation>,
    val type: String
)

data class Accident(
    val name: String,
    val preAddressOps: List<Operation>,
    val address: String,
    val postOps: List<Operation>,
    val type: String
)

data class Park(
    val name: String,
    val operations: List<Operation>,
    val block: Block
)

data class FireHydrant(
    val name: String,
    val operations: List<Operation>,
    val point: Coordinate
)

data class CircleHeatmap(
    val name: String,
    val operations: List<Operation>,
    val center: Coordinate,
    val radius: Expr
)


data class IfElse(
    val left: Expr,
    val comparator: String,
    val right: Expr,
    val thenOps: List<Operation>,
    val elseOps: List<Operation>?
)



class Parser(private val scanner: Scanner) {
    private var currentToken: Token = scanner.getToken()

    private fun match(expectedSymbol: Int): Token {
        if (currentToken.symbol == expectedSymbol) {
            val matched = currentToken
            currentToken = scanner.getToken()
            return matched
        } else {
            error("Expected ${name(expectedSymbol)} but found ${name(currentToken.symbol)} ${currentToken.symbol} at ${currentToken.startRow}:${currentToken.startColumn}")
        }
    }

    // Country ::= country string{ Regions }
    fun parseCountry() : Country {
        match(COUNTRY)
        val name = match(STRING).lexeme
        match(LBRACE)
        val regions = parseRegions()
        match(RBRACE)
        return Country(name,regions)
    }


    /*
    Regions  ::= Region Regions'
    Regions' ::= Region Regions' | ε
    Region   ::= region string { Block Cities }
    preskocimo Regions' z uporabo  while zanko
    */
    private fun parseRegions() : List<Region> {
        val regions = mutableListOf<Region>()
        while (currentToken.symbol == REGION) {
            regions.add(parseRegion())
        }
        return regions
    }


    private fun parseRegion() : Region {
        match(REGION)
        val name = match(STRING).lexeme
        match(LBRACE)
        val block = parseBlock()
        val cities = parseCities()
        match(RBRACE)
        return Region(name, block, cities)
    }

    /*
    Cities ::= City Cities'
    Cities' ::= City Cities' | ε
    City ::= city string { Expression }
    preskocimo Cities' z uporabo  while zanko
    */

    private fun parseCities() : List<City> {
        val cities = mutableListOf<City>()
        while (currentToken.symbol == CITY) {
            cities.add(parseCity())
        }
        return cities
    }


    private fun parseCity() : City {
        match(CITY)
        val name = match(STRING).lexeme
        match(LBRACE)
        //Expression ::= Operations MandatoryElementsList AdditionalElementsList
        val ops = parseOperations()
        val mandatory = parseMandatoryElementsList()
        val additional = parseAdditionalElementsList()
        match(RBRACE)
        return City(name, ops, mandatory, additional)
    }



    /*
    Operations ::= Operation Operations | ε
    Operation ::= AssignString | AssignInt | AssignCoord | AssignDouble | IfElse
    */

    private fun parseOperations(): List<Operation> {
        val ops = mutableListOf<Operation>()
        while (currentToken.symbol in setOf(DEC_STRING, DEC_INT, DEC_COORD, DEC_DOUBLE, IF)) {
            if (currentToken.symbol == IF) {
                val ifelse = parseIfElse()
                ops.add(Operation("ifelse", "", ifelse))
            } else {
                ops.add(parseOperation())
            }
        }
        return ops
    }


    private fun parseOperation(): Operation {
        return when (currentToken.symbol) {
            DEC_STRING -> {
                match(DEC_STRING)
                val variable = match(VAR).lexeme
                match(ASSIGN)
                val value = match(STRING).lexeme
                Operation("dec_string", variable, value)
            }
            DEC_INT -> {
                match(DEC_INT)
                val variable = match(VAR).lexeme
                match(ASSIGN)
                val value = match(INT).lexeme.toInt()
                Operation("dec_int", variable, value)
            }
            DEC_COORD -> {
                match(DEC_COORD)
                val variable = match(VAR).lexeme
                match(ASSIGN)
                val value = parseCoordinate()
                Operation("dec_coord", variable, value)
            }
            DEC_DOUBLE -> {
                match(DEC_DOUBLE)
                val variable = match(VAR).lexeme
                match(ASSIGN)
                val value = match(DOUBLE).lexeme.toDouble()
                Operation("dec_double", variable, value)
            }
            else -> error("Unsupported operation")
        }
    }




    //Coordinate ::= ( Expr, Expr )
    private fun parseCoordinate() : Coordinate {
        match(LPAREN)
        val x = parseExpr().value.toDouble()
        match(COMMA)
        val y = parseExpr().value.toDouble()
        match(RPAREN)
        return Coordinate(x, y)
    }



    /*
    Expr ::= int Expr' | var Expr' | double Expr'
    Expr' ::= + Expr | - Expr | ε
    */
    private fun parseExpr() : Expr {
        val valueToken = match(currentToken.symbol)
        val value = valueToken.lexeme
        return if (currentToken.symbol == PLUS || currentToken.symbol == MINUS) {
            val op = match(currentToken.symbol).lexeme
            val right = parseExpr()
            Expr(value, Pair(op, right))
        } else {
            Expr(value)
        }
    }


    /*
    IfElse ::= if ( Expr Compare Expr ) { Operations } IfElse'
    Compare ::= == | != | < | >
    IfElse' ::= else { Operations } | ε
    */
    private fun parseIfElse() : IfElse {
        match(IF)
        match(LPAREN)
        val left = parseExpr()
        val comparator = match(currentToken.symbol).lexeme
        val right = parseExpr()
        match(RPAREN)
        match(LBRACE)
        val thenOps = parseOperations()
        match(RBRACE)
        var elseOps: List<Operation>? = null
        if (currentToken.symbol == ELSE) {
            match(ELSE)
            match(LBRACE)
            elseOps = parseOperations()
            match(RBRACE)
        }
        return IfElse(left, comparator, right, thenOps,elseOps)
    }



    /*
    MandatoryElementsList ::= MandatoryElements MandatoryElements'
    MandatoryElements ::= Streets Stations Accidents
    MandatoryElements' ::= MandatoryElementsList | ε
    */

    private fun parseMandatoryElementsList() : MandatoryElements {
        val streets = mutableListOf<Street>()
        val stations = mutableListOf<Station>()
        val accidents = mutableListOf<Accident>()
        while (currentToken.symbol in setOf(STREET, STATION, ACCIDENT)) {
            when (currentToken.symbol) {
                STREET -> streets.add(parseStreet())
                STATION -> stations.add(parseStation())
                ACCIDENT -> accidents.add(parseAccident())
            }
        }
        return MandatoryElements(streets, stations, accidents)
    }




    /*
    AdditionalElementsList ::= AdditionalElements AdditionalElements'
    AdditionalElements ::= Parks FireHydrants CircleHeatmaps
    AdditionalElements' ::= AdditionalElementsList | ε
    */

    private fun parseAdditionalElementsList() : AdditionalElements? {
        val parks = mutableListOf<Park>()
        val hydrants = mutableListOf<FireHydrant>()
        val heatmaps = mutableListOf<CircleHeatmap>()
        while (currentToken.symbol in setOf(PARK, FIRE_HYDRANT, CIRCLE_HEATMAP)) {
            when (currentToken.symbol) {
                PARK -> parks.add(parsePark())
                FIRE_HYDRANT -> hydrants.add(parseFireHydrant())
                CIRCLE_HEATMAP -> heatmaps.add(parseCircleHeatmap())
            }
        }
        return if (parks.isEmpty() && hydrants.isEmpty() && heatmaps.isEmpty()) null
        else AdditionalElements(parks, hydrants, heatmaps)
    }



    /*
    Streets ::= Street Streets'
    Streets' ::= Street Streets' | ε
    Street ::= street string { Operations Bend Operations Line }
    */

    private fun parseStreet() : Street{
        match(STREET)
        val name = match(STRING).lexeme
        match(LBRACE)
        val preBendOps = parseOperations()
        val bend = parseBend()
        val postBendOps = parseOperations()
        val line = parseLine()
        match(RBRACE)
        return Street(name, preBendOps, bend, postBendOps, line)
    }

    /*
    Stations ::= Station Stations'
    Stations' ::= Station Stations' | ε
    Station ::= station string { Operations Address Operations AvailableUnits Operations StationType}
    */

    private fun parseStation() : Station{
        match(STATION)
        val name = match(STRING).lexeme
        match(LBRACE)
        val preOps = parseOperations()
        match(ADDRESS)
        match(ASSIGN)
        val address = match(currentToken.symbol).lexeme
        val midOps = parseOperations()
        match(AVAILABLE_UNITS)
        match(ASSIGN)
        val units = match(currentToken.symbol).lexeme.toInt()
        val postOps = parseOperations()
        match(STATION_TYPE)
        match(ASSIGN)
        val type = match(currentToken.symbol).lexeme
        match(RBRACE)
        return Station(name, preOps, address, midOps, units, postOps, type)
    }

    /*
    Accidents ::= Accident Accidents'
    Accidents' ::= Accident Accidents' | ε
    Accident ::= accident string {Operations Address Operations AccidentType}
    */


    private fun parseAccident():Accident{
        match(ACCIDENT)
        val name = match(STRING).lexeme
        match(LBRACE)
        val preOps = parseOperations()
        match(ADDRESS)
        match(ASSIGN)
        val address = match(currentToken.symbol).lexeme
        val postOps = parseOperations()
        match(ACCIDENT_TYPE)
        match(ASSIGN)
        val type = match(currentToken.symbol).lexeme
        match(RBRACE)
        return Accident(name, preOps, address, postOps, type)
    }

    /*
    Parks ::= Park Parks'
    Parks' ::= Park Parks' | ε
    Park ::= park string { Operations Block }
    */

    private fun parsePark(): Park{
        match(PARK)
        val name = match(STRING).lexeme
        match(LBRACE)
        val ops = parseOperations()
        val block =parseBlock()
        match(RBRACE)
        return Park(name, ops, block)
    }


    /*
    FireHydrants ::= FireHydrant FireHydrants'
    FireHydrants' ::= FireHydrant FireHydrants' | ε
    FireHydrant ::= fire_hydrant string { Operations Point }
    */

    private fun parseFireHydrant() : FireHydrant {
        match(FIRE_HYDRANT)
        val name = match(STRING).lexeme
        match(LBRACE)
        val ops = parseOperations()
        match(POINT)
        val coord = parseCoordinate()
        match(RBRACE)
        return FireHydrant(name, ops, coord)
    }

    /*
    CircleHeatmaps ::= CircleHeatmap CircleHeatmaps'
    CircleHeatmaps' ::= CircleHeatmap CircleHeatmaps' | ε
    CircleHeatmap ::= circle_heatmap string { Operations Circle }
    */

    private fun parseCircleHeatmap() : CircleHeatmap {
        match(CIRCLE_HEATMAP)
        val name = match(STRING).lexeme
        match(LBRACE)
        val ops = parseOperations()
        match(CIRCLE)
        match(LPAREN)
        val center = parseCoordinate()
        match(COMMA)
        val radius = parseExpr()
        match(RPAREN)
        match(RBRACE)
        return CircleHeatmap(name, ops, center, radius)
    }

    //Point ::= point Coordinate
    private fun parsePoint() {
        match(POINT)
        parseCoordinate()
    }

    //Block ::= block ( Coordinate , Coordinate , Coordinate , Coordinate )
    private fun parseBlock() : Block {
        match(BLOCK)
        match(LPAREN)
        val corners = listOf(
            parseCoordinate(),
            { match(COMMA); parseCoordinate() }(),
            { match(COMMA); parseCoordinate() }(),
            { match(COMMA); parseCoordinate() }()
        )
        match(RPAREN)
        return Block(corners)
    }

    //Bend ::= bend ( Coordinate , Coordinate, Angle )
    private fun parseBend() : Bend {
        match(BEND)
        match(LPAREN)
        val start = parseCoordinate()
        match(COMMA)
        val end = parseCoordinate()
        match(COMMA)
        val angle = parseExpr()
        match(RPAREN)
        return Bend(start, end, angle)
    }

    //Line ::= line ( Coordinate , Coordinate )
    private fun parseLine() : Line {
        match(LINE)
        match(LPAREN)
        val start = parseCoordinate()
        match(COMMA)
        val end = parseCoordinate()
        match(RPAREN)
        return Line(start, end)
    }

}


fun generateCircleCoordinates(center: Coordinate, radius: Double, numPoints: Int = 50): List<String> {
    return (0..numPoints).map { i ->
        val angle = 2 * PI * i / numPoints
        val lat = center.x + radius * sin(angle)
        val lon = center.y + radius * cos(angle)
        "[%.6f,%.6f]".format(lat, lon)
    }
}

fun generateBendCoordinates(start: Coordinate, end: Coordinate, angleDegrees: Double, segments: Int = 100): List<String> {
    val coords = mutableListOf<String>()
    coords.add("[${start.x},${start.y}]")

    val dx = end.x - start.x
    val dy = end.y - start.y
    val length = sqrt(dx * dx + dy * dy)
    val angleRad = Math.toRadians(angleDegrees)

    for (i in 1..segments) {
        val t = i.toDouble() / (segments + 1)
        val x = start.x + dx * t
        val y = start.y + dy * t

        val nx = -dy / length
        val ny = dx / length
        val offset = sin(t * Math.PI) * angleRad * 0.1 * length

        coords.add("[${x + nx * offset},${y + ny * offset}]")
    }

    coords.add("[${end.x},${end.y}]")
    return coords
}


fun main() {
    val inputStream = FileInputStream("C:\\Users\\user\\IdeaProjects\\prevajanjeJezik\\src\\input2.txt")
    val scanner = Scanner(Lexer, inputStream)
    val parser = Parser(scanner)

    try {
        val country = parser.parseCountry()
        println("✅ AST parsed successfully.")

        val features = mutableListOf<String>()

        for (region in country.regions) {
            val coords = region.block.corners.map { "[${it.x},${it.y}]" }.toMutableList()
            if (coords.first() != coords.last()) coords += coords.first()
            features += """
                {
                  "type": "Feature",
                  "geometry": {
                    "type": "Polygon",
                    "coordinates": [ [${coords.joinToString(",")}] ]
                  },
                  "properties": { "name": "${region.name.trim('"')}", "type": "region" }
                }
            """.trimIndent()

            for (city in region.cities) {
                val allOps = mutableListOf<Operation>()
                allOps += city.operations
                city.mandatory.streets.forEach { allOps += it.preBendOps + it.postBendOps }
                city.mandatory.stations.forEach { allOps += it.preAddressOps + it.midOps + it.postOps }
                city.mandatory.accidents.forEach { allOps += it.preAddressOps + it.postOps }
                city.additional?.parks?.forEach { allOps += it.operations }
                city.additional?.hydrants?.forEach { allOps += it.operations }
                city.additional?.heatmaps?.forEach { allOps += it.operations }
                val context = buildEvaluationContext(allOps)

                for (street in city.mandatory.streets) {
                    val angle = street.bend.angle.evaluate(context)
                    val bendCoords = generateBendCoordinates(street.bend.start, street.bend.end, angle)
                    features += """
                        {
                          "type": "Feature",
                          "geometry": {
                            "type": "LineString",
                            "coordinates": [ ${bendCoords.joinToString(",")} ]
                          },
                          "properties": { "name": "${street.name.trim('"')}_bend", "type": "bend", "angle": $angle }
                        }
                    """.trimIndent()

                    val lineCoords = listOf(
                        "[${street.line.start.x},${street.line.start.y}]",
                        "[${street.line.end.x},${street.line.end.y}]"
                    )
                    features += """
                        {
                          "type": "Feature",
                          "geometry": {
                            "type": "LineString",
                            "coordinates": [ ${lineCoords.joinToString(",")} ]
                          },
                          "properties": { "name": "${street.name.trim('"')}_line", "type": "line" }
                        }
                    """.trimIndent()
                }

                city.mandatory.stations.forEach { s ->
                    val stationContext = buildEvaluationContext(s.preAddressOps + s.midOps + s.postOps)
                    val address = resolveStringValue(s.address, stationContext)
                    val units = resolveIntValue(s.units.toString(), stationContext)
                    val type = resolveStringValue(s.type, stationContext)

                    features += """
                        {
                          "type": "Feature",
                          "geometry": null,
                          "properties": {
                            "name": "${s.name.trim('"')}",
                            "type": "station",
                            "address": "${address.trim('"')}",
                            "available_units": ${units},
                            "station_type": "${type.trim('"')}"
                          }
                        }
                    """.trimIndent()
                }

                city.mandatory.accidents.forEach { a ->
                    val accidentContext = buildEvaluationContext(a.preAddressOps + a.postOps)
                    val address = resolveStringValue(a.address, accidentContext)
                    val type = resolveStringValue(a.type, accidentContext)

                    features += """
                        {
                          "type": "Feature",
                          "geometry": null,
                          "properties": {
                            "name": "${a.name.trim('"')}",
                            "type": "accident",
                            "address": "${address.trim('"')}",
                            "accident_type": "${type.trim('"')}"
                          }
                        }
                    """.trimIndent()
                }

                city.additional?.parks?.forEach { park ->
                    val parkCoords = park.block.corners.map { "[${it.x},${it.y}]" }.toMutableList()
                    if (parkCoords.first() != parkCoords.last()) parkCoords += parkCoords.first()
                    features += """
                        {
                          "type": "Feature",
                          "geometry": {
                            "type": "Polygon",
                            "coordinates": [ [${parkCoords.joinToString(",")}] ]
                          },
                          "properties": { "name": "${park.name.trim('"')}", "type": "park" }
                        }
                    """.trimIndent()
                }

                city.additional?.hydrants?.forEach { hydrant ->
                    val p = hydrant.point
                    features += """
                        {
                          "type": "Feature",
                          "geometry": {
                            "type": "Point",
                            "coordinates": [${p.x},${p.y}]
                          },
                          "properties": { "name": "${hydrant.name.trim('"')}", "type": "fire_hydrant" }
                        }
                    """.trimIndent()
                }

                city.additional?.heatmaps?.forEach { heatmap ->
                    val center = heatmap.center
                    val radius = heatmap.radius.evaluate(context)
                    val circleCoords = generateCircleCoordinates(center, radius)
                    features += """
                        {
                          "type": "Feature",
                          "geometry": {
                            "type": "Polygon",
                            "coordinates": [ [${circleCoords.joinToString(",")}] ]
                          },
                          "properties": { "name": "${heatmap.name.trim('"')}", "type": "circle_heatmap", "radius": $radius }
                        }
                    """.trimIndent()
                }
            }
        }

        val geoJson = """
            {
              "type": "FeatureCollection",
              "features": [
                ${features.joinToString(",\n")}
              ]
            }
        """.trimIndent()

        File("output.geojson").writeText(geoJson)
        println("✅ GeoJSON written to output.geojson")

    } catch (e: Exception) {
        println("❌ Error while parsing or exporting: ${e.message}")
    }
}

/*
fun main(args: Array<String>) {
    val inputstream = FileInputStream("input1.txt")
    printTokens(Scanner(Lexer,inputstream))

}/*