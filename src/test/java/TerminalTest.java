import org.junit.jupiter.api.Test;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import static org.junit.jupiter.api.Assertions.*;

class TerminalTest {
    @Test
    void selectWithWhitespaces() {
        CharacterIterator iterator = new StringCharacterIterator("   SELECT");
        assertEquals(Terminal.TS_SELECT, Terminal.getTerminal(iterator));
    }

    @Test
    void fromWithWhitespaces() {
        CharacterIterator iterator = new StringCharacterIterator("   FROM");
        assertEquals(Terminal.TS_FROM, Terminal.getTerminal(iterator));
    }

    @Test
    void whereWithWhitespaces() {
        CharacterIterator iterator = new StringCharacterIterator("   WHERE");
        assertEquals(Terminal.TS_WHERE, Terminal.getTerminal(iterator));
    }

    @Test
    void andWithWhitespaces() {
        CharacterIterator iterator = new StringCharacterIterator("   AND");
        assertEquals(Terminal.TS_AND, Terminal.getTerminal(iterator));
    }

    @Test
    void skipWithWhitespaces() {
        CharacterIterator skipIterator = new StringCharacterIterator("   SKIP");
        CharacterIterator offsetIterator = new StringCharacterIterator("   OFFSET");
        assertEquals(Terminal.TS_SKIP, Terminal.getTerminal(skipIterator));
        assertEquals(Terminal.TS_SKIP, Terminal.getTerminal(offsetIterator));
    }

    @Test
    void limitWithWhitespaces() {
        CharacterIterator iterator = new StringCharacterIterator("   LIMIT");
        assertEquals(Terminal.TS_LIMIT, Terminal.getTerminal(iterator));
    }

    @Test
    void unknownTerminal() {
        CharacterIterator iterator = new StringCharacterIterator("   UNKNOWN");
        assertThrows(IllegalArgumentException.class, () -> Terminal.getTerminal(iterator));
    }

    @Test
    void badSelect() {
        CharacterIterator iterator = new StringCharacterIterator("   SELETC");
        assertThrows(IllegalArgumentException.class, () -> Terminal.getTerminal(iterator));
    }

    @Test
    void noWhitespaceBetweenTerminalAndExpression() {
        CharacterIterator iterator = new StringCharacterIterator("SELECT*");
        assertThrows(IllegalArgumentException.class, () -> Terminal.getTerminal(iterator));
    }
}