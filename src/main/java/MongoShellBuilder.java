import java.util.ArrayDeque;
import java.util.Queue;

public class MongoShellBuilder {
    private final StringBuilder builder;
    private char[] chars;

    public static class WhereExpression {
        public enum CompareSign {
            EQUALS("$eq"),
            GREATER("$gt"),
            LOWER("$lt"),
            NON_EQUALS("$ne");

            String value;

            CompareSign(String value) {
                this.value = value;
            }
        }

        public final CharsMapping varMapping;
        public final CompareSign sign;
        public final CharsMapping valueMapping;
        private WhereExpression(CharsMapping varMapping, CompareSign sign, CharsMapping valueMapping) {
            this.varMapping = varMapping;
            this.sign = sign;
            this.valueMapping = valueMapping;
        }
    }

    // SELECT expression
    private boolean isSelectAll = false;
    private final Queue<CharsMapping> selectFields;

    // FROM expression
    private CharsMapping fromDatabaseName;

    // WHERE expression
    private final Queue<WhereExpression> whereExpressions;

    // SKIP expression
    private CharsMapping skipValue;

    // LIMIT expression
    private CharsMapping limitValue;

    public MongoShellBuilder(char[] chars) {
        if (chars == null) {
            throw new NullPointerException();
        }
        this.chars = chars;
        this.builder = new StringBuilder();
        selectFields = new ArrayDeque<>();
        whereExpressions = new ArrayDeque<>();
    }

    public void setSelectAll() {
        isSelectAll = true;
    }

    public void addSelectField(CharsMapping mapping) {
        if (mapping == null) {
            throw new NullPointerException();
        }
        if (isSelectAll) {
            throw new IllegalStateException("selectAll is already set");
        }
        if (mapping.isNumber) {
            throw new IllegalArgumentException("unknown value in select statement");
        }

        selectFields.add(mapping);
    }

    public void setFromDatabaseName(CharsMapping mapping) {
        if (fromDatabaseName != null) {
            throw new IllegalStateException("the from database name is already set");
        }

        fromDatabaseName = mapping;
    }

    public void addWhereExpression(CharsMapping varMapping, WhereExpression.CompareSign sign,
                                   CharsMapping valueMapping) {
        if (varMapping == null || valueMapping == null) {
            throw new NullPointerException();
        }
        whereExpressions.add(
                new WhereExpression(
                        varMapping,
                        sign,
                        valueMapping
                )
        );
    }

    public void setSkipValue(CharsMapping skipValue) {
        if (skipValue == null) {
            throw new NullPointerException();
        }
        if (this.skipValue != null) {
            throw new IllegalStateException("the skip value is already set");
        }
        if (!skipValue.isNumber) {
            throw new IllegalArgumentException("unknown variable in skip/offset statement");
        }

        this.skipValue = skipValue;
    }

    public void setLimitValue(CharsMapping limitValue) {
        if (limitValue == null) {
            throw new NullPointerException();
        }
        if (this.limitValue != null) {
            throw new IllegalStateException("the limit value is already set");
        }
        if (!limitValue.isNumber) {
            throw new IllegalArgumentException("unknown variable in skip/offset statement");
        }

        this.limitValue = limitValue;
    }

    public String build() {
        if (fromDatabaseName == null || !isSelectAll && selectFields.isEmpty()) {
            throw new IllegalStateException("Not enough input values");
        }

        builder.append("db.").append(chars, fromDatabaseName.offset, fromDatabaseName.length).append(".find({");
        if (!whereExpressions.isEmpty()) {
            WhereExpression firstExpression = whereExpressions.remove();
            builder.append(chars, firstExpression.varMapping.offset, firstExpression.varMapping.length).append(": {")
                    .append(firstExpression.sign.value).append(": ")
                    .append(chars, firstExpression.valueMapping.offset, firstExpression.valueMapping.length);
            for (WhereExpression whereExpression : whereExpressions) {
                builder.append(", ")
                        .append(chars, whereExpression.varMapping.offset, whereExpression.varMapping.length).append(": {")
                        .append(whereExpression.sign.value).append(": ")
                        .append(chars, whereExpression.valueMapping.offset, whereExpression.valueMapping.length);
            }
        }

        builder.append('}');
        if (!selectFields.isEmpty()) {
            builder.append(", {");
            CharsMapping firstField = selectFields.remove();
            builder.append(chars, firstField.offset, firstField.length)
                    .append(": 1");
            for (CharsMapping field : selectFields) {
                builder.append(", ")
                        .append(chars, field.offset, field.length)
                        .append(": 1");
            }
            builder.append('}');
        }
        builder.append(')');

        if (skipValue != null) {
            builder.append(".skip(")
                    .append(chars, skipValue.offset, skipValue.length)
                    .append(')');
        }

        if (limitValue != null) {
            builder.append(".limit(")
                    .append(chars, limitValue.offset, limitValue.length)
                    .append(')');
        }

        return builder.toString();
    }
}
