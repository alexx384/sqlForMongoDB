import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TranslatorTest {
    @Test
    void selectAnyFromWhereOneExpression() {
        assertEquals(
                "db.customers.find({age: {$gt: 22}})",
                Translator.translate("SELECT * FROM customers WHERE age > 22")
        );
    }

    @Test
    void selectAnyFromWhereFourExpression() {
        assertEquals(
                "db.customers.find({age: {$gt: 22, $ne: 25}, purchases: {$eq: 2}, id: {$lt: 5}})",
                Translator.translate(
                        "SELECT * FROM customers WHERE age > 22 AND purchases = 2 AND id < 5 AND age <> 25"
                )
        );
    }

    @Test
    void selectAnyFromWhereReverseThreeExpression() {
        assertEquals(
                "db.customers.find({age: {$gt: 22, $ne: 25}, purchases: {$eq: 2}, id: {$lt: 5}})",
                Translator.translate(
                        "SELECT * FROM customers WHERE 22 < age AND 2 = purchases AND 5 > id AND 25 <> age"
                )
        );
    }

    @Test
    void selectOneFieldFrom() {
        assertEquals(
                "db.collection.find({}, {name: 1})",
                Translator.translate("SELECT name FROM collection")
        );
    }

    @Test
    void selectThreeFieldsFrom() {
        assertEquals(
                "db.collection.find({}, {name: 1, surname: 1, patronymic: 1})",
                Translator.translate("SELECT name, surname, patronymic FROM collection")
        );
    }

    @Test
    void selectAnyFromOffsetLimit() {
        assertEquals(
                "db.collection.find({}).skip(5).limit(10)",
                Translator.translate("SELECT * FROM collection OFFSET 5 LIMIT 10")
        );
    }

    @Test
    void selectAnyFromSkipLimit() {
        assertEquals(
                "db.collection.find({}).skip(5).limit(10)",
                Translator.translate("SELECT * FROM collection SKIP 5 LIMIT 10")
        );
    }

    @Test
    void selectAnyFromLimit() {
        assertEquals(
                "db.sales.find({}).limit(10)",
                Translator.translate("SELECT * FROM sales LIMIT 10")
        );
    }

    @Test
    void selectAnyFromBadSymbol() {
        assertThrows(IllegalArgumentException.class, () -> Translator.translate("SELECT * FROM <s LIMIT 10"));
        assertThrows(IllegalArgumentException.class, () -> Translator.translate("SELECT * FROM s< LIMIT 10"));
        assertThrows(IllegalArgumentException.class, () -> Translator.translate("SELECT * FROM * LIMIT 10"));
    }

    @Test
    void selectBadSymbolFrom() {
        assertThrows(IllegalArgumentException.class, () -> Translator.translate("SELECT ** FROM s LIMIT 10"));
        assertThrows(IllegalArgumentException.class,
                () -> Translator.translate("SELECT *, * FROM s LIMIT 10"));
        assertThrows(IllegalArgumentException.class,
                () -> Translator.translate("SELECT n, s* FROM s LIMIT 10"));
    }

    @Test
    void selectAnyFromWhereSkipLimitWithWhitespaces() {
        assertEquals(
                "db.s.find({age: {$gt: 25}}).skip(5).limit(10)",
                Translator.translate("SELECT  *  FROM  s  WHERE  age  >  25  SKIP 5  LIMIT  10 ")
        );
    }

    @Test
    void selectAnyWhereWithBadLogic() {
        assertThrows(IllegalArgumentException.class, () -> Translator.translate("SELECT * WHERE age > 25"));
    }

    @Test
    void selectAnyFromWhereCompareTwoNumbers() {
        assertThrows(IllegalArgumentException.class, () -> Translator.translate("SELECT * FROM s WHERE 1 = 1"));
    }

    @Test
    void selectAnyFromWhereBadCompareSign() {
        assertThrows(IllegalArgumentException.class, () -> Translator.translate("SELECT * FROM s WHERE 1 != 1"));
    }

    @Test
    void selectAnyFromWhereNoWhitespaceAfterCompareSign() {
        assertThrows(IllegalArgumentException.class, () -> Translator.translate("SELECT * FROM s WHERE 1 =1"));
    }

    @Test
    void selectAnyFromWhereCompareTwoVariables() {
        assertThrows(IllegalArgumentException.class,
                () -> Translator.translate("SELECT * FROM s WHERE age = number"));
    }
}