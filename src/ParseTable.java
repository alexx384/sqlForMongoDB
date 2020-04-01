public class ParseTable {
    private final static byte UNK = Byte.MIN_VALUE;

    private final static byte[][] table = {
/* SELECT_QUERY */ {0, UNK, UNK, UNK, UNK, UNK, UNK},
/* WHERE_CLAUSE */ {UNK, UNK, 2, 1, 1, UNK, 1},
/* WHERE_EXPR   */ {UNK, UNK, UNK, 4, 4, 3, 4},
/* SKIP_CLAUSE  */ {UNK, UNK, UNK, 5, 6, UNK, 6},
/* LIMIT_CLAUSE */ {UNK, UNK, UNK, UNK, 7, UNK, 8}
    };

    public static int getCase(Terminal nonTerminalSymbol, Terminal terminalSymbol) {
        byte tableRow = (byte) nonTerminalSymbol.value;
        byte tableColumn = (byte) terminalSymbol.value;

        return ParseTable.table[tableRow][tableColumn];
    }
}
