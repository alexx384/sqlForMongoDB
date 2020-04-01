import java.text.CharacterIterator;
import java.util.Stack;

// TODO: Implement constraint - "Database names must have fewer than 64 characters".

/**
 * LL grammar in BNF form
 *
 * 0.  SELECT_QUERY -> "SELECT" "FROM" WHERE_CLAUSE
 * 1.  WHERE_CLAUSE -> SKIP_CLAUSE
 * 2.  WHERE_CLAUSE -> "WHERE" WHERE_EXPR
 * 3.  WHERE_EXPR -> "AND" WHERE_EXPR
 * 4.  WHERE_EXPR -> SKIP_CLAUSE
 * 5.  SKIP_CLAUSE -> "SKIP" LIMIT_CLAUSE
 * 6.  SKIP_CLAUSE -> "OFFSET" LIMIT_CLAUSE
 * 7.  SKIP_CLAUSE -> LIMIT_CLAUSE
 * 8.  LIMIT_CLAUSE -> "LIMIT" $
 * 9.  LIMIT_CLAUSE -> $
 */

public class Translator {
    /**
     * Check for bad naming symbol according to MongoDB documentation
     * https://docs.mongodb.com/manual/reference/limits/#naming-restrictions
     *
     * @return true is the symbol can present in the name
     */
    private static boolean isBadNameSymbol(char value) {
        switch (value) {
            case '/':
            case '\\':
            case '.':
            case ' ':
            case '"':
            case '$':
            case '*':
            case '<':
            case '>':
            case ':':
            case '|':
            case '?':{
                return true;
            }
            default:{
                return false;
            }
        }
    }

    public static CharsMapping parseNameOrValue(CharacterIterator iterator) {
        boolean isNumber = true;
        char value = iterator.current();
        while (value == ' ') {
            value = iterator.next();
        }

        int startIdx = iterator.getIndex();
        while (value != ' ' && value != ',' && value != CharacterIterator.DONE) {
            if (value < '0' || '9' < value) {
                isNumber = false;
                if (isBadNameSymbol(value)) {
                    throw new IllegalArgumentException("is not a valid symbol '" + iterator.current()
                            + "' at position " + iterator.getIndex());
                }
            }

            value = iterator.next();
        }
        return new CharsMapping(startIdx, iterator.getIndex(), isNumber);
    }

    public static void parseSelectExpression(CharacterIterator iterator, MongoShellBuilder mongoShellBuilder) {
        boolean isParsingNotDone = true;

        char value = iterator.next();
        while (value == ' ') {
            value = iterator.next();
        }
        if (value == '*') {
            if (iterator.next() == ' ') {
                mongoShellBuilder.setSelectAll();
                return;
            } else {
                throw new IllegalArgumentException("is not a valid symbol '" + iterator.current()
                        + "' at position " + iterator.getIndex());
            }
        }
        while (isParsingNotDone) {
            CharsMapping charsMapping = parseNameOrValue(iterator);
            mongoShellBuilder.addSelectField(charsMapping);
            value = iterator.current();
            while (value == ' ') {
                value = iterator.next();
            }
            if (value != ',') {
                isParsingNotDone = false;
            }
            iterator.setIndex(iterator.getIndex() + 1);
        }
        iterator.setIndex(iterator.getIndex() - 1);
    }

    public static void parseWhereExpression(CharacterIterator iterator, MongoShellBuilder mongoShellBuilder) {
        CharsMapping firstMapping = parseNameOrValue(iterator);
        char value = iterator.next();
        while (value == ' ') {
            value = iterator.next();
        }
        MongoShellBuilder.WhereExpression.CompareSign sign;
        switch (value) {
            case '<': {
                value = iterator.next();
                if (value == '>') {
                    sign = MongoShellBuilder.WhereExpression.CompareSign.NON_EQUALS;
                } else {
                    if (firstMapping.isNumber) {
                        sign = MongoShellBuilder.WhereExpression.CompareSign.GREATER;
                    } else {
                        sign = MongoShellBuilder.WhereExpression.CompareSign.LOWER;
                    }
                }
            } break;
            case '=': {
                sign = MongoShellBuilder.WhereExpression.CompareSign.EQUALS;
            } break;
            case '>': {
                if (firstMapping.isNumber) {
                    sign = MongoShellBuilder.WhereExpression.CompareSign.LOWER;
                } else {
                    sign = MongoShellBuilder.WhereExpression.CompareSign.GREATER;
                }
            } break;
            default: {
                throw new IllegalArgumentException("is not a valid symbol '" + iterator.current()
                        + "' at position " + iterator.getIndex());
            }
        }
        if (iterator.next() != ' ') {
            throw new IllegalArgumentException("is not a valid symbol '" + iterator.current()
                    + "' at position " + iterator.getIndex());
        }
        CharsMapping secondMapping = parseNameOrValue(iterator);
        if (firstMapping.isNumber) {
            if (!secondMapping.isNumber) {
                mongoShellBuilder.addWhereExpression(secondMapping, sign, firstMapping);
            } else {
                throw new IllegalArgumentException("in Where statement must be one number and one variable");
            }
        } else {
            if (secondMapping.isNumber) {
                mongoShellBuilder.addWhereExpression(firstMapping, sign, secondMapping);
            } else {
                throw new IllegalArgumentException("in Where statement must be one number and one variable");
            }
        }
    }

    public static String translate(String sqlQuery) {
        char[] charSqlQuery = sqlQuery.toCharArray();
        CharacterIterator inputIterator = new CharArrayIterator(charSqlQuery);
        MongoShellBuilder mongoShellBuilder = new MongoShellBuilder(charSqlQuery);

        final Stack<Terminal> expectedSymbolStack = new Stack<>();
        final Stack<Terminal> actualSymbolStack = new Stack<>();
        expectedSymbolStack.push(Terminal.NTS_SELECT_QUERY);
        actualSymbolStack.push(Terminal.getTerminal(inputIterator));

        while (!expectedSymbolStack.isEmpty()) {
            if (expectedSymbolStack.peek() == actualSymbolStack.peek()) {

                actualSymbolStack.pop();
                switch (expectedSymbolStack.pop()) {
                    case TS_SELECT: {
                        parseSelectExpression(inputIterator, mongoShellBuilder);
                    } break;
                    case TS_FROM: {
                        mongoShellBuilder.setFromDatabaseName(parseNameOrValue(inputIterator));
                    } break;
                    case TS_WHERE:
                    case TS_AND: {
                        parseWhereExpression(inputIterator, mongoShellBuilder);
                    } break;
                    case TS_SKIP: {
                        mongoShellBuilder.setSkipValue(parseNameOrValue(inputIterator));
                    } break;
                    case TS_LIMIT: {
                        mongoShellBuilder.setLimitValue(parseNameOrValue(inputIterator));
                    } break;
                }

                if (!expectedSymbolStack.isEmpty()) {
                    actualSymbolStack.push(Terminal.getTerminal(inputIterator));
                }
            } else {
                switch (ParseTable.getCase(expectedSymbolStack.pop(), actualSymbolStack.peek())) {
                    case 0: {       // 0.  SELECT_QUERY -> "SELECT" "FROM" WHERE_CLAUSE
                        expectedSymbolStack.push(Terminal.NTS_WHERE_CLAUSE);
                        expectedSymbolStack.push(Terminal.TS_FROM);
                        expectedSymbolStack.push(Terminal.TS_SELECT);
                        System.out.println("0.  SELECT_QUERY -> \"SELECT\" \"FROM\" WHERE_CLAUSE");
                    } break;
                    case 1: {       // 1.  WHERE_CLAUSE -> SKIP_CLAUSE
                        expectedSymbolStack.push(Terminal.NTS_SKIP_CLAUSE);
                        System.out.println("1.  WHERE_CLAUSE -> SKIP_CLAUSE");
                    } break;
                    case 2: {       // 2.  WHERE_CLAUSE -> "WHERE" WHERE_EXPR
                        expectedSymbolStack.push(Terminal.NTS_WHERE_EXPR);
                        expectedSymbolStack.push(Terminal.TS_WHERE);
                        System.out.println("2.  WHERE_CLAUSE -> \"WHERE\" WHERE_EXPR");
                    } break;
                    case 3: {       // 3.  WHERE_EXPR -> "AND" WHERE_EXPR
                        expectedSymbolStack.push(Terminal.NTS_WHERE_EXPR);
                        expectedSymbolStack.push(Terminal.TS_AND);
                        System.out.println("3.  WHERE_EXPR -> \"AND\" WHERE_EXPR");
                    } break;
                    case 4: {       // 4.  WHERE_EXPR -> SKIP_CLAUSE
                        expectedSymbolStack.push(Terminal.NTS_SKIP_CLAUSE);
                        System.out.println("4.  WHERE_EXPR -> SKIP_CLAUSE");
                    } break;
                    case 5: {       // 5.  SKIP_CLAUSE -> "SKIP" LIMIT_CLAUSE
                        expectedSymbolStack.push(Terminal.NTS_LIMIT_CLAUSE);
                        expectedSymbolStack.push(Terminal.TS_SKIP);
                        System.out.println("5.  SKIP_CLAUSE -> \"SKIP\" LIMIT_CLAUSE");
                    } break;
                    case 6: {       // 6.  SKIP_CLAUSE -> LIMIT_CLAUSE
                        expectedSymbolStack.push(Terminal.NTS_LIMIT_CLAUSE);
                        System.out.println("6.  SKIP_CLAUSE -> LIMIT_CLAUSE");
                    } break;
                    case 7: {       // 7.  LIMIT_CLAUSE -> "LIMIT" $
                        expectedSymbolStack.push(Terminal.TS_END);
                        expectedSymbolStack.push(Terminal.TS_LIMIT);
                        System.out.println("7.  LIMIT_CLAUSE -> \"LIMIT\" $");
                    } break;
                    case 8: {       // 8.  LIMIT_CLAUSE -> $
                        expectedSymbolStack.push(Terminal.TS_END);
                        System.out.println("8.  LIMIT_CLAUSE -> $");
                    } break;
                    default: {
                        throw new IllegalArgumentException("logic of your SQL query is not correct");
                    }
                }
            }
        }

        return mongoShellBuilder.build();
    }

    public static void main(String[] args) {
        System.out.println(
                Translator.translate("SELECT * FROM customers WHERE age > 22")
//                Translator.translate("SELECT * FROM collection OFFSET 5 LIMIT 10")
//                Translator.translate("SELECT name, surname FROM collection")
//                Translator.translate("SELECT * FROM sales LIMIT 10")
        );
    }
}
