import java.text.CharacterIterator;

public class CharArrayIterator implements CharacterIterator {
    private final char[] chars;
    private int begin;
    private int end;
    // invariant: begin <= pos <= end
    private int pos;

    public CharArrayIterator(char[] chars) {
        if (chars == null) {
            throw new NullPointerException();
        }

        this.chars = chars;
        this.begin = 0;
        this.end = chars.length;
        this.pos = 0;
    }

    /**
     * Sets the position to getBeginIndex() and returns the character at that
     * position.
     *
     * @return the first character in the text, or DONE if the text is empty
     * @see #getBeginIndex()
     */
    @Override
    public char first() {
        pos = begin;
        return current();
    }

    /**
     * Sets the position to getEndIndex()-1 (getEndIndex() if the text is empty)
     * and returns the character at that position.
     *
     * @return the last character in the text, or DONE if the text is empty
     * @see #getEndIndex()
     */
    @Override
    public char last() {
        if (end != begin) {
            pos = end - 1;
        } else {
            pos = end;
        }
        return current();
    }

    /**
     * Gets the character at the current position (as returned by getIndex()).
     *
     * @return the character at the current position or DONE if the current
     * position is off the end of the text.
     * @see #getIndex()
     */
    @Override
    public char current() {
        if (pos >= begin && pos < end) {
            return chars[pos];
        } else {
            return DONE;
        }
    }

    /**
     * Increments the iterator's index by one and returns the character
     * at the new index.  If the resulting index is greater or equal
     * to getEndIndex(), the current index is reset to getEndIndex() and
     * a value of DONE is returned.
     *
     * @return the character at the new position or DONE if the new
     * position is off the end of the text range.
     */
    @Override
    public char next() {
        if (pos < end - 1) {
            pos++;
            return chars[pos];
        } else {
            pos = end;
            return DONE;
        }
    }

    /**
     * Decrements the iterator's index by one and returns the character
     * at the new index. If the current index is getBeginIndex(), the index
     * remains at getBeginIndex() and a value of DONE is returned.
     *
     * @return the character at the new position or DONE if the current
     * position is equal to getBeginIndex().
     */
    @Override
    public char previous() {
        if (pos > begin) {
            pos--;
            return chars[pos];
        } else {
            return DONE;
        }
    }

    /**
     * Sets the position to the specified position in the text and returns that
     * character.
     *
     * @param position the position within the text.  Valid values range from
     *                 getBeginIndex() to getEndIndex().  An IllegalArgumentException is thrown
     *                 if an invalid value is supplied.
     * @return the character at the specified position or DONE if the specified position is equal to getEndIndex()
     */
    @Override
    public char setIndex(int position) {
        if (position < begin || position > end) {
            throw new IllegalArgumentException("Invalid index");
        }
        pos = position;
        return current();
    }

    /**
     * Returns the start index of the text.
     *
     * @return the index at which the text begins.
     */
    @Override
    public int getBeginIndex() {
        return begin;
    }

    /**
     * Returns the end index of the text.  This index is the index of the first
     * character following the end of the text.
     *
     * @return the index after the last character in the text
     */
    @Override
    public int getEndIndex() {
        return end;
    }

    /**
     * Returns the current index.
     *
     * @return the current index.
     */
    @Override
    public int getIndex() {
        return pos;
    }

    /**
     * Create a copy of this iterator
     *
     * @return A copy of this
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }
}
