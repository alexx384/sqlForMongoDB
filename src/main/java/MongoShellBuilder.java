import java.util.ArrayDeque;
import java.util.Arrays;
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
        public final Queue<CompareSign> signQueue;
        public final Queue<CharsMapping> valueMappingQueue;
        private WhereExpression(CharsMapping varMapping, CompareSign sign, CharsMapping valueMapping) {
            this.varMapping = varMapping;
            signQueue = new ArrayDeque<>();
            signQueue.add(sign);
            valueMappingQueue = new ArrayDeque<>();
            valueMappingQueue.add(valueMapping);
        }
        public void addConstraint(CompareSign sign, CharsMapping valueMapping) {
            if (valueMapping == null) {
                throw new NullPointerException();
            }
            signQueue.add(sign);
            valueMappingQueue.add(valueMapping);
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
        if (mapping.length >= 64) {
            throw new IllegalArgumentException("database names must have fewer than 64 characters");
        }

        fromDatabaseName = mapping;
    }

    public void addWhereExpression(CharsMapping varMapping, WhereExpression.CompareSign sign,
                                   CharsMapping valueMapping) {
        if (varMapping == null || valueMapping == null) {
            throw new NullPointerException();
        }

        for (WhereExpression expression : whereExpressions) {
            if (expression.varMapping.length == varMapping.length) {
                if (Arrays.compare(
                        chars, expression.varMapping.offset, expression.varMapping.getEndIndex(),
                        chars, varMapping.offset, varMapping.getEndIndex()
                ) == 0) {
                    expression.addConstraint(sign, valueMapping);
                    return;
                }
            }
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

    private void buildWhereExpression(WhereExpression whereExpression) {
        {
            WhereExpression.CompareSign firstSign = whereExpression.signQueue.remove();
            CharsMapping firstValueMapping = whereExpression.valueMappingQueue.remove();
            builder.append(chars, whereExpression.varMapping.offset, whereExpression.varMapping.length).append(": {")
                    .append(firstSign.value).append(": ")
                    .append(chars, firstValueMapping.offset, firstValueMapping.length);
        }

        while (!whereExpression.signQueue.isEmpty() && !whereExpression.valueMappingQueue.isEmpty()) {
            WhereExpression.CompareSign sign = whereExpression.signQueue.remove();
            CharsMapping valueMapping = whereExpression.valueMappingQueue.remove();
            builder.append(", ")
                    .append(sign.value).append(": ")
                    .append(chars, valueMapping.offset, valueMapping.length);
        }
        builder.append('}');
    }

    private void buildWhere() {
        if (whereExpressions.isEmpty()) {
            return;
        }

        WhereExpression firstExpression = whereExpressions.remove();
        buildWhereExpression(firstExpression);

        for (WhereExpression whereExpression : whereExpressions) {
            builder.append(", ");
            buildWhereExpression(whereExpression);
        }
    }

    private void buildSelect() {
        if (selectFields.isEmpty()) {
            return;
        }

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

    public String build() {
        if (fromDatabaseName == null || !isSelectAll && selectFields.isEmpty()) {
            throw new IllegalStateException("Not enough input values");
        }

        builder.append("db.").append(chars, fromDatabaseName.offset, fromDatabaseName.length).append(".find({");
        buildWhere();
        builder.append('}');
        buildSelect();
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
