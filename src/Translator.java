import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Stack;

/**
 * Grammatics in BNF
 *
 * 0.  SELECT_QUERY -> "SELECT" SELECT_CLAUSE
 * 1.  SELECT_CLAUSE -> \w SELECT_EXPR
 * 2.  SELECT_EXPR -> "," SELECT_CLAUSE
 * 3.  SELECT_CLAUSE -> "*" "FROM" FROM_CLAUSE
 * 4.  SELECT_EXPR -> "FROM" FROM_CLAUSE
 * 5.  FROM_CLAUSE -> \w FROM_EXPR
 * 6.  FROM_EXPR -> ',' FROM_CLAUSE
 * 7.  FROM_EXPR -> WHERE_OPTION
 * 8.  WHERE_OPTION -> "WHERE" WHERE_CLAUSE
 * 9.  WHERE_CLAUSE -> \w ['=''<''>''<>'] \d WHERE_EXPR
 * 10. WHERE_CLAUSE -> \d ['=''<''>''<>'] \w WHERE_EXPR
 * 11. WHERE_OPTION -> SKIP_OPTION
 * 12. SKIP_OPTION -> "SKIP" \d LIMIT_OPTION
 * 13. SKIP_OPTION -> "OFFSET" \d LIMIT_OPTION
 * 14. SKIP_OPTION -> LIMIT_OPTION
 * 15. LIMIT_OPTION -> "LIMIT" \d $
 * 16. LIMIT_OPTION -> $
 * 17. WHERE_EXPR -> "AND" WHERE_EXPR
 * 18. WHERE_EXPR -> SKIP_OPTION
 */

public class Translator {
    public static void translate(String sqlQuery) {
        CharacterIterator inputIterator = new StringCharacterIterator(sqlQuery);

        final Stack<Terminal> expectedSymbolStack = new Stack<>();
        final Stack<Terminal> actualSymbolStack = new Stack<>();
        expectedSymbolStack.push(Terminal.NTS_SELECT_QUERY);
        actualSymbolStack.push(Terminal.toTerminal(inputIterator));

        while (!expectedSymbolStack.isEmpty()) {
            if (expectedSymbolStack.peek() == actualSymbolStack.peek()) {
                System.out.println(actualSymbolStack.pop().toString());
                expectedSymbolStack.pop();
                if (!expectedSymbolStack.isEmpty()) {
                    actualSymbolStack.push(Terminal.toTerminal(inputIterator));
                }
            } else {
                switch (ParseTable.getCase(expectedSymbolStack.pop(), actualSymbolStack.peek())) {
                    case 0: {       // 0.  SELECT_QUERY -> "SELECT" SELECT_CLAUSE
                        expectedSymbolStack.push(Terminal.NTS_SELECT_CLAUSE);
                        expectedSymbolStack.push(Terminal.TS_SELECT);
                        System.out.println("0.  SELECT_QUERY -> \"SELECT\" SELECT_CLAUSE");
                    } break;
                    case 1: {       // 1.  SELECT_CLAUSE -> \w SELECT_EXPR
                        expectedSymbolStack.push(Terminal.NTS_SELECT_EXPR);
                        expectedSymbolStack.push(Terminal.TS_NAME);
                        System.out.println("1.  SELECT_CLAUSE -> \\w SELECT_EXPR");
                    } break;
                    case 2: {       // 2.  SELECT_EXPR -> "," SELECT_CLAUSE
                        expectedSymbolStack.push(Terminal.NTS_SELECT_CLAUSE);
                        expectedSymbolStack.push(Terminal.TS_COMMA);
                        System.out.println("2.  SELECT_EXPR -> \",\" SELECT_CLAUSE");
                    } break;
                    case 3: {       // 3.  SELECT_CLAUSE -> "*" "FROM" FROM_CLAUSE
                        expectedSymbolStack.push(Terminal.NTS_FROM_CLAUSE);
                        expectedSymbolStack.push(Terminal.TS_FROM);
                        expectedSymbolStack.push(Terminal.TS_ASTERISK);
                        System.out.println("3.  SELECT_CLAUSE -> \"*\" \"FROM\" FROM_CLAUSE");
                    } break;
                    case 4: {       // 4.  SELECT_EXPR -> "FROM" FROM_CLAUSE
                        expectedSymbolStack.push(Terminal.NTS_FROM_CLAUSE);
                        expectedSymbolStack.push(Terminal.TS_FROM);
                        System.out.println("4.  SELECT_EXPR -> \"FROM\" FROM_CLAUSE");
                    } break;
                    case 5: {       // 5.  FROM_CLAUSE -> \w FROM_EXPR
                        expectedSymbolStack.push(Terminal.NTS_FROM_EXPR);
                        expectedSymbolStack.push(Terminal.TS_NAME);
                        System.out.println("5.  FROM_CLAUSE -> \\w FROM_EXPR");
                    } break;
                    case 6: {       // 6.  FROM_EXPR -> ',' FROM_CLAUSE
                        expectedSymbolStack.push(Terminal.NTS_FROM_CLAUSE);
                        expectedSymbolStack.push(Terminal.TS_COMMA);
                        System.out.println("6.  FROM_EXPR -> ',' FROM_CLAUSE");
                    } break;
                    case 7: {       // 7.  FROM_EXPR -> WHERE_OPTION
                        expectedSymbolStack.push(Terminal.NTS_WHERE_OPTION);
                        System.out.println("7.  FROM_EXPR -> WHERE_OPTION");
                    } break;
                    case 8: {       // 8.  WHERE_OPTION -> "WHERE" WHERE_CLAUSE
                        expectedSymbolStack.push(Terminal.NTS_WHERE_CLAUSE);
                        expectedSymbolStack.push(Terminal.TS_WHERE);
                        System.out.println("8.  WHERE_OPTION -> \"WHERE\" WHERE_CLAUSE");
                    } break;
                    case 9: {       // 9.  WHERE_CLAUSE -> \w ['=''<''>''<>'] \d WHERE_EXPR
                        expectedSymbolStack.push(Terminal.NTS_WHERE_EXPR);
                        expectedSymbolStack.push(Terminal.TS_NUMBER);
                        expectedSymbolStack.push(Terminal.TS_COMPARATOR);
                        expectedSymbolStack.push(Terminal.TS_NAME);
                        System.out.println("9.  WHERE_CLAUSE -> \\w ['=''<''>''<>'] \\d WHERE_EXPR");
                    } break;
                    case 10: {      // 10. WHERE_CLAUSE -> \d ['=''<''>''<>'] \w WHERE_EXPR
                        expectedSymbolStack.push(Terminal.NTS_WHERE_EXPR);
                        expectedSymbolStack.push(Terminal.TS_NAME);
                        expectedSymbolStack.push(Terminal.TS_COMPARATOR);
                        expectedSymbolStack.push(Terminal.TS_NUMBER);
                        System.out.println("10. WHERE_CLAUSE -> \\d ['=''<''>''<>'] \\w WHERE_EXPR");
                    } break;
                    case 11: {      // 11. WHERE_OPTION -> SKIP_OPTION
                        expectedSymbolStack.push(Terminal.NTS_SKIP_OPTION);
                        System.out.println("11. WHERE_OPTION -> SKIP_OPTION");
                    } break;
                    case 12: {      // 12. SKIP_OPTION -> "SKIP" \d LIMIT_OPTION
                        expectedSymbolStack.push(Terminal.NTS_LIMIT_OPTION);
                        expectedSymbolStack.push(Terminal.TS_NUMBER);
                        expectedSymbolStack.push(Terminal.TS_SKIP);
                        System.out.println("12. SKIP_OPTION -> \"SKIP\" \\d LIMIT_OPTION");
                    } break;
                    case 13: {      // 13. SKIP_OPTION -> "OFFSET" \d LIMIT_OPTION
                        expectedSymbolStack.push(Terminal.NTS_LIMIT_OPTION);
                        expectedSymbolStack.push(Terminal.TS_NUMBER);
                        expectedSymbolStack.push(Terminal.TS_OFFSET);
                        System.out.println("13. SKIP_OPTION -> \"OFFSET\" \\d LIMIT_OPTION");
                    } break;
                    case 14: {      // 14. SKIP_OPTION -> LIMIT_OPTION
                        expectedSymbolStack.push(Terminal.NTS_LIMIT_OPTION);
                        System.out.println("14. SKIP_OPTION -> LIMIT_OPTION");
                    } break;
                    case 15: {      // 15. LIMIT_OPTION -> "LIMIT" \d $
                        expectedSymbolStack.push(Terminal.TS_NUMBER);
                        expectedSymbolStack.push(Terminal.TS_LIMIT);
                        System.out.println("15. LIMIT_OPTION -> \"LIMIT\" \\d $");
                    } break;
                    case 16: {      // 16. LIMIT_OPTION -> $
                        System.out.println("16. LIMIT_OPTION -> $");
                    } break;
                    case 17: {      // 17. WHERE_EXPR -> "AND" WHERE_EXPR
                        expectedSymbolStack.push(Terminal.NTS_WHERE_EXPR);
                        expectedSymbolStack.push(Terminal.TS_AND);
                        System.out.println("17. WHERE_EXPR -> \"AND\" WHERE_EXPR");
                    } break;
                    case 18: {      // 18. WHERE_EXPR -> SKIP_OPTION
                        expectedSymbolStack.push(Terminal.NTS_SKIP_OPTION);
                        System.out.println("18. WHERE_EXPR -> SKIP_OPTION");
                    } break;
                    default: {
                        throw new IllegalArgumentException("logic of your SQL query is not correct");
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        Translator.translate("SELECT * FROM sales LIMIT 10");
    }
}
