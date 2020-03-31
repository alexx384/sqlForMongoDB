public class ParseTable {
    private final static byte UNK = Byte.MIN_VALUE;

    private final static byte[][] table = {
/* SELECT_QUERY  */ {0, UNK, UNK, UNK, UNK, UNK, UNK, UNK, UNK, UNK, UNK, UNK, UNK},
/* SELECT_CLAUSE */ {UNK, 1, UNK, UNK, 3, UNK, UNK, UNK, UNK, UNK, UNK, UNK, UNK},
/* SELECT_EXPR   */ {UNK, UNK, 4, 2, UNK, UNK, UNK, UNK, UNK, UNK, UNK, UNK, UNK},
/* FROM_CLAUSE   */ {UNK, 5, UNK, UNK, UNK, UNK, UNK, UNK, UNK, UNK, UNK, UNK, UNK},
/* FROM_EXPR     */ {UNK, UNK, UNK, 6, UNK, 7, UNK, 7, 7, 7, UNK, 7, UNK},
/* WHERE_OPTION  */ {UNK, UNK, UNK, UNK, UNK, 8, UNK, 11, 11, 11, UNK, 11, UNK},
/* WHERE_CLAUSE  */ {UNK, 9, UNK, UNK, UNK, UNK, 10, UNK, UNK, UNK, UNK, UNK, UNK},
/* SKIP_OPTION   */ {UNK, UNK, UNK, UNK, UNK, UNK, UNK, 12, 13, 14, UNK,14, UNK},
/* LIMIT_OPTION  */ {UNK, UNK, UNK, UNK, UNK, UNK, UNK, UNK, UNK, 15, UNK, 16, UNK},
/* WHERE_EXPR    */ {UNK, UNK, UNK, UNK, UNK, UNK, UNK, 18, 18, 18, 17, 18, UNK},
    };

    public static int getCase(Terminal nonTerminalSymbol, Terminal terminalSymbol) {
        byte tableRow = (byte) nonTerminalSymbol.value;
        byte tableColumn = (byte) terminalSymbol.value;

        return ParseTable.table[tableRow][tableColumn];
    }
}
