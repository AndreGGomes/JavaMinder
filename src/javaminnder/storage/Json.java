package javaminnder.storage;

import java.util.ArrayList;
import java.util.Arrays;
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
 *
 * <p>Duas limitações do OpenJML 21 moldam a implementação, sem mudar o
 * comportamento observável:</p>
 * <ul>
 *   <li>um laço não pode chamar métodos com efeitos colaterais (nem
 *       {@code List.add}, nem {@code Map.put}), por isso a leitura de objetos e
 *       arrays avança um membro/elemento por chamada recursiva;</li>
 *   <li>{@code List.add(null)} não é verificável ({@code containsNull} nunca é
 *       provado), por isso elementos nulos viajam como um valor sentinela e são
 *       convertidos no final por {@link #unbox(Object[])}.</li>
 * </ul>
 */
public final class Json {

    /** Marca um elemento JSON {@code null} enquanto o array é montado. */
    private static final Object NULL_VALUE = new Object();

    private Json() {
    }

    /**
     * Lê um documento JSON completo. O resultado é sempre um dos tipos
     * escalares/estruturais aceitos (ou {@code null} para o literal {@code null}).
     */
    /*@ public behavior
      @   ensures text != null;
      @   ensures \result == null
      @        || \result instanceof String
      @        || \result instanceof Long
      @        || \result instanceof Double
      @        || \result instanceof Boolean
      @        || \result instanceof Map
      @        || \result instanceof List;
      @   signals (JsonSyntaxException e) true;
      @*/
    public static /*@ nullable @*/ Object parse(/*@ nullable @*/ String text) {
        if (text == null) {
            throw new JsonSyntaxException("Conteúdo JSON nulo.");
        }
        Parser parser = new Parser(text);
        return parser.parseDocument();
    }

    /**
     * Serializa um valor compatível com JSON com indentação de 2 espaços
     * e quebra de linha final.
     */
    /*@ public behavior
      @   ensures \result != null;
      @   ensures \result.length() >= 2;
      @   ensures \result.charAt(\result.length() - 1) == '\n';
      @*/
    public static String stringify(/*@ nullable @*/ Object value) {
        return render(value, 0) + "\n";
    }

    /**
     * Exige que o valor seja um objeto JSON. Em caso de sucesso devolve o
     * próprio argumento, apenas com o tipo genérico ajustado.
     */
    /*@ public behavior
      @   ensures value instanceof Map;
      @   ensures \result == value;
      @   signals (JsonSyntaxException e) !(value instanceof Map);
      @*/
    @SuppressWarnings("unchecked")
    public static Map<String, Object> asObject(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new JsonSyntaxException("Era esperado um objeto JSON, veio " + describe(value) + ".");
    }

    /**
     * Exige que o valor seja um array JSON e devolve uma cópia com a mesma
     * quantidade de elementos.
     */
    /*@ public behavior
      @   ensures value instanceof List;
      @   ensures \result != null;
      @   ensures \result.size() == ((List<?>) value).size();
      @   signals (JsonSyntaxException e) !(value instanceof List);
      @*/
    public static List<Object> asArray(Object value) {
        if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>(list);
            return copy;
        }
        throw new JsonSyntaxException("Era esperado um array JSON, veio " + describe(value) + ".");
    }

    /**
     * Lê um campo de texto. Ausência do campo ({@code null}) continua sendo
     * ausência; qualquer outro valor é convertido com {@code String.valueOf}.
     */
    /*@ public behavior
      @   requires data != null;
      @   requires key != null;
      @   ensures data.get(key) == null <==> \result == null;
      @*/
    public static /*@ nullable @*/ String getString(Map<String, Object> data, String key) {
        /*@ nullable @*/ Object value = data.get(key);
        return value == null ? null : String.valueOf(value);
    }

    /**
     * Lê um campo numérico. Números usam {@code longValue}; textos numéricos
     * são interpretados; qualquer outra coisa (inclusive ausência) devolve
     * {@code fallback}.
     */
    /*@ public behavior
      @   requires data != null;
      @   requires key != null;
      @   ensures data.get(key) instanceof Number
      @        ==> \result == ((Number) data.get(key)).longValue();
      @   ensures !(data.get(key) instanceof Number)
      @        && !(data.get(key) instanceof String)
      @        ==> \result == fallback;
      @   signals (NumberFormatException e) true;
      @*/
    public static long getLong(Map<String, Object> data, String key, long fallback) {
        /*@ nullable @*/ Object value = data.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.parseLong(text.trim());
        }
        return fallback;
    }

    /**
     * Lê um campo booleano. Booleanos são devolvidos como estão; textos seguem
     * {@code Boolean.parseBoolean}; qualquer outra coisa devolve {@code fallback}.
     */
    /*@ public behavior
      @   requires data != null;
      @   requires key != null;
      @   ensures data.get(key) instanceof Boolean
      @        ==> \result == ((Boolean) data.get(key)).booleanValue();
      @   ensures !(data.get(key) instanceof Boolean)
      @        && !(data.get(key) instanceof String)
      @        ==> \result == fallback;
      @*/
    public static boolean getBoolean(Map<String, Object> data, String key, boolean fallback) {
        /*@ nullable @*/ Object value = data.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text) {
            return Boolean.parseBoolean(text.trim());
        }
        return fallback;
    }

    /*@ private normal_behavior
      @   assignable \nothing;
      @   ensures \result != null;
      @   ensures value == null ==> \result.equals("null");
      @*/
    private static /*@ pure @*/ String describe(Object value) {
        return value == null ? "null" : value.getClass().getSimpleName();
    }

    /**
     * Converte a lista com sentinelas de {@code parseArray} em uma lista com
     * {@code null} de verdade (as duas transformações são puramente funcionais
     * e verificáveis).
     */
    /*@ private normal_behavior
      @   requires values != null;
      @   assignable \everything;
      @   ensures \result != null;
      @   ensures \result.size() == values.length;
      @*/
    private static List<Object> unbox(Object[] values) {
        /*@ nullable @*/ Object[] out = new /*@ nullable @*/ Object[values.length];
        /*@ maintaining 0 <= index && index <= values.length;
          @ maintaining out != null && out.length == values.length;
          @ decreasing values.length - index;
          @*/
        for (int index = 0; index < values.length; index++) {
            //@ assert 0 <= index && index < out.length;
            Object value = values[index];
            out[index] = value == NULL_VALUE ? null : value;
        }
        List<Object> result = new ArrayList<>(Arrays.asList(out));
        //@ assert result.size() == values.length;
        return result;
    }

    /*@ private behavior
      @   requires 0 <= indent && indent <= 10000;
      @   assignable \nothing;
      @   ensures \result != null;
      @   ensures \result.length() >= 1;
      @   signals (RuntimeException e) true;
      @*/
    private static String render(/*@ nullable @*/ Object value, int indent) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String text) {
            return quote(text);
        }
        if (value instanceof Number number) {
            if (number instanceof Double || number instanceof Float) {
                return Double.toString(number.doubleValue());
            }
            return Long.toString(number.longValue());
        }
        if (value instanceof Boolean bool) {
            if (bool) {
                return "true";
            }
            return "false";
        }
        if (value instanceof Map<?, ?> map) {
            return renderObject(map, indent);
        }
        if (value instanceof Collection<?> collection) {
            return renderArray(collection, indent);
        }
        return quote(String.valueOf(value));
    }

    /*@ private behavior
      @   requires map != null;
      @   requires 0 <= indent && indent <= 10000;
      @   assignable \nothing;
      @   ensures \result != null;
      @   ensures \result.length() >= 2;
      @   signals (RuntimeException e) true;
      @*/
    private static String renderObject(Map<?, ?> map, int indent) {
        if (map.isEmpty()) {
            return "{}";
        }
        int nextIndent = indent < 9998 ? indent + 2 : 10000;
        String out = "{\n";
        Object[] keys = map.keySet().toArray();
        /*@ maintaining 0 <= index && index <= keys.length;
          @ maintaining out != null && out.length() >= 2;
          @ decreasing keys.length - index;
          @*/
        for (int index = 0; index < keys.length; index++) {
            if (index > 0) {
                out = out + ",\n";
            }
            out = out + indentOf(nextIndent)
                    + quote(String.valueOf(keys[index]))
                    + ": " + render(map.get(keys[index]), nextIndent);
        }
        return out + "\n" + indentOf(indent) + "}";
    }

    /*@ private behavior
      @   requires collection != null;
      @   requires 0 <= indent && indent <= 10000;
      @   assignable \nothing;
      @   ensures \result != null;
      @   ensures \result.length() >= 2;
      @   signals (RuntimeException e) true;
      @*/
    private static String renderArray(Collection<?> collection, int indent) {
        if (collection.isEmpty()) {
            return "[]";
        }
        int nextIndent = indent < 9998 ? indent + 2 : 10000;
        String out = "[\n";
        Object[] elements = collection.toArray();
        /*@ maintaining 0 <= index && index <= elements.length;
          @ maintaining out != null && out.length() >= 2;
          @ decreasing elements.length - index;
          @*/
        for (int index = 0; index < elements.length; index++) {
            if (index > 0) {
                out = out + ",\n";
            }
            out = out + indentOf(nextIndent) + render(elements[index], nextIndent);
        }
        return out + "\n" + indentOf(indent) + "]";
    }

    /*@ private normal_behavior
      @   requires 0 <= size && size <= 10000;
      @   assignable \nothing;
      @   ensures \result != null;
      @   ensures \result.length() == size;
      @*/
    private static String indentOf(int size) {
        String out = "";
        /*@ maintaining 0 <= count && count <= size;
          @ maintaining out != null && out.length() == count;
          @ decreasing size - count;
          @*/
        for (int count = 0; count < size; count++) {
            out = out + " ";
        }
        return out;
    }

    /*@ private normal_behavior
      @   requires text != null;
      @   assignable \nothing;
      @   ensures \result != null;
      @   ensures \result.length() >= 2;
      @*/
    private static String quote(String text) {
        return "\"" + escape(text) + "\"";
    }

    /*@ private normal_behavior
      @   requires text != null;
      @   assignable \nothing;
      @   ensures \result != null;
      @*/
    private static String escape(String text) {
        String out = "";
        /*@ maintaining 0 <= index && index <= text.length();
          @ maintaining out != null;
          @ decreasing text.length() - index;
          @*/
        for (int index = 0; index < text.length(); index++) {
            out = out + escapeChar(text.charAt(index));
        }
        return out;
    }

    /*@ private normal_behavior
      @   assignable \nothing;
      @   ensures \result != null;
      @   ensures \result.length() >= 1;
      @*/
    private static String escapeChar(char ch) {
        if (ch == '"' || ch == '\\' || ch == '\n' || ch == '\r'
                || ch == '\t' || ch == '\b' || ch == '\f') {
            return "\\" + escapedLetter(ch);
        }
        if (ch < 0x20) {
            return unicodeEscape(ch);
        }
        return String.valueOf(ch);
    }

    /** Letra usada nos escapes de dois caracteres ({@code n}, {@code r}, ...). */
    /*@ private normal_behavior
      @   assignable \nothing;
      @*/
    private static char escapedLetter(char ch) {
        if (ch == '\n') {
            return 'n';
        }
        if (ch == '\r') {
            return 'r';
        }
        if (ch == '\t') {
            return 't';
        }
        if (ch == '\b') {
            return 'b';
        }
        if (ch == '\f') {
            return 'f';
        }
        return ch;
    }

    /*@ private normal_behavior
      @   assignable \nothing;
      @   ensures \result != null;
      @   ensures \result.length() == 6;
      @*/
    private static String unicodeEscape(char ch) {
        return "\\u" + hexDigit((ch >>> 12) & 0xF)
                + hexDigit((ch >>> 8) & 0xF)
                + hexDigit((ch >>> 4) & 0xF)
                + hexDigit(ch & 0xF);
    }

    /*@ private normal_behavior
      @   requires 0 <= nibble && nibble < 16;
      @   assignable \nothing;
      @   ensures \result != null;
      @   ensures \result.length() == 1;
      @*/
    private static String hexDigit(int nibble) {
        if (nibble < 10) {
            return String.valueOf((char) ('0' + nibble));
        }
        return String.valueOf((char) ('a' + (nibble - 10)));
    }

    /**
     * Analisador descendente recursivo. O estado observável é apenas
     * {@code position}, sempre dentro de {@code [0, source.length()]}, e cada
     * método registra exatamente o quanto consumiu.
     */
    private static final class Parser {

        private final String source;
        private int position;

        /*@ private normal_behavior
          @   requires source != null;
          @   ensures this.source == source;
          @   ensures this.position == 0;
          @*/
        private Parser(String source) {
            this.source = source;
            this.position = 0;
        }

        /**
         * Lê um documento e exige que todo o texto seja consumido: na saída
         * normal {@code position == source.length()}.
         */
        /*@ private behavior
          @   requires 0 <= position;
          @   ensures position >= source.length();
          @   ensures \result == null
          @        || \result instanceof String
          @        || \result instanceof Long
          @        || \result instanceof Double
          @        || \result instanceof Boolean
          @        || \result instanceof Map
          @        || \result instanceof List;
          @   ensures 0 <= position;
          @   signals (JsonSyntaxException e) true;
          @*/
        private /*@ nullable @*/ Object parseDocument() {
            position = skipWhitespace();
            /*@ nullable @*/ Object value = parseValue();
            position = skipWhitespace();
            if (position < source.length()) {
                fail("Conteúdo inesperado depois do fim do JSON");
            }
            return value;
        }

        /**
         * Lê um valor: objetos, arrays, strings, literais e números. Sempre
         * consome pelo menos um caractere.
         */
        /*@ private behavior
          @   requires 0 <= position;
          @   ensures \result == null
          @        || \result instanceof String
          @        || \result instanceof Long
          @        || \result instanceof Double
          @        || \result instanceof Boolean
          @        || \result instanceof Map
          @        || \result instanceof List;
          @   ensures 0 <= position;
          @   signals (JsonSyntaxException e) true;
          @*/
        private /*@ nullable @*/ Object parseValue() {
            int entryPos = position;
            position = skipWhitespace();
            if (position >= source.length()) {
                fail("JSON incompleto");
            }
            char character = source.charAt(position);
            /*@ nullable @*/ Object value;
            if (character == '{') {
                value = parseObject();
            } else if (character == '[') {
                value = parseArray();
            } else if (character == '"') {
                value = parseString();
            } else if (character == 't') {
                value = parseLiteral("true", Boolean.TRUE);
            } else if (character == 'f') {
                value = parseLiteral("false", Boolean.FALSE);
            } else if (character == 'n') {
                value = parseLiteral("null", null);
            } else {
                value = parseNumber();
            }
            return value;
        }

        /** Lê um objeto; um objeto vazio consome exatamente dois caracteres. */
        /*@ private behavior
          @   requires 0 <= position && position < source.length();
          @   ensures 0 <= position;
          @   ensures \result != null;
          @   signals (JsonSyntaxException e) true;
          @*/
        private Map<String, Object> parseObject() {
            expect('{');
            int after = position;
            Map<String, Object> object = new LinkedHashMap<>();
            position = after;
            return parseObjectMembers(object);
        }

        /**
         * Lê os membros de um objeto até o {@code '}'} de fechamento,
         * acrescentando um membro por chamada (um laço não poderia chamar
         * {@code Map.put} e continuar verificável no OpenJML 21).
         */
        /*@ private behavior
          @   requires 0 <= position;
          @   requires object != null;
          @   ensures \result == object;
          @   ensures 0 <= position;
          @   signals (JsonSyntaxException e) true;
          @*/
        private Map<String, Object> parseObjectMembers(Map<String, Object> object) {
            position = skipWhitespace();
            if (peek() == '}') {
                position++;
                return object;
            }
            String key = parseString();
            position = skipWhitespace();
            expect(':');
            /*@ nullable @*/ Object val = parseValue();
            if (val != null) {
                object.put(key, val);
            } else {
                object.remove(key);
            }
            position = skipWhitespace();
            if (peek() == ',') {
                position++;
                return parseObjectMembers(object);
            }
            expect('}');
            return object;
        }

        /** Lê um array; um array vazio consome exatamente dois caracteres. */
        /*@ private behavior
          @   requires 0 <= position && position < source.length();
          @   ensures 0 <= position;
          @   ensures \result != null;
          @   signals (JsonSyntaxException e) true;
          @*/
        private List<Object> parseArray() {
            expect('[');
            int after = position;
            List<Object> items = new ArrayList<>();
            position = after;
            List<Object> marked = parseArrayElements(items);
            int end = position;
            List<Object> result = unbox(marked.toArray());
            position = end;
            return result;
        }

        /**
         * Lê os elementos de um array até o {@code ']'} de fechamento,
         * acrescentando um elemento por chamada. Elementos nulos entram como
         * {@link Json#NULL_VALUE} porque {@code List.add(null)} não é
         * verificável.
         */
        /*@ private behavior
          @   requires 0 <= position;
          @   requires list != null;
          @   ensures \result == list;
          @   ensures 0 <= position;
          @   signals (JsonSyntaxException e) true;
          @*/
        private List<Object> parseArrayElements(List<Object> list) {
            position = skipWhitespace();
            if (peek() == ']') {
                position++;
                return list;
            }
            /*@ nullable @*/ Object val = parseValue();
            if (val == null) {
                list.add(NULL_VALUE);
            } else {
                list.add(val);
            }
            position = skipWhitespace();
            if (peek() == ',') {
                position++;
                return parseArrayElements(list);
            }
            expect(']');
            return list;
        }

        /*@ private behavior
          @   assignable \nothing;
          @   ensures 0 <= \result && \result < 16;
          @   signals (JsonSyntaxException e) true;
          @*/
        private static int parseHexDigit(char ch) {
            if (ch >= '0' && ch <= '9') {
                return ch - '0';
            }
            if (ch >= 'a' && ch <= 'f') {
                return 10 + (ch - 'a');
            }
            if (ch >= 'A' && ch <= 'F') {
                return 10 + (ch - 'A');
            }
            throw new JsonSyntaxException("Escape unicode inválido: " + ch);
        }

        /**
         * Lê uma string com escapes. O resultado nunca é nulo e a posição
         * avança ao menos as duas aspas.
         */
        /*@ private behavior
          @   requires 0 <= position;
          @   assignable position;
          @   ensures position >= \old(position) + 2;
          @   ensures \result != null;
          @   signals (JsonSyntaxException e) true;
          @*/
        private String parseString() {
            expect('"');
            if (position >= source.length()) {
                fail("String não terminada");
            }
            return readStringBody();
        }

        /**
         * Lê o corpo de uma string JSON até consumir a aspa de fechamento,
         * devolvendo o texto já decodificado. Um caractere por chamada, para
         * que cada membro da recursão tenha uma prova pequena.
         */
        /*@ private behavior
          @   requires 0 <= position && position < source.length();
          @   assignable position;
          @   ensures position > \old(position);
          @   ensures \result != null;
          @   signals (JsonSyntaxException e) true;
          @*/
        private String readStringBody() {
            char character = source.charAt(position);
            position++;
            if (character == '"') {
                return "";
            }
            if (character != '\\') {
                return String.valueOf(character) + readStringBodyAfterChar();
            }
            if (position >= source.length()) {
                fail("Sequência de escape incompleta");
            }
            return readStringEscape(source.charAt(position));
        }

        /** Continua a leitura do corpo de uma string, ou falha se acabou o texto. */
        /*@ private behavior
          @   requires 0 <= position;
          @   assignable position;
          @   ensures position > \old(position);
          @   ensures \result != null;
          @   signals (JsonSyntaxException e) true;
          @*/
        private String readStringBodyAfterChar() {
            if (position >= source.length()) {
                fail("String não terminada");
            }
            return readStringBody();
        }

        /** Decodifica uma sequência de escape já iniciada pela barra invertida. */
        /*@ private behavior
          @   requires 0 <= position && position < source.length();
          @   assignable position;
          @   ensures position > \old(position);
          @   ensures \result != null;
          @   signals (JsonSyntaxException e) true;
          @*/
        private String readStringEscape(char escaped) {
            position++;
            if (escaped == 'u') {
                return readUnicodeEscape();
            }
            return readSimpleEscape(escaped);
        }

        /** Decodifica um escape simples (que não é {@code 'u'}). */
        /*@ private behavior
          @   requires 0 <= position;
          @   assignable position;
          @   ensures position > \old(position);
          @   ensures \result != null;
          @   signals (JsonSyntaxException e) true;
          @*/
        private String readSimpleEscape(char escaped) {
            char decoded = simpleEscapeChar(escaped);
            if (decoded == 0) {
                fail("Escape inválido: \\" + escaped);
            }
            return String.valueOf(decoded) + readStringBodyAfterChar();
        }

        /** Decodifica um escape unicode de quatro dígitos já iniciado. */
        /*@ private behavior
          @   requires 0 <= position;
          @   assignable position;
          @   ensures position > \old(position);
          @   ensures \result != null;
          @   signals (JsonSyntaxException e) true;
          @*/
        private String readUnicodeEscape() {
            if (position > source.length() - 4) {
                fail("Escape unicode incompleto");
            }
            int value = (parseHexDigit(source.charAt(position)) << 12)
                    | (parseHexDigit(source.charAt(position + 1)) << 8)
                    | (parseHexDigit(source.charAt(position + 2)) << 4)
                    | parseHexDigit(source.charAt(position + 3));
            //@ assert 0 <= value && value <= 0xFFFF;
            String decoded = String.valueOf((char) value);
            position += 4;
            return decoded + readStringBodyAfterChar();
        }

        /**
         * Lê um literal e garante o valor associado e o consumo exato do
         * literal (por exemplo, {@code "true"} consome quatro caracteres).
         */
        /*@ private behavior
          @   requires 0 <= position;
          @   requires literal != null && literal.length() > 0;
          @   assignable position;
          @   ensures position == \old(position) + literal.length();
          @   ensures \result == value;
          @   signals (JsonSyntaxException e) true;
          @*/
        private /*@ nullable @*/ Object parseLiteral(String literal, /*@ nullable @*/ Object value) {
            if (position > source.length() - literal.length()) {
                fail("Literal inválido, esperado " + literal);
            }
            if (!source.startsWith(literal, position)) {
                fail("Literal inválido, esperado " + literal);
            }
            position += literal.length();
            return value;
        }

        /**
         * Lê um número e garante um dos dois tipos numéricos usados no formato:
         * {@code Long} para inteiros e {@code Double} para decimais.
         */
        /*@ private behavior
          @   requires 0 <= position;
          @   requires position < source.length();
          @   assignable position;
          @   ensures position > \old(position);
          @   ensures \result instanceof Long || \result instanceof Double;
          @   signals (JsonSyntaxException e) true;
          @*/
        private Object parseNumber() {
            int start = position;
            boolean decimal = false;
            /*@ maintaining start <= position;
              @ loop_modifies position, decimal;
              @ decreases source.length() - position;
              @*/
            while (position < source.length()) {
                char character = peek();
                if (character == '.' || character == 'e' || character == 'E') {
                    decimal = true;
                    position++;
                } else if ((character >= '0' && character <= '9')
                        || character == '-'
                        || character == '+') {
                    position++;
                } else {
                    break;
                }
            }
            if (start == position) {
                fail("Valor inválido");
            }
            String text = source.substring(start, position);
            try {
                return convertNumber(text, decimal);
            } catch (NumberFormatException ex) {
                fail("Número inválido: " + text);
                return 0L;
            }
        }

        /** Converte o texto já validado em {@code Double} (decimal) ou {@code Long}. */
        /*@ private behavior
          @   requires text != null;
          @   assignable \nothing;
          @   ensures \result instanceof Long || \result instanceof Double;
          @   signals (NumberFormatException e) true;
          @*/
        private static Object convertNumber(String text, boolean decimal) {
            if (decimal) {
                return Double.valueOf(text);
            }
            return Long.valueOf(text);
        }

        /**
         * Traduz um escape simples (sem {@code 'u'}) para o caractere
         * correspondente; devolve {@code 0} quando o escape não é simples.
         */
        /*@ private normal_behavior
          @   assignable \nothing;
          @*/
        private static char simpleEscapeChar(char escaped) {
            if (escaped == '"') {
                return '"';
            }
            if (escaped == '\\') {
                return '\\';
            }
            if (escaped == '/') {
                return '/';
            }
            if (escaped == 'b') {
                return '\b';
            }
            if (escaped == 'f') {
                return '\f';
            }
            if (escaped == 'n') {
                return '\n';
            }
            if (escaped == 'r') {
                return '\r';
            }
            if (escaped == 't') {
                return '\t';
            }
            return 0;
        }

        /** Espia o caractere atual sem consumir; exige que ainda haja texto. */
        /*@ private behavior
          @   requires 0 <= position;
          @   assignable \nothing;
          @   ensures position == \old(position);
          @   ensures position < source.length();
          @   signals (JsonSyntaxException e) \old(position) >= source.length();
          @*/
        private char peek() {
            if (position >= source.length()) {
                fail("JSON incompleto");
            }
            return source.charAt(position);
        }

        /** Consome exatamente um caractere, que precisa ser o esperado. */
        /*@ private behavior
          @   requires 0 <= position;
          @   assignable position;
          @   ensures position == \old(position) + 1;
          @   signals (JsonSyntaxException e) true;
          @*/
        private void expect(char expected) {
            if (peek() != expected) {
                fail("Esperado '" + expected + "'");
            }
            position++;
        }

        /**
         * Calcula a posição do primeiro caractere que não é espaço em branco
         * (ou o fim do texto), sem alterar o estado do analisador.
         */
        /*@ private normal_behavior
          @   requires 0 <= position;
          @   assignable \nothing;
          @   ensures \result >= position;
          @*/
        private int skipWhitespace() {
            int index = position;
            /*@ maintaining position <= index;
              @ loop_modifies index;
              @*/
            while (index < source.length()) {
                char ch = source.charAt(index);
                if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
                    index++;
                } else {
                    break;
                }
            }
            return index;
        }

        /** Falha a leitura na posição indicada: nunca retorna normalmente. */
        /*@ private exceptional_behavior
          @   requires message != null;
          @   assignable \nothing;
          @   signals (JsonSyntaxException e) true;
          @*/
        private static void failAt(String message, int at) {
            throw new JsonSyntaxException(message + " (posição " + at + ")");
        }

        /** Falha a leitura: nunca retorna normalmente. */
        /*@ private exceptional_behavior
          @   requires message != null;
          @   assignable \nothing;
          @   signals (JsonSyntaxException e) true;
          @*/
        private void fail(String message) {
            throw new JsonSyntaxException(message + " (posição " + position + ")");
        }
    }
}
