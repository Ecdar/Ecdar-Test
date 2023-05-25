package parsing

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import parser.RelationLexer
import parser.RelationParser

data class AntlrParsingResult(val root: RelationParser.FullContext?, val errors: List<Error>) {
    fun isCorrect() = errors.isEmpty() && root != null
}

fun String.toStream(charset: Charset = Charsets.UTF_8) = ByteArrayInputStream(toByteArray(charset))

object RelationAntlrParserFacade {

    fun parse(code: String): AntlrParsingResult = parse(code.toStream())

    fun parse(file: File): AntlrParsingResult = parse(FileInputStream(file))

    fun parse(inputStream: InputStream): AntlrParsingResult {
        val lexicalAndSyntaticErrors = LinkedList<Error>()
        val errorListener =
            object : ANTLRErrorListener {
                override fun reportAmbiguity(
                    p0: Parser?,
                    p1: DFA?,
                    p2: Int,
                    p3: Int,
                    p4: Boolean,
                    p5: BitSet?,
                    p6: ATNConfigSet?
                ) {
                    // Ignored for now
                }

                override fun reportAttemptingFullContext(
                    p0: Parser?,
                    p1: DFA?,
                    p2: Int,
                    p3: Int,
                    p4: BitSet?,
                    p5: ATNConfigSet?
                ) {
                    // Ignored for now
                }

                override fun syntaxError(
                    recognizer: Recognizer<*, *>?,
                    offendingSymbol: Any?,
                    line: Int,
                    charPositionInline: Int,
                    msg: String,
                    ex: RecognitionException?
                ) {
                    lexicalAndSyntaticErrors.add(
                        Error(msg, Exception("{line}{charPositionInline}")))
                }

                override fun reportContextSensitivity(
                    p0: Parser?,
                    p1: DFA?,
                    p2: Int,
                    p3: Int,
                    p4: Int,
                    p5: ATNConfigSet?
                ) {
                    // Ignored for now
                }
            }

        val lexer = RelationLexer(ANTLRInputStream(inputStream))
        lexer.removeErrorListeners()
        lexer.addErrorListener(errorListener)
        val parser = RelationParser(CommonTokenStream(lexer))
        parser.removeErrorListeners()
        parser.addErrorListener(errorListener)
        val antlrRoot = parser.full()
        return AntlrParsingResult(antlrRoot, lexicalAndSyntaticErrors)
    }
}

object RelationParserFacade {

    fun parse(code: String): RelationParser.FullContext? = parse(code.toStream())

    fun parse(file: File): RelationParser.FullContext? = parse(FileInputStream(file))

    fun parse(inputStream: InputStream): RelationParser.FullContext? {
        val antlrParsingResult = RelationAntlrParserFacade.parse(inputStream)
        val lexicalAnsSyntaticErrors = antlrParsingResult.errors
        val antlrRoot = antlrParsingResult.root
        return antlrRoot
    }
}
