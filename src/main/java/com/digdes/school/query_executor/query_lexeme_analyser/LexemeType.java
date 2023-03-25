package com.digdes.school.query_executor.query_lexeme_analyser;

public enum LexemeType {
    WHERE,
    SELECT,
    DELETE,
    INSERT,
    UPDATE,
    VALUES,

    LIKE,
    ILIKE,

    COMPARISON,
    AND,
    OR,

    LONG_VALUE,
    DOUBLE_VALUE,
    STRING_VALUE,
    BOOLEAN_VALUE,
    NULL_VALUE
}
