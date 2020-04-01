import java.text.CharacterIterator;

public enum Terminal {
    /* Terminal symbols */
    TS_SELECT(0, true),
    TS_FROM(1, true),
    TS_WHERE(2, true),
    TS_SKIP(3, true),
    TS_LIMIT(4, true),
    TS_AND(5, true),
    TS_END(6, true),
    TS_UNKNOWN(-1, true),

    /* Non-terminal symbols */
    NTS_SELECT_QUERY(0, false),
    NTS_WHERE_CLAUSE(1, false),
    NTS_WHERE_EXPR(2, false),
    NTS_SKIP_CLAUSE(3, false),
    NTS_LIMIT_CLAUSE(4, false);

    public final int value;
    public final boolean isTerminal;

    Terminal(int value, boolean isTerminal) {
        this.value = value;
        this.isTerminal = isTerminal;
    }

    public static Terminal getTerminal(CharacterIterator iterator) {
        char value = iterator.current();
        while (value == ' ') {
            value = iterator.next();
        }

        Terminal result = Terminal.TS_UNKNOWN;
        switch (value) {
            case 'A': {     // AND
                if (iterator.next() == 'N' && iterator.next() == 'D') {
                    result = TS_AND;
                }
            } break;
            case 'F': {     // FROM
                if (iterator.next() == 'R' && iterator.next() == 'O' && iterator.next() == 'M') {
                    result = TS_FROM;
                }
            } break;
            case 'L': {     // LIMIT
                if (iterator.next() == 'I' && iterator.next() == 'M' && iterator.next() == 'I'
                        && iterator.next() == 'T') {
                    result = TS_LIMIT;
                }
            } break;
            case 'O': {     // OFFSET
                if (iterator.next() == 'F' && iterator.next() == 'F' && iterator.next() == 'S'
                        && iterator.next() == 'E' && iterator.next() == 'T') {
                    result = TS_SKIP;
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
            case 'W': {     // WHERE
                if (iterator.next() == 'H' && iterator.next() == 'E' && iterator.next() == 'R'
                        && iterator.next() == 'E') {
                    result = TS_WHERE;
                }
            } break;
            case CharacterIterator.DONE:
                return TS_END;
            default:
                throw new IllegalArgumentException("Can't parse '" + value + "' at position " + iterator.getIndex());
        }

        if (result == Terminal.TS_UNKNOWN) {
            throw new IllegalArgumentException("Can't parse '" + value + "' at position " + iterator.getIndex());
        }

        value = iterator.next();
        if (value == ' ' || value == CharacterIterator.DONE) {
            return result;
        } else {
            throw new IllegalArgumentException("Can't parse '" + value + "' at position " + iterator.getIndex());
        }
    }
}
