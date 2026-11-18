package javaminnder.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação mínima de JSON, escrita com recursos da própria JDK.
 *
 * <p>O JavaMinder não usa nenhuma biblioteca externa: objetos JSON viram
 * {@code Map<String, Object>}, arrays viram {@code List<Object>} e os valores
 * escalares viram {@code String}, {@code Long}, {@code Double}, {@code Boolean}
 * ou {@code null}.</p>
 */
public final class Json {

    private Json() {
    }

    public static Object parse(String text) {
        if (text == null) {
            throw new JsonSyntaxException("Conteúdo JSON nulo.");
        }
        return new Parser(text).parseDocument();
    }

    public static String stringify(Object value) {
        StringBuilder builder = new StringBuilder();
        write(value, builder, 0);
        builder.append('\n');
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asObject(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new JsonSyntaxException("Era esperado um objeto JSON, veio " + describe(value) + ".");
    }

    public static List<Object> asArray(Object value) {
        if (value instanceof List<?> list) {
            return new ArrayList<>(list);
        }
        throw new JsonSyntaxException("Era esperado um array JSON, veio " + describe(value) + ".");
    }

    public static String getString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value == null ? null : String.valueOf(value);
    }

    public static long getLong(Map<String, Object> data, String key, long fallback) {
        Object value = data.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.parseLong(text.trim());
        }
        return fallback;
    }

    public static boolean getBoolean(Map<String, Object> data, String key, boolean fallback) {
        Object value = data.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text) {
            return Boolean.parseBoolean(text.trim());
        }
        return fallback;
    }

    private static String describe(Object value) {
        return value == null ? "null" : value.getClass().getSimpleName();
    }

    private static void write(Object value, StringBuilder builder, int indent) {
        if (value == null) {
            builder.append("null");
            return;
        }
        if (value instanceof String text) {
            writeString(text, builder);
            return;
        }
        if (value instanceof Number || value instanceof Boolean) {
            builder.append(value);
            return;
        }
        if (value instanceof Map<?, ?> map) {
            writeObject(map, builder, indent);
            return;
        }
        if (value instanceof Collection<?> collection) {
            writeArray(collection, builder, indent);
            return;
        }
        writeString(String.valueOf(value), builder);
    }

    private static void writeObject(Map<?, ?> map, StringBuilder builder, int indent) {
        if (map.isEmpty()) {
            builder.append("{}");
            return;
        }
        builder.append("{\n");
        int index = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            writeIndent(builder, indent + 2);
            writeString(String.valueOf(entry.getKey()), builder);
            builder.append(": ");
            write(entry.getValue(), builder, indent + 2);
            if (++index < map.size()) {
                builder.append(',');
            }
            builder.append('\n');
        }
        writeIndent(builder, indent);
        builder.append('}');
    }

    private static void writeArray(Collection<?> collection, StringBuilder builder, int indent) {
        if (collection.isEmpty()) {
            builder.append("[]");
            return;
        }
        builder.append("[\n");
        int index = 0;
        for (Object element : collection) {
            writeIndent(builder, indent + 2);
            write(element, builder, indent + 2);
            if (++index < collection.size()) {
                builder.append(',');
            }
            builder.append('\n');
        }
        writeIndent(builder, indent);
        builder.append(']');
    }

    private static void writeIndent(StringBuilder builder, int size) {
        builder.append(" ".repeat(Math.max(size, 0)));
    }

    private static void writeString(String text, StringBuilder builder) {
        builder.append('"');
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            switch (character) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                default -> {
                    if (character < 0x20) {
                        builder.append(String.format("\\u%04x", (int) character));
                    } else {
                        builder.append(character);
                    }
                }
            }
        }
        builder.append('"');
    }

    private static final class Parser {

        private final String source;
        private int position;

        private Parser(String source) {
            this.source = source;
        }

        private Object parseDocument() {
            skipWhitespace();
            Object value = parseValue();
            skipWhitespace();
            if (position < source.length()) {
                fail("Conteúdo inesperado depois do fim do JSON");
            }
            return value;
        }

        private Object parseValue() {
            skipWhitespace();
            if (position >= source.length()) {
                fail("JSON incompleto");
            }
            char character = source.charAt(position);
            return switch (character) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't' -> parseLiteral("true", Boolean.TRUE);
                case 'f' -> parseLiteral("false", Boolean.FALSE);
                case 'n' -> parseLiteral("null", null);
                default -> parseNumber();
            };
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> object = new LinkedHashMap<>();
            expect('{');
            skipWhitespace();
            if (peek() == '}') {
                position++;
                return object;
            }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                object.put(key, parseValue());
                skipWhitespace();
                if (peek() == ',') {
                    position++;
                    continue;
                }
                expect('}');
                return object;
            }
        }

        private List<Object> parseArray() {
            List<Object> array = new ArrayList<>();
            expect('[');
            skipWhitespace();
            if (peek() == ']') {
                position++;
                return array;
            }
            while (true) {
                array.add(parseValue());
                skipWhitespace();
                if (peek() == ',') {
                    position++;
                    continue;
                }
                expect(']');
                return array;
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder builder = new StringBuilder();
            while (true) {
                if (position >= source.length()) {
                    fail("String não terminada");
                }
                char character = source.charAt(position++);
                if (character == '"') {
                    return builder.toString();
                }
                if (character != '\\') {
                    builder.append(character);
                    continue;
                }
                if (position >= source.length()) {
                    fail("Sequência de escape incompleta");
                }
                char escape = source.charAt(position++);
                switch (escape) {
                    case '"' -> builder.append('"');
                    case '\\' -> builder.append('\\');
                    case '/' -> builder.append('/');
                    case 'b' -> builder.append('\b');
                    case 'f' -> builder.append('\f');
                    case 'n' -> builder.append('\n');
                    case 'r' -> builder.append('\r');
                    case 't' -> builder.append('\t');
                    case 'u' -> {
                        if (position + 4 > source.length()) {
                            fail("Escape unicode incompleto");
                        }
                        builder.append((char) Integer.parseInt(source.substring(position, position + 4), 16));
                        position += 4;
                    }
                    default -> fail("Escape inválido: \\" + escape);
                }
            }
        }

        private Object parseLiteral(String literal, Object value) {
            if (!source.startsWith(literal, position)) {
                fail("Literal inválido, esperado " + literal);
            }
            position += literal.length();
            return value;
        }

        private Object parseNumber() {
            int start = position;
            while (position < source.length()) {
                char character = source.charAt(position);
                boolean partOfNumber = Character.isDigit(character)
                        || character == '-'
                        || character == '+'
                        || character == '.'
                        || character == 'e'
                        || character == 'E';
                if (!partOfNumber) {
                    break;
                }
                position++;
            }
            if (start == position) {
                fail("Valor inválido");
            }
            String text = source.substring(start, position);
            if (text.contains(".") || text.contains("e") || text.contains("E")) {
                return Double.parseDouble(text);
            }
            return Long.parseLong(text);
        }

        private char peek() {
            if (position >= source.length()) {
                fail("JSON incompleto");
            }
            return source.charAt(position);
        }

        private void expect(char expected) {
            if (peek() != expected) {
                fail("Esperado '" + expected + "'");
            }
            position++;
        }

        private void skipWhitespace() {
            while (position < source.length() && Character.isWhitespace(source.charAt(position))) {
                position++;
            }
        }

        private void fail(String message) {
            throw new JsonSyntaxException(message + " (posição " + position + ")");
        }
    }
}
