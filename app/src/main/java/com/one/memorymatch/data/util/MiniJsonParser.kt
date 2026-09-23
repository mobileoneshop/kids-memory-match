package com.one.memorymatch.data.util

sealed class JsonElement {
    data class JsonObject(val map: Map<String, JsonElement>) : JsonElement() {
        fun getObject(key: String): JsonObject = (map[key] as? JsonObject) ?: error("Missing object key: $key")
        fun getArray(key: String): JsonArray = (map[key] as? JsonArray) ?: error("Missing array key: $key")
        fun getString(key: String): String = (map[key] as? JsonString)?.value ?: error("Missing string key: $key")
        fun optString(key: String, fallback: String? = null): String? = (map[key] as? JsonString)?.value ?: fallback
        fun getLong(key: String): Long = (map[key] as? JsonNumber)?.value?.toLong() ?: error("Missing number key: $key")
    }

    data class JsonArray(val list: List<JsonElement>) : JsonElement() {
        val size: Int get() = list.size
        operator fun get(index: Int): JsonElement = list[index]
    }

    data class JsonString(val value: String) : JsonElement()
    data class JsonNumber(val value: Double) : JsonElement()
    data class JsonBoolean(val value: Boolean) : JsonElement()
    data object JsonNull : JsonElement()

    companion object {
        fun parse(json: String): JsonElement = MiniJsonParser(json).parse()
    }
}

class MiniJsonParser(private val src: String) {
    private var index = 0

    fun parse(): JsonElement {
        skipWhitespace()
        val result = parseValue()
        skipWhitespace()
        return result
    }

    private fun parseValue(): JsonElement {
        skipWhitespace()
        if (index >= src.length) error("Unexpected end of JSON")
        return when (val c = src[index]) {
            '{' -> parseObject()
            '[' -> parseArray()
            '"' -> parseString()
            't', 'f' -> parseBoolean()
            'n' -> parseNull()
            '-', in '0'..'9' -> parseNumber()
            else -> error("Unexpected character: '$c' at offset $index")
        }
    }

    private fun parseObject(): JsonElement.JsonObject {
        expect('{')
        val map = mutableMapOf<String, JsonElement>()
        skipWhitespace()
        if (peek() == '}') {
            index++
            return JsonElement.JsonObject(map)
        }
        while (true) {
            skipWhitespace()
            val key = parseString().value
            skipWhitespace()
            expect(':')
            val value = parseValue()
            map[key] = value
            skipWhitespace()
            when (val c = peek()) {
                '}' -> {
                    index++
                    break
                }
                ',' -> {
                    index++
                }
                else -> error("Expected ',' or '}' in object, got '$c' at $index")
            }
        }
        return JsonElement.JsonObject(map)
    }

    private fun parseArray(): JsonElement.JsonArray {
        expect('[')
        val list = mutableListOf<JsonElement>()
        skipWhitespace()
        if (peek() == ']') {
            index++
            return JsonElement.JsonArray(list)
        }
        while (true) {
            val value = parseValue()
            list.add(value)
            skipWhitespace()
            when (val c = peek()) {
                ']' -> {
                    index++
                    break
                }
                ',' -> {
                    index++
                }
                else -> error("Expected ',' or ']' in array, got '$c' at $index")
            }
        }
        return JsonElement.JsonArray(list)
    }

    private fun parseString(): JsonElement.JsonString {
        expect('"')
        val sb = StringBuilder()
        while (index < src.length) {
            val c = src[index++]
            if (c == '"') {
                return JsonElement.JsonString(sb.toString())
            }
            if (c == '\\') {
                if (index >= src.length) error("Unterminated escape sequence")
                when (val esc = src[index++]) {
                    '"' -> sb.append('"')
                    '\\' -> sb.append('\\')
                    '/' -> sb.append('/')
                    'b' -> sb.append('\b')
                    'f' -> sb.append('\u000C')
                    'n' -> sb.append('\n')
                    'r' -> sb.append('\r')
                    't' -> sb.append('\t')
                    'u' -> {
                        if (index + 4 > src.length) error("Invalid unicode escape")
                        val hex = src.substring(index, index + 4)
                        sb.append(hex.toInt(16).toChar())
                        index += 4
                    }
                    else -> sb.append(esc)
                }
            } else {
                sb.append(c)
            }
        }
        error("Unterminated string starting at $index")
    }

    private fun parseNumber(): JsonElement.JsonNumber {
        val start = index
        if (peek() == '-') index++
        while (index < src.length && src[index].isDigit()) index++
        if (index < src.length && src[index] == '.') {
            index++
            while (index < src.length && src[index].isDigit()) index++
        }
        if (index < src.length && (src[index] == 'e' || src[index] == 'E')) {
            index++
            if (index < src.length && (src[index] == '+' || src[index] == '-')) index++
            while (index < src.length && src[index].isDigit()) index++
        }
        val numStr = src.substring(start, index)
        return JsonElement.JsonNumber(numStr.toDouble())
    }

    private fun parseBoolean(): JsonElement.JsonBoolean {
        return if (src.startsWith("true", index)) {
            index += 4
            JsonElement.JsonBoolean(true)
        } else if (src.startsWith("false", index)) {
            index += 5
            JsonElement.JsonBoolean(false)
        } else {
            error("Invalid boolean at $index")
        }
    }

    private fun parseNull(): JsonElement.JsonNull {
        if (src.startsWith("null", index)) {
            index += 4
            return JsonElement.JsonNull
        }
        error("Invalid null at $index")
    }

    private fun expect(expected: Char) {
        skipWhitespace()
        if (index >= src.length || src[index] != expected) {
            val found = if (index < src.length) src[index] else "EOF"
            error("Expected '$expected' but found '$found' at offset $index")
        }
        index++
    }

    private fun peek(): Char {
        skipWhitespace()
        if (index >= src.length) error("Unexpected EOF")
        return src[index]
    }

    private fun skipWhitespace() {
        while (index < src.length) {
            val c = src[index]
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                index++
            } else {
                break
            }
        }
    }
}
