/**
 * Holder for array mapping indexes
 */
public class CharsMapping {
    public final int offset;
    public final int length;
    public final boolean isNumber;
    public CharsMapping(int startIdx, int endIdx, boolean isNumber) {
        this.offset = startIdx;
        this.length = endIdx - startIdx;
        this.isNumber = isNumber;
    }
    public int getEndIndex() {
        return offset + length;
    }
}
