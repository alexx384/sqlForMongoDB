import java.text.CharacterIterator;

public enum Terminal {
    /* Terminal symbols */
    TS_SELECT(0, true),
    TS_NAME(1, true),
    TS_FROM(2, true),
    TS_COMMA(3, true),
    TS_ASTERISK(4, true),
    TS_WHERE(5, true),
    TS_NUMBER(6, true),
    TS_SKIP(7, true),
    TS_OFFSET(8, true),
    TS_LIMIT(9, true),
    TS_AND(10, true),
    TS_COMPARATOR(11, true),
//    TS_END(11),

    /* Non-terminal symbols */
    NTS_SELECT_QUERY(0, false),
    NTS_SELECT_CLAUSE(1, false),
    NTS_SELECT_EXPR(2, false),
    NTS_FROM_CLAUSE(3, false),
    NTS_FROM_EXPR(4, false),
    NTS_WHERE_OPTION(5, false),
    NTS_WHERE_CLAUSE(6, false),
    NTS_SKIP_OPTION(7, false),
    NTS_LIMIT_OPTION(8, false),
    NTS_WHERE_EXPR(9, false);

    public final int value;
    public final boolean terminal;

    Terminal(int value, boolean terminal) {
        this.value = value;
        this.terminal = terminal;
    }
    /**
     * Check for bad naming symbol according to MongoDB documentation
     * https://docs.mongodb.com/manual/reference/limits/#naming-restrictions
     *
     * @return true is the symbol can present in the name
     */
    private static boolean isBadSymbol(char value) {
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

    public static Terminal toTerminal(CharacterIterator iterator) {
        // TODO: Implement constraint - "Database names must have fewer than 64 characters".

        char value = iterator.current();
        while (value == ' ') {
            value = iterator.next();
        }

//        if (isBadSymbol(value)) {
//            throw new IllegalArgumentException("is not a valid symbol " + value
//                    + " at position " + iterator.getIndex());
//        }

        Terminal result = TS_NAME;
        switch (value) {
            case '*': {
                result = TS_ASTERISK;
            } break;
            case ',': {
                result = TS_COMMA;
            } break;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9': {     // NUMBER
                do {
                    value = iterator.next();
                }while ('0' <= value && value <= '9');
                iterator.setIndex(iterator.getIndex() - 1);
                result = TS_NUMBER;
            } break;
            case '<': {
                if (iterator.next() != '>') {
                    iterator.setIndex(iterator.getIndex() - 1);
                }
                result = TS_COMPARATOR;
            } break;
            case 'A': {
                if (iterator.next() == 'N' && iterator.next() == 'D') {
                    result = TS_AND;
                }
            } break;
            case 'F': {     // FROM
                if (iterator.next() == 'R' && iterator.next() == 'O' && iterator.next() == 'M') {
                    result = TS_FROM;
                }
            } break;
            case 'L': {
                if (iterator.next() == 'I' && iterator.next() == 'M' && iterator.next() == 'I'
                        && iterator.next() == 'T') {
                    result = TS_LIMIT;
                }
            } break;
            case 'O': {     // OFFSET maybe unused
                if (iterator.next() == 'F' && iterator.next() == 'F' && iterator.next() == 'S'
                        && iterator.next() == 'E' && iterator.next() == 'T') {
                    result = TS_OFFSET;
                }
            } break;
            case 'S': {     // SELECT, SKIP
                value = iterator.next();
                if (value == 'E' && iterator.next() == 'L' && iterator.next() == 'E'
                        && iterator.next() == 'C' && iterator.next() == 'T') {
                    result = TS_SELECT;
                } else if (value == 'K' && iterator.next() == 'I' && iterator.next() == 'P') {
                    result = TS_SKIP;
                }
            } break;
            case 'W': {
                if (iterator.next() == 'H' && iterator.next() == 'E' && iterator.next() == 'R'
                        && iterator.next() == 'E') {
                    result = TS_WHERE;
                }
            } break;
            case CharacterIterator.DONE:    throw new IllegalStateException("Reached end of string to parsing");
        }

        if (result != TS_NAME) {
            value = iterator.next();
            if (value == ' ' || value == CharacterIterator.DONE) {
                return result;
            }
        }

        if (!isBadSymbol(iterator.current())) {
            do {
                value = iterator.next();

//                if (isBadSymbol(value)) {
//                    throw new IllegalArgumentException("is not a valid symbol " + value
//                            + " at position " + iterator.getIndex());
//                }
            } while (value != ' ' && value != CharacterIterator.DONE);
            return TS_NAME;
        } else {
            throw new IllegalArgumentException("is not a valid symbol " + value
                    + " at position " + iterator.getIndex());
        }
    }
}
